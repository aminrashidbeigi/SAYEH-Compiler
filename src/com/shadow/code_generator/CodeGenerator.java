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
    private Stack<String> operatorStack;
    private int numOfExpressions = 0;
    private int lastValue;
    private Map<String, Integer> variableMemoryPosition;
    private String lastVariable = "";
    private Stack<Integer> parenthesisStack;
    private boolean inIf = false;
    private ArrayList<String> inIfCodes;
    private int lastIfValue = 0;

    public CodeGenerator(ArrayList<String> tokens) {
        ssm = new SyntaxStateMachine(StatementTransitionTable.stt, ExpressionTransitionTable.ett, tokens, new int[1]);
        R = new int[4];
        isValidRegisterIndex = new boolean[4];
        memory = new String[1024];
        inIfCodes = new ArrayList<>();
        variableInMemoryIndex = new HashMap();
        processingRegisters = new Stack<>();
        operandStack = new Stack<>();
        operatorStack = new Stack<>();
        parenthesisStack = new Stack<>();
        variableMemoryPosition = new HashMap<>();
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
            if (token.equals("("))
                parenthesisStack.push(1);
            else if (token.equals(")"))
                parenthesisStack.pop();
            if (scs == 14 || scs == 15 || scs == 7){
                key = ssm.expressionKeywordValueGenerator(token);
                if (!token.equals("{"))
                    System.out.println(token);
                    ecs = ExpressionTransitionTable.ett[ecs][key];
                if (parenthesisStack.isEmpty()){
                    calculate();
//                    lastIfValue = operandStack.pop();
                    inIf = true;
                    scs = 0;
                    continue;
                }
                expressionCodeGeneratorHandler(ecs, token, key);
            } else {
                key = ssm.statementKeywordValueGenerator(token);
                ecs = 0;
                if (token.equals("}") ){
                    if (inIf){
                        loopAndIfJumpCheck();
                        inIf = false;
                    }
                    scs = 0;
                    continue;
                } else if (token.equals("{"))
                    continue;
                scs = stt.stt[scs][key];
                if (scs == -8 && ls == 10 && key == 12) scs = 4;
                statementCodeGeneratorHandler(scs, token, key);
            }
            ls = scs;
        }
    }

    private void expressionCodeGeneratorHandler(int cs, String token, int key){
        switch (cs){
            case 8:
            case 1:
                if (token.equals(")")){
                    if (!operatorStack.contains('('))
                        break;
                    while (!operatorStack.peek().equals("("))
                        operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                    operatorStack.pop();
                } else if (token.equals("true")){
                    String bits  = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(1)).replace(" ","0");
                    int registerIndex = uselessRegisterIndexFinder();
                    System.out.println("token: " + token);
                    System.out.println("R_" + registerIndex);
                    processingRegisters.push(registerIndex);
                    mil(registerIndex, bits);
                    mih(registerIndex, bits);
                    operandStack.push(1);
                }
                else if (token.equals("false")){
                    String bits  = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(0)).replace(" ","0");
                    int registerIndex = uselessRegisterIndexFinder();
                    System.out.println("token: " + token);
                    System.out.println("R_" + registerIndex);
                    processingRegisters.push(registerIndex);
                    mil(registerIndex, bits);
                    mih(registerIndex, bits);
                    operandStack.push(0);
                }
                break;

            case 6:
            case 2:
                if (token.equals(")")){
                    while (!operatorStack.peek().equals("("))
                        operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                    operatorStack.pop();
                }
                else{
                    if (key == 3){
                        lastValue = Integer.parseInt(token);
                        operandStack.push(lastValue);
                    }
                    else {
                        int memoryAddressForLoad = uselessRegisterIndexFinder();
                        String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(variableMemoryPosition.get(token))).replace(" ","0");
                        processingRegisters.push(memoryAddressForLoad);
                        mil(memoryAddressForLoad, bits);
                        mih(memoryAddressForLoad, bits);
                        int loadedRegister = uselessRegisterIndexFinder();
                        lda(loadedRegister, memoryAddressForLoad);
                        operandStack.push(memoryAddressForLoad - 1000);
                    }
                }
                break;

            case 7:
            case 3:
                if (token.equals(")")){
                    while (operatorStack.peek().equals("("))
                        operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                    operatorStack.pop();
                }
                else{
                    int memoryAddressForLoad = uselessRegisterIndexFinder();
                    String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(variableMemoryPosition.get(token))).replace(" ","0");
                    processingRegisters.push(memoryAddressForLoad);
                    mil(memoryAddressForLoad, bits);
                    mih(memoryAddressForLoad, bits);
                    int loadedRegister = uselessRegisterIndexFinder();
                    lda(loadedRegister, memoryAddressForLoad);
                    operandStack.push(memoryAddressForLoad - 1000);
                }
                break;

            case 5:
            case 4:
                if (token.equals("(")) {
                    operatorStack.push(token);
                } else if (token.equals(">") || token.equals("<") || token.equals("==") || token.equals("!=") ||
                        token.equals(">=") || token.equals("<=") || token.equals("+") || token.equals("-") ||
                        token.equals("*") || token.equals("/")) {
                    while (!operatorStack.empty() && hasPrecedence(token, operatorStack.peek()))
                        operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                    operatorStack.push(token);
                }
                break;

            case 9:
                if (token.equals("(")) {
                    operatorStack.push(token);
                } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
                    while (!operatorStack.empty() && hasPrecedence(token, operatorStack.peek()))
                        operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                    operatorStack.push(token);
                }
                break;

            default:
                break;
        }
    }

    private void statementCodeGeneratorHandler(int cs, String token, int key){
        switch (cs){
            case 0 : {
                expressionCheckCodeToPrint(token, cs, key);
                break;
            }

            case 2 : {
                lastVariable = token;
                memoryFiller(token);
                expressionCheckCodeToPrint(token,cs, key);
                break;
            }

            case 4 : {
                expressionCheckCodeToPrint(token,cs, key);
                break;
            }

            case 6 : {
                lastVariable = token;
                memoryFiller(token);
                expressionCheckCodeToPrint(token,cs, key);
                break;
            }

            case 9 : {
                lastVariable = token;
                expressionCheckCodeToPrint(token,cs, key);
                memoryFiller(token);
                break;
            }

            case 10: {
                expressionCheckCodeToPrint(token,cs, key);
                break;
            }

            case 11: {
                expressionCheckCodeToPrint(token,cs, key);
                break;
            }

            case 12: {
                expressionCheckCodeToPrint(token,cs, key);
                memoryFiller(token);
                lastVariable = token;
            }

            case 13: {
                expressionCheckCodeToPrint(token,cs, key);
                break;
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
        variableMemoryPosition.put(data, lastEmptyMemoryWordIndex);
        memory[lastEmptyMemoryWordIndex] = data;
    }

    private int uselessRegisterIndexFinder(){
        int index = 0;
        for (int i = 0; i < 63; i++){
            index = i;
//            System.out.println(processingRegisters);
            if (!processingRegisters.contains(index)) break;
        }
        if (index > 4){
            awp(1);
        }
        return index;
    }

    private int registerIndexInStack(int R){
        for (int i = 0;i < processingRegisters.size(); i++){
            if (processingRegisters.get(i) == R)
                return i;
        }
        return -1;
    }

    private void expressionCheckCodeToPrint(String token, int cs, int key){

        if (cs == 0){
            if (ls == 11 || ls == 7 || ls == 4){
                if (ls == 11) calculate();
                String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(lastUsedMemoryWord)).replace(" ","0");
                int registerIndex = uselessRegisterIndexFinder();
                Rd = registerIndex;
                variableRegister = registerIndex;
                System.out.println("token: " + lastVariable);
                System.out.println("R_" + registerIndex);
                processingRegisters.push(registerIndex);
                mil(registerIndex, bits);
                mih(registerIndex, bits);
                sta();
                processingRegisters.removeAllElements();
            }
            numOfExpressions = 0;
        } else if (cs == 10) {
            if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/")) {
                while (!operatorStack.empty() && hasPrecedence(token, operatorStack.peek()))
                    operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                operatorStack.push(token);
            }
            if (!token.equals("="))
                numOfExpressions++;
        } else if (cs == 11){
            if (token.equals(")")){
                while (operatorStack.peek().equals("("))
                    operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
                operatorStack.pop();
            }
            else{
                if (token.equals("true")){
                    lastValue = 1;
                    operandStack.push(lastValue);
                }
                else if (token.equals("false")){
                    lastValue = 0;
                    operandStack.push(lastValue);
                }
                else if (key == 17){
                    lastValue = Integer.parseInt(token);
                    operandStack.push(lastValue);
                }
                else {
                    int memoryAddressForLoad = uselessRegisterIndexFinder();
                    String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(variableMemoryPosition.get(token))).replace(" ","0");
                    processingRegisters.push(memoryAddressForLoad);
                    mil(memoryAddressForLoad, bits);
                    mih(memoryAddressForLoad, bits);
                    int loadedRegister = uselessRegisterIndexFinder();
                    lda(loadedRegister, memoryAddressForLoad);
                    operandStack.push(memoryAddressForLoad - 1000);
                }
            }
            numOfExpressions++;
        } else if (cs == 4) {
            char[] chars = token.toCharArray();
            String bits  = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString((int)chars[1])).replace(" ","0");
            int registerIndex = uselessRegisterIndexFinder();
            Rs = registerIndex;
            System.out.println("token: " + token);
            System.out.println("R_" + registerIndex);
            lastValue = (int)token.charAt(1);
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
        } else if (cs == 13) {
            String bits  = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(1)).replace(" ","0");
            int registerIndex = uselessRegisterIndexFinder();
            System.out.println("token: " + token);
            System.out.println("R_" + registerIndex);
            processingRegisters.push(registerIndex);
            mil(registerIndex, bits);
            mih(registerIndex, bits);

            int memoryAddressForLoad = uselessRegisterIndexFinder();
            bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(variableMemoryPosition.get(lastVariable))).replace(" ","0");
            processingRegisters.push(memoryAddressForLoad);
            mil(memoryAddressForLoad, bits);
            mih(memoryAddressForLoad, bits);
            lda(memoryAddressForLoad, memoryAddressForLoad);

            if (token.equals("++"))
                add(memoryAddressForLoad, registerIndex);
            else if (token.equals("--"))
                sub(memoryAddressForLoad, registerIndex);

        }
    }

    private void loopAndIfJumpCheck(){
        int register = uselessRegisterIndexFinder();
        String bits = "0000000000000000";
        processingRegisters.push(register);
        mil(register, bits);
        mih(register, bits);
        cmp(processingRegisters.pop(), lastIfValue);
        inIf = false;
        brz(inIfCodes.size());
        for (String s : inIfCodes){
            System.out.println(s);
        }
        inIfCodes.clear();
    }

    private int variableRegister = 0;

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
        while (!operatorStack.empty()){
            if (operatorStack.size() == 1){
                lastIfValue = operandStack.peek();
            }
            operandStack.push(applyOp(operatorStack.pop(), operandStack.pop(), operandStack.pop()));
        }
        return operandStack.pop();
    }


    public static boolean hasPrecedence(String op1, String op2) {
        int o1 = precedenceNumber(op1);
        int o2 = precedenceNumber(op2);

        if(o1 <= o2 || o1 == -1)
            return true;
        else
            return false;
    }

    private static int precedenceNumber(String s){
        switch (s){
            case "|" : return 0;
            case "&" : return 1;
            case "!" : return 2;
            case ">" : return 2;
            case "<" : return 2;
            case "<=" : return 2;
            case ">=" : return 2;
            case "==" : return 2;
            case "!=" : return 2;
            case "-" : return 4;
            case "+" : return 4;
            case "*" : return 5;
            case "/" : return 5;
            case "(" : return -1;
            case ")" : return 6;
        }
        return -1;
    }

    public int applyOp(String op, int b, int a) {
        int [] registers = new int[2];
        switch (op) {

            case "+":
                registers = addToRegisters(a,b);
                add(registers[0], registers[1]);
                return registers[0]-1000;

            case "-":
                registers = addToRegisters(a,b);
                sub(registers[0], registers[1]);
                return registers[0]-1000;

            case "*":
                registers = addToRegisters(a,b);
                mul(registers[0], registers[1]);
                return registers[0]-1000;

            case "<=":
            case ">":
                registers = addToRegisters(a,b);
                cmp(registers[1], registers[0]);
                brc(4);

                String bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(1)).replace(" ","0");
                int registerIndex1 = uselessRegisterIndexFinder();
                processingRegisters.push(registerIndex1);
                mil(registerIndex1, bits);
                mih(registerIndex1, bits);

                jpr(3);

                bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(0)).replace(" ","0");
                registerIndex1 = uselessRegisterIndexFinder();
                processingRegisters.push(registerIndex1);
                mil(registerIndex1, bits);
                mih(registerIndex1, bits);

                return registers[0]-1000;

            case ">=":
            case "<":
                registers = addToRegisters(a,b);
                cmp(registers[0], registers[1]);
                brc(4);

                bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(1)).replace(" ","0");
                registerIndex1 = uselessRegisterIndexFinder();
                processingRegisters.push(registerIndex1);
                mil(registerIndex1, bits);
                mih(registerIndex1, bits);

                jpr(3);
                bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(0)).replace(" ","0");
                registerIndex1 = uselessRegisterIndexFinder();
                processingRegisters.push(registerIndex1);
                mil(registerIndex1, bits);
                mih(registerIndex1, bits);

                return registers[0]-1000;

            case "|":
                registers = addToRegisters(a,b);
                orr(registers[0], registers[1]);
                return registers[0]-1000;
        }
        return 0;
    }

    private int[] addToRegisters(int a, int b){
        int [] registers = new int[2];
        String bits;
        int registerIndex1 = 0;
        int registerIndex2 = 0;
        if (a < -995 && a > -1001) {
            a = a +1000;
            registerIndex1 = a;
            System.out.println("Rd to ... : " + registerIndex1);
        } else {
            bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(a)).replace(" ","0");
            registerIndex1 = uselessRegisterIndexFinder();
            System.out.println("token: " + a);
            System.out.println("R_" + registerIndex1);
            processingRegisters.push(registerIndex1);
            mil(registerIndex1, bits);
            mih(registerIndex1, bits);
        }

        if (b < -995 && b > -1001){
            b = b + 1000;
            registerIndex2 = b;
            System.out.println("Rs to sub : " + registerIndex2);
        } else {
            bits = String.format("%"+Integer.toString(16)+"s",Integer.toBinaryString(b)).replace(" ","0");
            registerIndex2 = uselessRegisterIndexFinder();
            System.out.println("token: " + b);
            System.out.println("R_" + registerIndex2);
            processingRegisters.push(registerIndex2);
            mil(registerIndex2, bits);
            mih(registerIndex2, bits);
        }
        registers[0] = registerIndex1;
        registers[1] = registerIndex2;
        return registers;
    }

    private void add(int Rdd, int Rss){
        if (Rdd > Rss){
            int temp = Rdd;
            Rdd = Rss;
            Rss = temp;
        }
        Rs = Rdd;
        if (inIf){
            inIfCodes.add("add : " + "1011" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
            processingRegisters.remove(registerIndexInStack(Rss));
        } else {
            System.out.println("add : " + "1011" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
            System.out.println(processingRegisters);
            processingRegisters.remove(registerIndexInStack(Rss));
            System.out.println(processingRegisters);
        }
    }

    private void sub(int Rdd, int Rss){
        System.out.print("sub : ");
        if (Rdd > Rss){
            int temp = Rdd;
            Rdd = Rss;
            Rss = temp;
        }
        Rs = Rdd;
        if (inIf){
            inIfCodes.add("1100" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
        } else {
            System.out.println("1100" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
        }

        if (registerIndexInStack(Rss) > 0){
            processingRegisters.remove(registerIndexInStack(Rss));
        }
    }

    private void mul(int Rdd, int Rss){
        if (Rdd > Rss){
            int temp = Rdd;
            Rdd = Rss;
            Rss = temp;
        }
        Rs = Rdd;
        if (registerIndexInStack(Rss) > 0){
            processingRegisters.remove(registerIndexInStack(Rss));
        }
        if (inIf){
            inIfCodes.add("mul : " + "1101" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
        } else {
            System.out.println("mul : " + "1101" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
            System.out.println(processingRegisters);
        }

    }

    private void orr(int Rdd, int Rss){
        if (Rdd > Rss){
            int temp = Rdd;
            Rdd = Rss;
            Rss = temp;
        }
        Rs = Rdd;
        if (inIf){
            inIfCodes.add("add : " + "0111" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
            processingRegisters.remove(registerIndexInStack(Rss));
        } else{
            System.out.println("add : " + "0111" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000");
            processingRegisters.remove(registerIndexInStack(Rss));
            System.out.println(processingRegisters);
        }

    }

    private void mil(int registerIndex, String bits){
        if(inIf){
            String s = "mil : " + "1111" + binaryRegisterIndex(registerIndex) + "00";
            for (int i = 8; i < 16; i++)
                s += bits.toCharArray()[i];
            inIfCodes.add(s);
        } else {
            System.out.print("mil : " + "1111" + binaryRegisterIndex(registerIndex) + "00");
            for (int i = 8; i < 16; i++)
                System.out.print(bits.toCharArray()[i]);
            System.out.println();
        }
    }

    private void mih(int registerIndex, String bits){
        if (inIf){
            String s = "mih : " + "1111" + binaryRegisterIndex(registerIndex) + "01";
            for (int i = 0; i < 8; i++)
                s += bits.toCharArray()[i];
            inIfCodes.add(s);
        }else {
            System.out.print("mih : " + "1111" + binaryRegisterIndex(registerIndex) + "01");
            for (int i = 0; i < 8; i++)
                System.out.print(bits.toCharArray()[i]);
            System.out.println();
            System.out.println(processingRegisters);
        }
    }

    private void sta(){
        if (Rd > Rs && Rd != variableRegister){
            int temp = Rd;
            Rd = Rs;
            Rs = temp;
        }
        //        processingRegisters.remove(registerIndexInStack(Rd));
        if (inIf){
            inIfCodes.add("sta : " + "0011" + binaryRegisterIndex(Rd) + binaryRegisterIndex(Rs) + "00000000");
            inIfCodes.add("mvr : " + "0001" + binaryRegisterIndex(Rs) + binaryRegisterIndex(Rd) + "00000000");
        } else {
            System.out.println(processingRegisters);
            System.out.print("sta : " + "0011" + binaryRegisterIndex(Rd) + binaryRegisterIndex(Rs) + "00000000\n");
            System.out.print("mvr : " + "0001" + binaryRegisterIndex(Rs) + binaryRegisterIndex(Rd) + "00000000\n");
        }
    }

    private void lda(int Rdd, int Rss){
        if (Rdd > Rss && Rdd != variableRegister){
            int temp = Rdd;
            Rdd = Rss;
            Rss = temp;
        }
        if (inIf){
            inIfCodes.add("lda : " + "0010" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000\n");
            inIfCodes.add("mvr : " + "0001" + binaryRegisterIndex(Rss) + binaryRegisterIndex(Rdd) + "00000000\n");
        } else {
            System.out.print("lda : " + "0010" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000\n");
            System.out.print("mvr : " + "0001" + binaryRegisterIndex(Rss) + binaryRegisterIndex(Rdd) + "00000000\n");

        }
    }

    private void cmp(int Rdd, int Rss){
        if (inIf){
            inIfCodes.add("cmp : " + "1110" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000\n");
        } else {
            System.out.print("cmp : " + "1110" + binaryRegisterIndex(Rdd) + binaryRegisterIndex(Rss) + "00000000\n");
        }

    }

    private void brz(int number){
        if (inIf){
            inIfCodes.add("brz : " + "0000" + "10" + "00" +
                    String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(number)).replace(" ","0"));
        } else {
            System.out.print("brz : " + "0000" + "10" + "00" +
                    String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(number)).replace(" ","0") + "\n");

        }
    }

    private void brc(int number){
        if (inIf){
            inIfCodes.add("brc : " + "0000" + "10" + "01" +
                    String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(number)).replace(" ","0"));
        } else {
            System.out.print("brc : " + "0000" + "10" + "01" +
                    String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(number)).replace(" ","0") + "\n");

        }
    }

    private void jpr(int number){
        if (inIf){
            inIfCodes.add("jpr : " + "0000" + "01" + "11" +
                    String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(number)).replace(" ","0"));

        } else {
            System.out.print("jpr : " + "0000" + "01" + "11" +
                    String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(number)).replace(" ","0") + "\n");
        }
    }

    private void awp(int num){
        if (inIf){
            inIfCodes.add("awp : " + "00001010" + String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(num)).replace(" ","0"));
        } else {
            System.out.println("awp : " + "00001010" + String.format("%"+Integer.toString(8)+"s",Integer.toBinaryString(num)).replace(" ","0"));
        }
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
