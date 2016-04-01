/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import groovy.util.logging.Slf4j

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Slf4j
class GridLockTest {

    private static final AtomicLong nStarted = new AtomicLong();

    private final MyManagerAccess managerAccess = new MyManagerAccess()
    private final Map<UseCaseSpec, AtomicInteger> useCaseFailureMap = new HashMap<>()

    private final Timer useCaseTimer = new Timer("use-case-timer", true)
    private Optional<UseCaseStaggerTask> runningUseCase = Optional.empty()
    private final long stagger_ms
    private final AtomicBoolean stopInitiated = new AtomicBoolean(false)
    private ManagerState managerState = ManagerState.IDLE

    private final List<PFunctionTask> initialPFunctionTasks = new ArrayList<>()
    private final ExecutorService pool = Executors.newCachedThreadPool(new DaemonThreadFactory())

    private final EventBus runningUseCaseEventBus = new EventBus()
    private final LoggingListener loggingListener = new LoggingListener()
    private final Set<UseCaseInstance> runningUseCaseInstances = Collections.synchronizedSet(new HashSet<>());

    private final CountDownLatch doneSignal = new CountDownLatch(1);

    private MetricsTracker metricsTracker;

    public GridLockTest(long stagger_ms) {
        this.stagger_ms = stagger_ms
        this.runningUseCaseEventBus.register(this.loggingListener)
    }

    public void addUseCase(UseCaseSpec<?> useCaseSpec, Closure<Long> pFunction, long executionTime_ms) {
        useCaseFailureMap.put(useCaseSpec, new AtomicInteger(0))
        UseCaseInstanceFactory<?> factory = new UseCaseInstanceFactory<>(useCaseSpec, managerAccess, executionTime_ms)
        initialPFunctionTasks.add(new PFunctionTask(factory, pFunction))
    }

    public void runForever(MetricsRate metricsRate) {
        runForTime(Integer.MAX_VALUE, TimeUnit.SECONDS, metricsRate)
    }

    public void runForever(MetricsConfig metricsConfig) {
        runForTime(Integer.MAX_VALUE, TimeUnit.SECONDS, metricsConfig)
    }

    public void runForTime(Integer timeToRun, TimeUnit timeUnit, MetricsRate metricsRate) {
        MetricsConfig metricsConfig
        PrintStream printStream
        try {
            if (metricsRate.get() > 0) {
                printStream = new PrintStream(new FileOutputStream(new File("./metrics.tsv")));
                metricsConfig = new MetricsConfig(metricsRate, printStream)
            } else {
                metricsConfig = new MetricsConfig(metricsRate, new NullStream())
            }

            runForTime(timeToRun, timeUnit, metricsConfig)
        } finally {
          if( printStream != null) {
              printStream.close()
          }
        }

    }

    public void runForTime(Integer timeToRun, TimeUnit timeUnit, MetricsConfig metricsConfig) {
        if(metricsConfig.samplePeriod != 0) {
            metricsTracker = new MetricsTracker(metricsConfig.samplePeriod, new PrintStream(metricsConfig.outputStream))
            metricsTracker.start()
        }
        startTest()
        doneSignal.await(timeToRun, timeUnit)
        beginShutdown()
        awaitShutdown()
        if( metricsTracker != null) {
            metricsTracker.stop()
        }
    }

    public void beginShutdownNow() {
        doneSignal.countDown()
    }

    private class NullStream extends OutputStream {
        @Override
        void write(int b) throws IOException {

        }
    }

    public void startTest() {
        if (managerState.equals(ManagerState.STARTED)) {
            throw new IllegalStateException("UseCaseManager is already running test")
        } else if (managerState.equals(ManagerState.STOPPING)) {
            throw new IllegalStateException("UseCaseManager is stopping and cannot be started until awaitShutdown() has been called")
        } else {
            UseCaseStaggerTask useCaseStaggerTask = new UseCaseStaggerTask()
            useCaseStaggerTask.schedule()
            runningUseCase = Optional.of(useCaseStaggerTask)
            managerState = ManagerState.STARTED

        }
    }

    private void beginShutdown() {
        if (managerState.equals(ManagerState.IDLE)) {
            throw new IllegalStateException("UseCaseManager cannot begin shutdown because it is not running")
        } else if (managerState.equals(ManagerState.STOPPING)) {
            throw new IllegalStateException("UseCaseManager cannot begin shutdown because it is already shutting down")
        } else {
            UseCaseStaggerTask useCaseStaggerTask = runningUseCase.get()
            useCaseStaggerTask.cancel()
            stopInitiated.set(true)
            managerState = ManagerState.STOPPING
        }
    }

    private void awaitShutdown() {
        if (managerState.equals(ManagerState.IDLE)) {
            throw new IllegalStateException("UseCaseManager cannot await shutdown because it is not running")
        } else if (managerState.equals(ManagerState.STARTED)) {
            beginShutdown()
        }
        CountDownLatch awaitInFlightLatch = new CountDownLatch(1)
        LatchingEmptySetListener emptySetListener = new LatchingEmptySetListener(runningUseCaseInstances, awaitInFlightLatch)
        runningUseCaseEventBus.register(emptySetListener)
        awaitInFlightLatch.await()

        runningUseCaseEventBus.unregister(emptySetListener)
        runningUseCase = Optional.empty()
        managerState = ManagerState.IDLE
    }

