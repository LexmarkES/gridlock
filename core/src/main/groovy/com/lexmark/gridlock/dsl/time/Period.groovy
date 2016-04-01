/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl.time

import java.util.concurrent.TimeUnit

class Period {

    final Double value
    final TimeUnit unit
    final Long periodInMs

    public Period(Number input, TimeUnit unit, Long periodInMs) {
        this.value = input
        this.unit = unit
        this.periodInMs = periodInMs
    }

    @Override
    public String toString() {
        return "Period: $periodInMs milliseconds"
    }

}
