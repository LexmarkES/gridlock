/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class MetricsSampler {

    private final MetricsDimensions metricsDimensions

    MetricsSampler(MetricsDimensions metricsDimensions) {
        this.metricsDimensions = metricsDimensions
    }

    void report(long startTime, long stopTime) {
        if (startTime != null && stopTime != null) {
            MetricsInterval.acceptSample(new MetricsSamplePoint(new Date(startTime),new Date(stopTime), metricsDimensions))
        }
    }
}
