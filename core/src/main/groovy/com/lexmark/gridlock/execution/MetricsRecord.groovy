/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.text.DateFormat
import java.text.SimpleDateFormat

class MetricsRecord implements Comparable<MetricsRecord>{
    private static final ThreadLocal<DateFormat> dateFormatter = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        }
    }
    private final MetricsDimensions dimensions
    private final MetricsResult result
    private final Date startTime
    private final Date stopTime

    MetricsRecord(MetricsDimensions dimensions, MetricsResult result, Date startTime, Date stopTime ) {
        this.stopTime = stopTime
        this.startTime = startTime
        this.result = result
        this.dimensions = dimensions
    }

    public String toString() {
        return "StartTime: ${dateFormatter.get().format(startTime)} StopTime: ${dateFormatter.get().format(stopTime)} Action: ${dimensions.actionName} Category: ${dimensions.category} Occurrences: ${result.occurrences} TotalSecs: ${result.totalSeconds} AveSecs: ${result.averageSeconds}"
    }

    public String asFileEntry() {
        return "${dateFormatter.get().format(stopTime)}\t${dimensions.category}\t${dimensions.actionName}\t${result.occurrences}\t${result.averageSeconds}"
    }

    public ArrayList<String> asLineArray() {
        return [dateFormatter.get().format(stopTime),
                dimensions.category,
                dimensions.useCaseName,
                dimensions.actionName,
                result.occurrences,
                result.averageSeconds
        ] as ArrayList<String>
    }

    @Override
    int compareTo(MetricsRecord o) {
        int comparedTime = this.stopTime.compareTo(o.stopTime)
        return comparedTime != 0 ? comparedTime : dimensions.compareTo(o.dimensions)
    }

}
