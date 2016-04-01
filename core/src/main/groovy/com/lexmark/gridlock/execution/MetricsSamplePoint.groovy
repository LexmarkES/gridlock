/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class MetricsSamplePoint {
    public final Date startTime
    public final Date stopTime
    public final MetricsDimensions dimensions

    MetricsSamplePoint(Date startTime, Date stopTime, MetricsDimensions dimensions) {
        this.dimensions = dimensions
        this.stopTime = stopTime
        this.startTime = startTime
    }

    MetricsSamplePoint(MetricsDimensions dimensions) {
        this(new Date(), dimensions)
    }

    private MetricsSamplePoint(Date date, MetricsDimensions dimensions) {
        this(date, date, dimensions)
    }
}
