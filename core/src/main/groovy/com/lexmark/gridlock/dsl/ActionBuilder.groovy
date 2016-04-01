/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl
class ActionBuilder {

    private final Map<String, ActionDef> actions

    ActionBuilder(Map<String, ActionDef> actions) {

        this.actions = actions
    }

    public void buildSpec(String name, @DelegatesTo(ActionArgsDefDelegate) Closure actionDef) {
        ActionDef spec = actions.get(name)
        if (spec == null) {
            throw new Exception("Did not find action: $name in the flow specs");
        }
        if (spec.fullyDefined) {
            throw new Exception("Have already fully defined action: $name")
        }
        def add = new ActionDefDelegate(spec)
        actionDef.setDelegate(add)
        actionDef.setResolveStrategy(Closure.DELEGATE_ONLY)
        try {
            actionDef.call()
        } catch (MissingPropertyException mpe) {
            println "Caught mpe: ${mpe.messageWithoutLocationText} for property: ${mpe.property}"
        }
        add.verifyTokenMatches()
        spec.fullyDefined = true
    }

}



