/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

public class FlowBuilder {
    Integer nextRunStep
    Integer currentActionDepth
    String useCaseName

    public final Map<String /*name*/, ActionDef> flowTree
    FlowBuilder(Map<String, ActionDef> actions, String useCaseName) {
        this.flowTree = actions
        nextRunStep = 1
        currentActionDepth = 1
        this.useCaseName = useCaseName
    }

    public void flowSequence(@DelegatesTo(FlowDefDelegate) Closure flowDef) {
        flowDef.setDelegate(new FlowDefDelegate(this))
        flowDef.setResolveStrategy(Closure.DELEGATE_FIRST)
        flowDef.call()
    }


}


