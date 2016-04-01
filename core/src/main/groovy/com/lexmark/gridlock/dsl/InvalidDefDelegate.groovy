/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

class InvalidDefDelegate {

    private Tokens tokens
    List<Object> targets
    String parsingContext
    String defaultFunction
    List<Tokens.Matcher> funcMatchers

    public InvalidDefDelegate(Object target, String parsingContext, String defaultFunction) {
        this([target], parsingContext, defaultFunction)
    }

    public InvalidDefDelegate(List<Object> targets, String parsingContext, String defaultFunction ) {
        this.targets = targets
        this.funcMatchers = targets.collect {Object clazz -> new Tokens.Matcher(clazz)}
        this.tokens = new Tokens()
        this.parsingContext = parsingContext
        this.defaultFunction = defaultFunction
    }

    public void verifyTokenMatches() {
        def unconfirmed = tokens.unconfirmedCandidates
        if (!unconfirmed.isEmpty()) {
            throw new NonExistentDelegateField("While parsing $parsingContext," +
                    " encountered the following unexpected tokens${unconfirmed.size()>1?'s':''}" +
                    ": ${unconfirmed.collect {it.name}.join(", ")}\nPerhaps " +
                    "${unconfirmed.size()>1?'these were intended to be DSL delegate fields.':'it was intended to be a DSL delegate field.'}" +
                    "  Valid DSL delegate field assignments are:\n" +
                     targets.collect { Object target -> AvailableSpecs.listSpecFields(target, '\t\t', '\n')}.join(''))
        }
        tokens.clearCandidates()
    }

    public void confirmTokenByName(Tokens.Name name) {
        tokens.confirmCandidate(name)
    }

    def handleMissingProperty(String name) {
        return tokens.proposeCandidate(name)
    }

    def handleMissingMethod(String name, args) {

        Tokens.Matcher.FunctionProxy proxy = funcMatchers.collect {Tokens.Matcher matcher -> matcher.proxyToMatchingFunction(name, args, true)}.find()
        if (proxy != null) {
            return proxy.invoke()
        } else if (defaultFunction != null) {
            //This approach will assume that any undeclared function is shorthand for a default function with name: defaultFunction.
            if ((proxy = funcMatchers.collect {Tokens.Matcher matcher -> matcher.proxyToArgListFunction(defaultFunction, args, true)}.find()) != null) {
                return proxy.invoke()
            }
        }
        throw new NonExistentDelegateFunction("While parsing $parsingContext, encountered the presumed DSL delegate function: $name(${args.join(',')})." +
                "\nThis however does not match any actual DSL delegate functions.  Valid DSL delegate functions are:\n" +
                targets.collect { Object target -> AvailableSpecs.listSpecFuncs(target, '\t\t', '\n')}.join(''))
    }

    void handleInvalidSpec(String coreMessage) {
        throw new InvalidSpecification("While parsing $parsingContext, $coreMessage")
    }
}
