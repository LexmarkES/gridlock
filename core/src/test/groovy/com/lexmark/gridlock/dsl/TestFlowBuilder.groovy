/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import org.junit.Test

public class TestFlowBuilder {
    @Test
    public void testActionFlow() {
        def builder = new FlowBuilder(new HashMap<String, ActionDef>(), 'flowey')
        builder.flowSequence {
            action(top1) {
                action(mid1,2) {}
                action('mid2') {
                    action('bottom2')
                    action(bottom2b)
                    action(bottom2c) {
                        action(subBottom2a)
                        action(action:subBottom2b, reps:5)
                    }
                    //Action('bob',6)
                    //action2()
                }
            }
            action(name:top2, reps:3)
            action(name:'top3', reps:2)
            action('top4',2.5) {
            }
        }

        builder.flowTree.each { println "Action: $it"}
    }

}
