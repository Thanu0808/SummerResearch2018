package com.example.a96llegend.ar4ece.FSM;

import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
//Calculates the path the stick man animation should take, between two fixed points.
public class MathToolBox {

    private static final int step = 45; //steps for have of the path
    private static final String tag = "=======Debug=======";

    //Calculate a path, return a list of locations that the animation view should go through
    public static List<Float[]> pathCalculator(Rect origin, Rect middle, Rect destination){
        float radiusFirstPart = findRadius(origin,middle,destination);
        float radiusSecondPart = findRadius(middle,destination,origin);
//        List<Float[]> firstHalf = pathForOnePart(origin, middle, radiusFirstPart);
//        List<Float[]> secondHalf = pathForOnePart(middle, destination, radiusSecondPart);
//        List<Float[]> path = new ArrayList<Float[]>();
//        for(int i = 0; i < step; i++){
//            path.add(i, firstHalf.get(i));
//        }
//        for(int i = 0; i < step; i++){
//            path.add(i+step, secondHalf.get(i));
//        }
//        return path;

        List<Float[]> firstHalf = pathForOnePart(origin, destination, radiusFirstPart);
        List<Float[]> path = new ArrayList<Float[]>();
        for(int i = 0; i < step; i++){
            path.add(i, firstHalf.get(i));
        }
        return path;
    }

    //Calculate a path, for when the state is not change
    public static List<Float[]> pathForNotStateChange(Rect state, boolean leftToRight){
        List<Float[]> path = new ArrayList<Float[]>();
        int shortStep = 20;
        float stepSize = (state.right - state.left) / shortStep;
        if(leftToRight){
            for (int i = 0; i < shortStep; i++) {
                Float[] xy = {state.left + (stepSize * i), (state.centerY() * 1.0f)};
                path.add(i, xy);
            }
        } else {
            for (int i = 0; i < shortStep; i++) {
                Float[] xy = {state.right - (stepSize * i), (state.centerY() * 1.0f)};
                path.add(i, xy);
            }
        }
        return path;
    }

    //Calculate the radius of a arc d = (H/2) + ((2W)^2 / 8H), return -1 means straight, no radius
    public static float findRadius(Rect origin, Rect middle, Rect destination){
        int Xmidline;
        int Ymidline;
        float radius;
        Xmidline = (origin.centerX()+destination.centerX())/2;
        if (Xmidline<0){
            Xmidline = Xmidline * -1;
        }
        Ymidline = (origin.centerY()+destination.centerY())/2;
        if (Ymidline<0){
            Ymidline = Ymidline *-1;
        }
        double r = Math.pow(Xmidline - middle.centerX(),2)+ Math.pow(Ymidline - middle.centerY(),2);
        radius = (float)Math.sqrt(r);
//        Log.d(tag, "MiddleCenter is " + Integer.toString(middle.centerY()));
//        Log.d(tag, "OriginCenter is " + Integer.toString(origin.centerY()));
//        Log.d(tag, "Radius is " + Float.toString(radius));
//        Log.d(tag, "Xmidline is " + Integer.toString(Xmidline));
//        Log.d(tag, "Ymidline is " + Integer.toString(Ymidline));
//        Log.d(tag, "r is " + Double.toString(r));

        //if((middle.centerY() < (origin.centerY()-5)) || (middle.centerY() > (origin.centerY()+5))) { //accounting for top and bottom arc
        if(radius>5){//if radius > 5 then return radius
            int W = middle.centerX() - origin.centerX();
            if (W < 0){
                W = W * -1;
            }
            int H = middle.centerY() - origin.centerY();
            if (H < 0) {
                H = H * -1;
            }

            Double d = (H/2) + (Math.pow(2*W, 2)/(8*H));
            return d.floatValue();

        } else { //In other case, treat it as a straight;
            return -1.0f;
        }
    }

    //What degree to start and what degree to finish in between two point in an arc in the given radius
    public static float[] findStartAndStopDegree(Rect startPoint, Rect stopPoint, float radius){
        int W = startPoint.centerX() - stopPoint.centerX();
        if (W < 0){
            W = W * -1;
        }
        float degreeInBetween = Double.valueOf(Math.toDegrees(Math.asin(Float.valueOf(W)/radius))).floatValue();
        //Log.d(tag, "DegreesInBetween is " + Double.toString(degreeInBetween));
        float[] startStop = {0, 0};

        if(startPoint.centerX() < stopPoint.centerX() && startPoint.centerY() > stopPoint.centerY()){ //Top left
            startStop[0] = 360 - degreeInBetween;
            startStop[1] = 360;
        } else if(startPoint.centerX() < stopPoint.centerX() && startPoint.centerY() < stopPoint.centerY()){ //Top right
            startStop[0] = 0;
            startStop[1] = degreeInBetween;
        } else if(startPoint.centerX() > stopPoint.centerX() && startPoint.centerY() < stopPoint.centerY()) { //Bottom right
            startStop[0] = 180 - degreeInBetween;
            startStop[1] = 180;
        } else { //Bottom left
            startStop[0] = 180;
            startStop[1] = 180 + degreeInBetween;
        }

        return startStop;
    }

