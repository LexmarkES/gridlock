/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import org.junit.Ignore
import org.junit.Test
import com.lexmark.gridlock.execution.GridLockTest

class TestTestBuilder {

    @Ignore
    @Test
    public void buildBasicTest() {
        TestBuilder builder = new TestBuilder("burgerDay")
        builder.define {
            useCaseStaggerInSecs = 3
            useCase('delton') {
                paramSource = {-> [plan: 'sketchy', user: 'dave']}
                paramSink = { -> println "Work is Done!"}
                numAllowedFailures(3)
                flowStartSpacingInSecs = 3
                flowDurationInSecs 10
                actionFlow {
                    action('hitSnooze', 4)
                    action(name:'getReady') {
                        action(eatFood)
                        action(brushTeeth, 2 /*twice*/) {
                            action(name:'upper', reps:30)
                            action(name:lower, reps:30)
                            action(tongue) {
                                action(scrape)
                            }
                        }
                        action('fluffHair')
                    }
                    action(commute) {
                        action(passPeople,3) {
                            action('honk',2)
                            action(name:'wave')
                        }
                        action(getParkingSpot)
                    }
                    action(doWork,50)
                    action(name:'surf', reps:2)
                    action(burgerBuddyTime)
                }
                actionDef('hitSnooze') {
                    args = [result('hitSnooze')]
                    executes = {Integer previousSnoozes -> println "Snoozing for the ${++previousSnoozes} time!"; return previousSnoozes}
                    returns = [value(snoozeCnt,Integer.class)]
                }
                actionDef(getReady) {
                    args = [snoozeCnt]
                    executes = { Integer snoozeCnt ->
                        if (snoozeCnt <= 1) {
                            println "Running on time.  Yeah!"
                        } else if (snoozeCnt == 2) {
                            println "Better rush!"
                        } else {
                            println "Going to be late!"
                        }
                    }
                    returns = []
                }
            }

            useCase(bam) {
                paramSource = {-> [plan: 'dictated', user: 'ben']}
                paramSink = { -> println "Time for Baby!"}
                numAllowedFailures = 0
                flowStartSpacingInSecs 5
                flowDurationInSecs (12)
                actionFlow {
                    action(yesDear, 5.5)
                    action(getReady) {
                        action(eatSomething) {}
                        action(dressQuickly)
                    }
                    action(commute) {
                        action(passPeople,37)
                        action('park')
                    }
                    action(routine,20) {
                        action(doWork1,2)
                        action(heyWayne,5)
                        action(doWork2,3)
                        action(surfReddit) {
                            action(lookWayne,2)
                        }
                    }
                    action(burgerBuddyTime)
                }
                // TODO: fill in action defs
            }
        }

        builder.validate()
        builder.cases.each {
            println "UseCaseDef: $it"
        }

        GridLockTest test = builder.getTest()
        test.startTest()
        println "Test: $test"
        sleep(60000)
    }
}
