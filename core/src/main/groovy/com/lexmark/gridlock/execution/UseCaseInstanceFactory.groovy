/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

class UseCaseInstanceFactory<T> {

    private final ManagerAccess managerAccess
    private final UseCaseSpec<T> spec
    private final long runLength
    private final AtomicInteger instanceCount = new AtomicInteger(0)
    private final Timer useCaseTimer = new Timer(true)
    private final ExecutorService useCaseThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        Thread newThread(Runnable r) {
            Thread thread = new Thread(r)
            thread.setDaemon(true)
            return thread
        }
    })

    public UseCaseInstanceFactory(UseCaseSpec<T> spec, ManagerAccess managerAccess, long runLength) {
        this.spec = spec
        this.managerAccess = managerAccess
        this.runLength = runLength
    }

    synchronized UseCaseInstance<T> createUseCase() {
        ActionInstanceCounter actionInstanceCounter = new ActionInstanceCounter()
        Closure paramSource = (Closure) spec.paramSource.clone()
        T param = paramSource()

        UseCaseInstance<T> useCaseInstance = new UseCaseInstance<T>(spec, instanceCount.incrementAndGet(), param, managerAccess, runLength, useCaseTimer, useCaseThreadPool)
        spec.rootActionSpecs.each { ActionSpec actionSpec ->
            actionSpec.multiplier.times {
                ActionInstance actionInstance = makeActionInstanceTree(actionSpec, actionInstanceCounter)
                useCaseInstance.addActionInstance(actionInstance)
            }
        }

        return useCaseInstance
    }

    private ActionInstance makeActionInstanceTree(ActionSpec actionSpec, ActionInstanceCounter actionInstanceCounter) {
        ActionInstance rootActionInstance = new ActionInstance(actionSpec, actionInstanceCounter.nextActionInstanceNumber(actionSpec))
        makeChildrenActionInstances(rootActionInstance, actionInstanceCounter)
        return rootActionInstance
    }

    private void makeChildrenActionInstances(ActionInstance parentActionInstance, ActionInstanceCounter actionInstanceCounter) {
        parentActionInstance.spec.children.each { ActionSpec childActionSpec ->
            childActionSpec.multiplier.times {
                ActionInstance childActionInstance = new ActionInstance(childActionSpec, actionInstanceCounter.nextActionInstanceNumber(childActionSpec))
                parentActionInstance.addChildAction(childActionInstance)
                makeChildrenActionInstances(childActionInstance, actionInstanceCounter)
            }
        }
    }

    public UseCaseSpec<T> getSpec() {
        return spec
    }

    private static class ActionInstanceCounter {
        private final Map<ActionSpec, AtomicInteger> actionMap = new HashMap<>()

        public Integer nextActionInstanceNumber(ActionSpec actionSpec) {
            AtomicInteger actionCounter = actionMap.get(actionSpec)
            if (actionCounter == null) {
                actionMap.put(actionSpec, new AtomicInteger(0))
                actionCounter = actionMap.get(actionSpec)
            }
            return actionCounter.incrementAndGet()
        }
    }
}
