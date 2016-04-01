/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.util.function.Supplier

enum MetricsRate implements Supplier<Integer> {
    per_second (1),
    per_minute (60),
    per_hour (360),
    none(0)

    private final int timeInSeconds

    MetricsRate(int timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @Override
    Integer get() {
        return timeInSeconds
    }
}
