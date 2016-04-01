/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import groovy.util.logging.Slf4j

import com.google.common.collect.Iterables
import com.google.common.collect.TreeTraverser

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@Slf4j
class UseCaseInstance<T> {

    private static final TreeTraverser<ActionInstance> traverser = new TreeTraverser<ActionInstance>() {
        @Override
        Iterable<ActionInstance> children(ActionInstance root) {
            root.getChildren();
        }
    }

    private final UseCaseSpec<T> spec
    private final Integer instance
    private final T param
    private int numActions
    private long prevDepth = -1
    private long runTime_ms = 0

    private final List<ActionInstance> rootActions = new ArrayList<ActionInstance>(16)
    private final Map<String, Object> instanceContext = new HashMap<String, Object>()
    private final ResultNode rootResultNode = new ResultNode(null, [:])
    private final Stack<ResultNode> ancestorResults = new Stack<>()
    private Iterator<ActionInstance> actionIterator;

    private Throwable failure
    private final ManagerAccess managerAccess
    private final long desiredRunLength
    private AtomicInteger actionCountdown
    private Scheduler scheduler
    private final AtomicReference<RescheduleManager> rescheduleManager = new AtomicReference<>(new RescheduleManager())

    public UseCaseInstance(UseCaseSpec<T> spec, Integer instance, T param, ManagerAccess managerAccess, long desiredRunLength, Timer useCaseTimer, ExecutorService executorService) {
        this.spec = spec
        this.instance = instance
        this.param = param
        this.managerAccess = managerAccess
        this.desiredRunLength = desiredRunLength
        ancestorResults.push(this.rootResultNode)
    }

    public UseCaseSpec<T> getSpec() {
        return spec
    }

    public T getParam() {
        return param
    }

    public Throwable getFailure() {
        return failure
    }

    public void addActionInstance(ActionInstance... actionInstance) {
        rootActions.addAll(actionInstance)
    }

    public Iterable<ActionInstance> getActionIterable() {
        return Iterables.<ActionInstance>concat(rootActions.collect {ActionInstance a -> traverser.preOrderTraversal(a)})
    }

    public void start() {
        this.numActions = (Integer) getActionIterable().count {true}
        this.actionCountdown = new AtomicInteger(numActions)
        log.trace("Starting UseCase: ${spec.name} with [$numActions] actions")

        //start the scheduler, this will fire off the first action
        long actionInterval
        if (desiredRunLength == 0) {
            actionInterval = 0
        } else {
            actionInterval = (desiredRunLength / numActions)
        }
        scheduler = new Scheduler(actionInterval)
        scheduler.start()
    }

    public void executeNext() {

        if (this.actionIterator == null) {
            this.actionIterator = getActionIterable().iterator()
        }

        ActionInstance action = actionIterator.next()
        ActionSpec actionSpec = action.spec

        try {
            //first run will initialize the iterator
            while(actionSpec.depth <= prevDepth) {
                ancestorResults.pop()
                prevDepth--
            }

            Gridlock<T> gridlock = new Gridlock<>(param)
            gridlock.with {
                upNodes = getAncestorResults()
                sibNodes = getSiblingResults()
                prevNodes = getPreviousResults(actionSpec.name)
                contextMap = instanceContext
            }

            List<Object> args = new ArrayList<Object>(actionSpec.args)
            args.eachWithIndex { Object object, int i ->
                if (object instanceof Closure) {
                    Closure argClosure = (Closure) object.clone()
                    argClosure.setDelegate(gridlock)
                    //noinspection UnnecessaryQualifiedReference
                    argClosure.setResolveStrategy(Closure.DELEGATE_FIRST)
                    args[i] = argClosure()
                }
            }

            Closure actionClosure = (Closure) action.execute.clone()
            actionClosure.setDelegate(gridlock)
            actionClosure.setResolveStrategy(Closure.DELEGATE_FIRST)

            Map<String, Object> actionResult
            long actionStartTime_ms = System.currentTimeMillis()

            if (args.size() == 0) {
                actionResult = actionClosure()
            } else if (args.size() == 1) {
                actionResult = actionClosure(args[0])
            } else {
                actionResult = actionClosure(args)
            }

            long actionEndTime_ms = System.currentTimeMillis()

            MetricsInterval.acceptSample(new MetricsSamplePoint(new Date(actionStartTime_ms), new Date(actionEndTime_ms), new MetricsDimensions(spec.name, actionSpec.name, "USER_ACTION")))
            runTime_ms += actionEndTime_ms - actionStartTime_ms

            ResultNode node = new ResultNode(actionSpec.name, actionResult)
            ancestorResults.peek().addChild(node)
            ancestorResults.push(node)
            prevDepth++

            boolean reQueue = registerExecutionCompletionAndCheckForShouldStart()
            if(!reQueue && actionCountdown.get() == 0) {
                managerAccess.done(this)
                MetricsInterval.acceptSample(new MetricsSamplePoint(new Date(actionEndTime_ms - runTime_ms), new Date(actionEndTime_ms), new MetricsDimensions(spec.name, "allActions", "USE_CASE")))
            } else if (reQueue) {
                managerAccess.ready(this)
            }
        } catch (Throwable t) {
            this.failure = t
            log.error("use case instance execution failed, use case: [${spec.name}], action: [${actionSpec.name}], reason: ${t}")
            if (log.isDebugEnabled()) {
                StringWriter stringWriter = new StringWriter()
                t.printStackTrace(new PrintWriter(stringWriter))
                log.debug(stringWriter.toString())
                stringWriter.close()
            }

            managerAccess.failed(this)
        }
    }

