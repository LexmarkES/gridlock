/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

public interface ManagerAccess {
    public void failed(UseCaseInstance useCaseInstance)
    public void ready(UseCaseInstance useCaseInstance)
    public void done(UseCaseInstance useCaseInstance)
}