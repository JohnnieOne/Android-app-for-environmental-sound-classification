package com.example.audiorecorder;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Interpreter.Options;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ImageClassifier {

    private static final String TAG = "ImageClassifier";
    private static String MODEL_PATH;
    private int BATCH_SIZE;
    private int IMAGE_WIDTH;
    private int IMAGE_HEIGHT;
    private int CHANNELS;
    private int NUM_CLASSES;

    private Interpreter interpreter;
    private TensorBuffer inputBuffer;
    private TensorBuffer outputBuffer;

    public ImageClassifier(AssetManager assetManager, int batchSize, int imageWidth, int imageHeight, int channels, int numClasses, String modelPath) {
        this.BATCH_SIZE = batchSize;
        this.IMAGE_WIDTH = imageWidth;
        this.IMAGE_HEIGHT = imageHeight;
        this.CHANNELS = channels;
        this.NUM_CLASSES = numClasses;
        MODEL_PATH = modelPath;
        try {
            MappedByteBuffer modelBuffer = loadModelFile(assetManager);
            Options options = new Options();
            interpreter = new Interpreter(modelBuffer, options);
            inputBuffer = TensorBuffer.createFixedSize(new int[]{BATCH_SIZE, IMAGE_HEIGHT, IMAGE_WIDTH, CHANNELS}, DataType.FLOAT32);
            outputBuffer = TensorBuffer.createFixedSize(new int[]{BATCH_SIZE, NUM_CLASSES}, DataType.FLOAT32);

        } catch (IOException e) {
            Log.e(TAG, "Error loading TFLite model: " + e.getMessage());
        }
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(ImageClassifier.MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] classifyImage(float[][] image) {
        float[] flattenedImage = flattenImage(image);

        // Set the flattened image in the input TensorBuffer
        inputBuffer.loadArray(flattenedImage, inputBuffer.getShape());

        // Run inference
        interpreter.run(inputBuffer.getBuffer(), outputBuffer.getBuffer());

        return outputBuffer.getFloatArray();
    }

    private float[] flattenImage(float[][] image) {
        int width = image[0].length;
        int height = image.length;
        float[] flattenedImage = new float[width * height];

        for (int i = 0; i < height; i++) {
            System.arraycopy(image[i], 0, flattenedImage, i * width, width);
        }
        return flattenedImage;
    }
}

