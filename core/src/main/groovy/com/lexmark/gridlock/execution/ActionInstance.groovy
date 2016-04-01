/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.util.concurrent.atomic.AtomicLong

class ActionInstance {

    private static final Map<ActionSpec,AtomicLong> iAbsMap = new HashMap<ActionSpec,AtomicLong>() {
        @Override
        AtomicLong get(Object key) {
            AtomicLong returnValue = super.get(key)
            if (returnValue == null) {
                synchronized (this) {
                    returnValue = super.get(key)
                    if (returnValue == null) {
                        returnValue = new AtomicLong(0)
                        super.put(key,returnValue)
                    }
                }
            }
            return returnValue
        }
    }

    private final ActionSpec spec
    private final Collection<ActionInstance> children

    public final Integer actionInstanceNumber
    public long iAbs
    public long iSibs
    public long iPrev


    public ActionInstance(ActionSpec spec, Integer actionInstanceNumber) {
        this(spec, actionInstanceNumber, new ArrayList<ActionInstance>(16))
    }

    public ActionInstance(ActionSpec spec, Integer actionInstanceNumber, Collection<ActionInstance> children) {
        this.spec = spec
        this.actionInstanceNumber = actionInstanceNumber
        this.children = children
        this.iAbs = iAbsMap.get(this.spec).getAndIncrement()
    }

    public ActionSpec getSpec() {
        return spec
    }

    public void addChildAction(ActionInstance actionInstance) {
        actionInstance.iSibs = children.size()
        actionInstance.iPrev = children.collect {it.spec == actionInstance.spec}.size()
        children.add(actionInstance)
    }

    public Closure getExecute() {
        return spec.execute
    }

    Iterable<ActionInstance> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return spec.name
    }
}
