package com.example.a96llegend.ar4ece.Resistor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import com.example.a96llegend.ar4ece.Gate.GateActivity;
import com.example.a96llegend.ar4ece.R;

public class ResistorActivity extends Activity implements CvCameraViewListener2{

    static {
        OpenCVLoader.initDebug();
    }

    private ResistorCameraView _resistorCameraView;
    private ResistorImageProcessor _resistorProcessor;

    private BaseLoaderCallback _loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    _resistorCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resistor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        _resistorCameraView = (ResistorCameraView) findViewById(R.id.ResistorCameraView);
        _resistorCameraView.setVisibility(SurfaceView.VISIBLE);
        _resistorCameraView.setZoomControl((SeekBar) findViewById(R.id.CameraZoomControls));
        _resistorCameraView.setCvCameraViewListener(this);
        _resistorCameraView.setMaxFrameSize(480,320);
        _resistorProcessor = new ResistorImageProcessor();

        SharedPreferences settings = getPreferences(0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Position the green line over the colour bands of the" +
                "resistor, and keep the forth band out")
                .setTitle("How to scan resistors")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
        SharedPreferences.Editor editor = settings.edit();
        editor.apply();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (_resistorCameraView != null)
            _resistorCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (_resistorCameraView != null)
            _resistorCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        return _resistorProcessor.processFrame(inputFrame);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        _loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }
}
