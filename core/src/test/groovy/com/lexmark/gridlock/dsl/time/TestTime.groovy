/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl.time

import org.junit.Test

import java.util.concurrent.TimeUnit

class TestTime {
    @Test
    public void TestDuration() {
        def d = new Duration(5, TimeUnit.MINUTES)
        println "5 Minutes = $d"
    }

    @Test
    public void TestFrequency() {
        def f= new Frequency(5, TimeUnit.MINUTES)
        println "5 Minutes = $f"
    }


}
