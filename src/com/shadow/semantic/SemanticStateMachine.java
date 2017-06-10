package com.shadow.semantic;

import java.util.*;

/**
 * Created by Amin Rashidbeigi on 6/5/2017.
 */
public class SemanticStateMachine {

    private ArrayList<String> tokens;
    private Map isVariableDefined;
    private Map variableValue;
    private Map variableType;
    private boolean semanticIsOK = true;

    public SemanticStateMachine(ArrayList<String> tokens) {
        this.tokens = tokens;
        isVariableDefined = new HashMap();
        variableValue = new HashMap();
        variableType = new HashMap();
    }

    private boolean afterEq = false;
    public void semanticHandler(){
        int cs = 0;
        int ls = 0;
        int key = 0;
        int tokenCounter = 0;
        String lastVariable = "";
        String afterEqString = "";
        boolean commentStarts = false;
        int commentType = 0;
        boolean isNewLine = false;
        String lastToken = "";
        boolean isNegativeValue = false;
        String lastType = "";
        String lastDefinedVariable = "";
        String beforEqVariable = "";


        for (String token : tokens){
            tokenCounter++;
            if (token.equals("/*") || token.equals("//")){
                if (token.equals("/*")) commentType = 1;
                else if (token.equals("//")) commentType = 2;
                commentStarts = true;
            } else if (isKeyword(token)) continue;
            if (commentStarts){
                if ((commentType == 2 && isNewLine) || token.equals("*/"))
                    commentStarts = false;
                continue;
            }
            key = semanticKeyValueGenerator(token);
            if (key == 0)
                lastType = token;
            if (key == 2)
                if (token.toCharArray()[0] == '-')
                    isNegativeValue = true;
            if (key == 1 && variableValue.containsKey(token))
                if (((String)variableType.get(token)).equals("int"))
                    if (Integer.parseInt((String) variableValue.get(token)) < 0)
                        isNegativeValue = true;

            if (semanticKeyValueGenerator(token) == 7) lastVariable = lastToken;
            ls = cs;
            if (key < 0){
                System.out.println(token + " : invalid token!");
                return;
            }

            cs = SemanticTransitionTable.semanticTT[cs][key];
            if (key == 7) beforEqVariable = lastToken;
            if (afterEq) {
                if (((String) variableType.get(lastDefinedVariable)).equals("int")){
                    if (key == 8) {
                        afterEq = false;
//                        System.out.println(afterEqString);
                        variableValue.put(lastVariable, Integer.toString(mathExpressionEvaluator(afterEqString)));
                        isNegativeValue = false;
                        afterEqString = "";
                    } else {
                        String value;
                        if (semanticKeyValueGenerator(token) == 1) {
                            value = variableValue.get(token) + " ";
                            if (isNegativeValue) {
                                value = ((String) variableValue.get(token)).toCharArray()[0] + " ";
                                for (int i = 1; i < ((String) variableValue.get(token)).toCharArray().length; i++)
                                    value += ((String) variableValue.get(token)).toCharArray()[i];
                            }
                            afterEqString += value;
                        }
                        else{
                            value = token + " ";
                            if (isNegativeValue){
                                value = token.toCharArray()[0] + " ";
                                for (int i = 1; i < token.toCharArray().length; i++)
                                    value+= token.toCharArray()[i];
                            }
                            afterEqString += value;
                        }
                    }
                }
            }
            if (key == 7) afterEq = true;
            switch (cs){
                case -1:
                    System.out.println("Semantic undefined error :(");
                    return;
                case 2:
                    if (ls == 1){
                        if (isVariableDefined.containsKey(token)){
                            errorHandler(-5, token);
                            return;
                        }else {
                            lastDefinedVariable = token;
                            variableType.put(token, lastType);
                            isVariableDefined.put(token, true);
                        }
                    } else {
                        if (!isVariableDefined.containsKey(token)){
                            errorHandler(-4,token);
                            return;
                        }
                    }
                    break;
                case 4:
                    variableValue.put(lastVariable, token);
                    break;
                case 5:
                    if (!variableValue.containsKey(token)){
                        errorHandler(-2, token);
                        return;
                    }
                    if (!variableType.get(beforEqVariable).equals(variableType.get(token))){
//                        System.out.println(beforEqVariable + " : "++ " " + token + " : "+ variableType.get(token));
                        errorHandler(-6, token);
                    }
                    break;
                case 8:
                    if (lastVariable.equals(token)){
                        if (Integer.parseInt((String)variableValue.get(token)) == 0){
                            errorHandler(-3, token);
                            return;
                        }
                    } else if (token.length() == 1 && (int)token.toCharArray()[0] == 48){
                        errorHandler(-3, token);
                        return;
                    }
                    break;

                default:
                    break;
            }
            lastToken = token;
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
                System.out.println(seenString + " : variable value is not defined");
                break;
            case -3:
                System.out.println("division by zero error");
                break;
            case -4:
                System.out.println(seenString + " : undeclared variable");
                break;
            case -5:
                System.out.println(seenString + " : multiple deceleration of variable");
                break;

            case -6:
                System.out.println("type problem.");
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
                token.equals("%") || token.equals("||") || token.equals("&&") || token.equals("==")
                || token.equals("<") || token.equals(">") || token.equals("<=")
                || token.equals(">=")) return 5;
        else if (token.equals("/")) return 6;
        else if (token.equals("=")) return 7;
        else if (token.equals(";")) return 8;
        else if (token.equals("(") || token.equals(")")) return 9;
        else return -1;
    }

    private boolean isKeyword(String token){
        return token.equals("if") || token.equals("while") || token.equals("else");
    }



    public int mathExpressionEvaluator(String expression) {
        char[] tokens = expression.toCharArray();
        Stack<Integer> values = new Stack<Integer>();
        Stack<Character> ops = new Stack<Character>();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ')
                continue;
            if (tokens[i] >= '0' && tokens[i] <= '9') {
                StringBuffer sbuf = new StringBuffer();
                while (i < tokens.length && tokens[i] >= '0' && tokens[i] <= '9')
                    sbuf.append(tokens[i++]);
                values.push(Integer.parseInt(sbuf.toString()));
            }

            else if (tokens[i] == '(')
                ops.push(tokens[i]);
            else if (tokens[i] == ')') {
                while (ops.peek() != '(')
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.pop();
            }
            else if (tokens[i] == '+' || tokens[i] == '-' ||
                    tokens[i] == '*' || tokens[i] == '/') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.push(tokens[i]);
            }
        }
        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        return values.pop();
    }

    public static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    public int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0){
                    errorHandler(-3," ");
                    return Integer.MAX_VALUE;
                }
                return a / b;
        }
        return 0;
    }
}
