package utils;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestDataUtil {

    private static boolean isDebug = false;

    public static String getTrace(Graph graph){
        String ft = graph.getSourceCodeTrace();
        return ft;
    }

    public static String getSourceCode(String path) {
        String sc = "";
        int cnt = 1;
        try {
            Scanner scanner = new Scanner(new File(path));
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                cnt++;
                if(isDebug){
                    System.out.println(cnt + ": " + line);
                }
                sc += line + "\r\n";

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sc;
    }

    public static HashMap<Integer, String> getSourceLines(String path){
        HashMap<Integer,String> lines = new HashMap<>();
        int cnt = 1;
        try {
            Scanner scanner = new Scanner(new File(path));
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                lines.put(cnt, line);
                cnt++;
                if(isDebug){
                    System.out.println(cnt + ": " + line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static int getBeginLine(GraphNode node){
        String scinfo = node.getInfo();
        int i = scinfo.indexOf(" ");
        int begin = Integer.parseInt(scinfo.substring(0,i));
        return begin;
    }

    public static int getEndLine(GraphNode node){
        String scinfo = node.getInfo();
        int i = scinfo.indexOf(" ");
        int j = scinfo.indexOf(" ",i+1);
        int end = Integer.parseInt(scinfo.substring(i+1, j));
        return end;
    }

    public static String getStmt(GraphNode node){
        String scinfo = node.getInfo();
        int i = scinfo.indexOf(" ");
        int j = scinfo.indexOf(" ",i+1);
        String stmt = scinfo.substring(j);
        return stmt;
    }

    public static String convert2SourceCode(Map<Integer, String> sourceLines) {
        String sc = "";
        for (int i = 0; i < sourceLines.size(); i++) {
            sc = sc + sourceLines.get(i+1) + "\r\n";
        }
        return sc;
    }


    public static String convert2SourceCode(Map<Integer, String> sourceLines, int b, int e) {
        String sc = "";
        for (int i = b-1; i < e; i++) {
            sc = sc + sourceLines.get(i+1) + "\r\n";
        }
        return sc;
    }


    public static String replaceLine2Line(int begin, int end, Map<Integer, String> sourceLines, String mark) {
        StringBuilder rs = new StringBuilder(replaceIndex(sourceLines, begin, begin, end, mark));
        for (int i = begin + 1; i < end + 1; i++) {
            rs.append(" ");
            rs.append(replaceIndex(sourceLines, i, begin, end, ""));
        }
        return rs.toString();
    }

    private static String replaceIndex(Map<Integer, String> sourceLines, int i, int b, int e, String mark) {
        String stmt = sourceLines.get(i);
        String trimstmt = stmt.trim();
        String rs = "";
        if(i < e-1 && (trimstmt.startsWith("}")) && stmt.length() > 1) {
            int k = stmt.indexOf("}");
            if(b < i){// e.g. "}else if(){", remove the block before elseif
                sourceLines.put(i, mark + stmt.substring(0,k)+stmt.substring(k+1));
                rs = "}";
            }
            else if(b == i){// e.g. "}if(){", remove the if block after "}"
                sourceLines.put(i, stmt.substring(0, k+1) + mark);
                rs = stmt.substring(k+1).trim();
            }
        }
        else if(i < e-1 && trimstmt.startsWith("{") && stmt.length() > 1){
            int k = stmt.indexOf("{");
            if(b < i) {// e.g. "if() \r\n { ...", remove if block
                sourceLines.put(i, mark);
                rs = stmt.trim();
            }
            else if(b == i) {// e.g. "{ stmt", remove stmt after "{"
                sourceLines.put(i, stmt.substring(0, k+1) + mark);
                rs = stmt.substring(k+1).trim();
            }
        }
        else{
            sourceLines.put(i, mark);
            rs = stmt.trim();
        }
        return rs.replace("/*hole*/","").trim();// default
    }

    public static String replaceStmt(int line, String ostmt,Map<Integer, String> sourceLines, String mark) {
        String stmt = sourceLines.get(line);
        ostmt = ostmt.trim();// the original stmt is not trim... if ignore this step, there will be some bug.
        if(stmt.contains(ostmt)) {
            int i = stmt.indexOf(ostmt);
            int j = i + ostmt.length();
            sourceLines.put(line, stmt.substring(0, i) + mark + stmt.substring(j));
            return ostmt.replace("/*hole*/","").trim();
        }
        else{
            return replaceIndex(sourceLines, line, line, line, mark);
        }
    }
}
