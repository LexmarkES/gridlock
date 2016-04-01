/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RunOneCaseManager {

    private final CountDownLatch latch = new CountDownLatch(1)
    private final UseCaseSpec<?> oneUseCaseSpec
    private final ExecutorService pool = Executors.newCachedThreadPool()
    private final long runLength_ms

    public RunOneCaseManager(UseCaseSpec<?> useCaseSpec, long runLength_ms) {
        this.oneUseCaseSpec = useCaseSpec
        this.runLength_ms = runLength_ms
    }

    public void startTest() {
        UseCaseInstanceFactory<?> factory = new UseCaseInstanceFactory<>(oneUseCaseSpec, new MyManagerAccess(), runLength_ms)
        UseCaseInstance useCaseInstance = factory.createUseCase()
        useCaseInstance.start()
        latch.await()
    }

    private class MyManagerAccess implements ManagerAccess {

        @Override
        void failed(UseCaseInstance useCaseInstance) {
            println "use case: ${useCaseInstance.spec.name} failed with message: ${useCaseInstance.getFailure().message}"
            useCaseInstance.failure.printStackTrace()
            this.done(useCaseInstance)
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
            sink.setDelegate(useCaseInstance)
            sink.setResolveStrategy(Closure.DELEGATE_FIRST)
            sink()

            latch.countDown()
        }
    }
}
