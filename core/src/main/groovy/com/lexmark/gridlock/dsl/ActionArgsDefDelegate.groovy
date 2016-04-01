/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

class ActionArgsDefDelegate {

    public final ActionDefDelegate actionDefDelegate
    public final String actionName
    //@Delegate
    //public final MissingDefDelegate mdd

    public ActionArgsDefDelegate(ActionDefDelegate actionDefDelegate) {
        //this.mdd = new MissingDefDelegate(this,"TBD", null)
        this.actionDefDelegate = actionDefDelegate
        this.actionName = actionDefDelegate.actionDef.name
    }

    private ActionDef.ArgDef resultInternal(String fromAction, String argName) {
        return new ActionDef.Result(name: argName, fromAction: fromAction)
    }

    @SpecFunc
    public ActionDef.ArgDef result(@SpecFuncParam('resultParams') Map<String, Object> args) {
        String name
        String from = null
        args.each { key, value ->
            if (value instanceof Tokens.Name) {
                actionDefDelegate.confirmTokenByName(value)
            }
            switch (key) {
                case 'name':
                case 'item': name = value.toString(); break
                case 'from': from = value.toString(); break
                default:
                    throw new IllegalArgumentException("Unknown parameter type: $key found in an 'res' spec in the args list of action: $actionName")
            }
        }

        if (name == null) {
            throw new Exception("No 'name' or 'item' parameter given in an 'result' spec in the args list of action: $actionName")
        }

        return resultInternal(from, name)
    }

    @SpecFunc
    public ActionDef.ArgDef result(@SpecFuncParam('fromAction') Tokens.Name fromAction, @SpecFuncParam('argName') Tokens.Name argName) {
        actionDefDelegate.confirmTokenByName(argName)
        actionDefDelegate.confirmTokenByName(fromAction)
        return resultInternal(fromAction.name, argName.name)
    }

    @SpecFunc
    public ActionDef.ArgDef result(@SpecFuncParam('argName')Tokens.Name argName) {
        actionDefDelegate.confirmTokenByName(argName)
        return resultInternal(null, argName.name)
    }

    private ActionDef.ArgDef suppliedInternal(Object value) {
        return new ActionDef.Supplied(value:value)
    }

    @SpecFunc
    public ActionDef.ArgDef supplied(@SpecFuncParam('suppliedParams') Map<String, Object> args) {
        Object supplied = null
        Boolean found = false
        args.each { key, value ->
            if (value instanceof Tokens.Name) {
                actionDefDelegate.confirmTokenByName(value)
            }
            switch (key) {
                case 'value': supplied = value; found=true; break
                default:
                    throw new Exception("Unknown paramater type: $key found in an 'supplied' spec in the args list of action: $actionName")
            }
        }

        if (!found) {
            throw new Exception("No 'value' parameter given in an 'supplied' spec in the args list of action: $actionName")
        }
        return suppliedInternal(supplied)

    }

    @SpecFunc
    public ActionDef.ArgDef supplied(@SpecFuncParam('value')Object value) {
        return suppliedInternal(value)
    }

}
