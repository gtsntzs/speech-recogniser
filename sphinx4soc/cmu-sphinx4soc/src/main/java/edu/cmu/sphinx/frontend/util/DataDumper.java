/*
 * Copyright 1999-2002 Carnegie Mellon University.  
 * Portions Copyright 2002 Sun Microsystems, Inc.  
 * Portions Copyright 2002 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL 
 * WARRANTIES.
 *
 */
package edu.cmu.sphinx.frontend.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.Signal;
import edu.cmu.sphinx.frontend.endpoint.SpeechClassifiedData;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Boolean;
import edu.cmu.sphinx.util.props.S4String;

/** Dumps the data */
public class DataDumper extends BaseDataProcessor {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	/** The property that specifies whether data dumping is enabled */
	@S4Boolean(defaultValue = true)
	public final static String PROP_ENABLE = "enable";
	/** The property that specifies the format of the output. */
	@S4String(defaultValue = "0.00000E00;-0.00000E00")
	public final static String PROP_OUTPUT_FORMAT = "outputFormat";
	/** The property that enables the output of signals. */
	@S4Boolean(defaultValue = true)
	public final static String PROP_OUTPUT_SIGNALS = "outputSignals";
	// --------------------------
	// Configuration data
	// --------------------------
	private int frameCount;
	private boolean enable;
	private boolean outputSignals;
	private DecimalFormat formatter;

	public DataDumper(boolean enable, String format, boolean outputSignals) {
		initLogger();
		this.formatter = new DecimalFormat(format, new DecimalFormatSymbols(
				Locale.US));
		this.outputSignals = outputSignals;
		this.enable = enable;
	}

	public DataDumper() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.cmu.sphinx.util.props.Configurable#newProperties(edu.cmu.sphinx.util
	 * .props.PropertySheet)
	 */
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);

		logger = ps.getLogger();

		enable = ps.getBoolean(PROP_ENABLE);
		String format = ps.getString(PROP_OUTPUT_FORMAT);
		formatter = new DecimalFormat(format, new DecimalFormatSymbols(
				Locale.US));
		outputSignals = ps.getBoolean(PROP_OUTPUT_SIGNALS);
	}

	/** Constructs a DataDumper */
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Reads and returns the next Data object from this DataProcessor, return
	 * null if there is no more audio data.
	 *
	 * @return the next Data or <code>null</code> if none is available
	 * @throws DataProcessingException
	 *             if there is a data processing error
	 */
	public Data getData(Data input) throws DataProcessingException {
		// Data input = getPredecessor().getData();
		if (enable) {
			dumpData(input);
		}
		return input;
	}

	/**
	 * Reads and returns the next Data object from this DataProcessor, return
	 * null if there is no more audio data.
	 *
	 * @return the next Data or <code>null</code> if none is available
	 * @throws DataProcessingException
	 *             if there is a data processing error
	 */
	@Override
	public Data getData() throws DataProcessingException {
		Data input = getPredecessor().getData();
		if (enable) {
			dumpData(input);
		}
		return input;
	}

	/**
	 * Dumps the given input data
	 *
	 * @param input
	 *            the data to dump
	 */
	public void dumpData ( Data input )
    {
    	StringBuffer buffer = new StringBuffer();
        if ( input instanceof Signal ) {
            if ( outputSignals ) {
                buffer.append("Signal: "+input );
                if ( input instanceof DataStartSignal ) {
                    frameCount = 0;
                }
            }
        } else if ( input instanceof DoubleData ) {
            DoubleData dd = (DoubleData)input;
            double[] values = dd.getValues();
            buffer.append("Frame "+values.length );
            for ( double val : values ) {
                buffer.append(' '+formatter.format( val ));
            }
            buffer.append("\n");
        } else if ( input instanceof SpeechClassifiedData ) {
            SpeechClassifiedData dd = (SpeechClassifiedData)input;
            double[] values = dd.getValues();
            buffer.append("Frame ");
            if ( dd.isSpeech() ) {
                buffer.append("*");
            } else {
                buffer.append(" ");
            }
            buffer.append(" "+values.length);
            for ( double val : values ) {
                buffer.append(' '+formatter.format( val ));
            }
        } else if ( input instanceof FloatData ) {
            FloatData fd = (FloatData)input;
            float[] values = fd.getValues();
            buffer.append("Frame "+values.length );
            for ( float val : values ) {
                buffer.append("" +formatter.format( val ));
            }
        }
        
        LOGGER.info(buffer.toString());
        frameCount++;
    }
}
