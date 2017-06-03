package com.shadow.lexical;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Amin Rashidbeigi on 6/2/2017.
 */
public class InputReader {

    private String fileName;
    private File file;
    private ArrayList<String> tokens;
    private ArrayList<Character> invalidTokens;

    public InputReader(String fileName) {
        this.fileName = fileName;
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

            for (String s : tempTokens){
                System.out.println(s);
            }
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

    public ArrayList<String> getTokens() {
        return tokens;
    }
}
