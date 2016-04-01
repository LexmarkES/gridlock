/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

class ActionReturnsDefDelegate {

    public final ActionDefDelegate actionDefDelegate
    public final String actionName

    public ActionReturnsDefDelegate(ActionDefDelegate actionDefDelegate) {
        this.actionDefDelegate = actionDefDelegate
        this.actionName = actionDefDelegate.actionDef.name
    }

    private ActionDef.ValDef valueInternal(String argName, Boolean notNull, Class valType) {
        return new ActionDef.ValDef(name: argName, notNull: notNull, type:valType)
    }

    @SpecFunc
    public ActionDef.ValDef value(@SpecFuncParam('valueParams') Map<String, Object> args) {
        String name
        Boolean notNull = false
        Class valType = Object.class
        args.each { key, value ->
            if (value instanceof Tokens.Name) {
                actionDefDelegate.confirmTokenByName(value)
            }
            switch (key) {
                case 'item':
                case 'name': name = value.toString(); break
                case 'notNull':
                    if (value instanceof Boolean) notNull = value
                    else {
                        throw new IllegalArgumentException("Expected Boolean value for <notnull> parameter of returns spec for action: $actionName.  " +
                                                            "Value received was: $value of type: ${value.class.name}")
                    }
                    break
                case 'type':
                    if (value instanceof Class) {
                        valType = value
                    }
                    else if (value instanceof String || value instanceof Tokens.Name) {
                        // TODO: Wrap for unknown type exception.
                        //ClassNotFoundException
                        valType = Class.forName(value.toString())
                    }
                    else {
                        throw new IllegalArgumentException("Expected String or Class value for <type> parameter of <returns> list spec for action: $actionName.  " +
                                                            "Value received wa: $value of type: ${value.class.name}")
                    }
                    break
                default:
                    throw new Exception("Unknown parameter type: $key found in an 'value' spec in the <returns> list spec of action: $actionName")
            }
        }
        if (name == null) {
            throw new Exception("No 'name' or 'item' parameter given in the <value($args)> spec in the <returns> list spec of action: $actionName")
        }
        valueInternal(name, notNull, valType)
    }

    @SpecFunc
    public ActionDef.ValDef value(@SpecFuncParam('argName') Tokens.Name argName) {
        actionDefDelegate.confirmTokenByName(argName)
        return valueInternal(argName.name, false, Object.class)
    }

    @SpecFunc
    public ActionDef.ValDef value(@SpecFuncParam('argName') Tokens.Name argName, @SpecFuncParam('notNull') Boolean notNull) {
        actionDefDelegate.confirmTokenByName(argName)
        return valueInternal(argName.name, notNull, Object.class)
    }

    @SpecFunc
    public ActionDef.ValDef value(@SpecFuncParam('argName')Tokens.Name argName, @SpecFuncParam('valType') Class valType) {
        actionDefDelegate.confirmTokenByName(argName)
        return valueInternal(argName.name, false, valType)
    }

    @SpecFunc
    public ActionDef.ValDef value(@SpecFuncParam('argName')Tokens.Name argName, @SpecFuncParam('notNull') Boolean notNull, @SpecFuncParam('valType')Class valType) {
        actionDefDelegate.confirmTokenByName(argName)
        return valueInternal(argName.name, notNull, valType)
    }


}
