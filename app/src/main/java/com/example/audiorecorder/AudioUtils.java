package com.example.audiorecorder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioUtils {

    public static short[] getAmplitudes(String filePath) {
        MediaExtractor extractor = new MediaExtractor();
        MediaCodec codec = null;
        try {
            extractor.setDataSource(filePath);
            int audioTrackIndex = getAudioTrackIndex(extractor);
            if (audioTrackIndex < 0) {
                throw new RuntimeException("No audio track found in the file");
            }

            extractor.selectTrack(audioTrackIndex);
            MediaFormat format = extractor.getTrackFormat(audioTrackIndex);
            String mime = format.getString(MediaFormat.KEY_MIME);
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();

            final int TIMEOUT_US = 10000;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            boolean outputDone = false;
            short[] amplitudes = new short[0];

            while (!outputDone) {
                if (!inputDone) {
                    int inputBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                        int sampleSize = extractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            inputDone = true;
                        } else {
                            codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                if (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);

                    int bufferSize = bufferInfo.size / 2; // Assuming 16-bit audio
                    if (bufferSize > 0) {
                        if (amplitudes.length < bufferSize) {
                            amplitudes = new short[bufferSize];
                        }

                        outputBuffer.asShortBuffer().get(amplitudes, 0, bufferSize);
                        // Process the amplitudes array here
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                }

            }

            return amplitudes;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (codec != null) {
                codec.stop();
                codec.release();
            }
            extractor.release();
        }

        return null;
    }

    private static int getAudioTrackIndex(MediaExtractor extractor) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }

    public static short[] resizeAmplitudes(short[] amplitudes, int targetSize) {
        if (amplitudes.length == targetSize) {
            // No need to resize
            return amplitudes;
        } else if (amplitudes.length > targetSize) {
            // Truncate the array
            short[] resizedAmplitudes = new short[targetSize];
            System.arraycopy(amplitudes, 0, resizedAmplitudes, 0, targetSize);
            return resizedAmplitudes;
        } else {
            // Pad with zeros
            short[] resizedAmplitudes = new short[targetSize];
            System.arraycopy(amplitudes, 0, resizedAmplitudes, 0, amplitudes.length);
            return resizedAmplitudes;
        }
    }
}
