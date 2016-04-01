/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.text.DateFormat
import java.text.SimpleDateFormat

class Safe8601DateFormat {
    private static final String ISO_8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    private final DateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_PATTERN)

    public Safe8601DateFormat(TimeZone timeZone = TimeZone.getTimeZone("UTC")) {
        dateFormat.setTimeZone(timeZone)
    }

    public String now() {
        synchronized (dateFormat) {
            return dateFormat.format(new Date())
        }
    }

    public String format(Date date) {
        synchronized (dateFormat) {
            return dateFormat.format(date)
        }
    }
}
