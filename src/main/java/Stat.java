import javafx.scene.paint.Stop;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class Stat {
    String simulation;
    String name;
    long start, end;
    long count, errorCount;
    long min, max, mean, stddev, p50, p95, p99;
    double rps;
    double duration;
    private List<Double> durations;

    public Stat(String simulation, String name) {
        this.simulation = simulation;
        this.name = name;
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
        mean = (long) StatUtils.mean(times);
        p50 = (long) StatUtils.percentile(times, 50.0);
        p95 = (long) StatUtils.percentile(times, 95.0);
        p99 = (long) StatUtils.percentile(times, 99.0);
        StandardDeviation stdDev = new StandardDeviation();
        stddev = (long) stdDev.evaluate(times, mean);
        this.duration = duration;
        rps = (count - errorCount) / duration;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%.2f",
                simulation, name, duration, start, end, count, errorCount, min, mean, max, stddev,
                p50, p95, p99, rps);
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
}
