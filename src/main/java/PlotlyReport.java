import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PlotlyReport {

    public static String generate(File outputDirectory, SimulationStat stats) throws IOException {
        File output = new File(outputDirectory, "index.html");
        //output.createNewFile();
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("report.mustache");
        mustache.execute(new FileWriter(output), stats).flush();
        return output.getAbsolutePath();
    }
}
