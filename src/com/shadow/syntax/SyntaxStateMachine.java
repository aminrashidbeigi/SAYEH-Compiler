package com.shadow.syntax;

import java.util.ArrayList;

/**
 * Created by Amin Rashidbeigi on 6/3/2017.
 */
public class SyntaxStateMachine {

    private int[][] statementTransitionTable;
    private int[][] expressionTransitionTable;
    private ArrayList<String> tokens;
    private boolean isOpenBraceOrSemicolon = false;
    private boolean afterEq = false;
    private int scs = 0;
    private boolean oneTime = false;
    private int parenthesis = 0;
    private boolean syntaxIsOK = false;
    private int[] tokensOfEachLine;
    private boolean isBoolExpression = false;
    private int lineCounter =  0;
    private int ls = 0;

    public SyntaxStateMachine(int[][] statementTransitionTable, int[][] expressionTransitionTable,
                              ArrayList<String> tokens, int[] tokensOfEachLine) {
        this.statementTransitionTable = statementTransitionTable;
        this.expressionTransitionTable = expressionTransitionTable;
        this.tokens = tokens;
        this.tokensOfEachLine = tokensOfEachLine;
    }
    public void syntaxHandler(){
        int ecs = 0;
        int tokenCounter = 0;
        for (String string : tokens){
            while (tokenCounter == tokensOfEachLine[lineCounter]){
                lineCounter ++;
            }
            tokenCounter++;
            if (scs == -100 || ecs == -100 ) return;
            else if (scs == 14 || scs == 15 || scs == 7){
                isBoolExpression = true;
                ecs = expressionHandler(string, ecs);
                if (ecs == -100) return;
            } else {
                scs = statementHandler(string, scs);
                if (scs == -100) return;
            }
            if (tokenCounter == tokens.size()){
                if (!(string.equals(";") || string.equals("}"))){
                    System.out.println("; or } expected");
                    return;
                }
                syntaxIsOK = true;
                System.out.println("Syntax is OK :)");
            }
            ls = scs;
        }
    }

    private int statementHandler(String string, int cs){
        int key = statementKeywordValueGenerator(string);
        if (key < 0){
            System.out.println("In line" + lineCounter + ", " + string + " : invalid token.\n");
            return -100;
        } else if (key == 50){
            isOpenBraceOrSemicolon = true;
            return 0;
        } else if (key == 51 && isOpenBraceOrSemicolon){
            return 0;
        } else if (key == 51){
            System.out.println("In line "+ lineCounter + ", " + string + " seen! " + "; expected.\n");
            return -100;
        }
        if (cs >= 0 && cs != 15 && cs != 14){
            if (afterEq){
//                if (key == 17) cs = 10;
//                if (key == 12) cs = 4;
                cs = statementTransitionTable[cs][key];
                afterEq = false;
            } else{
                cs = statementTransitionTable[cs][key];
            }
        }
        if (cs == -8 && ls == 10){
            if (key == 12)
                cs = 4;
        }
        if (key == 8){
            afterEq = true;
        }
        if (cs < 0){
            errorHandler(cs, string);
            return -100;
        }
        isOpenBraceOrSemicolon = key == 10;
        return cs;
    }

    private boolean isParenthesis = false;
    public int expressionHandler(String string, int cs){
        int key = expressionKeywordValueGenerator(string);
        if (string.equals(";") && isBoolExpression){
            isBoolExpression = false;
            scs = 0;
            return 0;
        }
        if (key < 0){
            System.out.println(string + " : invalid token.\n");
            return -100;
        } else if (key == 0){
            isParenthesis = true;
            parenthesis++;
        } else if (key == 1){
            parenthesis--;
        }
        if (parenthesis == 1 && !isBoolExpression){
            if (!oneTime){
                oneTime = true;
                return 0;
            }
        } else if (parenthesis == 0 && isParenthesis){
            scs = 0;
            isParenthesis = false;
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

    public int statementKeywordValueGenerator(String string){
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
        else if (string.equals("{")) return 50; // is not in state machine
        else if (string.equals("}")) return 51; // is not in state machine
        return -1;
    }

    public int expressionKeywordValueGenerator(String token){
        if (token.equals("(")) return 0;
        else if (token.equals(")")) return 1;
        else if (token.equals("true") || token.equals("false")) return 7;
        else if (token.matches("\\w+")){
            if ((int)token.toCharArray()[0] > 57)return 2;
            else return 3;
        }
        else if (token.equals("'")) return 4;
        else if (token.equals("||") || token.equals("&&")) return 5;
        else if (token.equals("==") || token.equals(">=") || token.equals("<=") ||
                 token.equals("<") || token.equals(">")) return 6;
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
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "keyword or variable expected.\n"); //state 0
                break;
            case -2:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "variable expected.\n"); //state 1, 5, 8
                break;
            case -3:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "= or ; or , expected.\n"); //state 2, 6, 9
                break;
            case -4:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "variable or character or parenthesis expected.\n"); // state 3
                break;
            case -5:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "; or parenthesis expected.\n"); // state 4
                break;
            case -6:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "variable expected.\n"); //state 5
                break;
            case -7:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "; expected.\n"); //state 7, 13
                break;
            case -8:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "variable or number or parenthesis expected.\n"); //state 10
                break;
            case -9:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "; or operator or parenthesis expected.\n"); //state 11
                break;
            case -10:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "operator or ++ or -- or = or += ... expected.\n"); //state 12
                break;
            case -11:
                System.out.println("handle nashode"); //state 14, 15
                break;
            case -12:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "variable or number or parenthesis or character expected.\n"); //Estate 0
                break;
            case -13:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "logical operation or parenthesis expected.\n"); //Estate 1
                break;
            case -14:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or conditional operation expected.\n"); //Estate 2
                break;
            case -15:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or variable or number expected.\n"); //Estate 4, 9
                break;
            case -16:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or conditional or logical operation expected.\n"); //Estate 3
                break;
            case -17:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or variable or number or boolean value or character expected.\n"); //Estate 5
                break;
            case -18:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or operation or logical operation expected.\n"); //Estate 6, 7
                break;
            case -19:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or logical operation expected.\n"); //Estate 8
                break;
            case -20:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or conditional operation expected.\n"); //Estate 10
                break;
            case -21:
                System.out.println("In line "+ lineCounter + ", " + seenString + " seen! " + "parenthesis or variable or character expected.\n"); //Estate 11
                break;
            default:
                System.out.println("Undefined error");
                break;
            }
    }

    public boolean isSyntaxOK() {
        return syntaxIsOK;
    }
}
