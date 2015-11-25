import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrendStat {

    String scenario;
    List<String> xvalues = new ArrayList<>();
    List<Double> yvalues = new ArrayList<>();
    List<Long> yerrors = new ArrayList<>();
    List<Double> rps = new ArrayList<>();

    public TrendStat(List<SimulationStat> stats) {
        Set<String> names = new HashSet<>();
        Collections.sort(stats, (a, b) -> (int) (a.simStat.start - b.simStat.start));
        for (SimulationStat stat : stats) {
            names.add(stat.simulationName);
            xvalues.add(String.format("'%s'", stat.simStat.startDate));
            yvalues.add(stat.simStat.avg);
            yerrors.add(stat.simStat.stddev);
            rps.add(stat.simStat.rps);
        }
        scenario = String.join(" ", names);
    }

}
