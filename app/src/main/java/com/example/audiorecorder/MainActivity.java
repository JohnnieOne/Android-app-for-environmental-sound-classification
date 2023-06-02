package com.example.audiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.audiorecorder.ml.ModelEsc50;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
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

    private int count = 0;

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
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

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
        String randomFileNamePath = "/audiorecordtest" + randNumString + ".3gp";

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += randomFileNamePath;
    }

    private byte[] readSoundFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        int fileSize = (int) file.length();
        byte[] soundData = new byte[fileSize];
        fileInputStream.read(soundData);
        fileInputStream.close();
        return soundData;
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
        TextView textView = findViewById(R.id.textView);

        classifyButton.setOnClickListener(new View.OnClickListener() {
            String classifyMsg;

            @Override
            public void onClick(View view) {
                count++;
                classifyMsg = "Test " + count + ": " + fileName;

                try {
                    File file = new File(fileName);
                    byte[] soundData = readSoundFile(file);

                    StringBuilder data = new StringBuilder();
                    for (byte soundDatum : soundData) {
                        String temp = String.valueOf(soundDatum) + " ";
                        data.append(temp);
                    }

                    textView.setText(String.valueOf(soundData.length));
                    textView.append("\n");
                    textView.append(data);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        });


        // integrare model tflite sample code

//        try {
//            ModelEsc50 model = ModelEsc50.newInstance(this); // context se poate inlocui cu this sau modelul respectiv
//
//            // Creates inputs for reference.
//            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 11, 220, 1}, DataType.FLOAT32);
//            ByteBuffer byteBuffer = null;
//            inputFeature0.loadBuffer(byteBuffer);
//
//            // Runs model inference and gets result.
//            ModelEsc50.Outputs outputs = model.process(inputFeature0);
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//            // Releases model resources if no longer used.
//            model.close();
//        } catch (IOException e) {
//            // TODO Handle the exception
//        }


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
