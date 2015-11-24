import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Stat {
    String scenario;
    String request;
    String startDate;
    long start, end;
    long count, successCount, errorCount;
    long min, max, avg, stddev, p50, p95, p99;
    double rps;
    double duration;
    List<Double> durations;

    public Stat(String scenario, String request, long start) {
        this.scenario = scenario;
        this.request = request;
        this.start = start;
        durations = new ArrayList<>();
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
        durations.add((double) (end - start));
    }

    public void computeStat() {
        computeStat((end - start) / 1000.0);
    }

    public void computeStat(double duration) {
        double[] times = getDurationAsArray();
        min = (long) StatUtils.min(times);
        max = (long) StatUtils.max(times);
        double sum = 0;
        for (double d : times) sum += d;
        avg = (long) sum / times.length;
        p50 = (long) StatUtils.percentile(times, 50.0);
        p95 = (long) StatUtils.percentile(times, 95.0);
        p99 = (long) StatUtils.percentile(times, 99.0);
        StandardDeviation stdDev = new StandardDeviation();
        stddev = (long) stdDev.evaluate(times, avg);
        this.duration = duration;
        rps = (count - errorCount) / duration;
        startDate = Instant.ofEpochMilli(start).toString();
        successCount = count - errorCount;
    }

    public static String header() {
        return "scenario\trequest\tstart\tstartDate\tduration\tend\tcount\tsuccessCount\terrorCount\tmin\tp50\tp95" +
                "\tp99\tmax\tavg\tstddev\trps";
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%s\t%s\t%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.2f",
                scenario, request, start, startDate, duration, end, count, successCount, errorCount, min, p50, p95,
                p99, max, avg,
                stddev, rps);
    }

    private double[] getDurationAsArray() {
        double[] ret = new double[durations.size()];
        for (int i = 0; i < durations.size(); i++) {
            ret[i] = durations.get(i);
        }
        return ret;
    }

    public double getDuration() {
        return duration;
    }

    public void setScenario(String name) {
        scenario = name;
    }

    public void setStart(long start) {
        this.start = start;
    }
}
