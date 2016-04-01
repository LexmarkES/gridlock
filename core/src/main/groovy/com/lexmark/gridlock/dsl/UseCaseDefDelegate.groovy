/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

public class UseCaseDefDelegate {

    private final String useCaseName
    private final UseCaseBuilder builder
    private final UseCaseDef useCaseDef
    public final @Delegate
    InvalidDefDelegate idd
    Tokens tokens = new Tokens()

    def propertyMissing(String name) {
        return handleMissingProperty(name)
    }

    def propertyMissing(String name, value) {
        return handleMissingProperty(name)
    }

    def methodMissing(String name, args) {
        return handleMissingMethod(name, args)
    }

    public UseCaseDefDelegate(UseCaseBuilder builder) {
        this.builder = builder
        this.useCaseName = builder.useCaseDef.name
        this.useCaseDef = builder.useCaseDef
        this.idd = new InvalidDefDelegate([this], "the spec for UseCase: $useCaseName", null)
    }

    @SpecFunc
    public void actionDef(@SpecFuncParam('name')String name, @SpecFuncParam('actionDef') Closure actionDefClosure) {
        builder.actionBuilder.buildSpec(name, actionDefClosure)
    }

    @SpecFunc
    public void actionDef(@SpecFuncParam('name')Tokens.Name action, @SpecFuncParam('actionDef')Closure actionDefClosure) {
        tokens.confirmCandidate(action)
        actionDef(action.name, actionDefClosure)
    }

    // TODO: Rename actionFlowTree or maybe actionTree or flowTree?
    @SpecField(['Closure'])
    public void setActionFlow(Closure flowDef) {
        builder.flowBuilder.flowSequence(flowDef)
    }

    @SpecFunc
    public void actionFlow(@SpecFuncParam('flowTree')Closure flowDef) {
        builder.flowBuilder.flowSequence (flowDef)
    }

    @SpecField(['Closure'])
    public void setParamSource(Object source) {
        paramSource(source)
    }

    @SpecFunc
    public void paramSource(@SpecFuncParam('source')Object source) {
        if (source instanceof Closure) {
            useCaseDef.paramSource = source
        } else {
            throw new IllegalArgumentException("In UseCase: '$useCaseName', must supply a Closure for <paramSource> spec." +
                    "  Instead found value: '${source.toString()}' of type: ${source.class.name}")
        }
    }

    @SpecField(['Closure'])
    public void setParamSink(Object sink) {
        paramSink(sink)
    }

    @SpecFunc
    public void paramSink(@SpecFuncParam('sink')Object sink) {
        if (sink instanceof Closure) {
            useCaseDef.paramSink = sink
        } else {
            throw new IllegalArgumentException("In UseCase: '$useCaseName', must supply a Closure for <paramSink> spec." +
                    "  Instead found value: '${sink.toString()}' of type: ${sink.class.name}")
        }
    }

    @SpecField(['Closure'])
    public void setOnActionFailure(Object fail) {
        onActionFailure(fail)
    }

    @SpecFunc
    public void onActionFailure(@SpecFuncParam('onFailure')Object fail) {
        if (fail instanceof Closure) {
            useCaseDef.onActionFailure = fail
        } else {
            throw new IllegalArgumentException("In UseCase: '$useCaseName', must supply a Closure for <onActionFailure> spec." +
                    "  Instead found value: '${fail.toString()}' of type: ${fail.class.name}")
        }
    }

    @SpecField('String')
    public void setTraceDump(Object path) {
        traceDump(path)
    }

    @SpecFunc
    public void traceDump(@SpecFuncParam('traceDump')Object path) {
        if (path instanceof String) {
            useCaseDef.traceDump = (String) path
        } else {
            throw new IllegalArgumentException("In UseCase: '$useCaseName', must supply a String for 'traceDump'")
        }
    }

    @SpecField('Integer')
    public void setNumAllowedFailures(Object num) {
        numAllowedFailures(num)
    }

    @SpecFunc
    public void numAllowedFailures(@SpecFuncParam('number')Object num) {
        if (num instanceof Integer) {
            useCaseDef.numAllowedFailures = (Integer)num
        } else {
            throw new IllegalArgumentException("In UseCase: '$useCaseName', must supply an Integer for 'numAllowedFailures' spec." +
                    "  Instead found value: '${num.toString()}' of type: ${num.class.name}")
        }
    }

    @SpecField('Closure')
    void setTiming(Closure timingDef) {
        timing(timingDef)
    }

    @SpecFunc
    public void timing(@SpecFuncParam('timingDef')Closure timingDef) {
        timingDef.setDelegate(new FlowTimingDefDelegate(useCaseDef.flowTiming, useCaseName))
        timingDef.setResolveStrategy(Closure.DELEGATE_FIRST)
        timingDef.call()
    }

    public void validate() {
        verifyTokenMatches()
    }
}

