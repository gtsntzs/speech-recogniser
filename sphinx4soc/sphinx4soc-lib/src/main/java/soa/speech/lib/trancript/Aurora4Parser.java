package soa.speech.lib.trancript;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Aurora4Parser implements TranscriptionParser {

    static final Logger logger = Logger.getLogger(Aurora4Parser.class);

    @Override
    public Map<String, String> parse(String filePath) {

        Map<String, String> out = new HashMap<>();

        Path path = FileSystems.getDefault().getPath(filePath);
        try (InputStream in = Files.newInputStream(path)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String transcipt = "";
            String fileName = "";
            String line = null;
            while ((line = reader.readLine()) != null) {
                
                if (line.equalsIgnoreCase("#!MLF!#")) {
                    // ignore
                } else if (".".equalsIgnoreCase(line)) {
                    out.put(fileName, transcipt);
                    transcipt = "";
                    fileName = "";
                } else if (line.startsWith("\"*/")) {
                    fileName = line.substring(3, line.length() - 5);
                } else {
                    if ("".equalsIgnoreCase(transcipt))
                        transcipt = line.toLowerCase();
                    else
                        transcipt = transcipt + " " + line.toLowerCase();
                }
            }
        } catch (IOException e) {

        }
        return out;
    }
}
