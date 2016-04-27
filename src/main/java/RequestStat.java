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

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestStat {
    private static final long MAX_BOXPOINT = 50000;
    String simulation;
    String scenario;
    String request;
    String requestId;
    static AtomicInteger statCounter = new AtomicInteger();
    int indice;
    String startDate;
    long start, end;
    ArrayList <Long> startTimes;
    ArrayList <Long> endTimes;
    ArrayList <Long> errors;
    ArrayList <Integer> reqPerSec;
    ArrayList <Integer> resPerSec;
    ArrayList <Integer> errorPerSec;
    ArrayList <Integer> reqPerSecAvg;
    ArrayList <Integer> resPerSecAvg;
    ArrayList <Integer> errorPerSecAvg;
    ArrayList <Request> requestList;
    ArrayList <Request> minMaxResponse;
    long count, successCount, errorCount;
    long min, max, stddev, p50, p95, p99;
    double rps, avg;
    double duration;
    List<Double> durations;
    Graphite graphite;
    Apdex apdex;
    int maxUsers;

	public RequestStat(String simulation, String scenario, String request, long start, Float apdexT) {
        this.simulation = simulation;
        this.scenario = scenario;
        this.request = request;
        requestId = Utils.getIdentifier(request);
        this.start = start;
        durations = new ArrayList<>();
        
        indice = statCounter.incrementAndGet();
        apdex = new Apdex(apdexT);
        startTimes = new ArrayList<Long>();
        endTimes = new ArrayList<Long>();
        errors = new ArrayList<Long>();
        requestList = new ArrayList<Request>();
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
            errors.add(end);
        }
        long duration = end - start;
        startTimes.add(start);
        endTimes.add(end);
        durations.add((double) duration);
        apdex.addMs(duration);
        requestList.add(new Request("na", "na", start, end, success));
    }

    public void computeStat(int maxUsers, boolean normalised) {
    	System.out.println("ComputeStat minimal");
        computeStat((end - start) / 1000.0, maxUsers, normalised);
    }

    public void computeStat(double duration, int maxUsers, boolean normalised) {
    	System.out.println("ComputeStat bigger");
    	double[] times = getDurationAsArray();
        min = (long) StatUtils.min(times);
        max = (long) StatUtils.max(times);
        double sum = 0;
        for (double d : times) sum += d;
        avg = sum / times.length;
        p50 = (long) StatUtils.percentile(times, 50.0);
        p95 = (long) StatUtils.percentile(times, 95.0);
        p99 = (long) StatUtils.percentile(times, 99.0);
        StandardDeviation stdDev = new StandardDeviation();
        stddev = (long) stdDev.evaluate(times, avg);
        this.duration = duration;
        this.maxUsers = maxUsers;
        rps = (count - errorCount) / duration;
        startDate = getDateFromInstant(start);
        successCount = count - errorCount;
        
        if (normalised) {
	        initialiseTimeSlots();
	        calculateRollingErrorPerSec();
	        calculateRollingReqPerSec();
	        calculateRollingResPerSec();
	        reqPerSecAvg = calculateRollingAvgPerMinute(reqPerSec);
	        resPerSecAvg = calculateRollingAvgPerMinute(resPerSec);
	        errorPerSecAvg = calculateRollingAvgPerMinute(errorPerSec);
	        minMaxResponse = calculateRollingMinuteMaxMinResponse(requestList);
        }
    }
    
    public void initialiseTimeSlots() {
    	reqPerSec = new ArrayList<Integer>();
    	resPerSec = new ArrayList<Integer>();
    	errorPerSec = new ArrayList<Integer>();
    	System.out.println("Duration:  " + duration);
    	for (int i = 0; i <= duration + 1; i++) {
    		reqPerSec.add(0);
    		resPerSec.add(0);
    		errorPerSec.add(0);
    	}
    }
    public ArrayList<Request> calculateRollingMinuteMaxMinResponse(ArrayList<Request> dataset) {
    	ArrayList<Request> minMax = new ArrayList<Request>();
    	long min = Long.MAX_VALUE, max = 0, dur = 0, strt = 0, timeslot = 0;    	
    	Request mini = new Request("na", "na", 0, 0, true);
    	Request maxi = new Request("na", "na", 0, 0, true);
    	
    	for (Request req : dataset) {
    		dur = req.getDuration();
    		strt = req.getStart();
    		if (dur > max && strt < timeslot) {
    			max = dur;
    			maxi = req;
    		} else if (dur < min && strt < timeslot) {
    			min = dur;
    			mini = req;
    		} else if (strt > timeslot) {
    			System.out.println("TimeSlot: " + timeslot);
    			timeslot += 60000;
    			System.out.println("TimeSlot: " + timeslot);
    			min = Long.MAX_VALUE; 
    			max = 0;
    			minMax.add(mini);
    			minMax.add(maxi);
    		}
    	}
    	return minMax;
    }
    
    public ArrayList<Integer> calculateRollingAvgPerMinute(ArrayList<Integer> dataset) {
    	int value = 0;
    	int counter = 0;
    	ArrayList<Integer> avg = new ArrayList<Integer>();
    	for (int result : dataset) {
    		value += dataset.get(counter);
    		if(counter % 60 == 0) {
    			avg.add(value/60);
    			value = 0;
    		}
    		counter += 1;
    	}
    	return avg;
    }
    
    public void calculateRollingErrorPerSec() {
    	int value = 0;
    	Long timeSlot = new Long(0);
    	for (Long time : errors) {
    		timeSlot = time/1000;
    		value = errorPerSec.get(timeSlot.intValue());
    		value += 1;
    		errorPerSec.set(timeSlot.intValue(), value);
    	}
    }
    
    public void calculateRollingReqPerSec() {
    	int value = 0;
    	Long timeSlot = new Long(0);
    	for (Long time : startTimes) {
    		timeSlot = time/1000;
    		value = reqPerSec.get(timeSlot.intValue());
    		value += 1;
    		reqPerSec.set(timeSlot.intValue(), value);
    	}
    }

    public void calculateRollingResPerSec() {
    	int value = 0;
    	Long timeSlot = new Long(0);
    	for (Long time : endTimes) {
    		timeSlot = time/1000;
    		value = resPerSec.get(timeSlot.intValue());
    		value += 1;
    		resPerSec.set(timeSlot.intValue(), value);
    	}
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

    private String getDateFromInstant(long start) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd " +
                "HH:mm:ss").withZone(ZoneId.systemDefault());
        return formatter.format(Instant.ofEpochMilli(start));
    }

    private double[] getDurationAsArray() {
        double[] ret = new double[durations.size()];
        for (int i = 0; i < durations.size(); i++) {
            ret[i] = durations.get(i);
        }
        return ret;
    }

    public String getDuration() {
        return String.format(Locale.ENGLISH, "%.1f", duration);
    }

    public static String header() {
        return "simulation\tscenario\tmaxUsers\trequest\tstart\tstartDate\tduration\tend\tcount\tsuccessCount\t" +
                "errorCount\tmin\tp50\tp95\tp99\tmax\tavg\tstddev\trps\tapdex\trating";
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "%s\t%s\t%s\t%s\t%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.2f\t%s\t%.2f\t%.2f\t%s",
                simulation, scenario, maxUsers, request, start, startDate, duration, end, count, successCount,
                errorCount, min, p50, p95, p99, max, avg, stddev, rps, apdex.getScore(), apdex.getRating());
    }
}
