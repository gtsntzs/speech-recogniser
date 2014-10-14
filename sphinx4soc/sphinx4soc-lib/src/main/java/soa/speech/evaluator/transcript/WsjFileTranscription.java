package soa.speech.evaluator.transcript;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsjFileTranscription implements TranscriptionDao {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private Map<String, String> name2transcriptMap = new HashMap<>();

	private final String transcriptionFilePath;
	
	public WsjFileTranscription(String transcriptionFilePath) throws Exception{
		this.transcriptionFilePath = transcriptionFilePath; 
	}
	
	@Override
	public Map<String, String> getTranscriptions() {
		return name2transcriptMap;
	}
	
	@Override
	public String getTranscription(String identifier) throws Exception {
		String transcript = name2transcriptMap.get(FilenameUtils.getBaseName(identifier));
		return transcript;
	}

	@Override
	public void addOrUpdateTranscription(String identifier, String transcription) throws Exception {
		// TODO Auto-generated method stub
	}

	public void load() throws Exception {

		Path file = Paths.get(transcriptionFilePath);
		List<String> lines = Files.readAllLines(file, Charset.defaultCharset());

		Iterator<String> iterator = lines.iterator();
		while (iterator.hasNext()) {
			String nextTranscript = iterator.next();
			if (nextTranscript.matches("[\"*/.](.*)[.lab\"]")) {
				String name = nextTranscript.substring(3, nextTranscript.length()-5);
				StringBuffer buffer = new StringBuffer();
				while (true) {
					String line = iterator.next();
					if (!line.equals(".")) {
						if (buffer.length() != 0) {
							buffer.append(" ");
						}
						buffer.append(line);
					} else {
						name2transcriptMap.put(name, buffer.toString().toLowerCase());
						break;
					}

				}
				logger.debug(name +" --> "+buffer.toString());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		WsjFileTranscription transcription = new WsjFileTranscription("/media/Work/data/databases/AURORA4/files/wsj-cleantrain-word.mlf");
//		transcription.loadTranscriptions();
	}

}
