/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import com.google.common.collect.ComparisonChain

class MetricsDimensions implements Comparable<MetricsDimensions> {

    public final String useCaseName
    public final String actionName
    public final String category

    MetricsDimensions(String useCaseName, String actionName, String category) {
        this.useCaseName = useCaseName
        this.actionName = actionName
        this.category = category
    }

    @Override
    int compareTo(MetricsDimensions that) {
        return ComparisonChain.start()
            .compare(this.useCaseName, that.useCaseName)
            .compare(this.actionName, that.actionName)
            .compare(this.category, that.category)
            .result()
    }

    @Override
    public boolean equals(Object o) {
        return compareTo((MetricsDimensions)o) == 0
    }

    @Override
    public int hashCode() {
        return actionName.hashCode() + category.hashCode() * 31

    }

}
