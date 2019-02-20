package com.example.a96llegend.ar4ece;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


import com.example.a96llegend.ar4ece.FSM.StateEnteringActivity;

public class MainActivity extends Activity{

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
}
