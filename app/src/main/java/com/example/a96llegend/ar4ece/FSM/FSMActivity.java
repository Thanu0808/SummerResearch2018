package com.example.a96llegend.ar4ece.FSM;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a96llegend.ar4ece.Gate.GateActivity;
import com.example.a96llegend.ar4ece.Gate.TextFrameProcessor;
import com.example.a96llegend.ar4ece.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import android.util.SparseArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//Manages text detection and captures the user’s input while running the FSM feature.
public class FSMActivity extends AppCompatActivity {

    //Constant
    private static final String tag = "=======Debug=======";
    private static FSMModel fsm;

    //View
    private static DisplayMetrics displayMetrics = new DisplayMetrics();
    private static SurfaceView mCameraView;
    private static CameraSource mCameraSource;
    private static ImageView arrowView;
    private static ImageView locatorView;
    private static ImageView locatorView2;
    private static ImageView mAnimationView;
    private static Button input1Button;
    private static Button input2Button;
    private static Button input3Button;
    private static Button input4Button;

    //Lists for storing components' position
    //All states. note: first one always is the first state
    private static Map<String, Rect> states = new HashMap<String, Rect>();
    //All conditions at top zone
    private static Map<String, Rect> topZone = new HashMap<String, Rect>();
    //All conditions at bottom zone
    private static Map<String, Rect> bottomZone = new HashMap<String, Rect>();
    private static boolean stateMatch;

    //Animation
    private static Animation animationFadeIn;
    private static Animation animationFadeOut;
    private static boolean runAnimation;
    private static AnimationDrawable animationManger;
    private static Handler animationHandler = new Handler();
    private static int animationStep; //1 = origin to condition, 2 = condition to destination
    private static List<Float[]> animationPath = new ArrayList<Float[]>();

