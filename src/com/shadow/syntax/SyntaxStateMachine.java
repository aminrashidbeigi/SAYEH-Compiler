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
        int cs = 0;
        int ns = cs;
        for (String string : tokens){
            cs = ns;
            switch (cs){
                case 0 :
                    if (string.equals("int")) ns = 8;
                    else if (string.equals("char")) ns = 1;
                    else if (string.equals("bool")) ns = 5;
                    else if (string.equals("while")) ns = 15;
                    else if (string.equals("if")) ns = 14;
                    else if (isVariable(string)) ns = 12;
                    else {
                        System.out.println(string + "seen." + "Keyword Expected !");
                    }
                default:
                    System.out.println("Invalid token");
            }
        }
    }

    private boolean isVariable(String string){
        if (string.matches("^([a − z][A − Z]) + ([a − z][A − Z][0 − 9])∗")) return true;
        return false;
    }
}
