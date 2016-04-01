/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

public class FlowDefDelegate{

    @Delegate
    InvalidDefDelegate idd
    private final FlowBuilder builder

    String useCaseName

    def propertyMissing(String name) {
        return handleMissingProperty(name)
    }

    def methodMissing(String name, args) {
        return handleMissingMethod(name, args)
    }

    public FlowDefDelegate(FlowBuilder builder) {
        idd = new InvalidDefDelegate(this, "the 'actionFlow' spec for UseCase: ${builder.useCaseName}", null)
        this.builder = builder
        this.useCaseName = builder.useCaseName
    }

    // Note: this function is needed to redirect error handling to the common reporting mechanism.  Without it, the user
    // can specify action() in the DSL and will then be presented with a standard Groovy unable to find correct function message
    // that is not as clear as the custom messages from MissingDefDelegate.
    def action() {
        return handleMissingMethod('action', [])
    }

    @SpecFunc
    def action(@SpecFuncParam('name')Tokens.Name name, @SpecFuncParam('repetitions')Number repetitions, @SpecFuncParam('actionDef') Closure subActionsDef) {
        confirmTokenByName(name)
        return actionFlow(name.name, repetitions, subActionsDef)
    }

    private ActionDef actionFlow(String name, Number repetitions, Closure subActionsDef) {
        if (name == null) {
            throw new Exception("Failed to specify a name for the action!")
        }

        def action = new ActionDef(useCaseName:useCaseName, name:name, repetitions: repetitions, depth: builder.currentActionDepth, runStep: builder.nextRunStep++)
        builder.flowTree.put(action.name,action)
        if (subActionsDef != null) {
            builder.currentActionDepth++
            def flowDelegate = new FlowDefDelegate(builder)
            subActionsDef.setDelegate(flowDelegate)
            subActionsDef.setResolveStrategy(Closure.DELEGATE_FIRST)
            subActionsDef.call()
            flowDelegate.verifyTokenMatches()
            builder.currentActionDepth--
        }
        return action
    }

    @SpecFunc
    def action(@SpecFuncParam('name') Tokens.Name name) {
        confirmTokenByName(name)
        return actionFlow(name.name, 1, null)
    }

    @SpecFunc
    def action(@SpecFuncParam('name') Tokens.Name name, @SpecFuncParam('actionDef') Closure subActionsDef) {
        confirmTokenByName(name)
        return actionFlow(name.name, 1, subActionsDef)
    }

    @SpecFunc
    def action(@SpecFuncParam('name') Tokens.Name name, @SpecFuncParam('repetitions') Number repetitions) {
        confirmTokenByName(name)
        return actionFlow(name.name, repetitions, null)
    }

    @SpecFunc
    def action(@SpecFuncParam('namedArgs') Map<String,Object> args) {
        return action(args, null)
    }

    @SpecFunc
    def action(@SpecFuncParam('namedArgs') Map<String,Object> args, @SpecFuncParam('actionDef') Closure subActionsDef) {

        def name = args['name']?:args['action']
        if (name instanceof Tokens.Name) {
            confirmTokenByName(name)
        }

        if (name == null) {
            throw new Exception("Failed to specify a name for an action in the <actionFlow> spec of UseCase:$useCaseName!")
        }
        Double repetitions = (Double)(args['reps']?:args['repetitions']?:1.0)

        return actionFlow(name.toString(), repetitions, subActionsDef)
    }

}

