/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl.time

import java.util.concurrent.TimeUnit

class Duration extends Period {
    Duration(Number input, TimeUnit unit ) {
        super(input, unit, unit.toMillis(input.toLong()))
    }

    @Override
    public String toString() {
        return "Duration: ${value} ${unit}, Period: $periodInMs milliseconds"
    }
}
