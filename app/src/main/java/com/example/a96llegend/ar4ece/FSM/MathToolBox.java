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
    public static List<Float[]> pathCalculator(Rect origin, Rect destination){
        List<Float[]> firstHalf = pathForOnePart(origin, destination);
        List<Float[]> path = new ArrayList<Float[]>();
        for(int i = 0; i < step; i++){
            path.add(i, firstHalf.get(i));
        }
        return path;
    }

    //Calculate a path, for when the state is not change
    public static List<Float[]> pathForNotStateChange(Rect state, boolean leftToRight){
        List<Float[]> path = new ArrayList<Float[]>();
        int Xoffset = 3*(state.right-state.left);
        int Yoffset = 2*(state.bottom-state.top);
        int shortStep = 20;
        float stepSize = (state.right - state.left) / shortStep;
        if(leftToRight){
            for (int i = 0; i < shortStep; i++) {
                Float[] xy = {state.left + (stepSize * i)+Xoffset-40, (state.centerY() * 1.0f)+Yoffset-40};
                path.add(i, xy);
            }
        } else {
            for (int i = 0; i < shortStep; i++) {
                Float[] xy = {state.right - (stepSize * i)+Xoffset+40, (state.centerY() * 1.0f)+Yoffset-40};
                path.add(i, xy);
            }
        }
        return path;
    }

    //Each animation path have two part, each has different radius, so need two calculation;
    public static List<Float[]> pathForOnePart (Rect startPoint, Rect stopPoint){
        List<Float[]> path = new ArrayList<Float[]>();
            float XstepSize = (stopPoint.centerX() - startPoint.centerX()) / step;
            float YstepSize = (startPoint.centerY() - stopPoint.centerY()) / step;
            int Xoffset = 3*(stopPoint.right-stopPoint.left);
            int Yoffset = 2*(stopPoint.bottom-stopPoint.top);
            for(int i = 0; i < step; i++){
                Float[] xy = {(startPoint.centerX() + Xoffset+(XstepSize * i)), (startPoint.centerY() +Yoffset- (YstepSize * i))};
                path.add(i,xy);
            }
        return path;
    }
}
