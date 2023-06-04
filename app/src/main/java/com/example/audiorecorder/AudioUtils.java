package com.example.audiorecorder;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class AudioUtils {
    private static final int DATA_SAMPLE_AVERAGE = 220500;


    private static void normalize(float[][] array) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        // Find the minimum and maximum values in the array
        for (float[] row : array) {
            for (float value : row) {
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
        }

        float range = max - min;

        // Normalize each element in the array between -1 and 1
        for (float[] row : array) {
            for (int i = 0; i < row.length; i++) {
                row[i] = (row[i] - min) / range * 2 - 1;
            }
        }
    }

    public static float[][] readSignal(String filePath) throws IOException {
        byte[] audioData = readAudioData(filePath);
        int dataSize = audioData.length / 2;
        float[] floatData = new float[dataSize];

        ShortBuffer shortBuffer = ByteBuffer.wrap(audioData)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer();

        for (int i = 0; i < dataSize; i++) {
            floatData[i] = shortBuffer.get(i);
        }

        float[][] signal;

        if (dataSize <= DATA_SAMPLE_AVERAGE) {
            signal = new float[1][DATA_SAMPLE_AVERAGE];
            Arrays.fill(signal[0], 0, dataSize, 0f);
            System.arraycopy(floatData, 0, signal[0], 0, dataSize);
        } else {
            signal = new float[1][DATA_SAMPLE_AVERAGE];
            System.arraycopy(floatData, 0, signal[0], 0, DATA_SAMPLE_AVERAGE);
        }

        normalize(signal);

        return signal;
    }

    private static byte[] readAudioData(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        try (FileInputStream fileInputStream = new FileInputStream(path.toFile());
             FileChannel fileChannel = fileInputStream.getChannel()) {
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileChannel.size());
            fileChannel.read(byteBuffer);
            return byteBuffer.array();
        }
    }

    public static float[][] NRDT(String filename, int w, int flag, int[] channels) throws IOException {
        float[][] signal = readSignal(filename);
        // signal = convertToFloat32(signal);

        int Nsamples = signal[0].length;
        float delmax = w / 4f;
        int count = 0;
        for (int channel : channels) {
            if (channel <= delmax) {
                count++;
            }
        }
        int[] res = new int[count];
        int index = 0;
        for (int channel : channels) {
            if (channel <= delmax) {
                res[index++] = channel;
            }
        }
        channels = res;
        int m = channels.length;

        int spectrograms = Nsamples / w;
        int samples = spectrograms * w;
        float[][] matrix = reshape(signal[0], spectrograms, w);

        float[][] spectrum = new float[m][spectrograms];
        for (int i = 0; i < spectrograms; i++) {
            float[] values = matrix[i];
            for (int k = 0; k < m; k++) {
                int delay = channels[k];
                int[] t = new int[w - delay - 1];
                for (int j = 0; j < t.length; j++) {
                    t[j] = delay + j + 1;
                }
                float[] difus = new float[t.length];
                for (int j = 0; j < t.length; j++) {
                    int tIndex = t[j];
                    if (tIndex < values.length && tIndex - delay >= 0 && tIndex + delay < values.length) {
                        difus[j] = Math.abs(values[tIndex - delay] + values[tIndex + delay] - 2 * values[tIndex]);
                    } else {
                        // Handle out-of-bounds index
                        difus[j] = 0.0f;
                    }
                }
                if (flag == 0) {
                    spectrum[k][i] = mean(difus) / 4f;
                } else if (flag == 1) {
                    float denominator = mean(difus) / (meanAbs(values, t, delay) + 1e-12f);
                    spectrum[k][i] = denominator / 4f;
                }
            }
        }
        return spectrum;
    }


    private static float[][] convertToFloat32(float[][] signal) {
        // Implement the logic to convert the signal to float32 if required
        // and return the converted signal as a float[][] array
        return signal;
    }

    private static int[] findChannels(int[] channels, float delmax) {
        int count = 0;
        for (int channel : channels) {
            if (channel <= delmax) {
                count++;
            }
        }
        int[] res = new int[count];
        int index = 0;
        for (int channel : channels) {
            if (channel <= delmax) {
                res[index++] = channel;
            }
        }
        return res;
    }

    private static float[][] reshape(float[] array, int rows, int cols) {
        float[][] reshapedArray = new float[rows][cols];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                reshapedArray[i][j] = array[index++];
            }
        }
        return reshapedArray;
    }

    private static float mean(float[] array) {
        float sum = 0f;
        for (float num : array) {
            sum += num;
        }
        return sum / array.length;
    }

    private static float meanAbs(float[] values, int[] indices, int delay) {
        float sum = 0f;
        for (int index : indices) {
            sum += Math.abs(values[index]);
        }
        return sum / indices.length;
    }
}
