package com.shadow.semantic;

import java.util.*;

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
            if (semanticKeyValueGenerator(token) == 1) lastVariable = token;
            ls = cs;
            if (key < 0){
                System.out.println(token + " : invalid token!");
                return;
            }
            cs = SemanticTransitionTable.semanticTT[cs][key];

            if (afterEq) {
                if (key == 8) {
                    afterEq = false;
                    variableValue.put(lastVariable, Integer.toString(mathExpressionEvaluator(afterEqString)));
                } else {
                    afterEqString += token + " ";
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



    public static int mathExpressionEvaluator(String expression) {
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

    public static int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0)
                    throw new
                            UnsupportedOperationException("Cannot divide by zero");
                return a / b;
        }
        return 0;
    }

}
