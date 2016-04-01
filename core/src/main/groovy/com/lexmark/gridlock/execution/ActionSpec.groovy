/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class ActionSpec {

    public final int multiplier
    public final long depth
    public final String name
    public final Closure<Map<String,Object>> execute
    public final List<Object> args = new ArrayList<>(4)
    public final UseCaseSpec<?> useCaseSpec

    public final List<ActionSpec> children = new ArrayList<ActionSpec>()

    public ActionSpec(String name, long depth, UseCaseSpec<?> useCaseSpec, Closure<Map<String,Object>> execute, multiplier = 1) {
        this.name = name
        this.depth = depth
        this.useCaseSpec = useCaseSpec
        this.execute = execute
        this.multiplier = multiplier
    }

    public void addArg(Object object) {
        args.add(object)
    }

    public void addChildSpec(ActionSpec actionSpec) {
        children.add(actionSpec)
    }

    @Override
    int hashCode() {
        Objects.hash(this.name, useCaseSpec)
    }

    @Override
    boolean equals(Object obj) {
        return obj instanceof ActionSpec && ((ActionSpec) obj).useCaseSpec == this.useCaseSpec && ((ActionSpec) obj).name == this.name
    }
}
