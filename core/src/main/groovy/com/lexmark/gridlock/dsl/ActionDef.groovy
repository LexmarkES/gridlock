/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

public class ActionDef implements Comparable<ActionDef> {

    static class ValDef {
        String name
        Class type = Object.class
        Boolean notNull
        @Override
        public String toString() {
            "ValDef[name:$name, type:$type, nullable:${notNull?'no':'yes'}]"
        }
    }

    static interface ArgDef {
        public Object resolve()
        public boolean valid()
    }

    static class Result implements ArgDef {
        String name
        String fromAction

        @Override
        Object resolve() {
            if (fromAction == null) {
                return {gridlock.result(name)}
            } else {
                return {gridlock.result(fromAction, name)}
            }
        }

        @Override
        boolean valid() {
            return false
        }

        @Override
        public String toString() {
            "Result[name:$name, from:$fromAction}]"
        }
    }

    static class Supplied implements ArgDef {

        Object value

        @Override
        Object resolve() {
            return value
        }

        @Override
        boolean valid() {
            return true
        }

        @Override
        public String toString() {
            "Supplied[value:${value.toString()}]"
        }
    }

    String useCaseName
    String name
    Double repetitions
    Integer runStep
    Integer depth
    Boolean fullyDefined = false
    Boolean actionExplicitlySet = false

    List<ArgDef> inputValues = new ArrayList<ArgDef>(10)
    List<ValDef> returnedValues = new ArrayList<ValDef>(10)

    Closure executionAction = {->}

    @Override
    int compareTo(ActionDef o) {
        return this.runStep - o.runStep
    }

    @Override
    public String toString() {
        return "[name: $name, repetitions: $repetitions, depth: $depth, runStep: $runStep] " +
            "\n\tArgs:${inputValues.collect {it.toString()}}\n\tReturns:${returnedValues.collect { it.toString()}}"
    }

}
