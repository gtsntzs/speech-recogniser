package soa.speech.recogniser.frontend.process;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;

public class FeatureFileWriter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<float[]> allFeatures;
	private int featureLength = -1;
	private String outputDirectory;
	private String format;
	private String extension = "mfc";

	public FeatureFileWriter(String outputDirectory, String format, String extension) {
		this.outputDirectory = outputDirectory;
		this.format = format;
		this.extension = extension;
	}

	public Data storeFeature(Data feature, String fileName) {

		try {
			if (feature instanceof DataStartSignal) {
				allFeatures = new ArrayList<>();
			}

			if ((feature instanceof DataEndSignal)) {

				Path path = Paths.get(outputDirectory, fileName);
				Path parent = path.getParent();
				if (!Files.exists(parent)) {
					Files.createDirectories(parent);
				}
				
				String featureFile = path.toString() + "."+extension;

				if ("binary".equalsIgnoreCase(format)) {
					dumpBinary(featureFile);
				} else {
					dumpAscii(featureFile);
				}
			}

			if (feature instanceof DoubleData) {
				double[] featureData = ((DoubleData) feature).getValues();
				if (featureLength < 0) {
					featureLength = featureData.length;
					logger.info("Feature length: " + featureLength);
				}
				float[] convertedData = new float[featureData.length];
				for (int i = 0; i < featureData.length; i++) {
					convertedData[i] = (float) featureData[i];
				}
				allFeatures.add(convertedData);
			} else if (feature instanceof FloatData) {
				float[] featureData = ((FloatData) feature).getValues();
				if (featureLength < 0) {
					featureLength = featureData.length;
					logger.info("Feature length: " + featureLength);
				}
				allFeatures.add(featureData);
			}
		} catch (Exception e) {
			logger.error("Continue, directory doesn t exist will not save feature.", e);
		}

		return feature;
	}

	/**
	 * Returns the total number of data points that should be written to the
	 * output file.
	 * 
	 * @return the total number of data points that should be written
	 */
	private int getNumberDataPoints() {
		return (allFeatures.size() * featureLength);
	}

	/**
	 * Dumps the feature to the given binary output.
	 * 
	 * @param outputFile
	 *            the binary output file
	 */
	public void dumpBinary(String outputFile) throws IOException {
		DataOutputStream outStream = new DataOutputStream(new FileOutputStream(
				outputFile));
		outStream.writeInt(getNumberDataPoints());

		for (float[] feature : allFeatures) {
			for (float val : feature) {
				outStream.writeFloat(val);
			}
		}

		outStream.close();
	}

	/**
	 * Dumps the feature to the given ASCII output file.
	 * 
	 * @param outputFile
	 *            the ASCII output file
	 */
	public void dumpAscii(String outputFile) throws IOException {
		PrintStream ps = new PrintStream(new FileOutputStream(outputFile), true);
		ps.print(getNumberDataPoints());
		ps.print(' ');

		for (float[] feature : allFeatures) {
			for (float val : feature) {
				ps.print(val);
				ps.print(' ');
			}
		}

		ps.close();
	}
}
