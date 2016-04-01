/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl.time

import java.util.concurrent.TimeUnit

class Frequency extends Period {
    Frequency(Number input, TimeUnit unit) {
        super(input, unit, (1.0 / (Double)((Double)input / unit.toMillis(1))).round(0).toLong())
    }

    @Override
    public String toString() {
        return "Frequency: ${value} times per ${unit.toString().reverse().replaceFirst('S','').reverse()}," +
                " Period: $periodInMs milliseconds"
    }
}
