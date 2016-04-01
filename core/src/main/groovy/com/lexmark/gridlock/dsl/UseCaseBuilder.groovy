/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

class UseCaseBuilder {
    final public UseCaseDef useCaseDef
    final Map<String, ActionDef> actions
    final ActionBuilder actionBuilder
    final FlowBuilder flowBuilder
    UseCaseBuilder(UseCaseDef useCaseDef) {
        this.useCaseDef = useCaseDef
        this.actions = new HashMap<String, ActionDef>()
        this.flowBuilder = new FlowBuilder(actions, '[unknown]')
        this.actionBuilder = new ActionBuilder(actions)
    }

    public void useCase(String name, @DelegatesTo(UseCaseDefDelegate) Closure useCaseDefClosure) {
        this.useCaseDef.name = name
        this.flowBuilder.useCaseName = name
        def useCaseDelegate = new UseCaseDefDelegate(this)
        useCaseDefClosure.setDelegate(useCaseDelegate)
        useCaseDefClosure.setResolveStrategy(Closure.DELEGATE_FIRST)
        useCaseDefClosure.call()
        actions.each {key, value -> useCaseDef.actionTree.add(value)}
        useCaseDelegate.validate()
        useCaseDef.validate()
    }

}
