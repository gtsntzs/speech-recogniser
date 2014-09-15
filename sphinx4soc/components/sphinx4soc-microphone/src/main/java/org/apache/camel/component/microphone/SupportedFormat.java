package org.apache.camel.component.microphone;

import javax.sound.sampled.AudioFormat;

public class SupportedFormat {
    
    public static final SupportedFormat[] SUPPORTED_FORMATS = { 
        new SupportedFormat("s8", AudioFormat.Encoding.PCM_SIGNED, 8, true),
        new SupportedFormat("u8", AudioFormat.Encoding.PCM_UNSIGNED, 8, true),
        new SupportedFormat("s16_le", AudioFormat.Encoding.PCM_SIGNED, 16, false),
        new SupportedFormat("s16_be", AudioFormat.Encoding.PCM_SIGNED, 16, true),
        new SupportedFormat("u16_le", AudioFormat.Encoding.PCM_UNSIGNED, 16, false),
        new SupportedFormat("u16_be", AudioFormat.Encoding.PCM_UNSIGNED, 16, true),
        new SupportedFormat("s24_le", AudioFormat.Encoding.PCM_SIGNED, 24, false),
        new SupportedFormat("s24_be", AudioFormat.Encoding.PCM_SIGNED, 24, true),
        new SupportedFormat("u24_le", AudioFormat.Encoding.PCM_UNSIGNED, 24, false),
        new SupportedFormat("u24_be", AudioFormat.Encoding.PCM_UNSIGNED, 24, true),
        new SupportedFormat("s32_le", AudioFormat.Encoding.PCM_SIGNED, 32, false),
        new SupportedFormat("s32_be", AudioFormat.Encoding.PCM_SIGNED, 32, true),
        new SupportedFormat("u32_le", AudioFormat.Encoding.PCM_UNSIGNED, 32, false),
        new SupportedFormat("u32_be", AudioFormat.Encoding.PCM_UNSIGNED, 32, true) 
        };
    
    /**
     * The name of the format.
     */
    private String m_strName;

    /**
     * The encoding of the format.
     */
    private AudioFormat.Encoding m_encoding;

    /**
     * The sample size of the format. This value is in bits for a single sample (not for a
     * frame).
     */
    private int m_nSampleSize;

    /**
     * The endianess of the format.
     */
    private boolean m_bBigEndian;

    // sample size is in bits
    /**
     * Construct a new supported format.
     * 
     * @param strName
     *            the name of the format.
     * @param encoding
     *            the encoding of the format.
     * @param nSampleSize
     *            the sample size of the format, in bits.
     * @param bBigEndian
     *            the endianess of the format.
     */
    public SupportedFormat(String strName, AudioFormat.Encoding encoding, int nSampleSize, boolean bBigEndian) {
        m_strName = strName;
        m_encoding = encoding;
        m_nSampleSize = nSampleSize;
    }

    /**
     * Returns the name of the format.
     */
    public String getName() {
        return m_strName;
    }

    /**
     * Returns the encoding of the format.
     */
    public AudioFormat.Encoding getEncoding() {
        return m_encoding;
    }

    /**
     * Returns the sample size of the format. This value is in bits.
     */
    public int getSampleSize() {
        return m_nSampleSize;
    }

    /**
     * Returns the endianess of the format.
     */
    public boolean getBigEndian() {
        return m_bBigEndian;
    }
}