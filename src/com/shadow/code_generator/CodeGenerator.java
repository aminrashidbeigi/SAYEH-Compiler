package com.shadow.code_generator;

import com.shadow.syntax.ExpressionTransitionTable;
import com.shadow.syntax.StatementTransitionTable;
import com.shadow.syntax.SyntaxStateMachine;

import java.util.*;

/**
 * Created by Amin Rashidbeigi on 6/11/2017.
 */
public class CodeGenerator {

    private int[] R;
    private String[] memory;
    private StatementTransitionTable stt;
    private SyntaxStateMachine ssm;
    private Map variableInMemoryIndex;
    private boolean[] isValidRegisterIndex;
    private int lastUsedMemoryWord;
    private Stack<Integer> processingRegisters;
    private int Rd;
    private int Rs;
    private int ls;
    private Stack<Integer> operandStack;
    private Stack<Character> operatorStack;
    private int numOfExpressions = 0;
    private int lastValue;

    public CodeGenerator(ArrayList<String> tokens) {
        ssm = new SyntaxStateMachine(StatementTransitionTable.stt, ExpressionTransitionTable.ett, tokens, new int[1]);
        R = new int[4];
        isValidRegisterIndex = new boolean[4];
        memory = new String[1024];
        variableInMemoryIndex = new HashMap();
        processingRegisters = new Stack<>();
        operandStack = new Stack<>();
        operatorStack = new Stack<>();
        Arrays.fill(R, 0);
        Arrays.fill(memory, "");
        Arrays.fill(isValidRegisterIndex, true);
        codeGeneratorStateMachine(tokens);

    }

    private int scs = 0;
    private void codeGeneratorStateMachine(ArrayList<String> tokens){
        int ecs = 0;

        for (String token : tokens){
            int key;
            if (token.equals(";")) scs = 0;
            if (scs == 14 || scs == 15 || scs == 7){
                key = ssm.expressionKeywordValueGenerator(token);
                ecs = ExpressionTransitionTable.ett[ecs][key];
                expressionCodeGeneratorHandler(ecs, token);
            } else {
                key = ssm.statementKeywordValueGenerator(token);
                scs = stt.stt[scs][key];
                codeGeneratorStateHandler(scs, token);
            }
            ls = scs;
        }
    }

    private void expressionCodeGeneratorHandler(int cs, String token){
        switch (cs){
            case 1 : {
                checkCodeToPrint(token,cs);
                break;
            }
        }
    }

    private String lastVariable = "";

    private void codeGeneratorStateHandler(int cs, String token){
        switch (cs){
            case 0 : {
                checkCodeToPrint(token, cs);
                break;
            }

            case 2 : {
                lastVariable = token;
                memoryFiller(token);
                checkCodeToPrint(token,cs);
                break;
            }

            case 4 : {
                checkCodeToPrint(token,cs);
                break;
            }

            case 6 : {
                lastVariable = token;
                memoryFiller(token);
                checkCodeToPrint(token,cs);
                break;
            }

            case 9 : {
                lastVariable = token;
                checkCodeToPrint(token,cs);
                memoryFiller(token);
                break;
            }

            case 10: {
                checkCodeToPrint(token,cs);
                break;
            }
            case 11: {
                checkCodeToPrint(token,cs);
                break;
            }

            case 12: {
                lastVariable = token;
            }

            default:
                break;
        }
    }

    private void memoryFiller(String data){
        int lastEmptyMemoryWordIndex = 1024;
        for (int i = 1023; i > 0; i--){
            if (memory[i].equals("")){
                lastEmptyMemoryWordIndex = i;
                break;
            }
        }

        lastUsedMemoryWord = lastEmptyMemoryWordIndex;
        System.out.println(data + " --> " + lastEmptyMemoryWordIndex);
        variableInMemoryIndex.put(data, lastEmptyMemoryWordIndex);
        memory[lastEmptyMemoryWordIndex] = data;
    }

    private int uselessRegisterIndexFinder(){
        int index = 0;
        for (int i = 0; i < 4; i++){
            index = i;
//            System.out.println(processingRegisters);
            if (!processingRegisters.contains(index)) break;
        }
        return index;
    }

