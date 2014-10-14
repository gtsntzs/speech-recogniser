package soa.speech.recogniser.lib.trancript;

import java.util.Map;

public interface TranscriptionParser {

    public Map<String,String> parse(String filePath);
}
