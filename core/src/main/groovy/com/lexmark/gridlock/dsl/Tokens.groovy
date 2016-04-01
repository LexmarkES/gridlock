/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import java.lang.reflect.Method

public class Tokens {
    public static class Candidate implements Comparable<Candidate> {
        String name
        private Integer proposedInstances = 1
        private Integer confirmedInstances = 0

        @Override
        public int compareTo(Candidate o) {
            return name.compareTo(o.name)
        }

        public boolean isConfirmed() {
            confirmedInstances == proposedInstances
        }

        public void confirmInstance() {
            if (!isConfirmed()) {
                confirmedInstances++
            } else {
                throw new IllegalStateException("Attempting to reconfirm token candidate: $name")
            }
        }

        public void proposeInstance() {
            proposedInstances++
        }
    }

    public static class Name {
        public final boolean internal

        public final String name

        public Name(String name, Boolean internal) {
            this.name = name
            this.internal = internal
        }
        public Name(String name) {
            this(name, false)
        }

        @Override
        public String toString() {
            return name
        }

        public static void validate() {
            // TODO: Put validation logic here for valid variable names. I.e., no spaces, prohibited characters, etc.
        }


    }

    public static class Matcher {

        private final Object object

        static class FunctionProxy {

            Object object
            Method method
            Object[] args
            def invoke() {
                method.invoke(object,args)
            }
        }

        Matcher(Object object) {
            this.object = object
        }

        FunctionProxy proxyToMapArgFunction(String func, String name, Map<String, Object> args) {
            FunctionProxy proxy = null
             try {
                 Method method = object.class.getDeclaredMethod(func, Map.class)
                 if (method != null) {
                     def newArgs = new HashMap<String, Object>(args)
                     newArgs.put('name', name)
                     proxy = [method:method, args:newArgs, object:object] as FunctionProxy
                 }
             } catch (NoSuchMethodException e) {
                //Ignore
             }
             return proxy
        }

        FunctionProxy proxyToArgListFunction(String func, String name, Object[] args, Boolean tokenFlexible = false) {
            Object[] amendedArgs = new Object[args.length+1]
            amendedArgs[0] = name
            args.eachWithIndex{ def entry, int i -> amendedArgs[i+1] = entry }
            return proxyToMatchingFunction(func, amendedArgs, tokenFlexible)
        }

        FunctionProxy proxyToMatchingFunction(String name, Object[] args, Boolean tokenFlexible = false) {
            FunctionProxy proxy = null
            object.class.getDeclaredMethods().each { Method method ->
                if (method != null && method.name == name && method.parameterTypes.length == args.length) {
                    Boolean match = true
                    Object[] newArgs = new Object[args.length]
                    args.eachWithIndex { Object entry, int i ->
                        if (entry.class == null) {
                            // For some not yet understood reason, for a hashmap input, class is null.
                            // So, for that problematic case and potentially others, will just flag
                            // as a not match and go on.
                            match = false
                            println "Warning: Could not complete function lookup for parameter: ${entry}"
                        }
                        else if (method.parameterTypes[i].isAssignableFrom(entry.class)) {
                            newArgs[i] = entry
                        } else if (tokenFlexible && method.parameterTypes[i] == Name.class && entry instanceof String) {
                            newArgs[i] = new Name(entry, true)
                        } else {
                            match = false
                        }
                    }
                    if (match) {
                        proxy = [method:method, args:newArgs, object:object] as FunctionProxy
                    }
                }
            }
            return proxy
        }
    }

    final private Map<String, Candidate> candidates = new HashMap<String, Candidate>()

    public Name proposeCandidate(String name) {
        def var = candidates.get(name)
        if (var == null) {
            candidates.put(name, [name:name] as Candidate)
        } else {
            var.proposeInstance()
        }
        return new Name(name)
    }

    private void confirmCandidateByName(String name) {
        def var = candidates.get(name)
        if (var==null) {
            throw new Exception("Attempting to confirm non-existent candidate: $name")
        }
        var.confirmInstance()
    }

    public void confirmCandidate(Name name) {
        // Don't attempt to confirm tokens that were internally generated.
        if (!name.internal) confirmCandidateByName(name.toString())
    }

    public void clearCandidates() {
        candidates.clear()
    }

    public List<Candidate> getUnconfirmedCandidates() {
        ((List<Candidate>)candidates.collect {key, value -> value}).sort().findAll {!it.isConfirmed()}
    }



}
