package com.example.a96llegend.ar4ece.Gate;

import android.app.Activity;
import android.content.Intent;
import android.drm.DrmStore;
import android.media.Image;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.a96llegend.ar4ece.R;

import org.w3c.dom.Text;

public class LogicGateHandler {

    private static Gate currentGate;
    private static String xValue;
    private static String yValue;
    private static String fValue;
    private enum Gate{NOT, OR, AND};

    //Reset everythings
    public LogicGateHandler() {
        //Assign value to button
        fValue = "?";
        xValue = "?";
        yValue = "?";
        currentGate = null;
    }

    //Update the current gate when a new gate have detect
    public void updateCurrentGate(String gateName){
        //Don't do anything if the new gate is same as the old one
        if (!gateName.equals("")) {
            if (currentGate == null) {
                //Identify gate from tex
                if (gateName.equals("AND")) {
                    currentGate = Gate.AND;
                } else if (gateName.equals("OR")) {
                    currentGate = Gate.OR;
                } else if (gateName.equals("NOT")) {
                    currentGate = Gate.NOT;
                } else {
                    currentGate = null;
                }
                pressReset();
            } else {
                if (!gateName.equals(currentGate.name())){
                    //Identify gate from tex
                    if (gateName.equals("AND")) {
                        currentGate = Gate.AND;
                    } else if (gateName.equals("OR")) {
                        currentGate = Gate.OR;
                    } else if (gateName.equals("NOT")) {
                        currentGate = Gate.NOT;
                    } else {
                        currentGate = null;
                    }
                    pressReset();
                }
            }
        }
    }

    public String getCurrentGate(){
        if (currentGate != null) {
            return currentGate.name();
        } else {
            return null;
        }
    }

    //When user trigger change in X
    public void pressX(){
        if (xValue.equals("1")){
            xValue = "0";
        } else {
            xValue = "1";
        }

        if (yValue.equals("?")){
            yValue = "0";
        }
        changeF();
    }

    public String getX(){
        return xValue;
    }

    //When user trigger change in Y
    public void pressY(){
        if (yValue.equals("1")){
            yValue = "0";
        } else {
            yValue = "1";
        }

        if (xValue.equals("?")){
            xValue = "0";
        }
        changeF();
    }

    public String getY(){
        return yValue;
    }

    //When user trigger reset
    public void pressReset(){
        xValue = "?";
        yValue = "?";
        fValue = "?";
    }

    //Determine F value according to X and Y
    private void changeF(){
        if (xValue.equals("0") && yValue.equals("0")){
            switch(currentGate){
                case OR:
                    fValue = "0";
                    break;
                case AND:
                    fValue = "0";
                    break;
                default:
                    fValue = "1";
                    break;
            }
        } else if (xValue.equals("0") && yValue.equals("1")){
            switch(currentGate){
                case OR:
                    fValue = "1";
                    break;
                case AND:
                    fValue = "0";
                    break;
                default:
                    fValue = "1";
                    break;
            }
        } else if (xValue.equals("1") && yValue.equals("0")){
            switch(currentGate){
                case OR:
                    fValue = "1";
                    break;
                case AND:
                    fValue = "0";
                    break;
                default:
                    fValue = "0";
                    break;
            }
        } else if (xValue.equals("1") && yValue.equals("1")){
            switch(currentGate){
                case OR:
                    fValue = "1";
                    break;
                case AND:
                    fValue = "1";
                    break;
                default:
                    fValue = "0";
                    break;
            }
        } else {
            fValue = "?";
        }
    }

    public String getF(){
        return fValue;
    }

    //Determind what graph should show, return the id of that drawable
    public int getGraph(){
        if (xValue.equals("0") && yValue.equals("0")){
            switch(currentGate){
                case OR:
                    return R.drawable.or_trans_00;
                case AND:
                    return R.drawable.and_trans_00;
                default:
                    return R.drawable.not_trans_0;
            }
        } else if (xValue.equals("0") && yValue.equals("1")){
            switch(currentGate){
                case OR:
                    return R.drawable.or_trans_01;
                case AND:
                    return R.drawable.and_trans_01;
                default:
                    return R.drawable.not_trans_0;
            }
        } else if (xValue.equals("1") && yValue.equals("0")){
            switch(currentGate){
                case OR:
                    return R.drawable.or_trans_10;
                case AND:
                    return R.drawable.and_trans_10;
                default:
                    return R.drawable.not_trans_1;
            }
        } else if (xValue.equals("1") && yValue.equals("1")){
            switch(currentGate){
                case OR:
                    return R.drawable.or_trans_11;
                case AND:
                    return R.drawable.and_trans_11;
                default:
                    return R.drawable.not_trans_1;
            }
        } else {
            switch(currentGate){
                case OR:
                    return R.drawable.or_trans;
                case AND:
                    return R.drawable.and_trans;
                default:
                    return R.drawable.not_trans;
            }
        }
    }
}