    private List<ResultNode> getAncestorResults() {
        return new ArrayList<ResultNode>(ancestorResults.toList()).reverse()
    }

    private List<ResultNode> getSiblingResults() {
        return new ArrayList<ResultNode>(ancestorResults.peek().children).reverse()
    }

    private List<ResultNode> getPreviousResults(String name) {
        return new ArrayList<ResultNode>(ancestorResults.peek().children.findAll {it.name.equals(name)}.toList()).reverse()
    }

    public boolean registerTimerExpireAndCheckForShouldStart() {
        boolean transactionCompleted = false;
        boolean returnValue = false;
        while (!transactionCompleted) {
            RescheduleManager originalValue = rescheduleManager.get()
            RescheduleManager workingValue = originalValue.clearByTime();
            if (workingValue.shouldStart()) {
                workingValue = workingValue.adjustForStart();
                transactionCompleted = rescheduleManager.compareAndSet(originalValue, workingValue)
                returnValue = transactionCompleted;
            } else {
                transactionCompleted = rescheduleManager.compareAndSet(originalValue, workingValue)
            }
        }
        returnValue
    }

    public boolean registerExecutionCompletionAndCheckForShouldStart() {
        boolean transactionCompleted = false;
        boolean returnValue = false;
        while (!transactionCompleted) {
            RescheduleManager originalValue = rescheduleManager.get()
            RescheduleManager workingValue = originalValue.executionCompleted();
            if (workingValue.shouldStart()) {
                workingValue = workingValue.adjustForStart();
                transactionCompleted = rescheduleManager.compareAndSet(originalValue, workingValue)
                returnValue = transactionCompleted;
            } else {
                transactionCompleted = rescheduleManager.compareAndSet(originalValue, workingValue)
            }
        }
        returnValue
    }

    private class Scheduler extends TimerTask {
        private final long actionInterval
        private final Timer actionStartTimer

        public Scheduler(long actionInterval) {
            this.actionInterval = actionInterval > 0 ? actionInterval : 1 //we have a lower bound of 1ms for execution time
            this.actionStartTimer = new Timer(spec.name, true)
        }

        public void start() {
            actionStartTimer.schedule(this, 0, actionInterval)
        }

        @Override
        void run() {
            if (failure != null) {
                actionStartTimer.cancel()
                actionStartTimer.purge()
            } else {
                if (actionCountdown.decrementAndGet() == 0) {
                    //last action, stop timer
                    actionStartTimer.cancel()
                }
                if (registerTimerExpireAndCheckForShouldStart()) {
                    managerAccess.ready(UseCaseInstance.this)
                }
            }
        }
    }

    //This class is for some tricky lock free stuff. Just using synchronization may have
    private static class RescheduleManager {

        final long clearedByTime;
        final boolean running;

        RescheduleManager(long clearedByTime, boolean running) {
            this.clearedByTime = clearedByTime;
            this.running = running;
        }

        RescheduleManager() {
            this(0, false)
        }

        public RescheduleManager clearByTime() {
            ensureProperSavedState()
            return new RescheduleManager(clearedByTime + 1, running)
        }

        public boolean shouldStart() {
            return !running && clearedByTime > 0;
        }

        public RescheduleManager adjustForStart() {
            return new RescheduleManager(clearedByTime - 1, true)
        }

        public RescheduleManager executionCompleted() {
            ensureProperSavedState()
            return new RescheduleManager(clearedByTime, false)
        }

        private void ensureProperSavedState() {
            if (clearedByTime > 0 && !running) {
                throw new IllegalStateException("If we are cleared by time, why are we not running?")
            }
        }
    }

    public static class ResultNode {
        public String name
        public Map<String,Object> result
        public List<ResultNode> children = new ArrayList<>(8)

        public ResultNode(String name, Map<String,Object> resultMap) {
            this.name = name
            this.result = resultMap
        }

        public void addChild(ResultNode node) {
            children.add(node)
        }
    }
}
