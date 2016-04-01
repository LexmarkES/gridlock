/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import com.lexmark.gridlock.dsl.time.Duration

// Need to define 2 of the following 3 parameters to determine flow timings.
class FlowTimingDef {

    private Closure<Long> periodFunction
    private Duration executionTime
    private final String context

    public FlowTimingDef(String context) {
        this.context = context
    }

    public FlowTimingDef(String context, Closure<Long> periodFunction, Duration executionTime) {
        this.context = context
        this.periodFunction = periodFunction
        this.executionTime = executionTime
    }

    public setPeriodFunction(Closure<Long> c) {
        this.periodFunction = c
    }

    public getPeriodFunction() {
        return periodFunction
    }

    public setExecutionTime(Duration d) {
        this.executionTime = d
    }

    public Long getExecutionTimeInMillis() {
        return executionTime.periodInMs
    }

}
