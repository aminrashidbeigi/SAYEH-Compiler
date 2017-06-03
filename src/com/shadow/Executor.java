package com.shadow;

import com.shadow.lexical.InputReader;
import com.shadow.syntax.StatementTransitionTable;
import com.shadow.syntax.SyntaxStateMachine;

import java.util.ArrayList;

/**
 * Created by Amin Rashidbeigi on 6/3/2017.
 */
public class Executor {
    private ArrayList<String> tokens;
    String fileDirectory = "D:\\Uni\\Terms\\4\\Architecture - Dr Shiri\\Project\\Compiler\\ShadowCompiler\\examples\\test.c";

    public Executor() {
        InputReader ir = new InputReader(fileDirectory);
        tokens = ir.getTokens();
        SyntaxStateMachine ssm = new SyntaxStateMachine(StatementTransitionTable.stt, tokens);
        ssm.syntaxHandler();
    }

    public static void main(String[] args) {
        new Executor();
    }
}