    private void checkCodeToPrint(String token, int cs){

        if (cs == 0){

            if (ls == 11 || ls == 7 || ls == 4){
                calculate();
                String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(lastUsedMemoryWord)).replace(" ","0");
                int registerIndex = uselessRegisterIndexFinder();
                Rd = registerIndex;
                System.out.println("token: " + lastVariable);
                System.out.println("R_" + registerIndex);
                processingRegisters.push(registerIndex);
                processingRegisters.removeAllElements();
                mil(registerIndex, bits);
                mih(registerIndex, bits);
                sta();
            }
            numOfExpressions = 0;
        } else if (cs == 10) {
//            if (ls == 11){

                if (token.equals("(")) {
                    operatorStack.push(token.charAt(0));
                } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
                    while (!operatorStack.empty() && hasPrecedence(token.charAt(0), operatorStack.peek()))
                        operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                    operatorStack.push(token.charAt(0));
                }
                numOfExpressions++;
//            }
        } else if (cs == 11){
            if (token.equals(")")){
                while (operatorStack.peek() != '(')
                    operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                operatorStack.pop();
            }
            else{
                lastValue = Integer.parseInt(token);
                operandStack.push(lastValue);
            }
            numOfExpressions++;
        } else if (cs == 4) {
            char[] chars = token.toCharArray();
            String bits  = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString((int)chars[1])).replace(" ","0");
            int registerIndex = uselessRegisterIndexFinder();
            Rs = registerIndex;
            System.out.println("token: " + token);
            System.out.println("R_" + registerIndex);
            processingRegisters.push(registerIndex);
            mil(registerIndex, bits);
            mih(registerIndex, bits);
        } else if (token.equals("true") || token.equals("false")){
            int number;
            if (token.equals("true")) number = 1;
            else number = 0;
            String bits  = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(number)).replace(" ","0");
            int registerIndex = uselessRegisterIndexFinder();
            Rs = registerIndex;
            System.out.println("token: " + token);
            System.out.println("R_" + registerIndex);
            processingRegisters.push(registerIndex);
            mil(registerIndex, bits);
            mih(registerIndex, bits);
        }
    }


    public int calculate() {
        if (numOfExpressions == 1){
            String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(lastValue)).replace(" ","0");
            int registerIndex = uselessRegisterIndexFinder();
            Rs = registerIndex;
            System.out.println("token: " + lastValue);
            System.out.println("R_" + registerIndex);
            processingRegisters.push(registerIndex);
            mil(registerIndex, bits);
            mih(registerIndex, bits);

        }
        while (!operatorStack.empty())
            operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
        return operandStack.pop();
    }

    public static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')')
            return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    private int RsIndex = 0;
    public int applyOp(char op, int b, int a) {
        switch (op) {
            case '+':
                String bits;
                int registerIndex;
                if (a < -995 && a > -1001) {
                    a = a +1000;
                    Rd = a;
                    System.out.println("Rd to add" + Rd);
                } else {
                    bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(a)).replace(" ","0");
                    registerIndex = uselessRegisterIndexFinder();
                    Rd = registerIndex;
                    System.out.println("token: " + a);
                    System.out.println("R_" + registerIndex);
                    processingRegisters.push(registerIndex);
                    mil(registerIndex, bits);
                    mih(registerIndex, bits);

                }

                if (b < -995 && b > -1001){
                    b = b + 1000;
                    Rs = b;
                    System.out.println("Rs to add " + Rs);
                } else {
                    bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(b)).replace(" ","0");
                    registerIndex = uselessRegisterIndexFinder();
                    Rs = registerIndex;
                    RsIndex = Rs;
                    System.out.println("token: " + b);
                    System.out.println("R_" + registerIndex);
                    mil(registerIndex, bits);
                    mih(registerIndex, bits);
                    processingRegisters.push(registerIndex);
                }

                add();

                return Rd-1000;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0){
                    return Integer.MAX_VALUE;
                }
                return a / b;
        }
        return 0;
    }
    private void add(){
        System.out.print("add : ");
        System.out.println("1011" + binaryRegisterIndex(Rd) + binaryRegisterIndex(Rs) + "00000000");
        processingRegisters.pop();
        System.out.println(processingRegisters);
    }


    private void mil(int registerIndex, String bits){
        System.out.print("mil : ");
        System.out.print("1111" + binaryRegisterIndex(registerIndex) + "00");
        for (int i = 8; i < 16; i++)
            System.out.print(bits.toCharArray()[i]);
        System.out.println();
    }

    private void mih(int registerIndex, String bits){
        System.out.print("mih : ");
        System.out.print("1111" + binaryRegisterIndex(registerIndex) + "01");
        for (int i = 0; i < 8; i++)
            System.out.print(bits.toCharArray()[i]);
        System.out.println();
    }

    private void sta(){
        System.out.print("sta : ");
        System.out.print("0011" + binaryRegisterIndex(Rd) + binaryRegisterIndex(Rs) + "00000000");
        isValidRegisterIndex[Rs] = true;
        isValidRegisterIndex[Rd] = true;
    }


    private String binaryRegisterIndex(int number){
        switch (number){
            case 0 :
                return "00";
            case 1 :
                return "01";
            case 2:
                return "10";
            case 3:
                return "11";
        }
        return "00";
    }


}
