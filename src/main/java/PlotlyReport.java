import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class PlotlyReport {

    public static String generate(File outputDirectory, SimulationStat stats) throws IOException {
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
        mustache.execute(output, stats).flush();
        return ret;
    }
}
