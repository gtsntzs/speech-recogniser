package soa.speech.recogniser.evaluator.transcript;

import java.util.Map;

public interface TranscriptionDao {

	public Map<String, String> getTranscriptions();
	
	public String getTranscription(String identifier) throws Exception;
	
	public void addOrUpdateTranscription(String identifier, String transcription) throws Exception;
	
}
