import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class Parser {

    private static final String OK = "OK";
    private static final String REQUEST = "REQUEST";
    private static final String GZ = "gz";
    private final File file;

    public Parser(File file) {
        this.file = file;
    }

    public SimulationStat parse() throws IOException {
        SimulationStat ret = new SimulationStat(file.getName());
        CSVReader reader = new CSVReader(getReaderFor(file), '\t');
        String[] line;
        String name;
        long start, end;
        boolean success;
        while ((line = reader.readNext()) != null) {
            if (!REQUEST.equals(line[2])) {
                continue;
            }
            name = line[4];
            start = Long.parseLong(line[6]);
            end = Long.parseLong(line[8]);
            success = OK.equals(line[9]);
            ret.addRequest(name, start, end, success);
        }
        return ret;
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
