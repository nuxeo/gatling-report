import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class Parser {

    private static final String OK = "OK";
    private static final String REQUEST = "REQUEST";
    private static final String RUN = "RUN";
    private static final String GZ = "gz";
    private final File file;

    public Parser(File file) {
        this.file = file;
    }

    public SimulationStat parse() throws IOException {
        SimulationStat ret = new SimulationStat(file.getAbsolutePath());
        CSVReader reader = new CSVReader(getReaderFor(file), '\t');
        String[] line;
        String name, scenario;
        long start, end;
        boolean success;
        while ((line = reader.readNext()) != null) {
            if (line.length <= 2) {
                invalidFile();
            }
            switch (line[2]) {
                case RUN:
                    String version = line[5];
                    if (! version.startsWith("2.")) {
                        return invalidFile();
                    }
                    ret.setSimulationName(line[1]);
                    ret.setStart(Long.parseLong(line[3]));
                    break;
                case REQUEST:
                    scenario = line[0];
                    name = line[4];
                    start = Long.parseLong(line[6]);
                    end = Long.parseLong(line[8]);
                    success = OK.equals(line[9]);
                    ret.addRequest(name, start, end, success);
                break;

            }

        }
        return ret;
    }

    private SimulationStat invalidFile() {
        throw new IllegalArgumentException(String.format("Invalid simulation file: %s expecting " +
        "Gatling 2.x format", file.getAbsolutePath()));
    }


    private String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }


    private Reader getReaderFor(File file) throws IOException {
        if (GZ.equals(getFileExtension(file))) {
            InputStream fileStream = new FileInputStream(file);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            return new InputStreamReader(gzipStream, "UTF-8");
        }
        return new FileReader(file);
    }

}
