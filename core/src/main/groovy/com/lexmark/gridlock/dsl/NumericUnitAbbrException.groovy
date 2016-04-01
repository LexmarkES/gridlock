/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.dsl

public class NumericUnitAbbrException extends InvalidSpecification {
    public NumericUnitAbbrException() {
        super()
    }

    public NumericUnitAbbrException(String unknownAbbr) {
        super("Unknown numeric unit abbrevation: $unknownAbbr.  Valid abbreviations are: " +
                "${NumericUnitDslSupport.symbolToUnit.keySet().join(', ')}.")
    }
}
