/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class MetricsTracker {

    private final header = ['TIME', 'DURATION', 'CATEGORY', 'USECASE', 'ACTION', 'NUM', 'AVG']

    private final int samplingIntervalSeconds
    private final PrintStream outputStream
    private Timer timer;

    public MetricsTracker(Integer samplingIntervalSeconds, PrintStream outputStream) {
        this.outputStream = outputStream
        this.samplingIntervalSeconds = samplingIntervalSeconds
        this.timer = new Timer("Metrics Tracker", true)
    }

    public void start() {
        //write header
        outputStream.println(header.join('\t'))

        TimerTask samplingTask = new TimerTask() {
            @Override
            void run() {
                doSampling()
            }
        }
        timer.scheduleAtFixedRate(samplingTask, samplingIntervalSeconds * 1000, samplingIntervalSeconds * 1000)
    }

    public void doSampling() {
        MetricsInterval interval = new MetricsInterval(samplingIntervalSeconds * 1000)
        interval.processSamples()
        interval.report(outputStream)
        outputStream.flush()
    }

    public void stop() {
        timer.cancel()
    }
}
