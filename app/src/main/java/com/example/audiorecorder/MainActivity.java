package com.example.audiorecorder;

import android.Manifest;
import android.content.pm.PackageManager;
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
import java.util.Random;

import com.example.audiorecorder.AudioUtils;


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

    private short[] amplitudes;

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


                short[] ampl = AudioUtils.getAmplitudes(fileName);
                amplitudes = AudioUtils.resizeAmplitudes(ampl, DATA_SAMPLE_AVERAGE);

                StringBuilder data = new StringBuilder();
                for (short soundDatum : amplitudes) {
                    String temp = soundDatum + " ";
                    data.append(temp);
                }

                String text = fileName + " " + amplitudes.length + "\n";
                textView.setText(text);
                textView.append(data);

            }
        });


//        try {
//            ModelEsc50 model = ModelEsc50.newInstance(this);

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
