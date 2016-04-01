/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import com.lexmark.gridlock.execution.ActionSpec
import com.lexmark.gridlock.execution.GridLockTest
import com.lexmark.gridlock.execution.UseCaseSpec

class TestBuilder {

    public final String name
    public final List<UseCaseDef> cases
    private boolean isDefined = false;
    public Double useCaseStaggerInSecs = 3.3

    public TestBuilder(String name) {
        this.name = name
        this.cases = new ArrayList<UseCaseDef>(5)
    }

    public void validate() {
        // TODO:
        // Args count/type matches actionClosures
        // Args match specified returns
        //
    }

    public GridLockTest getTest() {
        def mgr = new GridLockTest((useCaseStaggerInSecs*1000).toLong())
        cases.each { UseCaseDef useCase ->
            mgr.addUseCase(mapUseCaseDefToSpec(useCase), useCase.flowTiming.periodFunction, useCase.flowTiming.executionTimeInMillis)
        }
        return mgr
    }

    public void define(@DelegatesTo(TestDefDelegate) Closure testDef) {
        if (isDefined) {
            throw new RuntimeException("Test: $name has already been defined!")
        }
        def tdd = new TestDefDelegate(this)
        testDef.setDelegate(tdd)
        testDef.setResolveStrategy(Closure.DELEGATE_FIRST)
        testDef.call()
        isDefined = true
    }

    static private ActionSpec addArgsToActionSpec(List<ActionDef.ArgDef> argDefs, ActionSpec spec) {
        argDefs.each {  argDef -> spec.addArg(argDef.resolve()) }
        return spec
    }

    static private UseCaseSpec<Object> mapUseCaseDefToSpec(UseCaseDef useCaseDef) {
        def caseSpec = new UseCaseSpec<Object>(useCaseDef.name, useCaseDef.numAllowedFailures, useCaseDef.paramSource, useCaseDef.paramSink)
        // Sort to run step order
        List<ActionSpec> ancestors = new ArrayList<>(10)
        useCaseDef.actionTree.sort().each { ActionDef actionDef ->
            if (actionDef.depth == 1) {
                if (ancestors.size() > 0) {
                    caseSpec.addActionSpec(ancestors.head())
                }
                ancestors.clear()
                ancestors.push(addArgsToActionSpec(actionDef.inputValues, new ActionSpec(actionDef.name, actionDef.depth, caseSpec, actionDef.executionAction, actionDef.repetitions)))
            } else {
                def newSpec = new ActionSpec(actionDef.name, actionDef.depth, caseSpec, actionDef.executionAction, actionDef.repetitions)
                addArgsToActionSpec(actionDef.inputValues, newSpec)
                actionDef.inputValues.each { ActionDef.ArgDef arg -> }
                def lastSpec = ancestors.last()
                if (lastSpec.depth == actionDef.depth) {
                    //Sibling so replace last sibling.
                    ancestors.pop()
                    ancestors.last().addChildSpec(newSpec)
                    ancestors.push(newSpec)
                } else if (lastSpec.depth > actionDef.depth) {
                    while (lastSpec.depth > actionDef.depth) {
                        ancestors.pop()
                        lastSpec = ancestors.last()
                    }
                    if (lastSpec.depth == actionDef.depth) {
                        //Sibling again, add to parent and replace.
                        ancestors.pop()
                        ancestors.last().addChildSpec(newSpec)
                        ancestors.push(newSpec)
                    } else {
                        throw new IllegalStateException("Skipped a generation unwinding ancestor stack for actionSpec: $newSpec.  " +
                                                        "Nearest ancestor depth: ${lastSpec.depth}, current spec depth: ${newSpec.depth}" )
                    }
                } else if (lastSpec.depth < actionDef.depth) {
                    // lastSpec was a ancestor
                    if (lastSpec.depth + 1 != actionDef.depth) {
                        throw new IllegalStateException("Skipped a generation building ancestor stack for actionSpec: $newSpec.  " +
                                "Nearest ancestor depth: ${lastSpec.depth}, current spec depth: ${newSpec.depth}" )
                    }
                    ancestors.last().addChildSpec(newSpec)
                    ancestors.push(newSpec)
                }
            }
        }

        if (ancestors.size() > 0) caseSpec.addActionSpec(ancestors.head())
        return caseSpec
    }

}
