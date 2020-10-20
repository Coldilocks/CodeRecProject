package codeAnalysis.textTokenProcess;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenize {

    public String splitCamelCaseOld(String clazzName)
    {
        String reg = "[A-Z][a-z]+";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(clazzName);
        String reg1 = "^[a-z][a-z]*";
        Pattern p1 = Pattern.compile(reg1);
        Matcher m1 = p1.matcher(clazzName);
        String reg2 = "[A-Z][A-Z]+";
        Pattern p2 = Pattern.compile(reg2);
        Matcher m2 = p2.matcher(clazzName);
        StringBuilder sb = new StringBuilder();
        while(m1.find())
        {
            sb.append(m1.group().toLowerCase()).append("_");
        }
        while(m.find()){
            sb.append(m.group().toLowerCase()).append("_");
        }
        while(m2.find()){
            sb.append(m2.group().toLowerCase()).append("_");
        }
        try {
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }catch(Exception e){
            String str = "i";
            return str;
        }catch(Error e){
            String str = "i";
            return str;
        }
    }

    //移除变量名中的数字和方括号
    public  String removeNumberAndParentheses(String string){
        if(string != null) {
            string = string.replaceAll("0", "_");
            string = string.replaceAll("1", "_");
            string = string.replaceAll("2", "_");
            string = string.replaceAll("3", "_");
            string = string.replaceAll("4", "_");
            string = string.replaceAll("5", "_");
            string = string.replaceAll("6", "_");
            string = string.replaceAll("7", "_");
            string = string.replaceAll("8", "_");
            string = string.replaceAll("9", "—");
            string = string.replaceAll("\\[\\]","_");
        }
        return string;

    }

    //分割用_或$符号命名的变量
    public List<String> splitSpecialCharacter(String string){
        List<String> list = new ArrayList<>();
        String[] result = null;
        result = string.split("_|\\$");
        for(int i = 0; i < result.length; i ++){
            if(!result[i].equals("")){
                list.add(result[i]);
            }
        }
        return list;
    }

    public  String splitCamelCase(String tokenToSplit){
        try {
            LinkedList<String> tokens = new LinkedList<>();
            StringBuilder tmpToken = new StringBuilder();
            char preChar = '\0';
            char thisChar = '\0';
            char nextChar = '\0';

            int tokenSize = tokenToSplit.length();
            for (int i = 0; i < tokenSize; i++) {
                thisChar = tokenToSplit.charAt(i);
                if (i < tokenSize - 1) {
                    nextChar = tokenToSplit.charAt(i + 1);
                }

                if (Character.isLowerCase(thisChar)) {
                    tmpToken.append(thisChar);
                } else if (Character.isUpperCase(thisChar)) {
                    if (tmpToken.length() > 0 && (Character.isLowerCase(preChar) || Character.isLowerCase(nextChar))) {
                        tokens.addLast(tmpToken.toString());
                        tmpToken = new StringBuilder();
                    }
                    tmpToken.append(thisChar);
                } else {
                    tmpToken.append(thisChar);
                    if (!Character.isDigit(nextChar)) {
                        tokens.addLast(tmpToken.toString());
                        tmpToken = new StringBuilder();
                    }
                }
                preChar = thisChar;
            }
            if (tmpToken.length() > 0) {
                tokens.addLast(tmpToken.toString());
            }

            StringBuilder result = new StringBuilder();
            if (tokens.size() > 0) {
                for (int i = 0; i < tokens.size() - 1; i++) {
                    result.append(tokens.get(i).toLowerCase()).append("_");
                }
                result.append(tokens.getLast().toLowerCase());
            }
            return result.toString();
        }catch(Exception e){
            return "i";
        }catch(Error e){
            return "i";
        }
    }


}
