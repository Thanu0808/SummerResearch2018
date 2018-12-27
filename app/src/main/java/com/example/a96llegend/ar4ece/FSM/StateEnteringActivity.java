package com.example.a96llegend.ar4ece.FSM;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.a96llegend.ar4ece.R;

import java.util.ArrayList;
import java.util.List;
//The view provided to the user for entering state names
public class StateEnteringActivity extends AppCompatActivity {

    private static final String tag = "=======Debug=======";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_entering);
    }

    //To scan activity
    public void toScaner(View view) {
        EditText enter1 = (EditText) findViewById(R.id.editState1);
        EditText enter2 = (EditText) findViewById(R.id.editState2);
        EditText enter3 = (EditText) findViewById(R.id.editState3);
        EditText enter4 = (EditText) findViewById(R.id.editState4);
        String state1 = enter1.getText().toString();
        String state2 = enter2.getText().toString();
        String state3 = enter3.getText().toString();
        String state4 = enter4.getText().toString();

        //If the user has enter 2 states name, jump to next activity(scan inputs)
        //If inputs not entered then toast appears
        if(state1.equals("") && state2.equals("")){
            //Both state 1 and 2 haven't been entered
            Toast toast= Toast.makeText(getApplicationContext(), "No state names", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (state1.equals("")){
            //Both state 1 haven't been entered
            Toast toast= Toast.makeText(getApplicationContext(), "No state 1 name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (state2.equals("")){
            //Both state 2 haven't been entered
            Toast toast= Toast.makeText(getApplicationContext(), "No state 2 name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            //Ignore case
            //Change first letter to upper case
            if(Character.isLowerCase(state1.charAt(0))){
                StringBuilder newString = new StringBuilder(state1);
                newString.setCharAt(0, Character.toUpperCase(state1.charAt(0)));
                state1 = newString.toString();
            }
            if(Character.isLowerCase(state2.charAt(0))){
                StringBuilder newString = new StringBuilder(state2);
                newString.setCharAt(0, Character.toUpperCase(state2.charAt(0)));
                state2 = newString.toString();
            }
            if(!state3.equals("")) {//If state3 exists
                if (Character.isLowerCase(state3.charAt(0))){
                    StringBuilder newString = new StringBuilder(state3);
                    newString.setCharAt(0, Character.toUpperCase(state3.charAt(0)));
                    state3 = newString.toString();
                }
            }
            if(!state4.equals("")) {//If state4 exists
                if (Character.isLowerCase(state4.charAt(0))){
                    StringBuilder newString = new StringBuilder(state4);
                    newString.setCharAt(0, Character.toUpperCase(state4.charAt(0)));
                    state4 = newString.toString();
                }
            }

            ArrayList<String> allState = new ArrayList<String>();
            allState.add(0, state1);
            allState.add(1, state2);
            if(!state3.equals("")) {
                allState.add(2, state3);
            }
            if(!state4.equals("")) {
                allState.add(3, state4);
            }

            //To next activity
            Intent intent = new Intent(this, FSMScaningActivity.class);
            intent.putStringArrayListExtra("stateList", allState);
            startActivity(intent);
        }
    }
}
