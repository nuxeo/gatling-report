import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulationStat {
    String filePath;
    String simulationName;
    Stat simStat;
    Map<String, Stat> reqStats;

    private static final String ALL_REQUESTS = "_all";
    private long start;

    public List<Stat> getRequests() {
        return new ArrayList<>(reqStats.values());
    }

    public SimulationStat(String filePath) {
        this.filePath = filePath;
        this.simStat = new Stat(filePath, ALL_REQUESTS, 0);
        reqStats = new HashMap<>();
    }

    public void addRequest(String requestName, long start, long end, boolean success) {
        Stat request = reqStats.get(requestName);
        if (request == null) {
            request = new Stat(simulationName, requestName, this.start);
            reqStats.put(requestName, request);
        }
        request.add(start, end, success);
        simStat.add(start, end, success);
    }

    public void computeStat() {
        simStat.computeStat();
        reqStats.values().forEach(request -> request.computeStat(simStat.getDuration()));
    }

    @Override
    public String toString() {
        return simStat.toString() + "\n" + reqStats.values().stream().map(Stat::toString)
                .collect(Collectors.joining("\n"));
    }

    public void setSimulationName(String name) {
        this.simulationName = name;
        simStat.setScenario(name);
    }

    public void setStart(long start) {
        this.start = start;
        simStat.setStart(start);
    }
}