    //Find centre of the circle
    public static float[] findCircule(Rect startPoint, Rect stopPoint, float radius){
        if(startPoint.centerX() < stopPoint.centerX() && startPoint.centerY() > stopPoint.centerY()){ //Top left
            float[] xy = {stopPoint.centerX(), (stopPoint.centerY() + radius)};
            return xy;
        } else if(startPoint.centerX() < stopPoint.centerX() && startPoint.centerY() < stopPoint.centerY()){ //Top right
            float[] xy = {startPoint.centerX(), (startPoint.centerY() + radius)};
            return xy;
        } else if(startPoint.centerX() > stopPoint.centerX() && startPoint.centerY() < stopPoint.centerY()) { //Bottom right
            float[] xy = {stopPoint.centerX(), (stopPoint.centerY() - radius)};
            return xy;
        } else { //Bottom left
            float[] xy = {startPoint.centerX(), (startPoint.centerY() - radius)};
            return xy;
        }
    }

    //Each animation path have two part, each has different radius, so need two calculation;
    public static List<Float[]> pathForOnePart (Rect startPoint, Rect stopPoint, float radius){
        List<Float[]> path = new ArrayList<Float[]>();
        //float d = findRadius(startPoint, stopPoint);
        float d = radius;
        d=-1;

        if(d == -1){ //Straight line
            float XstepSize = (stopPoint.centerX() - startPoint.centerX()) / step;//TT should be a check to see if negative?
            float YstepSize = (startPoint.centerY() - stopPoint.centerY()) / step;
//            if(YstepSize<0){
//                YstepSize= YstepSize * -1;
//            }
//            int oldOffset = states.get(fsm.getAllStateName().get(oldState)).right-states.get(fsm.getAllStateName().get(oldState)).left;
//            int newOffset = states.get(fsm.getAllStateName().get(newState)).right-states.get(fsm.getAllStateName().get(newState)).left;
            int Xoffset = 2*(stopPoint.right-stopPoint.left);
            int Yoffset = 2*(stopPoint.bottom-stopPoint.top);
//            int newOffset = stopPoint.right-stopPoint.left;
            for(int i = 0; i < step; i++){
                Float[] xy = {(startPoint.centerX() + Xoffset+(XstepSize * i)), (startPoint.centerY() +Yoffset- (YstepSize * i))};
                path.add(i,xy);
            }
        } else { //arc
            float[] centre = findCircule(startPoint, stopPoint, d);
            float[] startStop = findStartAndStopDegree(startPoint, stopPoint, d);
            float degreeCover = startStop[1] - startStop[0];
            float stepSize = degreeCover / step;

            for (int i = 0; i < step; i++){
                float currentAngle = startStop[0] + (i * stepSize);
                if(currentAngle >= 0 && currentAngle < 90){ //Top right
                    Float x = centre[0] + (float)(d * Math.sin(Math.toRadians(currentAngle)));
                    Float y = centre[1] - (float)(d * Math.cos(Math.toRadians(currentAngle)));
                    Float[] xy = {x.floatValue(), y.floatValue()};
                    path.add(i, xy);
                } else if(currentAngle >= 0 && currentAngle < 90){ //Bottom Right
                    Float x = centre[0] + (float)(d * Math.cos(Math.toRadians(currentAngle)));
                    Float y = centre[1] + (float)(d * Math.sin(Math.toRadians(currentAngle)));
                    Float[] xy = {x.floatValue(), y.floatValue()};
                    path.add(i, xy);
                } else if(currentAngle >= 0 && currentAngle < 90){ //Bottom left
                    Float x = centre[0] - (float)(d * Math.sin(Math.toRadians(currentAngle)));
                    Float y = centre[1] + (float)(d * Math.cos(Math.toRadians(currentAngle)));
                    Float[] xy = {x.floatValue(), y.floatValue()};
                    path.add(i, xy);
                } else{ //Top left
                    Double x = centre[0] + (d * Math.sin(Math.toRadians(currentAngle)));
                    Double y = centre[1] - (d * Math.cos(Math.toRadians(currentAngle)));
                    Float[] xy = {x.floatValue(), y.floatValue()};
                    path.add(i, xy);
                }
            }
        }
        return path;
    }
}
