/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.util.function.Supplier

class MetricsConfig {
    Integer samplePeriod;
    OutputStream outputStream;

    public MetricsConfig(Supplier<Integer> period, OutputStream outputStream) {
        this.samplePeriod = period.get();
        this.outputStream = outputStream;
    }

    public static MetricsConfig stdoutPerSecond() {
        return new MetricsConfig(MetricsRate.per_second, System.out)
    }

    public static MetricsConfig stdoutPerMinute() {
        return new MetricsConfig(MetricsRate.per_minute, System.out)
    }

    public static MetricsConfig stdoutPerHour() {
        return new MetricsConfig(MetricsRate.per_hour, System.out)
    }
}