    private void failAndCheckForHalt(UseCaseSpec spec) {
        if (useCaseFailureMap.get(spec).incrementAndGet() >= spec.numAllowedFailures) {
            log.error("maximum number of failures exceeded for spec: $spec")
            log.error("exiting")
            Runtime.getRuntime().halt(1)
        }
    }

    private class UseCaseStaggerTask extends TimerTask {

        private final Iterator<PFunctionTask> intervalTaskIterator = initialPFunctionTasks.iterator()

        public schedule() {
            useCaseTimer.schedule(this, 0, stagger_ms)
        }

        @Override
        void run() {
           if(intervalTaskIterator.hasNext()) {
                intervalTaskIterator.next().start()
           } else {
                this.cancel()
           }
        }
    }

    private class PFunctionTask extends TimerTask {

        private final UseCaseInstanceFactory factory
        private final Closure<Long> pFunction

        public PFunctionTask(UseCaseInstanceFactory factory, Closure<Long> pFunction) {
            this.factory = factory
            this.pFunction = pFunction
        }

        public start() {
            useCaseTimer.schedule(this, 0)
        }

        @Override
        void run() {
            //first check to see if the test is stopping, cancel if it is
            if (stopInitiated.get()) {
                this.cancel()
            } else {
                long nextDelay = pFunction()
                useCaseTimer.schedule(new PFunctionTask(factory, pFunction), nextDelay)

                try {
                    UseCaseInstance useCaseInstance = factory.createUseCase()
                    runningUseCaseInstances.add(useCaseInstance)

                    useCaseInstance.start()
                    nStarted.incrementAndGet();

                    MetricsInterval.acceptSample(new MetricsSamplePoint(
                            new MetricsDimensions(
                                    "SYSTEM",
                                    "IN-FLIGHT",
                                    "GRIDLOCK"
                            )
                    ))

                } catch (Throwable t) {
                    log.error("failed to create usecase instance, reason: $t", t)
                    failAndCheckForHalt(factory.getSpec())
                }
            }
        }
    }

    private class MyManagerAccess implements ManagerAccess {

        @Override
        void failed(UseCaseInstance useCaseInstance) {
            if (useCaseFailureMap.get(useCaseInstance.spec).incrementAndGet() >= useCaseInstance.spec.numAllowedFailures) {
                failAndCheckForHalt(useCaseInstance.getSpec())
            }
            runningUseCaseInstances.remove(useCaseInstance)
            runningUseCaseEventBus.post(new RemoveEvent(RemoveEvent.Reason.FAILED))
            nStarted.decrementAndGet();
        }

        @Override
        void ready(UseCaseInstance useCaseInstance) {
            Runnable runnable = new Runnable() {
                @Override
                void run() {
                    useCaseInstance.executeNext()
                }
            }
            pool.submit(runnable)
        }

        @Override
        void done(UseCaseInstance useCaseInstance) {
            Closure sink = (Closure) useCaseInstance.spec.paramSink.clone()
            try {
                sink(useCaseInstance.param)
            } catch (Throwable t) {
                log.error("failed to execute usecase sink, reason: $t")
                failAndCheckForHalt(useCaseInstance.getSpec())
            }
            runningUseCaseInstances.remove(useCaseInstance)
            runningUseCaseEventBus.post(new RemoveEvent(RemoveEvent.Reason.DONE))
            nStarted.decrementAndGet();
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        Thread newThread(Runnable r) {
            Thread thread = new Thread(r)
            thread.setDaemon(true)
            return thread;
        }
    }

    private static class LatchingEmptySetListener {
        private final Set emptyingSet
        private final CountDownLatch emptyLatch

        public LatchingEmptySetListener(Set set, CountDownLatch latch) {
            this.emptyingSet = set
            this.emptyLatch = latch

            if (this.emptyingSet.isEmpty()) {
                this.emptyLatch.countDown()
            }
        }

        @Subscribe
        public void countDownOnEmpty(RemoveEvent event) {
            int remaining = emptyingSet.size()
            log.debug("LatchingEmptySetListener handled RemoveEvent with [${remaining}] items remaining")
            if (emptyingSet.isEmpty()) {
                log.debug("LatchingEmptySetListener handled RemoveEvent with empty set, counting down latch")
                emptyLatch.countDown()
            }
        }
    }

    private static class RemoveEvent {
        public enum Reason { DONE, FAILED }
        private final Reason reason

        public RemoveEvent(Reason reason) {
            this.reason = reason
        }

        public Reason getReason() {
            return reason
        }
    }

    private static class LoggingListener {

        @Subscribe
        public void logOnRemoveEvent(RemoveEvent event) {
            log.debug("RemoveEvent fired with reason: ${event.getReason().name()}")
        }
    }

    private enum ManagerState { IDLE, STARTED, STOPPING}
}
