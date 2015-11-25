import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class PlotlyReport {

    public static String generate(File outputDirectory, SimulationStat stat) throws IOException {
        Writer output;
        String ret = "stdout";
        if (outputDirectory == null) {
            output = new PrintWriter(System.out);
        } else {
            File index = new File(outputDirectory, "index.html");
            output = new FileWriter(index);
            ret = index.getAbsolutePath();
        }
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("report.mustache");
        mustache.execute(output, stat).flush();
        return ret;
    }

    public static String generate(File outputDirectory, List<SimulationStat> stats) throws IOException {
        Writer output;
        String ret = "stdout";
        if (outputDirectory == null) {
            output = new PrintWriter(System.out);
        } else {
            File index = new File(outputDirectory, "index.html");
            output = new FileWriter(index);
            ret = index.getAbsolutePath();
        }
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("trend.mustache");
        mustache.execute(output, new TrendStat(stats)).flush();
        return ret;
    }

}
