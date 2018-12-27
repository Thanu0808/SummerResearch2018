package com.example.a96llegend.ar4ece;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.a96llegend.ar4ece.FSM.StateEnteringActivity;
import com.example.a96llegend.ar4ece.Gate.GateActivity;
import com.example.a96llegend.ar4ece.Resistor.ResistorActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //fsmSimulator
    public void fsmRunner(View view) {
        Intent intent = new Intent(this, StateEnteringActivity.class);
        startActivity(intent);
    }

    //Resistor value detector
    public void resistorDetector(View view) {
        Intent intent = new Intent(this, ResistorActivity.class);
        startActivity(intent);
    }

    //Logic gates
    public void gate(View view) {
        Intent intent = new Intent(this, GateActivity.class);
        startActivity(intent);
    }
}
