package com.shadow.syntax;

import java.util.ArrayList;

/**
 * Created by Amin Rashidbeigi on 6/3/2017.
 */
public class SyntaxStateMachine {

    int[][] transitionTable;
    ArrayList<String> tokens;

    public SyntaxStateMachine(int[][] transitionTable, ArrayList<String> tokens) {
        this.transitionTable = transitionTable;
        this.tokens = tokens;
    }

    public void syntaxHandler(){

        for (int i = 0; i < tokens.size(); i++){
            System.out.println(i + " : " + tokens.get(i));
        }

        int cs = 0;
        int tokenCounter = 0;
        for (String string : tokens){
            tokenCounter++;
            int key = keywordValueGenerator(string);
            if (key < 0){
                System.out.println("invalid token.\n");
                return;
            }
            cs = transitionTable[cs][key];
            if (cs < 0){
                errorHandler(cs, string);
                return;
            }
            if (tokenCounter == tokens.size()){
                System.out.println("Compiled Successfully :)");
            }
        }
    }

    private int keywordValueGenerator(String string){
        if (string.equals("int")) return 0;
        else if (string.equals("char")) return 1;
        else if (string.equals("bool")) return 2;
        else if (string.equals("while")) return 3;
        else if (string.equals("if")) return 4;
        else if (string.matches("\\w+")){
            if ((int)string.toCharArray()[0] > 57)return 5;
            else return 17;
        }
        else if (string.equals("(")) return 6;
        else if (string.equals(")")) return 7;
        else if (string.equals("=")) return 8;
        else if (string.equals("+") || string.equals("-") || string.equals("*") ||
                string.equals("/") || string.equals("%")) return 9;
        else if (string.equals(";")) return 10;
        else if (string.equals(",")) return 11;
        else if (string.equals("'")) return 12;
        else if (string.equals("+=") || string.equals("-=") || string.equals("*=") ||
                string.equals("/=") || string.equals("%=")) return 13;
        else if (string.equals("++") || string.equals("--")) return 16;
//        else if (string.matches("\\d+")) return 17;
        return -1;
    }

    private boolean isVariable(String string){
        if (string.matches("^([a − z][A − Z]) + ([a − z][A − Z][0 − 9])∗")) return true;
        return false;
    }

    private void errorHandler(int state, String seenString){
        switch (state){
            case -1:
                System.out.println(seenString + " seen!. " + "keyword expected.\n"); //state 0
                break;
            case -2:
                System.out.println(seenString + " seen!. " + "variable expected.\n"); //state 1, 5, 8
                break;
            case -3:
                System.out.println(seenString + " seen!. " + "= or ; or , expected.\n"); //state 2, 6, 9
                break;
            case -4:
                System.out.println(seenString + " seen!. " + "variable or character or parenthesis expected.\n"); // state 3
                break;
            case -5:
                System.out.println(seenString + " seen!. " + "; or parenthesis expected.\n"); // state 4
                break;
            case -6:
                System.out.println(seenString + " seen!. " + "variable expected.\n"); //state 5
                break;
            case -7:
                System.out.println(seenString + " seen!. " + "; expected.\n"); //state 7, 13
                break;
            case -8:
                System.out.println(seenString + " seen!. " + "variable or number or parenthesis expected.\n"); //state 10
                break;
            case -9:
                System.out.println(seenString + " seen!. " + "; or operator or parenthesis expected.\n"); //state 11
                break;
            case -10:
                System.out.println(seenString + " seen!. " + "operator or ++ or -- or = or += ... expected.\n"); //state 12
                break;
            case -11:
                System.out.println("handle nashode"); //state 14, 15
                break;
            default:
                System.out.println("Undefined error");
                break;
            }
    }
}
