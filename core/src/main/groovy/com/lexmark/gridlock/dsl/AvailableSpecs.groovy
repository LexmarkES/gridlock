/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import java.lang.annotation.Annotation
import java.lang.reflect.Parameter

class AvailableSpecs {
    public static String simplifyTypePaths(String fullPath) {
        def ret = fullPath
        ['perceptive.gridlock.dsl.','java.util.', 'java.lang.', 'groovy.lang.'].each {
            if (fullPath.startsWith(it)) ret = fullPath.substring(it.length())
        }
        ret=ret.replace('Tokens$Name', 'String')
        return ret
    }

    public static List<String> listSpecFuncs(Object object) {

        List<String> specFuncs = []

        object.class.declaredMethods.each { method ->
            if (SpecFunc.class in method.declaredAnnotations*.annotationType()) {
                def argSpecs = []
                method.getParameters().each {Parameter p ->
                    SpecFuncParam specFuncParam = (SpecFuncParam)(p.declaredAnnotations.find { Annotation a -> a.annotationType() == SpecFuncParam })
                    if (!specFuncParam) {
                        // The authors of this code are to blame if we get this error -- not a user of gridlock.
                        throw new IllegalArgumentException("Internal logic error: No description annotation for parameter of method: ${method.name}")
                    }
                    argSpecs.add("${simplifyTypePaths(p.type.typeName)} ${specFuncParam.value()}")
                }
                specFuncs.add("${simplifyTypePaths(method.genericReturnType.typeName)} ${method.name}(${argSpecs.join(',')})")
            }

        }

        return specFuncs

    }

    public static List<String> listSpecFields(Object object) {
        List<String> specFields = []
                object.class.declaredFields.each { field ->
            if (SpecField.class in field.declaredAnnotations*.annotationType()) {
                specFields.add(simplifyTypePaths(field.genericType.typeName) + " " + field.name)
            }
        }
        object.class.declaredMethods.each { method ->
            SpecField specField = (SpecField)(method.declaredAnnotations.find { Annotation a -> a.annotationType() == SpecField })
            if (specField != null) {
                if (method.getParameters().size() != 1) {
                    throw new IllegalArgumentException("Internal logic error: Have other than one parameter of method: ${method.name}")
                }

                if (!method.name.startsWith('set')) {
                    throw new IllegalArgumentException("Internal logic error: Expected method: ${method.name} to start with 'set'")
                }
                String name = method.name.substring(3,4).toLowerCase() + method.name.substring(4)

                def setTypes = specField.value().collect {it}
                if (setTypes.isEmpty()) setTypes = [simplifyTypePaths(method.getParameters().first().type.typeName)]

                setTypes.each {type -> specFields.add("$name = $type")}
            }
        }
        return specFields
    }

    public static String listSpecFuncs(Object object, String preFixSep, String postFixSep) {
        def specList = listSpecFuncs(object)
        specList.inject("") { acc, item -> acc + preFixSep + item + postFixSep}
    }

    public static String listSpecFields(Object object, String preFixSep, String postFixSep) {
        def specList = listSpecFields(object)
        specList.inject("") { acc, item -> acc + preFixSep + item + postFixSep}
    }
}
