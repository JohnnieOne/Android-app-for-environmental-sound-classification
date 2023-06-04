package com.example.audiorecorder;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public static float[][] NRDT(String filename) throws IOException {
        int[] channels = {2, 4, 8, 16, 20, 32, 50, 64, 100, 128, 200, 300};
        int w = 1000;
        int flag = 0;
        float[][] signal = readSignal(filename);
        int Nsamples = signal[0].length;
        float delmax = w / 4;
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < channels.length; i++) {
            if (channels[i] <= delmax) {
                res.add(channels[i]);
            }
        }
        int[] channels_res = new int[res.size()];
        for (int i = 0; i < res.size(); i++) {
            channels_res[i] = res.get(i);
        }
        int m = channels_res.length;
        int spectrograms = Nsamples / w;
        int Samples = spectrograms * w;
        float[][] matrix = new float[spectrograms][w];
        for (int i = 0; i < spectrograms; i++) {
            for (int j = 0; j < w; j++) {
                matrix[i][j] = signal[0][i * w + j];
            }
        }
        float[][] spectrum = new float[m][spectrograms];
        for (int i = 0; i < spectrograms; i++) {
            float[] values = matrix[i];
            for (int k = 0; k < m; k++) {
                int delay = channels_res[k];
                int[] t = new int[w - 2 * delay - 1];
                for (int j = 0; j < t.length; j++) {
                    t[j] = j + delay;
                }
                float[] difus = new float[t.length];
                for (int j = 0; j < t.length; j++) {
                    difus[j] = Math.abs(values[t[j] - delay] + values[t[j] + delay] - 2 * values[t[j]]);
                }
                if (flag == 0) {
                    spectrum[k][i] = mean(difus) / 4;
                } else if (flag == 1) {
                    float[] temp = new float[t.length];
                    for (int j = 0; j < t.length; j++) {
                        temp[j] = difus[j] / (Math.abs(values[t[j] - delay]) + Math.abs(values[t[j] + delay]) + 2 * Math.abs(values[t[j]]) + 1e-12f);
                    }
                    spectrum[k][i] = mean(temp) / 4;
                }
            }
        }
        return spectrum;
    }

    public static float mean(float[] arr) {
        float sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum / arr.length;
    }
}

