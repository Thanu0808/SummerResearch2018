package com.example.a96llegend.ar4ece.FSM;

import android.Manifest;
import android.app.Activity;
import android.arch.core.internal.FastSafeIterableMap;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.example.a96llegend.ar4ece.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//Manages text detection when the app is scanning the FSM from the paper
public class FSMScaningActivity extends AppCompatActivity {

    private static final String tag = "=======Debug=======";
    private static final DisplayMetrics displayMetrics = new DisplayMetrics();
    private static SurfaceView mCameraView;
    private static CameraSource mCameraSource;

    //FSM data
    private static List<String> statesName = new ArrayList<String>();
    private static List<String> booleanEquations = new ArrayList<String>();
    private static List<Rect> stateLocation = new ArrayList<Rect>();
    private static int numberOfStates;
    private static boolean stateMatch;

    //Delay for better UX
    private static boolean scanLock;
    private static Handler scanLockHandler = new Handler();
    private static Runnable scanLockTimer = new Runnable() {
        @Override
        public void run() {
            scanLock = false;
        }
    };

    private static String avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fsmscaning);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCameraView = (SurfaceView) findViewById(R.id.CameraView);

        //Initialise
        //Get stateList from intent in StateEnteringActivity(the entered states from user)
        statesName = (ArrayList<String>) getIntent().getStringArrayListExtra("stateList");
        numberOfStates = statesName.size();
        avatar = getIntent().getStringExtra("avatar");
        //Default conditions for state transitions
        for (int i = 0; i < statesName.size()-1; i++) {
            booleanEquations.add(i, statesName.get(i) + "\u2192" + statesName.get(i+1));
        }
        booleanEquations.add(statesName.size()-1,statesName.get(statesName.size()-1) + "\u2192" + statesName.get(0));


        //Start camera
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        startCameraSource();

        //Delay for 0.5 second
        scanLockHandler.removeCallbacks(scanLockTimer);
        scanLock = true;
        scanLockHandler.postDelayed(scanLockTimer, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        booleanEquations.clear();
        stateLocation.clear();
        scanLock = false;
    }

    private void startCameraSource() {
        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(tag, "Detector dependencies not loaded yet");
        } else {
            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(FSMScaningActivity.this,
                                    new String[]{Manifest.permission.CAMERA}, 101);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                /**
                 * Release resources for cameraSource
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    List<TextBlock> conditions = new ArrayList<TextBlock>();
                    Rect initial = new Rect(8, 8, 8, 8);
                    //initialising stateLocation list
                    for(int k=0;k<numberOfStates;k++){
                        stateLocation.add(k,initial);
                    }
                    if (items.size() != 0 && scanLock != true){
                        for(int i=0;i<items.size();i++){
                            TextBlock currentItem = items.valueAt(i);
                            //Find state position
                            //Store State locations and conditions
                            stateMatch = true;
                            for(int j=0;j<numberOfStates;j++){
                                if(currentItem.getValue().equals(statesName.get(j))){
                                    stateLocation.remove(j);
                                    stateLocation.add(j,currentItem.getBoundingBox());
                                    stateMatch = false;
                                }
                            }
                            if(stateMatch){
                                conditions.add(currentItem);
                            }
                        }

                        //Search for location
                        if (stateLocation.size() != 0){
                            Rect firstState = stateLocation.get(0);
                            Rect secondState = stateLocation.get(1);
                            if(statesName.size()==2) { //For 2 states
                                //Determine zone boundary
                                int leftBoundary = firstState.right;
                                int rightBoundary = secondState.left;
                                int centreBoundary = (firstState.centerY() + secondState.centerY()) / 2;
                                boolean noConditionTop = true;
                                boolean noConditionBottom = true;
                                for (int i = 0; i < conditions.size(); i++) {
                                    Rect currentBlock = conditions.get(i).getBoundingBox();
                                    String name = conditions.get(i).getValue();
                                    if (currentBlock.centerX() > leftBoundary && currentBlock.centerX() < rightBoundary) {
                                        if (currentBlock.centerY() < centreBoundary) {
                                            //Condition for state one to state two
                                            if (noConditionTop) {
                                                noConditionTop = false;
                                                booleanEquations.remove(0);
                                                booleanEquations.add(0, statesName.get(0) + "+" + name + "\u2192" + statesName.get(1));
                                            } else {
                                                booleanEquations.add(statesName.get(0) + "+" + name + "\u2192" + statesName.get(1));
                                            }
                                        } else if (currentBlock.centerY() > centreBoundary) {
                                            //Condition for state two to state one
                                            if (noConditionBottom) {
                                                noConditionBottom = false;
                                                booleanEquations.remove(1);
                                                booleanEquations.add(1, statesName.get(1) + "+" + name + "\u2192" + statesName.get(0));
                                            } else {
                                                booleanEquations.add(statesName.get(1) + "+" + name + "\u2192" + statesName.get(0));
                                            }
                                        }
                                    }
                                }
                            } else if(statesName.size()==3){
                                Rect thirdState = stateLocation.get(2);
                                //Zone Boundaries
                                int midXBoundary = secondState.centerX();
                                int bottomYBoundary = (firstState.centerY()+thirdState.centerY())/2;
                                int oneToTwoBoundaryX = (firstState.centerX()+secondState.centerX())/2;
                                int oneToTwoBoundaryY = (firstState.centerY()+secondState.centerY())/2;//Can be used as midY
                                int twoToThreeBoundaryX = (secondState.centerX()+thirdState.centerX())/2;
                                int twoToThreeBoundaryY = (secondState.centerY()+thirdState.centerY())/2;//Can be used as midY
                                int midYBoundary = (twoToThreeBoundaryY+oneToTwoBoundaryY)/2;
                                int lowMidYBoundary = (midYBoundary+bottomYBoundary)/2;//Used for 3to1 condition
                                boolean oneToTwo = true;
                                boolean twoToThree = true;
                                boolean threeToOne = true;
                                for (int i = 0; i < conditions.size(); i++) {
                                    Rect currentBlock = conditions.get(i).getBoundingBox();
                                    String name = conditions.get(i).getValue();
                                    if(currentBlock.centerX()<oneToTwoBoundaryX //&& currentBlock.centerY()>secondState.centerY()
                                            && currentBlock.centerY()<bottomYBoundary) {//State 1 to 2
                                        //oneToTwo set to false as initial condition is removed the first time
                                        if(oneToTwo){
                                            oneToTwo = false;
                                            booleanEquations.remove(0);
                                            booleanEquations.add(0,statesName.get(0)+"+"+name+"\u2192"+statesName.get(1));
                                        } else{
                                            booleanEquations.add(statesName.get(0)+"+"+name+"\u2192"+statesName.get(1));
                                        }
                                    } else if(currentBlock.centerX()>twoToThreeBoundaryX && currentBlock.centerY()<bottomYBoundary){//State 2 to 3
                                        if(twoToThree){
                                            twoToThree = false;
                                            booleanEquations.remove(1);
                                            booleanEquations.add(1,statesName.get(1)+"+"+name+"\u2192"+statesName.get(2));
                                        } else{
                                            booleanEquations.add(statesName.get(1)+"+"+name+"\u2192"+statesName.get(2));
                                        }
                                    } else if(currentBlock.centerY()>bottomYBoundary){//State 3 to 1
                                        if(threeToOne){
                                            threeToOne = false;
                                            booleanEquations.remove(2);
                                            booleanEquations.add(2, statesName.get(2)+"+"+name+"\u2192"+statesName.get(0));
                                        } else{
                                            booleanEquations.add(statesName.get(2)+"+"+name+"\u2192"+statesName.get(0));
                                        }

                                    } else if(currentBlock.centerX()>oneToTwoBoundaryX && currentBlock.centerX()<secondState.centerX()
                                            && currentBlock.centerY()<lowMidYBoundary){//State 2 to 1
                                        booleanEquations.add(statesName.get(1)+"+"+name+"\u2192"+statesName.get(0));
                                    } else if(currentBlock.centerX()<twoToThreeBoundaryX && currentBlock.centerX()>secondState.centerX()
                                            &&currentBlock.centerY()<lowMidYBoundary){//State 3 to 2
                                        booleanEquations.add(statesName.get(2)+"+"+name+"\u2192"+statesName.get(1));
                                    } else if(currentBlock.centerY()>lowMidYBoundary && currentBlock.centerY()<bottomYBoundary){//State 1 to 3
                                        booleanEquations.add(statesName.get(0)+"+"+name+"\u2192"+statesName.get(2));
                                    }
                                }
                            } else if(statesName.size()>3){
                                Rect thirdState = stateLocation.get(2);
                                Rect fourthState = stateLocation.get(3);
                                //Zone Boundaries
                                int midXBoundary = secondState.centerX();
                                int midYBoundary = (firstState.centerY()+thirdState.centerY())/2;
                                int leftBoundary = firstState.left;
                                int rightBoundary = thirdState.right;
                                int topBoundary = secondState.top;
                                int bottomBoundary = fourthState.bottom;
                                int oneToTwoBoundaryX = (firstState.centerX()+secondState.centerX())/2;
                                int oneToTwoBoundaryY = (firstState.centerY()+secondState.centerY())/2;
                                int twoToThreeBoundaryX = (secondState.centerX()+thirdState.centerX())/2;
                                int twoToThreeBoundaryY = (secondState.centerY()+thirdState.centerY())/2;
                                int threeToFourBoundaryX = (thirdState.centerX()+fourthState.centerX())/2;
                                int threeToFourBoundaryY = (thirdState.centerY()+fourthState.centerY())/2;
                                int fourToOneBoundaryX = (fourthState.centerX()+firstState.centerX())/2;
                                int fourToOneBoundaryY = (fourthState.centerY()+firstState.centerY())/2;
                                int offset = firstState.bottom-firstState.top;

                                boolean oneToTwo = true;
                                boolean twoToThree = true;
                                boolean threeToFour = true;
                                boolean fourToOne = true;
                                for (int i = 0; i < conditions.size(); i++) {
                                    Rect currentBlock = conditions.get(i).getBoundingBox();
                                    String name = conditions.get(i).getValue();
                                    if(currentBlock.centerX()<oneToTwoBoundaryX && currentBlock.centerY()<midYBoundary){//State 1 to 2
                                        if(oneToTwo){
                                            oneToTwo = false;
                                            booleanEquations.remove(0);
                                            booleanEquations.add(0,statesName.get(0)+"+"+name+"\u2192"+statesName.get(1));
                                        } else{
                                            booleanEquations.add(0,statesName.get(0)+"+"+name+"\u2192"+statesName.get(1));
                                        }
                                    } else if(currentBlock.centerX()>twoToThreeBoundaryX && currentBlock.centerY()<midYBoundary){//State 2 to 3
                                        if(twoToThree){
                                            twoToThree = false;
                                            booleanEquations.remove(1);
                                            booleanEquations.add(1,statesName.get(1)+"+"+name+"\u2192"+statesName.get(2));
                                        }else{
                                            booleanEquations.add(1,statesName.get(1)+"+"+name+"\u2192"+statesName.get(2));
                                        }
                                    } else if(currentBlock.centerX()>threeToFourBoundaryX && currentBlock.centerY()>midYBoundary){//State 3 to 4
                                        if(threeToFour){
                                            threeToFour = false;
                                            booleanEquations.remove(2);
                                            booleanEquations.add(2,statesName.get(2)+"+"+name+"\u2192"+statesName.get(3));
                                        }else{
                                            booleanEquations.add(2,statesName.get(2)+"+"+name+"\u2192"+statesName.get(3));
                                        }
                                    } else if(currentBlock.centerX()<fourToOneBoundaryX && currentBlock.centerY()>midYBoundary) {//State 4 to 1
                                        if(fourToOne){
                                            fourToOne = false;
                                            booleanEquations.remove(3);
                                            booleanEquations.add(3,statesName.get(3)+"+"+name+"\u2192"+statesName.get(0));
                                        } else{
                                            booleanEquations.add(3,statesName.get(3)+"+"+name+"\u2192"+statesName.get(0));
                                        }
                                    } else if(currentBlock.centerX()>oneToTwoBoundaryX && currentBlock.centerX()<midXBoundary
                                            && currentBlock.centerY()>topBoundary && currentBlock.centerY()<firstState.top-offset) {//State 2 to 1
                                        booleanEquations.add(statesName.get(1)+"+"+name+"\u2192"+statesName.get(0));
                                    } else if(currentBlock.centerX()<twoToThreeBoundaryX && currentBlock.centerX()>midXBoundary
                                            && currentBlock.centerY()>topBoundary && currentBlock.centerY()<firstState.top-offset) {//State 3 to 2
                                        booleanEquations.add(statesName.get(2)+"+"+name+"\u2192"+statesName.get(1));
                                    } else if(currentBlock.centerX()>fourToOneBoundaryX && currentBlock.centerX()<midXBoundary
                                            && currentBlock.centerY()<bottomBoundary && currentBlock.centerY()>firstState.bottom+offset) {//State 1 to 4
                                        booleanEquations.add(statesName.get(0)+"+"+name+"\u2192"+statesName.get(3));
                                    } else if(currentBlock.centerX()<threeToFourBoundaryX && currentBlock.centerX()>midXBoundary
                                            && currentBlock.centerY()<bottomBoundary && currentBlock.centerY()>firstState.bottom+offset) {//State 4 to 3
                                        booleanEquations.add(statesName.get(3)+"+"+name+"\u2192"+statesName.get(2));
                                    } else if(currentBlock.centerX()>leftBoundary && currentBlock.centerX()<midXBoundary
                                            && currentBlock.centerY()>(firstState.top-offset) && currentBlock.centerY()<firstState.bottom+offset) {//State 4 to 2
                                        booleanEquations.add(statesName.get(3)+"+"+name+"\u2192"+statesName.get(1));
                                    } else if(currentBlock.centerX()<rightBoundary && currentBlock.centerX()>midXBoundary
                                            && currentBlock.centerY()>(firstState.top-offset) && currentBlock.centerY()<firstState.bottom+offset) {//State 2 to 4
                                        booleanEquations.add(statesName.get(1)+"+"+name+"\u2192"+statesName.get(3));
                                    } else if(currentBlock.centerY()<topBoundary) {//State 1 to 3
                                        booleanEquations.add(statesName.get(0)+"+"+name+"\u2192"+statesName.get(2));
                                    } else if(currentBlock.centerY()>bottomBoundary) {//State 3 to 1
                                        booleanEquations.add(statesName.get(2)+"+"+name+"\u2192"+statesName.get(0));
                                    }
                                }

                            }

                        }
                        //Ask user to confirm
                        //Use UI thread to turn on the arrow
                        Activity uiActivity = (Activity)FSMScaningActivity.this;
                        uiActivity.runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                scanLock = true;
                                showResultDialogue(returnAllEquations());
                            } });
                    }
                }
            });
        }
    }

    //Form one big string for the confirm dialog
    private String returnAllEquations(){
        String result = "";
        for(int i = 0; i < booleanEquations.size(); i++){
            result = result + "\n" + booleanEquations.get(i);
        }
        return result;
    }

    //method to construct dialogue with scan results
    private void showResultDialogue(String result) {
        AlertDialog.Builder builder;
        //change the appearance of the dialogue box depending on Android version of the phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //builder = new AlertDialog.Builder(FSMScaningActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            builder = new AlertDialog.Builder(FSMScaningActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(FSMScaningActivity.this);
        }

        //fill in the dialogue box created
        builder.setTitle("Scan Result")
                .setMessage("Transaction conditions are: \n" + result)
                .setPositiveButton("Correct", new DialogInterface.OnClickListener() { //continue after scan button on dialogue box
                    public void onClick(DialogInterface dialog, int which) {
                        // continue to FSM simulator
                        Intent myIntent = new Intent(FSMScaningActivity.this, FSMActivity.class);
                        myIntent.putStringArrayListExtra("equationList", sortBooleanEquation());
                        myIntent.putExtra("avatar", avatar);
                        FSMScaningActivity.this.startActivity(myIntent);
                        finish();
                    }
                })
                .setNegativeButton("Scan again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //scan again, clear existing data
                        booleanEquations.clear();
                        stateLocation.clear();

                        //Re-initialised

                        for (int i = 0; i < statesName.size()-1; i++) {
                            booleanEquations.add(i, statesName.get(i) + "\u2192" + statesName.get(i+1));
                        }
                        booleanEquations.add(statesName.size()-1,statesName.get(statesName.size()-1) + "\u2192" + statesName.get(0));

                        //Add delay
                        scanLockHandler.removeCallbacks(scanLockTimer);
                        scanLock = true;
                        scanLockHandler.postDelayed(scanLockTimer, 1000);
                    }
                })
                .show();
    }

    //Sort boolean equation, so that the first state always append first
    private ArrayList<String> sortBooleanEquation(){
        ArrayList<String> firstStateEquation = new ArrayList<String>();
        ArrayList<String> secondStateEquation = new ArrayList<String>();
        ArrayList<String> thirdStateEquation = new ArrayList<String>();
        ArrayList<String> fourthStateEquation = new ArrayList<String>();
        ArrayList<Integer> length = new ArrayList<Integer>();
        for(int j=0;j<statesName.size();j++){
            length.add(j,statesName.get(j).length());
        }

        for(int i = 0; i < booleanEquations.size(); i++){
           // int length = statesName.get(0).length();
            if(booleanEquations.get(i).substring(0, length.get(0)).equals(statesName.get(0))){
                firstStateEquation.add(booleanEquations.get(i));
            } else if (booleanEquations.get(i).substring(0, length.get(1)).equals(statesName.get(1))){
                secondStateEquation.add(booleanEquations.get(i));
            }
            if(length.size()>2){
                if(booleanEquations.get(i).substring(0, length.get(2)).equals(statesName.get(2))){
                    thirdStateEquation.add(booleanEquations.get(i));
                }
            }
            if(length.size()>3){
                if(booleanEquations.get(i).substring(0, length.get(3)).equals(statesName.get(3))){
                    fourthStateEquation.add(booleanEquations.get(i));
                }
            }
        }

        //Merge two list
        for (int i = 0; i < secondStateEquation.size(); i++){
            firstStateEquation.add(secondStateEquation.get(i));
        }
        for (int i = 0; i < thirdStateEquation.size(); i++){
            firstStateEquation.add(thirdStateEquation.get(i));
        }
        for (int i = 0; i < fourthStateEquation.size(); i++){
            firstStateEquation.add(fourthStateEquation.get(i));
        }
        return firstStateEquation;
    }
}
