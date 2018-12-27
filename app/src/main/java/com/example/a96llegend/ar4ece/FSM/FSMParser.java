package com.example.a96llegend.ar4ece.FSM;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
//This generates Boolean type equations from the text detected on scanning an FSM. These
//equations correspond to the transactions that the FSM needs to make. It builds upon the scanned result
//obtained from FSMScanningActivity.
public class FSMParser {

    public static List<ArrayList<String>> getBooleanEquation(List<String> allEquations){
        List<ArrayList<String>> equations = new ArrayList<ArrayList<String>>();

        for(int i = 0; i < allEquations.size(); i++){
            String[] terms = splitTerm(allEquations.get(i));
            ArrayList<String> singleEquation = new ArrayList<String>();
            singleEquation.add(0, terms[0]);
            singleEquation.add(1, terms[1]);
            singleEquation.add(2, terms[2]);
            equations.add(i, singleEquation);
        }
        return equations;
    }


    //Split a equation into initial state term, conditions term and final state term, by finding
    // where the "+" and "=" are. It will Return a string array, first one is the initial state
    // term, second one is the conditions term, and the third one is the final state
    private static String[] splitTerm(String equation){
        String[] terms = {"-", "-", "-"};

        //Find where the symbols are
        int indexOfPlus = equation.indexOf("+");
        int indexOfEqual = equation.indexOf("=");

        //Add to the returning array
        if (indexOfPlus > 0){
            terms[0] = equation.substring(0,indexOfPlus);
            terms[1] = equation.substring(indexOfPlus+1, indexOfEqual);
            terms[2] = equation.substring(indexOfEqual+1, equation.length());
        } else {
            terms[0] = equation.substring(0,indexOfEqual);
            terms[2] = equation.substring(indexOfEqual+1, equation.length());
        }

        return terms;
    }

    //Multiple input may required in a condition(especially with "and"), so split them
    public static List<String> splitConditions(String conditionTerm){
        List<String> conditionList = new ArrayList<String>();

        //If only a "-" in the
        if(conditionList.equals("-")){
            return conditionList;
        }

        //Find where the "&" symbols are
        List<Integer> indexOfAnd = new ArrayList<Integer>();
        for (int i = 0; i < conditionTerm.length(); i++){
            if(conditionTerm.charAt(i) == '&'){
                indexOfAnd.add(i);
            }
        }

        //Only one conditions, just save the whole condition
        if(indexOfAnd.size() == 0){
            conditionList.add(conditionTerm);
        } else {
            //Get every single condition, which split by "&"
            conditionList.add(conditionTerm.substring(0, indexOfAnd.get(0)));
            for (int i = 0; i < indexOfAnd.size(); i++){
                if(i + 1 < indexOfAnd.size()){
                    conditionList.add(conditionTerm.substring(indexOfAnd.get(i)+1, indexOfAnd.get(i+1)));
                } else {
                    conditionList.add(conditionTerm.substring(indexOfAnd.get(i)+1, conditionTerm.length()));
                }
            }
        }
        return conditionList;
    }

    //Extract all states name from a group of boolean equation
    public static List<String> getAllStateFromAllBooleanEquation(List<ArrayList<String>> allBooleanEquations){
        List<String> allState = new ArrayList<String>();

        for(int i = 0; i < allBooleanEquations.size(); i++){
            String currentStateName = allBooleanEquations.get(i).get(0);

            //Found if it is exist already
            if(allState.size() == 0){
                allState.add(currentStateName);
            } else {
                boolean exist = false;
                for (int j = 0; j < allState.size(); j++) {
                    if (allState.get(j).equals(currentStateName)) {
                        exist = true;
                        break;
                    }
                }

                //If not exist, add to the list of state
                if (!exist) {
                    allState.add(currentStateName);
                }
            }
        }
        return allState;
    }

    //Extract all inputs name from a group of boolean equation
    public static List<String> getAllInputFromAllBooleanEquation(List<ArrayList<String>> allBooleanEquations){
        List<String> allInput = new ArrayList<String>();

        for(int i = 0; i < allBooleanEquations.size(); i++){
            String currentConditions = allBooleanEquations.get(i).get(1);
            if(!currentConditions.equals("-")) {
                List<String> allCondition = splitConditions(currentConditions);

                if (allCondition.size() != 0) {
                    for (int j = 0; j < allCondition.size(); j++) {
                        String currentInput = allCondition.get(j);

                        //Delete the "!" first, since that is not part of the input name
                        if(currentInput.charAt(0) == '!'){
                            currentInput = currentInput.substring(1, currentInput.length());
                        }

                        //Check if the input already in the input list
                        if (allInput.size() == 0) {
                            allInput.add(0, currentInput);
                        } else {
                            boolean exist = false;
                            for (int k = 0; k < allInput.size(); k++) {
                                if (allInput.get(k).equals(currentInput)) {
                                    exist = true;
                                    break;
                                }
                            }
                            //If not exist, add to the list of input
                            if (!exist) {
                                allInput.add(currentInput);
                            }
                        }
                    }
                }
            }
        }
        return allInput;
    }
}
