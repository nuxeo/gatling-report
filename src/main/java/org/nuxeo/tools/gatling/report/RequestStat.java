/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Benoit Delbosc
 */
package org.nuxeo.tools.gatling.report;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class RequestStat {
    public static final long MAX_BOXPOINT = 50000;

    protected static final AtomicInteger statCounter = new AtomicInteger();

    protected String simulation;

    protected String scenario;

    protected final String request;

    protected final String requestId;

    protected final int indice;

    protected String startDate;

    protected long start, end;

    protected long count, successCount, errorCount;

    protected long min, max, stddev, p50, p90, p95, p99;

    protected double rps, avg;

    protected double duration;

    protected final List<Double> durations;

    protected Graphite graphite;

    protected final Apdex apdex;

    protected int maxUsers;

    public long getCount() {
        return count;
    }

    public RequestStat(String simulation, String scenario, String request, long start, Float apdexT) {
        this.simulation = simulation;
        this.scenario = scenario;
        this.request = request;
        requestId = Utils.getIdentifier(request);
        this.start = start;
        durations = new ArrayList<>();
        indice = statCounter.incrementAndGet();
        apdex = new Apdex(apdexT);
    }

    public static String header() {
        return "simulation\tscenario\tmaxUsers\trequest\tstart\tstartDate\tduration\tend\tcount\tsuccessCount\t"
                + "errorCount\tmin\tp50\tp90\tp95\tp99\tmax\tavg\tstddev\trps\tapdex\trating";
    }

    public void add(long start, long end, boolean success) {
        count += 1;
        if (this.start == 0) {
            this.start = start;
        }
        this.start = Math.min(this.start, start);
        this.end = Math.max(this.end, end);
        if (!success) {
            errorCount += 1;
        }
        long duration = end - start;
        durations.add((double) duration);
        apdex.addMs(duration);
    }

    public void computeStat(int maxUsers) {
        computeStat((end - start) / 1000.0, maxUsers);
    }

    public void computeStat(double duration, int maxUsers) {
        double[] times = getDurationAsArray();
        min = (long) StatUtils.min(times);
        max = (long) StatUtils.max(times);
        double sum = 0;
        for (double d : times)
            sum += d;
        avg = sum / times.length;
        p50 = (long) StatUtils.percentile(times, 50.0);
        p90 = (long) StatUtils.percentile(times, 90.0);
        p95 = (long) StatUtils.percentile(times, 95.0);
        p99 = (long) StatUtils.percentile(times, 99.0);
        StandardDeviation stdDev = new StandardDeviation();
        stddev = (long) stdDev.evaluate(times, avg);
        this.duration = duration;
        this.maxUsers = maxUsers;
        rps = (count - errorCount) / duration;
        startDate = getDateFromInstant(start);
        successCount = count - errorCount;
    }

    public void setSimulationName(String name) {
        simulation = name;
    }

    public void setScenario(String name) {
        scenario = name;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String average() {
        return String.format(Locale.ENGLISH, "%.1f", avg);
    }

    public String boxpoints() {
        if (count < MAX_BOXPOINT) {
            return "'all'";
        }
        return "false";
    }

    public String throughput() {
        return String.format(Locale.ENGLISH, "%.1f", rps);
    }

    public String percentError() {
        if (count == 0) {
            return "0.00";
        }
        return String.format(Locale.ENGLISH, "%.2f", (errorCount * 100.0) / count);
    }

    protected String getDateFromInstant(long start) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd " + "HH:mm:ss")
                                                       .withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochMilli(start));
    }

    protected double[] getDurationAsArray() {
        double[] ret = new double[durations.size()];
        for (int i = 0; i < durations.size(); i++) {
            ret[i] = durations.get(i);
        }
        return ret;
    }

    public String getDuration() {
        return String.format(Locale.ENGLISH, "%.1f", duration);
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "%s\t%s\t%s\t%s\t%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.2f\t%s\t%.2f\t%.2f\t%s",
                simulation, scenario, maxUsers, request, start, startDate, duration, end, count, successCount,
                errorCount, min, p50, p90, p95, p99, max, avg, stddev, rps, apdex.getScore(), apdex.getRating());
    }
}
