/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import org.junit.Test

class TestActionBuilder {

    // Make sib prevSib and prev prevRep
    @Test
    public void testTwoActionDef() {

        def actions = new HashMap<String, ActionDef>()
        actions.put('bob', new ActionDef(name: 'bob', repetitions: 1, useCaseName: 'Bubba', runStep:1, depth: 1))
        actions.put('joe', new ActionDef(name: 'joe', repetitions: 1, useCaseName: 'CupOf', runStep:2, depth: 1))
        def builder = new ActionBuilder(actions)
        builder.buildSpec('bob') {
            args = ['duh', result(bob, z), result(yo),result(name: hum), kevin]
            executes = {Integer num -> println num}
            returns = [w, 'r', value(x), value(name:q, notNull:true), value(name:dude, type:'java.lang.Float'), value(name:bob, type:BigDecimal.class) ]
            //kevin = 5
           // args = [joe]//() //TODO -- this should be an error
            //cup()
            //123.5s
        }

        builder.buildSpec('joe') {
            args = {[supplied(5), {->println "Hello World"}, result(joe, y),result(x)]}
            executes = {String mood, num -> println mood * num}
            returns {[x, value(z, true, Integer.class), value(name:zed, notNull:true)]}
        }

        actions.each { String key, ActionDef it -> println "Action: $it" }

    }

}
