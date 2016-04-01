/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class Gridlock<T> {
    public List<UseCaseInstance.ResultNode> upNodes
    public List<UseCaseInstance.ResultNode> sibNodes
    public List<UseCaseInstance.ResultNode> prevNodes
    public Map<String, Object> contextMap
    public T param

    public Gridlock(T param) {
        this.param = param
    }

    public Gridlock getGridlock() {
        return this
    }

    public Object result(String action, String key) {
        //Find matching (i.e. key) value from specified action 'name'
        return [upNodes, sibNodes].flatten().find {UseCaseInstance.ResultNode node -> node?.name?.equals(action)}?.result?.get(key)
    }

    public Object result(String key) {
        //Find matching (i.e. key) value from nearest matching action.
        return [upNodes, sibNodes].flatten().find {UseCaseInstance.ResultNode node -> node?.result?.containsKey(key)}?.result?.get(key)
    }

    public Map<String, Object> getContext() {
        return contextMap
    }
}
