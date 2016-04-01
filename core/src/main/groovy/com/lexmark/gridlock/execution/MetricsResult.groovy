/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class MetricsResult {
    public Integer occurrences /* aka NUM */
    public Double totalSeconds  /* in seconds */
    public Double averageSeconds
    /* TODO: other metrics */

    MetricsResult(Integer occurrences, Double totalSeconds) {
        this.occurrences = occurrences
        this.totalSeconds = totalSeconds
        this.averageSeconds = occurrences != 0 ? totalSeconds / occurrences : 0
    }

    void accumulate(MetricsResult results) {
        this.occurrences += results.occurrences
        this.totalSeconds += results.totalSeconds
        this.averageSeconds = occurrences != 0 ? totalSeconds / occurrences : 0
    }

}
