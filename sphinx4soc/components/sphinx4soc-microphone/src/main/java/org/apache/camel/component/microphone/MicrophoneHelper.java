package org.apache.camel.component.microphone;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicrophoneHelper { 

    private static final transient Logger LOG = LoggerFactory.getLogger(MicrophoneHelper.class);
    private MicrophoneHelper(){}
    
    /**
     * Returns a native audio format that has the same encoding, endianness and sample size as the given format, and a
     * sample rate that is greater than or equal to the given sample rate.
     *
     * @param format the desired format
     * @param mixer  if non-null, use this Mixer; otherwise use AudioSystem
     * @return a suitable native audio format
     */
    public static AudioFormat getNativeAudioFormat ( AudioFormat format, Mixer mixer )
    {
        Line.Info[] lineInfos;

        if ( mixer!=null ) {
            lineInfos = mixer.getTargetLineInfo( new Line.Info( TargetDataLine.class ) );
        } else {
            lineInfos = AudioSystem.getTargetLineInfo( new Line.Info( TargetDataLine.class ) );
        }

        AudioFormat nativeFormat = null;

        // find a usable target line
        for ( Line.Info info : lineInfos ) {

            AudioFormat[] formats = ( (TargetDataLine.Info)info ).getFormats();

            for ( AudioFormat thisFormat : formats ) {

                // for now, just accept downsampling, not checking frame
                // size/rate (encoding assumed to be PCM)

                if ( thisFormat.getEncoding()==format.getEncoding()
                        &&thisFormat.isBigEndian()==format.isBigEndian()
                        &&thisFormat.getSampleSizeInBits()
                        ==format.getSampleSizeInBits()
                        &&thisFormat.getSampleRate()>=format.getSampleRate() ) {
                    nativeFormat = thisFormat;
                    break;
                }
            }
            if ( nativeFormat!=null ) {
                //no need to look through remaining lineinfos
                break;
            }
        }
        return nativeFormat;
    }
    
    public static double[] convertStereoToMono(double[] samples, int channels, String stereoToMono, int selectedChannel) {
        assert (samples.length % channels == 0);
        double[] finalSamples = new double[samples.length / channels];
        if (stereoToMono.equals("average")) {
            for (int i = 0, j = 0; i < samples.length; j++) {
                double sum = samples[i++];
                for (int c = 1; c < channels; c++) {
                    sum += samples[i++];
                }
                finalSamples[j] = sum / channels;
            }
        } else if (stereoToMono.equals("selectChannel")) {
            for (int i = selectedChannel, j = 0; i < samples.length; i += channels, j++) {
                finalSamples[j] = samples[i];
            }
        } else {
            throw new Error("Unsupported stereo to mono conversion: " + stereoToMono);
        }
        return finalSamples;
    }
    
    /**
     * Gets the Mixer to use. Depends upon selectedMixerIndex being defined.
     *
     * @see #newProperties
     */
    public static Mixer getSelectedMixer(String selectMixerIndex) {
        if (selectMixerIndex.equals("default")) {
            return null;
        } else {
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            if (selectMixerIndex.equals("last")) {
                return AudioSystem.getMixer(mixerInfo[mixerInfo.length - 1]);
            } else {
                int index = Integer.parseInt(selectMixerIndex);
                return AudioSystem.getMixer(mixerInfo[index]);
            }
        }
    }
    
    /**
     * Converts a big-endian byte array into an array of doubles. Each consecutive bytes in the byte array are converted
     * into a double, and becomes the next element in the double array. The size of the returned array is
     * (length/bytesPerValue). Currently, only 1 byte (8-bit) or 2 bytes (16-bit) samples are supported.
     *
     * @param byteArray     a byte array
     * @param offset        which byte to start from
     * @param length        how many bytes to convert
     * @param bytesPerValue the number of bytes per value
     * @param signedData    whether the data is signed
     * @return a double array, or <code>null</code> if byteArray is of zero length
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *
     */
    public static double[] bytesToValues ( byte[] byteArray,
                                           int offset,
                                           int length,
                                           int bytesPerValue,
                                           boolean signedData )
            throws ArrayIndexOutOfBoundsException
    {

        if ( 0<length&&( offset+length )<=byteArray.length ) {
            assert ( length%bytesPerValue==0 );
            double[] doubleArray = new double[ length/bytesPerValue ];

            int i = offset;

            for ( int j = 0; j<doubleArray.length; j++ ) {
                int val = (int)byteArray[i++];
                if ( !signedData ) {
                    val &= 0xff; // remove the sign extension
                }
                for ( int c = 1; c<bytesPerValue; c++ ) {
                    int temp = (int)byteArray[i++]&0xff;
                    val = ( val<<8 )+temp;
                }

                doubleArray[j] = (double)val;
            }

            return doubleArray;
        } else {
            throw new ArrayIndexOutOfBoundsException( "offset: "+offset+", length: "+length
                    +", array length: "+byteArray.length );
        }
    }

    /**
     * Converts a little-endian byte array into an array of doubles. Each consecutive bytes of a float are converted
     * into a double, and becomes the next element in the double array. The number of bytes in the double is specified
     * as an argument. The size of the returned array is (data.length/bytesPerValue).
     *
     * @param data          a byte array
     * @param offset        which byte to start from
     * @param length        how many bytes to convert
     * @param bytesPerValue the number of bytes per value
     * @param signedData    whether the data is signed
     * @return a double array, or <code>null</code> if byteArray is of zero length
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *
     */
    public static double[] littleEndianBytesToValues ( byte[] data,
                                                       int offset,
                                                       int length,
                                                       int bytesPerValue,
                                                       boolean signedData )
            throws ArrayIndexOutOfBoundsException
    {

        if ( 0<length&&( offset+length )<=data.length ) {
            assert ( length%bytesPerValue==0 );
            double[] doubleArray = new double[ length/bytesPerValue ];

            int i = offset+bytesPerValue-1;

            for ( int j = 0; j<doubleArray.length; j++ ) {
                int val = (int)data[i--];
                if ( !signedData ) {
                    val &= 0xff; // remove the sign extension
                }
                for ( int c = 1; c<bytesPerValue; c++ ) {
                    int temp = (int)data[i--]&0xff;
                    val = ( val<<8 )+temp;
                }

                // advance 'i' to the last byte of the next value
                i += ( bytesPerValue*2 );

                doubleArray[j] = (double)val;
            }

            return doubleArray;

        } else {
            throw new ArrayIndexOutOfBoundsException( "offset: "+offset+", length: "+length
                    +", array length: "+data.length );
        }
    }

}