    //Sound effect
    private static SoundPlayer soundPlayer;
    private static int currentStreamID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fsm);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCameraView = (SurfaceView) findViewById(R.id.CameraView);
        arrowView = (ImageView) findViewById(R.id.arrow);
        locatorView = (ImageView) findViewById(R.id.locator);
        locatorView2 = (ImageView) findViewById(R.id.locator2);
        mAnimationView = (ImageView) findViewById(R.id.AnimationView);
        input1Button = (Button) findViewById(R.id.input1);
        input2Button = (Button) findViewById(R.id.input2);
        input3Button = (Button) findViewById(R.id.input3);
        input4Button = (Button) findViewById(R.id.input4);

        //Initialise animation
        runAnimation = false;
        mAnimationView.setVisibility(View.INVISIBLE);
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        //Initialise FSM
        //
        List<String> equationList = (ArrayList<String>) getIntent().getStringArrayListExtra("equationList");//from scanning activity
        Log.d(tag, "equationList is " + equationList);
        fsm = new FSMModel(equationList);

        arrowView.setVisibility(View.GONE);
        //locatorView.setVisibility(View.GONE);
        setButtons();

        //Sound
        soundPlayer = new SoundPlayer(this);

        //Start camera
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        startCameraSource();
    }

    @Override
    protected void onResume(){
        super.onResume();
        runAnimation= false;
        fsm.reset();
        setButtons();
        arrowView.setVisibility(View.GONE);
        mAnimationView.setVisibility(View.GONE);
        setButtons();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Stop sound when return is pressed
        states.clear();
        topZone.clear();
        bottomZone.clear();
        soundPlayer.stopSound(currentStreamID);
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

                            ActivityCompat.requestPermissions(FSMActivity.this,
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

                    if (items.size() != 0 ){
                        for(int i=0;i<items.size();i++){
                            TextBlock currentItem = items.valueAt(i);
//                            if(currentItem.getValue().equals(fsm.getAllStateName().get(0))){
//                                states.put(currentItem.getValue(), currentItem.getBoundingBox()); //State 1
//                            } else if (currentItem.getValue().equals(fsm.getAllStateName().get(1))){
//                                states.put(currentItem.getValue(), currentItem.getBoundingBox()); //State 2
//                            } else {
//                                if (currentItem.getValue().indexOf("Start") == -1){ //Ignore "Start"
//                                    conditions.add(currentItem);
//                                }
//                            }
                            stateMatch = true;
                            for(int j=0;j<fsm.getAllStateName().size();j++){
                                if(currentItem.getValue().equals(fsm.getAllStateName().get(j))){
                                    states.put(currentItem.getValue(), currentItem.getBoundingBox());
                                    stateMatch = false;
                                }
                            }
                            if(stateMatch){
                                if (currentItem.getValue().indexOf("Start") == -1) { //Ignore "Start"
                                    conditions.add(currentItem);
                                }
                            }
                        }



                        //Now where ths condition is locate at, If states are not found, don't do it
                        if(states.get(fsm.getAllStateName().get(0)).top != 0 && states.get(fsm.getAllStateName().get(1)).top != 0) {
                            setOverlay();
                            //Determine zone boundary
                            Rect firstState = states.get(fsm.getAllStateName().get(0));
                            Rect secondState = states.get(fsm.getAllStateName().get(1));
                            int leftBoundary = firstState.right;
                            int rightBoundary = secondState.left;
                            int centreBoundary = (firstState.centerY() + firstState.centerY()) / 2;

                            for (int i = 0; i < conditions.size(); i++) {
                                Rect currentBlock = conditions.get(i).getBoundingBox();
                                String name = conditions.get(i).getValue();

                                if (currentBlock.centerX() > leftBoundary && currentBlock.centerX() < rightBoundary) {
                                    if (currentBlock.centerY() < centreBoundary) {
                                        topZone.put(name, currentBlock);
                                    } else if (currentBlock.centerY() > centreBoundary) {
                                        bottomZone.put(name, currentBlock);
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    //=====================================View===============================================
    private void setOverlay(){
        if (runAnimation){ //Arrow when running animation
//            int offset = states.get(fsm.getAllStateName().get(0)).right-states.get(fsm.getAllStateName().get(0)).left;
//            locatorView.setX(states.get(fsm.getAllStateName().get(0)).right+offset);
//            locatorView.setY(states.get(fsm.getAllStateName().get(0)).centerY());
//
//            int offset2 = states.get(fsm.getAllStateName().get(1)).right-states.get(fsm.getAllStateName().get(1)).left;
//            locatorView2.setX(states.get(fsm.getAllStateName().get(1)).right+offset2);
//            locatorView2.setY(states.get(fsm.getAllStateName().get(1)).centerY());

            //Use UI thread to turn on the arrow
            Activity uiActivity = (Activity)FSMActivity.this;
            uiActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    arrowView.setVisibility(View.GONE);
                } });
        } else {
            Rect currentStateBlock = states.get(fsm.getCurrentState());
            int length = currentStateBlock.right - currentStateBlock.left;
            arrowView.setX(currentStateBlock.centerX());
            arrowView.setY(currentStateBlock.centerY() + (length * 0.5f));

            //Use UI thread to turn on the arrow
            Activity uiActivity = (Activity)FSMActivity.this;
            uiActivity.runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    arrowView.setVisibility(View.VISIBLE);
                } });
            }
        }


    //==================================Buttons================================================
    //Translate boolean value, true = 1, false = 0
    private static int booleanToInt(boolean value){
        if(value){
            return 1;
        } else {
            return 0;
        }
    }

    //Maximum of 4 inputs, if not all are used, hide some. And put initial text on the button
    private static void setButtons(){
        List<String> allInput = fsm.getAllInputName();
        if(allInput.size() > 0 && allInput.get(0) != null){
            input1Button.setText(allInput.get(0) + "=" + booleanToInt(fsm.getInputValueByName(allInput.get(0))));
        } else {
            input1Button.setVisibility(View.GONE);
        }
        if(allInput.size() > 1 && allInput.get(1) != null) {
            input2Button.setText(allInput.get(1) + "=" + booleanToInt(fsm.getInputValueByName(allInput.get(1))));
        } else {
            input2Button.setVisibility(View.GONE);
        }
        if(allInput.size() > 2 && allInput.get(2) != null) {
            input3Button.setText(allInput.get(2) + "=" + booleanToInt(fsm.getInputValueByName(allInput.get(2))));
        }  else {
            input3Button.setVisibility(View.GONE);
        }
        if(allInput.size() > 3 && allInput.get(3) != null) {
            input4Button.setText(allInput.get(3) + "=" + booleanToInt(fsm.getInputValueByName(allInput.get(3))));
        } else {
            input4Button.setVisibility(View.GONE);
        }
    }

    //Button for input1
    public void pressInput1 (View view){
        if(!runAnimation) { //Disable input change when running animation
            String name = fsm.getAllInputName().get(0);
            input1Button.setText(name + "=" + booleanToInt(fsm.triggerInputChangeAndGetNewValue(name)));
        }
    }

    //Button for input2
    public void pressInput2 (View view){
        if(!runAnimation) { //Disable input change when running animation
            String name = fsm.getAllInputName().get(1);
            input2Button.setText(name + "=" + booleanToInt(fsm.triggerInputChangeAndGetNewValue(name)));
        }
    }

    //Button for input3
    public void pressInput3 (View view){
        if(!runAnimation) { //Disable input change when running animation
            String name = fsm.getAllInputName().get(2);
            input3Button.setText(name + "=" + booleanToInt(fsm.triggerInputChangeAndGetNewValue(name)));
        }
    }

    //Button for input4
    public void pressInput4 (View view){
        if(!runAnimation) { //Disable input change when running animation
            String name = fsm.getAllInputName().get(3);
            input4Button.setText(name + "=" + booleanToInt(fsm.triggerInputChangeAndGetNewValue(name)));
        }
    }

    //Button for trigger next state
    public void pressNext (View view){
        if(!runAnimation) {
            int oldState = fsm.getCurrentStateAsIndex();
            String conditionToFind = fsm.triggerStateChange();
            int newState = fsm.getCurrentStateAsIndex();
           // Log.d(tag, "Test 0");
            if(oldState!=newState){
             //   Log.d(tag, "Test 1");
                animationStep=0;
                mAnimationView.setX(states.get(fsm.getAllStateName().get(oldState)).centerX() - 40);
                mAnimationView.setY(states.get(fsm.getAllStateName().get(oldState)).centerY() - 40);
             //   Log.d(tag, "Test 2");
                //If condition = "-", set the middle point in the middle, so that travel in a straight line
                if(conditionToFind.equals("-")){
//                    Log.d(tag, "Test 3,New State is" + Integer.toString(newState));
                   // Log.d(tag, "X is:" + Integer.toString(states.get(fsm.getAllStateName().get(oldState)).centerX()));
                  //  Log.d(tag, "X is:" + Integer.toString(states.get(fsm.getAllStateName().get(newState)).centerX()));
                    int x = (states.get(fsm.getAllStateName().get(oldState)).centerX()+
                            states.get(fsm.getAllStateName().get(newState)).centerX()) / 2;
              //      Log.d(tag, "Test X");
                    int y = (states.get(fsm.getAllStateName().get(oldState)).centerY()+
                            states.get(fsm.getAllStateName().get(newState)).centerY()) / 2;
              //      Log.d(tag, "Test Y");
                    Rect middle = new Rect(x - 20, y - 20, x + 20, y + 20);
              //      Log.d(tag, "Test 4");
                    animationPath = MathToolBox.pathCalculator(states.get(fsm.getAllStateName().get(oldState)),
                            middle, states.get(fsm.getAllStateName().get(newState)));
             //       Log.d(tag, "Test 5");
                } else {//TT
               //     Log.d(tag, "WTF");
                    animationPath = MathToolBox.pathCalculator(states.get(fsm.getAllStateName().get(0)),
                            topZone.get(conditionToFind), states.get(fsm.getAllStateName().get(1)));//THIS iS CAUSING CRASH CAUSE NOT BOTTOM ZONE ON 2nd part
                }
              //  Log.d(tag, "Test 6");
                startAnimation(0); //Left to right TT
                //Log.d(tag, "Test 7");
 //           }
            //---------------------Determine what should be run
//            if (oldState == 0 && newState == 1) {
//                animationStep = 0;
//                mAnimationView.setX(states.get(fsm.getAllStateName().get(0)).centerX() - 20);
//                mAnimationView.setY(states.get(fsm.getAllStateName().get(0)).centerY() - 20);
//
//                //If condition = "-", set the middle point in the middle, so that travel in a straight line
//                if(conditionToFind.equals("-")){
//                    int x = (states.get(fsm.getAllStateName().get(1)).centerX()+
//                            states.get(fsm.getAllStateName().get(0)).centerX()) / 2;
//                    int y = (states.get(fsm.getAllStateName().get(1)).centerY()+
//                            states.get(fsm.getAllStateName().get(0)).centerY()) / 2;
//                    Rect middle = new Rect(x - 20, y - 20, x + 20, y + 20);
//                    animationPath = MathToolBox.pathCalculator(states.get(fsm.getAllStateName().get(0)),
//                            middle, states.get(fsm.getAllStateName().get(1)));
//                } else {
//                    animationPath = MathToolBox.pathCalculator(states.get(fsm.getAllStateName().get(0)),
//                            topZone.get(conditionToFind), states.get(fsm.getAllStateName().get(1)));
//                }
//                startAnimation(0); //Left to right
//
//            } else if (oldState == 1 && newState == 0) {
//                animationStep = 0;
//                mAnimationView.setX(states.get(fsm.getAllStateName().get(1)).centerX() - 40);
//                mAnimationView.setY(states.get(fsm.getAllStateName().get(1)).centerY() - 40);
//
//                //If condition = "-", set the middle point in the middle, so that travel in a straight line
//                if(conditionToFind.equals("-")){
//                    int x = (states.get(fsm.getAllStateName().get(1)).centerX()+
//                            states.get(fsm.getAllStateName().get(0)).centerX()) / 2;
//                    int y = (states.get(fsm.getAllStateName().get(1)).centerY()+
//                            states.get(fsm.getAllStateName().get(0)).centerY()) / 2;
//                    Rect middle = new Rect(x - 20, y - 20, x + 20, y + 20);
//                    animationPath = MathToolBox.pathCalculator(states.get(fsm.getAllStateName().get(1)),
//                            middle, states.get(fsm.getAllStateName().get(0)));
//                } else {
//                    animationPath = MathToolBox.pathCalculator(states.get(fsm.getAllStateName().get(1)),
//                            bottomZone.get(conditionToFind), states.get(fsm.getAllStateName().get(0)));
//                }
//                startAnimation(1); //Right to left

            } else if (oldState == 1 && newState == 1){ //state at state 2
                animationStep = 0;
                mAnimationView.setX(states.get(fsm.getAllStateName().get(1)).right - 40);
                mAnimationView.setY(states.get(fsm.getAllStateName().get(1)).centerY() - 40);
                animationPath = MathToolBox.pathForNotStateChange(states.get(fsm.getAllStateName().get(1)), false);
                startAnimation(1);

            } else if (oldState == 0 && newState == 0){ //state at state 1
                animationStep = 0;
                mAnimationView.setX(states.get(fsm.getAllStateName().get(0)).left - 40);
                mAnimationView.setY(states.get(fsm.getAllStateName().get(0)).centerY() - 40);
                animationPath = MathToolBox.pathForNotStateChange(states.get(fsm.getAllStateName().get(0)), true);
                startAnimation(0);
            }
        }
    }

    //Button for reset
    public void pressReset (View view){
        if(!runAnimation) { //Disable reset when running animation
            fsm.reset();
            setButtons();
        }
    }

    //=====================================Animation================================
    private void startAnimation(int direction){
        runAnimation = true;
        mAnimationView.setVisibility(View.VISIBLE);

        //Determine initial position, 0 = L2R, 1 = R2L
        if(direction == 0){
            mAnimationView.setBackgroundResource(R.drawable.animation_lr);
        } else {
            mAnimationView.setBackgroundResource(R.drawable.animation_rl);
        }

        //Start playing sound
        currentStreamID = soundPlayer.playShortResource(R.raw.waiting);

        //Fade in animation
        mAnimationView.startAnimation(animationFadeIn);

        //Wait for fade in finish first
        final Handler fadeDelayHandler = new Handler();
        fadeDelayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationManger = (AnimationDrawable)  mAnimationView.getBackground();
                animationManger.start();
                animationHandler.postDelayed(animationTimer, 200);
            }
        }, 1000);
    }

    //Animation handler
    private static Runnable animationTimer = new Runnable() {
        @Override
        public void run() {
            mAnimationView.setX(animationPath.get(animationStep)[0] - 40f);
            mAnimationView.setY(animationPath.get(animationStep)[1] - 40f);
            animationStep = animationStep + 1;

            //If new X and Y are same as old X and Y, means no change, on target, stop now
            if(animationStep == animationPath.size()){
                animationManger.stop();
                animationHandler.removeCallbacks(this);
                animationStep = 0;
                mAnimationView.startAnimation(animationFadeOut);

                //Wait for fade out finish first
                final Handler fadeDelayHandler = new Handler();
                fadeDelayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAnimationView.setVisibility(View.INVISIBLE);
                        soundPlayer.stopSound(currentStreamID);
                        runAnimation = false;
                    }
                }, 1000);
            } else {
                animationHandler.postDelayed(this, 100);
            }
        }
    };


}
