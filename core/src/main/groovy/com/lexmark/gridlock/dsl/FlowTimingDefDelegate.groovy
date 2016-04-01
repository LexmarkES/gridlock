/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import com.lexmark.gridlock.dsl.time.Duration

class FlowTimingDefDelegate {

    @Delegate
    InvalidDefDelegate idd
    final private FlowTimingDef flowTimingDef

    def propertyMissing(String name) {
        return handleMissingProperty(name)
    }

    def propertyMissing(String name, value) {
        return handleMissingProperty(name)
    }

    def methodMissing(String name, args) {
        return handleMissingMethod(name, args)
    }

    def periodFunction(Closure<Long> closure) {
        flowTimingDef.periodFunction = closure
    }

    def executionTime(Duration duration) {
        flowTimingDef.executionTime = duration
    }

    def setPeriodFunction(Object o) {
        if (o instanceof Closure<Long>) {
            periodFunction(o)
        } else {
            throw new RuntimeException("periodFunction must be of type Closure<Long>")
        }
    }

    def setExecutionTime(Object o) {
        if (o instanceof Duration) {
            executionTime(o)
        } else {
            throw new RuntimeException("executionTime must be of type Duration")
        }
    }

    FlowTimingDefDelegate(FlowTimingDef flowTimingDef, String useCaseName) {
        this.flowTimingDef = flowTimingDef
        this.metaClass = NumericUnitDslSupport.addTo(this.class)
        this.idd = new InvalidDefDelegate([this], "the 'timing' spec for UseCase: $useCaseName", null)
    }

}