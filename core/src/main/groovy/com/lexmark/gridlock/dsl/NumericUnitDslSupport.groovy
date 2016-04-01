/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

import com.lexmark.gridlock.dsl.time.Duration
import com.lexmark.gridlock.dsl.time.Frequency

import java.util.concurrent.TimeUnit

// NOTES:  The following class is focused on time quantity support as that is the only type of units
// currently of interest. However, it is conceivable that other numeric based quantities could be desired.
// Say distances.  It would make sense then to make this more general and add in more token names.
// Say foot, yard, mile.  Those other units would need to be added here with their corresponding logic
// and the symbolToUnit map would need to be likewise generalized.

class NumericUnitDslSupport {

    public static Map<String, TimeUnit> symbolToUnit =
            ['hour':TimeUnit.HOURS, 'min':TimeUnit.MINUTES, 'sec':TimeUnit.SECONDS, 'msec':TimeUnit.MILLISECONDS]

    public static addTo(Class clazz) {
        createEMC(clazz, { ExpandoMetaClass emc ->
            symbolToUnit.each { symbol, unit -> emc[symbol] = unit}

            Number.metaClass.getProperty = { String symbol ->
                Double numberValue = delegate.asType(Number.class).doubleValue()
                if (!symbolToUnit.containsKey(symbol)) {
                    throw new NumericUnitAbbrException(symbol)
                }
                return new Duration(numberValue, symbolToUnit[symbol])
            }

            Number.metaClass.call = { TimeUnit timeUnit ->
                Double numberValue = delegate.asType(Number.class).doubleValue()
                return new Duration(numberValue, timeUnit)
            }

            Number.metaClass.call = {  Tokens.Name token ->
                def symbol = token.name
                Double numberValue = delegate.asType(Number.class).doubleValue()
                if (!symbolToUnit.containsKey(symbol)) {
                    throw new NumericUnitAbbrException(symbol)
                }
                return new Duration(numberValue, symbolToUnit[symbol])
            }

            Number.metaClass.div = { TimeUnit timeUnit ->
                Double numberValue = delegate.asType(Number.class).doubleValue()
                return new Frequency(numberValue, timeUnit)
            }

            Number.metaClass.div = { Tokens.Name token ->
                throw new NumericUnitAbbrException(token.name)
            }
        })
    }

    private static ExpandoMetaClass createEMC(Class clazz, Closure cl) {
        ExpandoMetaClass emc = new ExpandoMetaClass(clazz, false)
        cl(emc)
        emc.initialize()
        return emc
    }
}


