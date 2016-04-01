/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class UseCaseSpec<T> {
    public final name
    public final int numAllowedFailures
    public final Closure<T> paramSource
    public final Closure paramSink
    public final List<ActionSpec> rootActionSpecs = new ArrayList<ActionSpec>()

    public UseCaseSpec(String name, int numAllowedFailures, Closure<T> paramSource, Closure paramSink) {
        this.name = name
        this.numAllowedFailures = numAllowedFailures
        this.paramSource = paramSource
        this.paramSink = paramSink
    }

    public void addActionSpec(ActionSpec actionSpec) {
        rootActionSpecs.add(actionSpec)
    }

    @Override
    String toString() {
        return name;
    }
}
