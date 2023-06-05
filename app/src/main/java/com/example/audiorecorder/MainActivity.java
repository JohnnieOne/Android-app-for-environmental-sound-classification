package com.example.audiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private ImageClassifier imageClassifier;

    private static final int DATA_SAMPLE_AVERAGE = 220500;


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void getFilename() {
        Random rand = new Random();
        int randNum = rand.nextInt(10000);
        String randNumString = String.valueOf(randNum);
        String randomFileNamePath = "/audiorecordtest" + randNumString + ".ogg";

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += randomFileNamePath;

    }

    private String processClassificationResults(float[] probabilities) {
        int maxIndex = 0;
        float maxProbability = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProbability) {
                maxIndex = i;
                maxProbability = probabilities[i];
            }
        }

        // Output the predicted class label
        return "Class " + maxIndex;
    }


    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_main);
        getFilename();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        Button recordButton = findViewById(R.id.btnRecord);
        recordButton.setOnClickListener(new View.OnClickListener() {
            boolean mStartRecording = true;
            final String startMsg = "Start recording";
            final String stopMsg = "Stop recording";

            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    recordButton.setText(stopMsg);
                } else {
                    recordButton.setText(startMsg);
                }
                mStartRecording = !mStartRecording;
            }
        });

        Button playButton = findViewById(R.id.btnPlay);
        playButton.setOnClickListener(new View.OnClickListener() {
            boolean mStartPlaying = true;
            final String startMsg = "Start playing";
            final String stopMsg = "Stop playing";

            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    playButton.setText(stopMsg);
                } else {
                    playButton.setText(startMsg);
                }
                mStartPlaying = !mStartPlaying;
            }
        });

        Button classifyButton = findViewById(R.id.btnClassify);

        classifyButton.setOnClickListener(new View.OnClickListener() {
            String classifyMsg;

            @Override
            public void onClick(View view) {
                try {
                    // readSignal function is giving the correct output is a (1,220500) 2D array
                    float[][] signal = AudioUtils.readSignal(fileName);
                    int w = 100;
                    int flag = 0;
                    int[] channels = {2, 4, 8, 16, 20, 32, 50, 64, 100, 128, 200, 300};
                    int batchSize = 1;
                    int imageWidth = 220;
                    int imageHeight = 11;
                    int chan = 1;
                    int numClasses = 50;
                    String modelPath = "model_esc50.tflite";

                    // NRDT function is giving the correct output is a (11,220) 2D array
                    // for both functions further tests will be run in order to make sure
                    // that they are working as designed
                    float[][] spectrum = AudioUtils.NRDT(fileName);

                    String signalString = Arrays.deepToString(signal);
                    int signalX = signal.length;
                    int signalY = signal[0].length;
                    String signalLength = signalX + " " + signalY + "\n";


                    String spectrumString = Arrays.deepToString(spectrum);
                    int spectrumX = spectrum.length;
                    int spectrumY = spectrum[0].length;
                    String spectrumLength = spectrumX + " " + spectrumY + "\n";

                    AssetManager assetManager = getAssets();
                    imageClassifier = new ImageClassifier(assetManager, batchSize, imageWidth, imageHeight, chan, numClasses,  modelPath);
                    TextView textView = findViewById(R.id.textView);
                    String msj = "Probabilities: \n";

                    float[] probabilities = imageClassifier.classifyImage(spectrum);

                    textView.setText(msj);
                    String predicted = processClassificationResults(probabilities);
                    textView.append(predicted);


//                    textView.setText(signalLength);
//                    textView.append(spectrumLength);
//                    textView.append(spectrumString);


//                    // Currently not working, but it is not a priority
//                    WaveformView waveformView = findViewById(R.id.waveformView);
//                    waveformView.setWaveformData(signal[0]);
//                    waveformView.setWaveformColor(Color.RED);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
