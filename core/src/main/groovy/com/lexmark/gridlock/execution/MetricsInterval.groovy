/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

import java.util.concurrent.atomic.AtomicReference

class MetricsInterval {

    private static final Object lock = new Object()
    private static AtomicReference<Integer> maxSampleSize = new AtomicReference<Integer>(100)
    private static AtomicReference<List<MetricsSamplePoint>> accumulatingSamples =
        new AtomicReference<List<MetricsSamplePoint>>(new ArrayList<MetricsSamplePoint>())

    private final samplingInterval_ms
    private List<MetricsSamplePoint> samples
    private Map<MetricsDimensions, MetricsResult> processedSamples
    private Date startTime
    private Date stopTime
    private List<MetricsRecord> records

    public MetricsInterval(Long samplingInterval_ms) {
        this.samplingInterval_ms = samplingInterval_ms
        synchronized (lock) {
            int maxSize = maxSampleSize.get()
            samples = accumulatingSamples.getAndSet(new ArrayList<MetricsSamplePoint>(maxSize))
            if (samples.size() > maxSize) {
                maxSampleSize.set(samples.size())
            }
        }
        processedSamples = new LinkedHashMap<MetricsDimensions, MetricsResult>()

    }

    public static void acceptSample(MetricsSamplePoint sample) {
        synchronized (lock) {
            accumulatingSamples.get().add(sample)
        }
    }

    public void processSamples() {

        samples.each { MetricsSamplePoint sample ->
            MetricsResult processedResults = processedSamples.get(sample.dimensions)
            MetricsResult currentResult = new MetricsResult(1, (sample.stopTime.time - sample.startTime.time)/1000.0)

            if (processedResults == null) {
                processedResults = currentResult
            }else {
                processedResults.accumulate(currentResult)
            }

            processedSamples.put(sample.dimensions, processedResults)

            if (startTime == null || startTime > sample.startTime) startTime = sample.startTime
            if (stopTime == null || stopTime < sample.stopTime) stopTime = sample.stopTime
        }

        records = new ArrayList<MetricsRecord>(processedSamples.size())

        processedSamples.keySet().each { MetricsDimensions dims ->
            records.add(new MetricsRecord(dims, processedSamples[dims], startTime, stopTime))
        }

        records.sort()
    }

    public report(PrintStream outputStream) {
        records.each {
            //have to add the sampling interval for "DURATION" column
            def line = it.asLineArray()
            line.add(1, samplingInterval_ms)
            outputStream.println(line.join('\t'))
        }
    }

}
