/*
 * Copyright 2016 Lexmark International Technology S.A.  All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.lexmark.gridlock.execution

class CustomMetricManager {

    private static final String CUSTOM_METRIC_CATEGORY = 'CUSTOM_METRIC'

    private static volatile CustomMetricManager manager;

    private final Map<String, PartialPoint> pendingMetricsMap = new HashMap<>();

    public static CustomMetricManager ensure() {
        if (manager) {
            return manager;
        } else {
            synchronized (CustomMetricManager.class) {
                if (manager) {
                    return manager;
                } else {
                    manager = new CustomMetricManager();
                    return manager;
                }
            }
        }
    }

    private CustomMetricManager() {
        //no public constructor
    }

    public String startMetric(Date startTime, String usecase, String action) {
        PartialPoint newSample = new PartialPoint(startTime, usecase, action)
        String newId = newSample.getId()
        pendingMetricsMap.put(newId, newSample)
        return newId
    }

    public stopMetric(String id, Date stopTime) {
        PartialPoint partialPoint = pendingMetricsMap.get(id)
        if (partialPoint == null) {
            throw new RuntimeException("attempt to stop custom metric failed, id: ${id} not found in partial points");
        }

        MetricsInterval.acceptSample(new MetricsSamplePoint(partialPoint.startTime, stopTime, new MetricsDimensions(partialPoint.usecase, partialPoint.action, CUSTOM_METRIC_CATEGORY)))
        pendingMetricsMap.remove(partialPoint)
    }

    public List<PartialPoint> getUnresolvedMetrics() {
        synchronized (pendingMetricsMap) {
            return new ArrayList<>(pendingMetricsMap.values());
        }
    }

    private static class PartialPoint {
        final String id = UUID.randomUUID().toString()
        final String usecase
        final String action
        final Date startTime

        PartialPoint(Date startTime, String usecase, String action) {
            this.usecase = usecase
            this.action = action
            this.startTime = startTime
        }

        @Override
        boolean equals(Object obj) {
            return id.equals(obj)
        }

        @Override
        int hashCode() {
            return id.hashCode()
        }

        @Override
        String toString() {
            return "id: ${id}, usecase: ${usecase}, action: ${action}, startTime: ${startTime.getTime()}"
        }
    }
}
