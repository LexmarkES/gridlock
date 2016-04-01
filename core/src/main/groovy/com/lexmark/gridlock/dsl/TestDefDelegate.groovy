/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

class TestDefDelegate {
    private final TestBuilder builder
    public Tokens tokens = new Tokens()


    public TestDefDelegate(TestBuilder builder) {
        this.builder = builder
    }

    def propertyMissing(String name) {
        return tokens.proposeCandidate(name)
    }

    public void setUseCaseStaggerInSecs(Object num) {
        useCaseStaggerInSecs(num)
    }

    @SpecFunc
    public void useCaseStaggerInSecs(Object num) {
        if (num instanceof Number ) {
            builder.useCaseStaggerInSecs = (Double)num
        } else {
            throw new IllegalArgumentException("In Test: ${builder.name}, must supply a number for <useCaseStaggerInSecs> spec." +
                    "  Instead found value: '${num.toString()}' of type: ${num.class.name}")
        }
    }

    @SpecFunc
    public void useCase(String name, Closure useCaseDefClosure) {
        def useCaseDef = new UseCaseDef(name, "UseCase: $name")
        def useCaseBuilder = new UseCaseBuilder(useCaseDef)
        useCaseBuilder.useCase(name, useCaseDefClosure)
        builder.cases.add(useCaseDef)
    }

    public void useCase(Tokens.Name name, Closure useCaseDefClosure) {
        useCase(name.name, useCaseDefClosure)
    }

}
