/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

class ActionDefDelegate {

    public final ActionDef actionDef
    public final @Delegate(includes = ['value'])
    ActionReturnsDefDelegate returnsDelegate
    public final @Delegate(includes = ['result', 'supplied'])
    ActionArgsDefDelegate argsDelegate
    public final @Delegate
    InvalidDefDelegate idd

    def propertyMissing(String name) {
        return handleMissingProperty(name)
    }

    def propertyMissing(String name, value) {
        return handleMissingProperty(name)
    }

    def methodMissing(String name, args) {
        return handleMissingMethod(name, args)
    }

    public ActionDefDelegate(ActionDef actionDef) {
        this.actionDef = actionDef
        this.returnsDelegate = new ActionReturnsDefDelegate(this)
        this.argsDelegate = new ActionArgsDefDelegate(this)
        this.idd = new InvalidDefDelegate([this, argsDelegate, returnsDelegate], "the 'actionDef' spec for action: ${actionDef.name} in UseCase: ${actionDef.useCaseName}", null)
    }

    @SpecField(['List<Object>','Closure<List>'])
    public void setArgs(Object values) {
        if (values instanceof List) {
            args((List) values)
        } else if (values instanceof Closure) {
            args((Closure) values)
        } else {
            handleInvalidSpec("encountered invalid 'args' spec argument type of: " +
                    "${AvailableSpecs.simplifyTypePaths(values.class.name)}." +
                    "  Expecting a List or a Closure which returns a List.")
//            throw new IllegalArgumentException("Invalid 'args' spec argument type of: " +
//                    "${AvailableSpecs.simplifyTypePaths(values.class.name)}.  Expecting a List or a Closure which returns a List.")
        }
    }

    @SpecFunc
    public void args(@SpecFuncParam('values') List values) {
        values.each { value ->
            if (value instanceof ActionDef.ArgDef) {
                actionDef.inputValues.add((ActionDef.ArgDef) value)
            } else if (value instanceof Tokens.Name) {
                def name = ((Tokens.Name) value).name
                confirmTokenByName(value)
                actionDef.inputValues.add(new ActionDef.Result(name: name))
            } else {
                actionDef.inputValues.add(new ActionDef.Supplied(value: value))
            }
        }
        verifyTokenMatches() //'args'
    }

    @SpecFunc
    public void args(@DelegatesTo(ActionArgsDefDelegate) @SpecFuncParam('argValuesClosure')Closure argItems) {
        argItems.setDelegate(argsDelegate)
        argItems.setResolveStrategy(Closure.DELEGATE_FIRST)
        def results = argItems.call()
        if (results instanceof List) {
            args((List) results)
        } else {
            handleInvalidSpec("the 'args' closure for action: ${actionDef.name} when executed must return a List.  It however returned: ${results.class.name}")
            //throw new Exception("The <args> closure for action: ${actionDef.name} when executed must return a List.  It however returned: ${results.class.name}")
        }
    }

    @SpecField
    public void setExecutes(Closure executionAction) {
        executes(executionAction)
    }

    @SpecFunc
    public void executes(@SpecFuncParam('actionExecutionClosure') Closure executionAction) {
        actionDef.executionAction = executionAction
        verifyTokenMatches() //'executes'
        actionDef.actionExplicitlySet = true
    }

    @SpecFunc
    public void returns(@DelegatesTo(ActionReturnsDefDelegate) @SpecFuncParam('returnItemsClosure') Closure returnItems) {
        returnItems.setDelegate(returnsDelegate)
        returnItems.setResolveStrategy(Closure.DELEGATE_FIRST)
        def results = returnItems.call()
        if (results instanceof List) {
            returns((List) results)
        } else {
            handleInvalidSpec("the 'returns' closure for action: ${actionDef.name} when executed must return a List.  It however returned: ${results.class.name}")
            //throw new Exception("The 'returns' closure for action: ${actionDef.name} when executed must return a List.  It however returned: ${results.class.name}")
        }
    }

    @SpecFunc
    public void returns(@SpecFuncParam('items') List values) {
        values.each { value ->
            if (value instanceof ActionDef.ValDef) {
                actionDef.returnedValues.add((ActionDef.ValDef) value)
            } else if (value instanceof Tokens.Name) {
                def name = ((Tokens.Name) value).name
                confirmTokenByName(value)
                actionDef.returnedValues.add(new ActionDef.ValDef(name: name))
            } else if (value instanceof String) {
                actionDef.returnedValues.add(new ActionDef.ValDef(name: value))
            } else {
                throw new Exception("While parsing the <returns> spec for action: ${actionDef.name}," +
                        " encountered an unexpected value: $value")
            }
        }
        verifyTokenMatches() //'returns'
    }

    @SpecField(['List<Object>','Closure<List>'])
    public void setReturns(Object values) {
        if (values instanceof List) {
            returns((List) values)
        } else if (values instanceof Closure) {
            returns((Closure) values)
        } else {
            throw new IllegalArgumentException("Invalid <return> argument type: ${values.class.name}.  Expecting a List or a Closure which returns a List.")
        }
    }

}
