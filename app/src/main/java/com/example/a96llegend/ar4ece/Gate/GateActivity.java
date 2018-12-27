package com.example.a96llegend.ar4ece.Gate;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.a96llegend.ar4ece.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GateActivity extends Activity {

    //Views
    private static SurfaceView mCameraView;
    private static SurfaceView mTransparentView;
    private static TextView mTextView;
    private static ImageView mImageView;
    private static Button switchButton;
    private static Button xButton;
    private static Button yButton;
    private static Button resetButton;
    private static CameraSource mCameraSource;
    private static TextFrameProcessor txtFrameProcessor;
    private static TextView xTextView;
    private static TextView yTextView;
    private static TextView fTextView;

    //Constant
    private static LogicGateHandler gateLogic = new LogicGateHandler();
    private static final String instruction = "Please place the component into the search area";
    private enum OverlayMode {TRANSISTORS, TRUETABLE};
    private static OverlayMode currentMode;
    private static float[] block = {0,0,0}; //X,Y,and height
    private static float[] roi = {0,0,0,0}; //top,right,bottom,left

    //=============================================================
    //Lock for stop updating the what gate currently scanning (ensure stable view)
    private static boolean textViewLock;
    private static Handler textViewLockHandler = new Handler();
    private static Runnable textViewLockTimer = new Runnable() {
        @Override
        public void run() {
            textViewLock = false;
            currentMode = OverlayMode.TRUETABLE;
            mTextView.setText(instruction);
            mImageView.setVisibility(View.GONE);
            switchButton.setVisibility(View.GONE);
            xButton.setVisibility(View.GONE);
            yButton.setVisibility(View.GONE);
            resetButton.setVisibility(View.GONE);
            xTextView.setVisibility(View.GONE);
            yTextView.setVisibility(View.GONE);
            fTextView.setVisibility(View.GONE);
        }
    };
    //=============================================================
    private void updateCurrentGate(String newGate){
        if (newGate == null) {
            if (!textViewLock){
                gateLogic.updateCurrentGate("");
                currentMode = OverlayMode.TRUETABLE;
                switchButton.setText(R.string.gate_toTransistors);
            }
        } else {
            if (newGate.equals(gateLogic.getCurrentGate())) {
                gateLogic.updateCurrentGate(newGate);
                textViewLockHandler.removeCallbacks(textViewLockTimer);
                textViewLock = true;
                textViewLockHandler.postDelayed(textViewLockTimer, 1000);
            } else {
                if (!textViewLock) {
                    gateLogic.updateCurrentGate(newGate);
                    textViewLockHandler.removeCallbacks(textViewLockTimer);
                    textViewLock = true;
                    textViewLockHandler.postDelayed(textViewLockTimer, 1000);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_gate);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get all the view, so that they can change during runtime
        mCameraView = (SurfaceView)findViewById(R.id.CameraView);
        mTextView = (TextView)findViewById(R.id.text_view);
        mTransparentView = (SurfaceView)findViewById(R.id.TransparentView);
        mImageView = (ImageView) findViewById(R.id.truthTable);
        switchButton = (Button) findViewById(R.id.switchButton);
        xButton = (Button) findViewById(R.id.button_x);
        yButton = (Button) findViewById(R.id.button_y);
        resetButton = (Button) findViewById(R.id.button_reset);
        xTextView = (TextView) findViewById(R.id.xText);
        yTextView = (TextView) findViewById(R.id.yText);
        fTextView = (TextView) findViewById(R.id.fText);

        //Draw initial view
        mTextView.setText(instruction);
        mImageView.setVisibility(View.GONE);
        switchButton.setText(R.string.gate_toTransistors);
        switchButton.setVisibility(View.GONE);
        xButton.setVisibility(View.GONE);
        yButton.setVisibility(View.GONE);
        resetButton.setVisibility(View.GONE);
        xTextView.setVisibility(View.GONE);
        yTextView.setVisibility(View.GONE);
        fTextView.setVisibility(View.GONE);

        startTransparentView();
        startCameraSource();
    }

    @Override
    protected void onResume(){
        super.onResume();
        startTransparentView();
    }

    private void startTransparentView(){
        SurfaceHolder drawingHolder = mTransparentView.getHolder();
        drawingHolder.setFormat(PixelFormat.TRANSPARENT);

        drawingHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                //SetUp Canvas
                Canvas canvas = holder.lockCanvas();
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);

                //Border's properties
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(3);

                //Set roi region
                float top = canvas.getHeight() / 6;
                float left = top * 2;
                float bottom = top * 5;
                float right = bottom + top;
                roi[0] = top;
                roi[1] = right;
                roi[2] = bottom;
                roi[3] = left;
                txtFrameProcessor.setRoi(Math.round(left), Math.round(top), Math.round(bottom - top));

                //Draw
                canvas.drawRect(left, top, right, bottom, paint);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    private void startCameraSource() {

        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        txtFrameProcessor = new TextFrameProcessor(textRecognizer);

        if (!textRecognizer.isOperational()) {
            Log.w("Text", "Detector dependencies not loaded yet");
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), txtFrameProcessor)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    //.setRequestedPreviewSize(1280, 1024)
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
                    //Draw camera view
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(GateActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    101);
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
            txtFrameProcessor.setProcessor(new Detector.Processor<TextBlock>() {

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

                    //Reset before scanning
                    updateCurrentGate(null);

                    if (items.size() != 0 ){
                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                //StringBuilder stringBuilder = new StringBuilder();
                                //if (items.size() == 0){
                                    /*stringBuilder.append("Pleas place the component");
                                    stringBuilder.append("\n");
                                    stringBuilder.append("+into the search area");
                                    stringBuilder.append("\n");*/

                                //} else {
                                if (items.size() != 0) {
                                    //Find what gate and where x, y, f is
                                    for (int i = 0; i < items.size(); i++) {
                                        TextBlock item = items.valueAt(i);
                                        if (item.getValue().equals("AND")) {
                                            updateCurrentGate("AND");
                                            block[0] = item.getBoundingBox().left;
                                            block[1] = item.getBoundingBox().top;
                                            block[2] = (item.getBoundingBox().bottom) - (item.getBoundingBox().top);
                                            Log.i("Position", "##########################");
                                        } else if (item.getValue().equals("NOT")) {
                                            updateCurrentGate("NOT");
                                            block[0] = item.getBoundingBox().left;
                                            block[1] = item.getBoundingBox().top;
                                            block[2] = (item.getBoundingBox().bottom) - (item.getBoundingBox().top);
                                            Log.i("Position", "##########################");
                                        } else if (item.getValue().equals("OR")) {
                                            updateCurrentGate("OR");
                                            block[0] = item.getBoundingBox().left;
                                            block[1] = item.getBoundingBox().top;
                                            block[2] = (item.getBoundingBox().bottom) - (item.getBoundingBox().top);
                                            Log.i("Position", "##########################");
                                        }
                                    }
                                }
                                upDateView();
                                //mTextView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }

    //Change view according to what is found
    private void upDateView(){
        setOverlay();

        if (gateLogic.getCurrentGate() == null) {
            currentMode = OverlayMode.TRUETABLE;
            mTextView.setText(instruction);
            mImageView.setVisibility(View.GONE);
            switchButton.setVisibility(View.GONE);
            xButton.setVisibility(View.GONE);
            yButton.setVisibility(View.GONE);
            resetButton.setVisibility(View.GONE);
            xTextView.setVisibility(View.GONE);
            yTextView.setVisibility(View.GONE);
            fTextView.setVisibility(View.GONE);

        } else {
            mImageView.setVisibility(View.VISIBLE);
            switchButton.setVisibility(View.VISIBLE);
            if (currentMode.equals(OverlayMode.TRANSISTORS)){
                switchButton.setText(R.string.gate_toTrueTable);
                xButton.setVisibility(View.VISIBLE);
                yButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE);
                xTextView.setVisibility(View.VISIBLE);
                yTextView.setVisibility(View.VISIBLE);
                fTextView.setVisibility(View.VISIBLE);
            } else {
                switchButton.setText(R.string.gate_toTransistors);
                xButton.setVisibility(View.GONE);
                yButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.GONE);
                xTextView.setVisibility(View.GONE);
                yTextView.setVisibility(View.GONE);
                fTextView.setVisibility(View.GONE);
            }


            switch(gateLogic.getCurrentGate()) {
                case "OR":
                    mTextView.setText("OR GATE");
                    if (currentMode == OverlayMode.TRUETABLE){
                        mImageView.setImageResource(R.drawable.or_truth);
                    } else {
                        yButton.setVisibility(View.VISIBLE);
                        mImageView.setImageResource(gateLogic.getGraph());
                    }
                    break;
                case "AND":
                    mTextView.setText("AND GATE");
                    if (currentMode == OverlayMode.TRUETABLE){
                        mImageView.setImageResource(R.drawable.and_truth);
                    } else {
                        yButton.setVisibility(View.VISIBLE);
                        mImageView.setImageResource(gateLogic.getGraph());
                    }
                    break;
                default:
                    mTextView.setText("NOT GATE");
                    yTextView.setVisibility(View.GONE);
                    if (currentMode == OverlayMode.TRUETABLE){
                        mImageView.setImageResource(R.drawable.not_truth);
                    } else {
                        yButton.setVisibility(View.GONE);
                        mImageView.setImageResource(gateLogic.getGraph());
                    }
                    break;
            }
        }
    }

    public void switchMode(View view){
        if (currentMode.equals(OverlayMode.TRUETABLE)){
            currentMode = OverlayMode.TRANSISTORS;
        } else {
            currentMode = OverlayMode.TRUETABLE;
        }
        upDateView();
    }

    public void changeX(View view){
        gateLogic.pressX();
        upDateView();
    }

    public void changeY(View view){
        gateLogic.pressY();
        upDateView();
    }

    public void pressReset(View view){
        gateLogic.pressReset();
        upDateView();
    }

    //Determine where to put the over lay message
    private void setOverlay(){

        if (gateLogic.getCurrentGate()!= null && gateLogic.getCurrentGate().equals("NOT")){ //Only show x and f
            xTextView.setX(roi[3] - 70);
            xTextView.setY(roi[0] + block[1] + (block[2] * 1.9f));
        } else { //Other
            xTextView.setX(roi[3] - 70);
            xTextView.setY(roi[0] + block[1] + (block[2] * 1f));
            yTextView.setX(roi[3] - 65);
            yTextView.setY(roi[0] + block[1] + (block[2] * 3f));
        }

        //y same for everybody
        fTextView.setX(roi[1] + 15);
        fTextView.setY(roi[0] + block[1] + (block[2] * 1.9f));

        //Set value
        xTextView.setText("x=" + gateLogic.getX());
        yTextView.setText("y=" + gateLogic.getY());
        fTextView.setText("F=" + gateLogic.getF());
    }
}
