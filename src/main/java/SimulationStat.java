import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class SimulationStat {

    private static final String ALL = "all";
    String simulationName;
    Stat simulation;
    Map<String, Stat> requests;

    public SimulationStat(String simulation) {
        simulationName = simulation;
        this.simulation = new Stat(simulation, ALL);
        requests = new HashMap<>();
    }

    public void addRequest(String name, long start, long end, boolean success) {
        Stat request = requests.get(name);
        if (request == null) {
            request = new Stat(simulationName, name);
            requests.put(name, request);
        }
        request.add(start, end, success);
        simulation.add(start, end, success);
    }

    public void computeStat() {
        simulation.computeStat();
        requests.values().forEach(request -> request.computeStat(simulation.duration));
    }

    @Override
    public String toString() {
        return simulation.toString() + "\n" + requests.values().stream().map(Stat::toString).collect(Collectors
                .joining("\n"));
    }
}
