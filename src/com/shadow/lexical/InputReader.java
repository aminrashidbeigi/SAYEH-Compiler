package com.shadow.lexical;

import java.io.*;
import java.util.*;

/**
 * Created by Amin Rashidbeigi on 6/2/2017.
 */
public class InputReader {

    private Map lineMap;
    private String fileName;
    private File file;
    private ArrayList<String> tokens;
    private ArrayList<Character> invalidTokens;
    private int[] tokensOfEachLine = new int[100];

    public InputReader(String fileName) {
        this.fileName = fileName;
        for (int i = 0; i < tokensOfEachLine.length; i++)
            tokensOfEachLine[i] = 0;
        lineMap = new HashMap();
        file = new File(fileName);
        tokens = new ArrayList<>();
//        invalidTokens = invalidTokensGenerator();
        tokens = tokensGenerator(file);
    }

    private ArrayList<String> tokensGenerator(File file){
        ArrayList<String> tempTokens = new ArrayList<>();
        ArrayList<String> tokens = new ArrayList<>();
        if (!file.exists()){
            System.out.println("File doesn't exist");
            return null;
        }
        if (!(file.isFile() && file.canRead())){
            System.out.println("File cannot be read");
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            String inputString = "";
            while (fis.available() > 0)
                inputString += (char)fis.read();

            mapFiller(inputString);
            for (String retval : inputString.split("\\s+"))
                if (!retval.isEmpty())
                    tempTokens.add(retval);

//            for (String s : tempTokens){
//                char[] chars = s.toCharArray();
//                String string = "";
//                int counter = 0;
//                for (char c : chars){
//                    counter++;
//                    if (!invalidTokens.contains(c)){
//                        string += c;
//                        if (counter == chars.length)
//                            tokens.add(string);
//
//                    } else {
//                        tokens.add(Character.toString(c));
//                    }
//                }
//            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempTokens;
    }


    private ArrayList<Character> invalidTokensGenerator(){
        ArrayList<Character> list = new ArrayList<>();
        list.add(' ');
        list.add('(');
        list.add(')');
        list.add('+');
        list.add('-');
        list.add('/');
        list.add('!');
        list.add('@');
        list.add('#');
        list.add('$');
        list.add('%');
        list.add('^');
        list.add('&');
        list.add('*');
        list.add('=');
        list.add('?');
        list.add(':');
        list.add(';');
        list.add('\"');
        return list;
    }

    private void mapFiller(String string){
        int lineNumber = 0;
        Scanner sc = new Scanner(string);
        int counter = 0;
        while (sc.hasNext()){
            int num = sc.nextLine().split("\\s+").length;
            if (num != 1)
                tokensOfEachLine[counter] = num;
            counter++;
        }

        for (String token : string.split("\\n+")){
            lineNumber++;
            for (String subToken : token.split("\\s+")){
                lineMap.put(subToken, lineNumber);
            }
        }
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public int[] getTokensOfEachLine() {
        return tokensOfEachLine;
    }
}
