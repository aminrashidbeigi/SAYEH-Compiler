package com.shadow.syntax;

import java.util.ArrayList;

/**
 * Created by Amin Rashidbeigi on 6/3/2017.
 */
public class SyntaxStateMachine {

    private int[][] statementTransitionTable;
    private int[][] expressionTransitionTable;
    private ArrayList<String> tokens;

    public SyntaxStateMachine(int[][] statementTransitionTable, int[][] expressionTransitionTable, ArrayList<String> tokens) {
        this.statementTransitionTable = statementTransitionTable;
        this.expressionTransitionTable = expressionTransitionTable;
        this.tokens = tokens;
    }
    int scs = 0;

    public void syntaxHandler(){
        int ecs = 0;
        int tokenCounter = 0;

        for (String string : tokens){
            tokenCounter++;
            if (scs == -100 || ecs == -100 ) return;
            else if (scs == 14 || scs == 15){
                ecs = expressionHandler(string, ecs);
            } else {
                scs = statementHandler(string, scs);
            }
            if (tokenCounter == tokens.size()){
                System.out.println("Compiled Successfully :)");
            }
        }
    }

    boolean isOpenBraceOrSemicolon = false;
    boolean afterEq = false;
    private int statementHandler(String string, int cs){
        int key = statementKeywordValueGenerator(string);
        if (key < 0){
            System.out.println("invalid token.\n");
            return -100;
        } else if (key == 18){
            isOpenBraceOrSemicolon = true;
            return 0;
        } else if (key == 19 && isOpenBraceOrSemicolon){
            return 0;
        } else if (key == 19){
            System.out.println(string + " seen!. " + "; expected.\n");
            return -100;
        }

        if (cs >= 0 && cs != 15 && cs != 14){
            if (afterEq){
                if (key == 17) cs = 10;
                else if (key == 12) cs = 4;
                else cs = statementTransitionTable[cs][key];
                afterEq = false;
            } else{
                cs = statementTransitionTable[cs][key];
            }
        }
        if (key == 8){
            afterEq = true;
        }
        if (cs < 0){
            errorHandler(cs, string);
            return -100;
        }
        if (key == 18 || key == 10){
            isOpenBraceOrSemicolon = true;
        } else {
            isOpenBraceOrSemicolon = false;
        }
        return cs;
    }

    boolean oneTime = false;

    int parenthesis = 0;

    private int expressionHandler(String string, int cs){
        int key = expressionKeywordValueGenerator(string);
        if (key < 0){
            System.out.println("invalid token.\n");
            return -100;
        } else if (key == 0){
            parenthesis++;
        } else if (key == 1){
            parenthesis--;
        }
        if (parenthesis == 1){
            if (!oneTime){
                oneTime = true;
                return 0;
            }
        } else if (parenthesis == 0){
            scs = 0;
            return 0;
        }
        if (cs >= 0 && cs != 15 && cs != 14)
            cs = expressionTransitionTable[cs][key];
        if (cs < 0){
            errorHandler(cs, string);
            return -100;
        }
        return cs;
    }

    private int statementKeywordValueGenerator(String string){
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
        else if (string.toCharArray()[0] == '\'') return 12;
        else if (string.equals("+=") || string.equals("-=") || string.equals("*=") ||
                 string.equals("/=") || string.equals("%=")) return 13;
        else if (string.equals("++") || string.equals("--")) return 16;
        else if (string.equals("{")) return 18; // is not in state machine
        else if (string.equals("}")) return 19; // is not in state machine
        return -1;
    }

    private int expressionKeywordValueGenerator(String token){
        if (token.equals("(")) return 0;
        else if (token.equals(")")) return 1;
        else if (token.matches("\\w+")){
            if ((int)token.toCharArray()[0] > 57)return 2;
            else return 3;
        }
        else if (token.equals("'")) return 4;
        else if (token.equals("||") || token.equals("&&")) return 5;
        else if (token.equals("==") || token.equals(">=") || token.equals("<=") ||
                 token.equals("<") || token.equals(">")) return 6;
        else if (token.equals("true") || token.equals("false")) return 7;
        else if (token.equals("+") || token.equals("-") || token.equals("*") ||
                 token.equals("%") || token.equals("/")) return 8;
        return -1;
    }



    private boolean isVariable(String string){
        if (string.matches("^([a − z][A − Z]) + ([a − z][A − Z][0 − 9])∗")) return true;
        return false;
    }

    private void errorHandler(int state, String seenString){
        switch (state){
            case -1:
                System.out.println(seenString + " seen!. " + "keyword or variable expected.\n"); //state 0
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
            case -12:
                System.out.println(seenString + " seen!. " + "variable or number or parenthesis or character expected.\n"); //Estate 0
                break;
            case -13:
                System.out.println(seenString + " seen!. " + "logical operation or parenthesis expected.\n"); //Estate 1
                break;
            case -14:
                System.out.println(seenString + " seen!. " + "parenthesis or conditional operation expected.\n"); //Estate 2
                break;
            case -15:
                System.out.println(seenString + " seen!. " + "parenthesis or variable or number expected.\n"); //Estate 4, 9
                break;
            case -16:
                System.out.println(seenString + " seen!. " + "parenthesis or conditional or logical operation expected.\n"); //Estate 3
                break;
            case -17:
                System.out.println(seenString + " seen!. " + "parenthesis or variable or number or boolean value or character expected.\n"); //Estate 5
                break;
            case -18:
                System.out.println(seenString + " seen!. " + "parenthesis or operation or logical operation expected.\n"); //Estate 6, 7
                break;
            case -19:
                System.out.println(seenString + " seen!. " + "parenthesis or logical operation expected.\n"); //Estate 8
                break;
            case -20:
                System.out.println(seenString + " seen!. " + "parenthesis or conditional operation expected.\n"); //Estate 10
                break;
            case -21:
                System.out.println(seenString + " seen!. " + "parenthesis or variable or character expected.\n"); //Estate 11
                break;
            default:
                System.out.println("Undefined error");
                break;
            }
    }
}