package com.shadow;

import com.shadow.code_generator.CodeGenerator;
import com.shadow.lexical.InputReader;
import com.shadow.semantic.SemanticStateMachine;
import com.shadow.syntax.ExpressionTransitionTable;
import com.shadow.syntax.StatementTransitionTable;
import com.shadow.syntax.SyntaxStateMachine;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Amin Rashidbeigi on 6/3/2017.
 */
public class Executor {
    private ArrayList<String> tokens;
    private Map lineMap;
    String fileDirectory = "D:\\Uni\\Terms\\4\\Architecture - Dr Shiri\\Project\\Compiler\\ShadowCompiler\\examples\\test.c";

    CodeGenerator cg;
    public Executor() {
        InputReader ir = new InputReader(fileDirectory);
        tokens = ir.getTokens();
        SyntaxStateMachine ssm = new SyntaxStateMachine(StatementTransitionTable.stt, ExpressionTransitionTable.ett, tokens, ir.getTokensOfEachLine());
        ssm.syntaxHandler();
        if (ssm.isSyntaxOK()){
            SemanticStateMachine semanticSM = new SemanticStateMachine(tokens);
            semanticSM.semanticHandler();
            if (semanticSM.isSemanticOk())
                cg = new CodeGenerator(tokens);
        }
    }

    public static void main(String[] args) {
        new Executor();
    }
}
