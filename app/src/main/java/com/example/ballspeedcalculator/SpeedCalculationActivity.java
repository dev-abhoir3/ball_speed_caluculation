package com.example.ballspeedcalculator;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SpeedCalculationActivity extends AppCompatActivity {

    private TextView textCalculatedSpeed;
    private EditText inputDistance;
    private Button buttonSaveSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_calculation);

        textCalculatedSpeed = findViewById(R.id.text_calculated_speed);
        inputDistance = findViewById(R.id.input_distance);
        buttonSaveSpeed = findViewById(R.id.button_save_speed);
        buttonSaveSpeed.setOnClickListener(v -> {
            // Code to save speed to the database
        });
    }
}
