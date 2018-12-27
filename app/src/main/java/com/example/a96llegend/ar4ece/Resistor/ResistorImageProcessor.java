package com.example.a96llegend.ar4ece.Resistor;

import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.List;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ResistorImageProcessor {
/*
    // HSV colour bounds //H 0 - 179, S&V 0 - 255
    private static final Scalar COLOR_BOUNDS[][] = {
            {new Scalar(0, 0, 0), new Scalar(180, 250, 50)},    // black
            {new Scalar(0, 90, 10), new Scalar(15, 250, 100)},    // brown
            {new Scalar(0, 0, 0), new Scalar(0, 0, 0)},         // red (defined by two bounds)
            {new Scalar(4, 100, 100), new Scalar(9, 250, 150)},   // orange
            {new Scalar(20, 130, 100), new Scalar(30, 250, 160)}, // yellow
            {new Scalar(45, 50, 60), new Scalar(72, 250, 150)},   // green
            {new Scalar(80, 50, 50), new Scalar(106, 250, 150)},  // blue
            {new Scalar(130, 40, 50), new Scalar(155, 250, 150)}, // purple
            {new Scalar(0, 0, 50), new Scalar(180, 50, 80)},       // gray
            {new Scalar(0, 0, 90), new Scalar(180, 15, 140)}      // white
    };

    // red wraps around in HSV, so we need two ranges
    private static Scalar LOWER_RED1 = new Scalar(0, 65, 100);
    private static Scalar UPPER_RED1 = new Scalar(2, 250, 150);
    private static Scalar LOWER_RED2 = new Scalar(171, 65, 50);
    private static Scalar UPPER_RED2 = new Scalar(180, 250, 150);

    //========================On Real=====================================
   private static final Scalar COLOR_BOUNDS[][] = {
            {new Scalar(0, 0, 0), new Scalar(180, 250, 50)},    // black
            {new Scalar(0, 90, 10), new Scalar(15, 250, 100)},    // brown
            {new Scalar(0, 0, 0), new Scalar(0, 0, 0)},         // red (defined by two bounds)
            {new Scalar(7, 170, 80), new Scalar(22, 250, 200)},   // orange
            {new Scalar(20, 130, 100), new Scalar(30, 250, 230)}, // yellow
            {new Scalar(30, 50, 60), new Scalar(80, 250, 150)},   // green
            {new Scalar(80, 50, 50), new Scalar(115, 255, 200)},  // blue
            {new Scalar(130, 40, 50), new Scalar(155, 250, 150)}, // purple
            {new Scalar(0, 0, 50), new Scalar(180, 50, 80)},       // gray
            {new Scalar(0, 0, 90), new Scalar(180, 15, 140)}      // white
    };

    // red wraps around in HSV, so we need two ranges
    private static Scalar LOWER_RED1 = new Scalar(0, 60, 80);
    private static Scalar UPPER_RED1 = new Scalar(10, 255, 220);
    private static Scalar LOWER_RED2 = new Scalar(160, 60, 80);
    private static Scalar UPPER_RED2 = new Scalar(180, 255, 200);*/
    //===============================================================================

    //========================On Paper(J5)=====================================
   private static final Scalar COLOR_BOUNDS[][] = {
            {new Scalar(0, 90, 0), new Scalar(15, 250, 100)},    // brown 17,185,120
            {new Scalar(7, 170, 80), new Scalar(22, 250, 200)},   // orange
            {new Scalar(0, 0, 0), new Scalar(0, 0, 0)},         // red 0,255,255
            {new Scalar(15, 95, 100), new Scalar(30, 255, 230)}, // yellow
            {new Scalar(30, 50, 60), new Scalar(80, 250, 150)},   // green
            {new Scalar(100, 50, 60), new Scalar(130, 250, 247)},  // blue 155,147,237
            {new Scalar(130, 40, 50), new Scalar(160, 250, 150)}, // purple
            {new Scalar(0, 0, 55), new Scalar(179, 40, 155)},       // gray don't care H, S and V only
            {new Scalar(0, 0, 195), new Scalar(179, 255, 255)}, // white 0,0,255 don't care H and S, V only
            {new Scalar(0, 0, 0), new Scalar(179, 250, 50)}    // black 0,0,0 don't care H and S, V only
    };

    // red wraps around in HSV, so we need two ranges
    private static Scalar LOWER_RED1 = new Scalar(0, 60, 80);
    private static Scalar UPPER_RED1 = new Scalar(10, 255, 220);
    private static Scalar LOWER_RED2 = new Scalar(160, 60, 80);
    private static Scalar UPPER_RED2 = new Scalar(179, 255, 200);

    //========================On Paper(N3)=====================================
    /*private static final Scalar COLOR_BOUNDS[][] = {
            {new Scalar(0, 90, 0), new Scalar(15, 250, 100)},    // brown 17,185,120
            {new Scalar(7, 170, 80), new Scalar(22, 250, 200)},   // orange
            {new Scalar(0, 0, 0), new Scalar(0, 0, 0)},         // red 0,255,255
            {new Scalar(15, 95, 100), new Scalar(30, 255, 230)}, // yellow
            {new Scalar(30, 50, 60), new Scalar(80, 250, 150)},   // green
            {new Scalar(100, 50, 60), new Scalar(130, 250, 247)},  // blue 155,147,237
            {new Scalar(130, 40, 50), new Scalar(160, 250, 150)}, // purple
            {new Scalar(0, 0, 55), new Scalar(179, 40, 155)},       // gray don't care H, S and V only
            {new Scalar(0, 0, 195), new Scalar(179, 255, 255)}, // white 0,0,255 don't care H and S, V only
            {new Scalar(0, 0, 0), new Scalar(179, 250, 50)}    // black 0,0,0 don't care H and S, V only
    };

    // red wraps around in HSV, so we need two ranges
    private static Scalar LOWER_RED1 = new Scalar(0, 60, 80);
    private static Scalar UPPER_RED1 = new Scalar(10, 255, 220);
    private static Scalar LOWER_RED2 = new Scalar(160, 60, 80);
    private static Scalar UPPER_RED2 = new Scalar(179, 255, 200);*/

    private static final String[] colour= {"brown", "orange", "red", "yellow", "green", "blue",
            "purple", "gray", "white", "black"};
    private static final int NUM_CODES = 10;
    private SparseIntArray _locationValues = new SparseIntArray(4);
    //================================================================================

    public Mat processFrame(CvCameraViewFrame frame) {
        Mat imageMat = frame.rgba();
        int cols = imageMat.cols();
        int rows = imageMat.rows();

        Mat subMat = imageMat.submat(rows / 2 - 5, rows / 2 + 15, cols / 2 - 65, cols / 2 + 65);
        Mat filteredMat = new Mat();
        Imgproc.cvtColor(subMat, subMat, Imgproc.COLOR_RGBA2BGR);
        Imgproc.bilateralFilter(subMat, filteredMat, 5, 80, 80);
        Imgproc.cvtColor(filteredMat, filteredMat, Imgproc.COLOR_BGR2HSV);

        findLocations(filteredMat);

        //=============================================Print what colour has been found========================================================
        String str = "";
        String[] allBands = {"None", "None", "None"}; //Save list of colour
        for (int i = 0; i < 3; i++){
            int indexOfList = _locationValues.keyAt(i);
            int indexOfColour = _locationValues.get(indexOfList);
            if (indexOfColour <= 9) {
                str = str + colour[indexOfColour] + ",";
                allBands[i] = colour[indexOfColour];
            }
        }
        str = str.substring(0, str.length()-1);
        Core.putText(imageMat, str, new Point(cols / 2 - 130, 20), Core.FONT_HERSHEY_COMPLEX, 0.8, new Scalar(0, 255, 0, 255), 1);

        //==================================================Calculate value======================================================================
        if (_locationValues.size() >= 3) {
            // recover the resistor value by iterating through the centroid locations
            // in an ascending manner and using their associated colour values
            int tens = colourToValue(_locationValues.get(_locationValues.keyAt(0)));
            int units = colourToValue(_locationValues.get(_locationValues.keyAt(1)));
            int power = colourToValue(_locationValues.get(_locationValues.keyAt(2)));

            float value = 10 * tens + units;
            value *= Math.pow(10, power);

            String valueStr;
            if (value >= 1000 && value < 1000000) {
                valueStr = Float.toString(value / 1000.0f) + " KOhm";
            } else if (value >= 1000000) {
                valueStr = Float.toString(value / 1000000.0f) + " MOhm";
            } else {
                valueStr = Float.toString(value) + " Ohm";
            }

            if (value <= 1000000000) {
                Core.putText(imageMat, valueStr, new Point(cols / 2 - 60, 100),
                        Core.FONT_HERSHEY_COMPLEX, 0.8, new Scalar(0, 255, 0, 255), 1);
            }

            //Show working
            String colourValueText = allBands[0] + "=" + tens + "," + allBands[1] + "=" +
                    units + "," + allBands[2] + "=" + power;
            String calculateText = "(10 x " + tens + " + " + units + ") x 10^" + power + " = " + valueStr;
            Core.putText(imageMat, colourValueText, new Point(cols / 2 - 140, 280),
                    Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(0, 255, 0, 255), 1);
            Core.putText(imageMat, calculateText, new Point(cols / 2 - 180, 300),
                    Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(0, 255, 0, 255), 1);


        }
        Core.line(imageMat, new Point(cols / 2 - 65, rows / 2),
                new Point(cols / 2 + 65, rows / 2), new Scalar(0, 255, 0, 255), 2);
        return imageMat;
    }

    // find contours of colour bands and the x-coords of their centroids
    private void findLocations(Mat searchMat) {
        _locationValues.clear();
        SparseIntArray areas = new SparseIntArray(4);

        for (int i = 0; i < NUM_CODES; i++) {
            Mat mask = new Mat();
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();

            if (i == 2) {
                //combine the two ranges for red
                Core.inRange(searchMat,LOWER_RED1, UPPER_RED1, mask);
                Mat rmask2 = new Mat();
                Core.inRange(searchMat, LOWER_RED2, UPPER_RED2, rmask2);
                Core.bitwise_or(mask, rmask2, mask);
                //Core.inRange(searchMat, COLOR_BOUNDS[i][0], COLOR_BOUNDS[i][1], mask);
            } else
                Core.inRange(searchMat, COLOR_BOUNDS[i][0], COLOR_BOUNDS[i][1], mask);

            Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            for (int contIdx = 0; contIdx < contours.size(); contIdx++) {
                int area;
                if ((area = (int) Imgproc.contourArea(contours.get(contIdx))) > 200) {
                    Moments M = Imgproc.moments(contours.get(contIdx));
                    int cx = (int) (M.get_m10() / M.get_m00());

                    // if a colour band is split into multiple contours, take the largest and consider only its centroid
                    boolean shouldStoreLocation = true;
                    for (int locIdx = 0; locIdx < _locationValues.size(); locIdx++) {
                        if (Math.abs(_locationValues.keyAt(locIdx) - cx) < 10) {
                            if (areas.get(_locationValues.keyAt(locIdx)) > area) {
                                shouldStoreLocation = false;
                                break;
                            } else {
                                _locationValues.delete(_locationValues.keyAt(locIdx));
                                areas.delete(_locationValues.keyAt(locIdx));
                            }
                        }
                    }
                    if (shouldStoreLocation) {
                        areas.put(cx, area);
                        _locationValues.put(cx, i);
                    }
                }
            }
        }
    }

    //Check what the colour is and translate them to their correspond value
    private int colourToValue(int index){
        if (index == 9) {
            return 0;
        } else if (index == 1){
            return 3;
        } else if (index == 2){
            return 2;
        } else {
            return index + 1;
        }
    }
}