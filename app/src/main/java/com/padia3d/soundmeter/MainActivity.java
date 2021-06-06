package com.padia3d.soundmeter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    TextView outputSound, maxDecibels;
    ProgressBar progress;
    LineChart graph;


    MediaRecorder sound;
    Thread runner;
    final Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateSound();
        }
    };
    final Handler mHandler = new Handler();

    public double maxDecib = 0;
    private static double ambient = 0;
    static final private double filter = 0.6;
    public int sleepTime = 25, x = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        outputSound = findViewById(R.id.decibelText);
        maxDecibels = findViewById(R.id.maxDecibelText);
        progress = findViewById(R.id.progressBar);

        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        sleepTime = preferences.getInt("updatingTime", 25);

        if (runner == null) {
            runner = new Thread() {
                @Override
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settings_button) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSound();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseSound();
    }

    public void updateSound() {
        DecimalFormat df1 = new DecimalFormat("###.0");
        double decib = soundDb(2);
        if (decib > maxDecib) {
            maxDecibels.setText("Max: " + Math.round(decib) + "dB");
            maxDecib = decib;
        } else {
            maxDecibels.setText("Max: " + Math.round(maxDecib) + "dB");
        }
        String decib1 = df1.format(decib);
        outputSound.setText(decib1 + "dB");
        progress.setProgress((int) (Double.parseDouble(decib1) / 3.6));
    }

    public double soundDb(double ampl) {
        double dbSPL = 20 * Math.log10(presionSoundPA() / ampl);

        if (dbSPL < 0) {
            return 0;
        } else {
            return dbSPL;
        }
    }

    public double ambientSound() {
        if (sound != null) {
            return (sound.getMaxAmplitude());
        } else {
            return 0;
        }
    }

    public double presionSoundPA() {
        double amp = ambientSound();
        ambient = filter * amp + (1 - filter) * ambient;
        return ambient;
    }

    public void startSound() {
        if (sound == null) {
            sound = new MediaRecorder();
            sound.setAudioSource(MediaRecorder.AudioSource.MIC);
            sound.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            sound.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            sound.setOutputFile("/dev/null");

            try {
                sound.prepare();
            } catch (java.io.IOException ioe) {
            } catch (java.lang.SecurityException e) {
            }

            try {
                sound.start();
            } catch (java.lang.SecurityException e) {
            }
        }
    }

    public void pauseSound() {
        if (sound != null) {
            sound.stop();
            sound.release();
            sound = null;
        }
    }
}