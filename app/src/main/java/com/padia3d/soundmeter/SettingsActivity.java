package com.padia3d.soundmeter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    SeekBar updatingTimeBar;
    TextView updatingTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        updatingTimeBar = findViewById(R.id.updateBar);
        updatingTimeText = findViewById(R.id.updatingTimeText);

        SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        updatingTimeText.setText(preferences.getInt("updatingTime", 25) + "ms");
        updatingTimeBar.setProgress(preferences.getInt("updatingTime", 25));

        updatingTimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatingTimeText.setText(updatingTimeBar.getProgress() + 16 + "ms");

                SharedPreferences preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("updatingTime", updatingTimeBar.getProgress() + 16);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}