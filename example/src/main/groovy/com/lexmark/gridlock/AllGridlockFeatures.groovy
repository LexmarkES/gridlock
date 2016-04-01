/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock

import com.lexmark.gridlock.dsl.TestBuilder
import com.lexmark.gridlock.execution.CustomMetricManager
import com.lexmark.gridlock.execution.GridLockTest
import com.lexmark.gridlock.execution.MetricsRate

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AllGridlockFeatures {

    public static def main(mainArgs) {

        //get reference to custom metrics manager, create timer for custom metric testing
        def custMetrics = CustomMetricManager.ensure()
        def timer = new Timer()
        def rand = new Random()

        //create some counters to help illustrate the flow of actions
        def paramCounter = new AtomicInteger(0)
        def innerActionCounter = new AtomicInteger(0)

        //create a new TestBuilder Object
        TestBuilder testBuilder = new TestBuilder("MainTest")

        //TestBuilder DSL
        testBuilder.define {
            //create a usecase
            useCase('SampleUseCase') {

                //define the param source and sink closures
                paramSource = {
                    return "Param#${paramCounter.incrementAndGet()}"
                }
                paramSink = { String param ->
                    println "sinking param: ${param}"
                }

                //set the number of allowed failures
                numAllowedFailures = 0

                //launch a new use case every 30 seconds, each use case should take 20 seconds to execute
                timing = {
                    periodFunction = { return 30000 }
                    executionTime = 20 sec
                }

                //the action flow, this is the outline of what action chain we will execute
                actionFlow {
                    action(firstAction)
                    action(secondAction) {
                        action(name: innerAction, repetitions: 3)
                    }
                    action(lastAction)
                }

                actionDef('firstAction') {
                    //this action requires no arguments
                    args = []

                    //code to execute for this action
                    executes {
                        //start a custom metric, schedule a task to randomly stop the metric
                        def metricId = custMetrics.startMetric(new Date(), 'SampleUseCase-custom', 'first-to-last')
                        timer.schedule(new TimerTask() {
                            @Override
                            void run() {
                                custMetrics.stopMetric(metricId, new Date())
                            }
                        }, rand.nextInt(2000-1000+1) + 1000) //random long between 1000 and 2000 (1 & 2 seconds)

                        //this demonstrates how to get the param
                        println "[firstAction] param is: ${gridlock.param}"

                        //return a map to capture output from this action, in this example our output is a UUID
                        return ['firstActionOutput': UUID.randomUUID().toString()]
                    }

                    //here we define which objects from the executes closure we want to pass on
                    returns = [firstActionOutput]
                }

                actionDef('secondAction') {
                    //this action takes one argument, firstActionOutput which is created by firstAction
                    args = [result(firstActionOutput)]

                    //the executes closure takes one argument, firstActionOutput
                    executes { String id ->
                        //this demonstrates that we received the unique ID from firstAction
                        println "[secondAction] firstAction output was: ${id}"

                        //return a list containing each part of the ID
                        def idParts = Arrays.asList(id.split('-'))
                        return [idParts: idParts]
                    }

                    returns = [idParts]
                }

                actionDef('innerAction') {
                    //this action takes two arguments, the idParts list and a position argument
                    args = [result(idParts), result(position)]

                    //the executes closure takes two arguments, idParts and the position
                    executes { List idParts, Integer position ->
                        //demonstrate the ability to use external objects
                        println "[innerAction] the inner action has now been run ${innerActionCounter.incrementAndGet()} times"

                        //demonstrate that the position can either be null if not set
                        println "[innerAction] the position argument is: ${position}"

                        //print the next position in the list
                        def currentPosition = position != null ? position + 1 : 0 //elvis operator
                        println "[innerAction] the current position is ${currentPosition} and the id piece is: ${idParts.get(currentPosition)}"

                        //return the current position so that the next innerAction can use it
                        return [position: currentPosition]
                    }
                }

                actionDef('lastAction') {
                    args = [result(firstActionOutput)]

                    executes { id ->
                        //demonstrate that we can get output from actions executed not immediately before us
                        println "[lastAction] id from firstAction is ${id}"

                        //this action returns nothing...
                    }

                    returns = []
                }
            } // end use case

            useCase('SecondUseCase') {

                paramSource = {
                    return "2ndUseCase"
                }
                paramSink = { String param ->
                    //do nothing
                }

                numAllowedFailures = 0

                timing = {
                    periodFunction = { return 15000 }
                    executionTime = 15 sec
                }

                actionFlow {
                    action(SingleAction)
                }

                actionDef('SingleAction') {
                    args = []
                    executes {
                        println gridlock.param
                    }
                    returns = []
                }
            }
        } // end test builder

        //create a GridLockTest object from the TestBuilder
        GridLockTest test = testBuilder.getTest()

        //run the test for two minutes, capture metrics per minute
        test.runForTime(2, TimeUnit.MINUTES, MetricsRate.per_minute)

        //cancel the custom metrics timer (demonstration purposes only)
        timer.cancel()

        //print any unresolved metrics (should be none)
        println "unresolved custom metrics: ${custMetrics.unresolvedMetrics}"
    }
}
