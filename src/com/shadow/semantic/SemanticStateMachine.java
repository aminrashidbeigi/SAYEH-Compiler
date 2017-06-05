package com.shadow.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amin Rashidbeigi on 6/5/2017.
 */
public class SemanticStateMachine {

    private ArrayList<String> tokens;
    private Map isVariableDefined;
    private Map variableValue;
    private boolean semanticIsOK = true;

    public SemanticStateMachine(ArrayList<String> tokens) {
        this.tokens = tokens;
        isVariableDefined = new HashMap();
        variableValue = new HashMap();
    }

    public void semanticHandler(){
        int cs = 0;
        int ls = 0;
        int key = 0;
        int tokenCounter = 0;
        String lastValue = "";
        for (String token : tokens){
            tokenCounter++;
            key = semanticKeyValueGenerator(token);
            if (semanticKeyValueGenerator(token) == 1) lastValue = token;
            ls = cs;
            cs = SemanticTransitionTable.semanticTT[cs][key];
            switch (cs){
                case 2:
                    if (ls == 1){
                        if (isVariableDefined.containsKey(token)){
                            errorHandler(-5, token);
                            return;
                        }else {
                            isVariableDefined.put(token, true);
                        }
                    } else {
                        if (!isVariableDefined.containsKey(token)){
                            errorHandler(-4,token);
                        }
                    }
                    break;
                case 4:
                    variableValue.put(lastValue, token);
                    break;
                case 5:
                    if (!variableValue.containsKey(token)){
                        errorHandler(-2, token);
                        return;
                    }
                    break;
                case 8:
                    if (Integer.parseInt(token) == 0){
                        errorHandler(-3, token);
                        return;
                    }
                    break;

                default:
                    break;
            }
        }

        if (tokenCounter == tokens.size() && semanticIsOK){
            semanticIsOK = true;
            System.out.println("Semantic is OK too ^_^");
        }
    }


    private void errorHandler(int error, String seenString){
        semanticIsOK = false;
        switch (error){
            case -2 :
                System.out.println("variable value is not defined");
                break;
            case -3:
                System.out.println("division by zero error");
                break;
            case -4:
                System.out.println("undeclared variable");
                break;
            case -5:
                System.out.println("multiple deceleration of variable");
                break;
        }
    }


    private int semanticKeyValueGenerator(String token){
        if (token.equals("int") || token.equals("char") || token.equals("bool")) return 0;
        else if (token.matches("\\w+")){
            if ((int)token.toCharArray()[0] > 57)return 1;
            else return 2;
        }
        else if (token.equals("true")||token.equals("false")) return 3;
        else if (token.toCharArray()[0] == '\'') return 4;
        else if (token.equals("+") ||token.equals("-") ||token.equals("*") ||
                token.equals("%")) return 5;
        else if (token.equals("/")) return 6;
        else if (token.equals("=")) return 7;
        else if (token.equals(";")) return 8;
        else return -1;
    }

    private boolean isKeyword(String token){
        return token.equals("int") || token.equals("char") || token.equals("bool") || token.equals("if")
                || token.equals("while") || token.equals("else") || token.equals("null")
                || token.equals("true") || token.equals("false");
    }
}
