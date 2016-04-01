/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import org.junit.Ignore
import org.junit.Test

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class UseCaseTest {

    @Test
    public void runOneCase() {

        Calendar calendar = GregorianCalendar.getInstance()
        Closure getTime = {
            calendar.setTimeInMillis(System.currentTimeMillis())
            return calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND)
        }

        Closure<String> source = {
            println "sourced at: ${getTime()}"
            return "sample param"
        }

        Closure sink = {
            println "sunk at: ${getTime()}"
        }

        UseCaseSpec<String> useCaseSpec = new UseCaseSpec<>("simple-case", 0, source, sink)
        ActionSpec rootActionSpec = new ActionSpec("root-action", 0, useCaseSpec, {
            ->
            println "param: ${gridlock.param}"
            println "root action completed at: ${getTime()}"
            return [parentOutput: "Luke, I am your father."]
        })

        ActionSpec childActionSpec = new ActionSpec("child-action", 1, useCaseSpec, {
            String parentOutput ->
            println "Obiwan said you killed my father.  ${parentOutput}"
            println "child action completed at: ${getTime()}"
            return[:]
        })
        childActionSpec.addArg({gridlock.result("parentOutput")})

        rootActionSpec.addChildSpec(childActionSpec)
        useCaseSpec.addActionSpec(rootActionSpec)

        RunOneCaseManager manager = new RunOneCaseManager(useCaseSpec, TimeUnit.SECONDS.toMillis(20))
        manager.startTest()
    }

    @Test
    public void runOmnicareCreateDocOnce() {
        UseCaseSpec useCaseSpec = createOmnicareCreateDocUseCase()

        RunOneCaseManager manager = new RunOneCaseManager(useCaseSpec, TimeUnit.SECONDS.toMillis(20))
        manager.startTest()
    }

    @Test
    public void runOmnicareCreateDocOnceWithMetrics() {
        MetricsTracker tracker = new MetricsTracker(123456, System.out)
        tracker.start()

        runOmnicareCreateDocOnce()

        tracker.stop()
        tracker.doSampling()
    }

    @Ignore
    @Test
    public void runOmnicareCreateDoc() {

        UseCaseSpec useCaseSpec = createOmnicareCreateDocUseCase()

        GridLockTest manager = new GridLockTest(TimeUnit.SECONDS.toMillis(10), TimeUnit.MINUTES.toMillis(5))
        manager.addUseCase(useCaseSpec, TimeUnit.MINUTES.toMillis(2), TimeUnit.SECONDS.toMillis(20))
        manager.startTest()

        new CountDownLatch(1).await(5, TimeUnit.MINUTES)
    }

    public UseCaseSpec createOmnicareCreateDocUseCase() {

        UseCaseSpec<String> useCaseSpec = new UseCaseSpec<>("omni-create-doc", 0, {return ""}, {})
        ActionSpec createDoc = new ActionSpec("create-doc", 0, useCaseSpec, {
            ->
            gridlock.context.put("runId", UUID.randomUUID().toString())
            String docId = "12345"
            println "created document with id: [$docId]"
            return [docId: docId]
        })

        ActionSpec setCp = new ActionSpec("set-cp", 1, useCaseSpec, {
            String docId ->
            println "set custom props on docId: [$docId]"
            return [:]
        })
        setCp.addArg({gridlock.result("docId")})

        ActionSpec addPage = new ActionSpec("add-page", 1, useCaseSpec, {
            String docId, Integer pageNum ->
            String pageId = docId + "-" + pageNum
            println "added page with pageId: [$pageId] to doc with docId: [$docId]"
            return [pageId: pageId, pageNum: pageNum]
        }, 5)
        addPage.addArg({gridlock.result("docId")})
        addPage.addArg({Integer pageNum = gridlock.result('add-page',"pageNum"); pageNum ? pageNum + 1 : 1})

        ActionSpec anotPage = new ActionSpec("anot-page", 2, useCaseSpec, {
            String pageId ->
            println "annotated page with pageId: [$pageId]"
            return [:]
        })
        anotPage.addArg({gridlock.result("pageId")})

        ActionSpec addToWf = new ActionSpec("add-to-wf", 1, useCaseSpec, {
            String docId ->
            String wfItemId = "67890"
            println "created wf item with id: [$wfItemId] from doc with docId: [$docId]"
            return [wfItemId: wfItemId]
        })
        addToWf.addArg({gridlock.result("docId")})

        ActionSpec routeFwd = new ActionSpec("route-fwd", 2, useCaseSpec, {
            String wfItemId, String queueId ->
            println "routed wf item with id: [$wfItemId] to queue with queuId: [$queueId]"
            println "run with id: ${gridlock.context.get("runId")} finished"
            return [:]
        })
        routeFwd.addArg({gridlock.result("wfItemId")})
        routeFwd.addArg("queue-777")

        //build action tree
        addPage.addChildSpec(anotPage)
        addToWf.addChildSpec(routeFwd)

        createDoc.addChildSpec(setCp)
        createDoc.addChildSpec(addPage)
        createDoc.addChildSpec(addToWf)

        useCaseSpec.addActionSpec(createDoc)

        return useCaseSpec
    }
}
