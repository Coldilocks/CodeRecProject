package utils;

import groum.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * 工具类，用于还原数据中的API调用、变量名、参数名、常量等
 */
public class NameExtractor {

    private static LinkedList<String> skiptokens;
    private static LinkedList<String> skippostfix;
    private boolean isDebug = false;

    public static void main(String[] args) throws FileNotFoundException {
        NameExtractor ne = new NameExtractor();
        ne.init();

        //testAll(ne);
        testOne(ne);
    }

    private static void testOne(NameExtractor ne) {

        //String predictionLine = "java.util.Scanner.Field";// -> java.util.Scanner
        String predictionLine = "java.util.HashMap.put(java.lang.Object,java.lang.Object)";
        String originalLine = "58 model.put(\"title\", \"Layout example\")" ;

//        predictionLine = "java.lang.Thread.setName(java.lang.String)";
//        originalLine = "693 schedulerThread.setName(\"Quartz Scheduler [\" + scheduler.getSchedulerName() + \"]\")" ;

//        predictionLine = "java.util.regex.Matcher.quoteReplacement(java.lang.String)";
//        originalLine = "304 matcher.appendReplacement(output, Matcher.quoteReplacement(methodInvocation.getMethod().getName()))";

//        predictionLine = "java.io.StringReader.new(java.lang.String,java.lang.String)";
//        originalLine = "230 Reader reader = new StringReader(source,source2);";
//
//        predictionLine  = "java.lang.String[].length";
//        originalLine = "25 System.arraycopy(r, 0, a, l.length, r.length);";

//        predictionLine = "System.arraycopy(int,int,int,int,int)";
//        originalLine = "25 System.arraycopy(r, 0, a, l.length, r.length);";

//        predictionLine = "java.io.StringBuilder.append(java.lang.String).append(java.lang.String).ap(int)";
//        originalLine = "230 builder.append(string).append(s2).ap(i);";

//        predictionLine = "java.io.StringBuilder.append(java.lang.String,int).append(java.lang.String).ap(int)";
//        originalLine = "230 builder.append(string,i).append(s2).ap(i);";


//        predictionLine = "java.lang.StringBuffer.append(float).append(java.lang.String).append(float).append(java.lang.String)";
//        originalLine = "521 body.append(getLocalizedText(req, \"LOGIN_DISABLED_MSG1\")).append(\" \").append(getLocalizedText(req, \"LOGIN_DISABLED_MSG2\")).append(\"\\n\\n\")";

//        predictionLine = "java.io.StringBuilder.append(java.lang.String,int).append(String)";
//        originalLine = "230 builder.append(f(),i).append(j);";

//        predictionLine = "java.text.DateFormat.getDateTimeInstance().format(java.util.Date)";
//        originalLine = "101 DateFormat.getDateTimeInstance().format(date);";

        predictionLine = "java.lang.StringBuilder.append(java.lang.String).append(java.lang.CharSequence).append(java.lang.String)";
        originalLine = "284 selection.append(\"(\").append(inboxes).append(\")\");";

        predictionLine = "java.lang.StringBuilder.append(java.lang.String).append(java.lang.CharSequence).append(java.lang.String)";
        originalLine = "284 selection.append(\"\\\"\").append(inboxes).append('\\'')";

        // Use case
        if(!ne.isSkip(predictionLine)) {
            Pair<NameItem, VariableItem> pair = ne.getReceiverParametersVariables(predictionLine, originalLine);
            System.out.println(predictionLine + " -> " + pair.a);
            System.out.println(originalLine + " -> " + pair.b);
            System.out.println();
        }
    }

    private static void testAll(NameExtractor ne) throws FileNotFoundException {
        String dir = "/Users/wangxin/Workspace/04-DataSpace/CodeRecommendation/抽取变量名例子/";
        String tp = dir + "trainingPrediction.txt";
        String to = dir + "trainOriginalStatements.txt";
        Scanner sp = new Scanner(new File(tp));
        Scanner so = new Scanner(new File(to));

        while(sp.hasNextLine() && so.hasNextLine()) {
            //String predictionLine = "java.util.Scanner.Field";// -> java.util.Scanner
            String predictionLine = sp.nextLine();//
            String originalLine = so.nextLine();// ->

            // Use case
            if(!ne.isSkip(predictionLine)) {
                Pair<NameItem, VariableItem> pair = ne.getReceiverParametersVariables(predictionLine, originalLine);
                System.out.println(predictionLine + " -> " + pair.a);
                System.out.println(originalLine + " -> " + pair.b);
                System.out.println();
            }
        }

        sp.close();
        so.close();
    }

    /* constructor */
    public NameExtractor() {
        //init();
    }

    public void init() {
        skippostfix = new LinkedList<>();
        skippostfix.add(".ArrayNull[]");
        skippostfix.add(".ArrayNull[][]");
        skippostfix.add(".ArrayConstant[]");
        skippostfix.add(".ArrayConstant[][]");
        skippostfix.add(".ArrayInit[]{}");
        skippostfix.add(".ArrayInit[][]{}");
        skippostfix.add(".ArrayDeclaration[]");
        skippostfix.add(".ArrayDeclaration[][]");
        skippostfix.add(".Null");
        skippostfix.add(".Constant");
        skippostfix.add(".Declaration");
        skippostfix.add(".Cast");
        skippostfix.add(".new[]");
        skippostfix.add(".new[][]");

        skiptokens = new LinkedList<>();
        skiptokens.add("if");
        skiptokens.add("elseif");
        skiptokens.add("else");
        skiptokens.add("for");
        skiptokens.add("while");
        skiptokens.add("doWhile");
        skiptokens.add("foreach");
        skiptokens.add("try");
        skiptokens.add("catch");
        skiptokens.add("finally");
        skiptokens.add("switch");
        skiptokens.add("case");
        skiptokens.add("default");
        skiptokens.add("end");
        skiptokens.add("conditionEnd");
        skiptokens.add("termination");
        skiptokens.add("continue");
        skiptokens.add("break");
        skiptokens.add("return");


        skiptokens.add("null");
    }

    /**
     * 抽取抽象表示的API调用语句（在records中的trainPredition.txt）中的receiver以及parameters，用空格隔开
     * 抽取变量名
     * 返回值用一个Pair打包起来
     */
    public Pair<NameItem,VariableItem> getReceiverParametersVariables(String predictionLine, String originalLine) {
        NameExtractor ne = new NameExtractor();
        NameItem ni = new NameItem();
        VariableItem vi = new VariableItem();

        if(predictionLine.indexOf("(") != predictionLine.lastIndexOf("(")){
            List<NameItem> nis = ne.getReceiverParametersOfConsecutiveCall(predictionLine);// 对于API连用做特殊处理
            ni.setType("ConsecutiveCall");
            int cnt = 1;
            for (int i = 0; i< nis.size(); i++) {
                NameItem tempni = nis.get(i);
                if (tempni != null) {
                    if(i == 0) {// 连用的第一个nameitem的receiver为整个连用的receiver
                        ni.setReceiver(tempni.receiver);
                    }
                    ni.getParameters().addAll(tempni.getParameters());// 将所有连用的parameters加入

                    String subLine = getSubLine(predictionLine, originalLine, cnt, nis);
                    VariableItem tempvi = null;
                    if(subLine != null) {
                        tempvi = ne.getVariableNames(subLine, tempni);
                    }
                    if (tempvi != null) {
                        if(i == 0) {
                            vi.getVariables().addAll(tempvi.getVariables());
                        }
                        else if(tempvi.getVariables().size() > 0){
                            vi.getVariables().addAll(tempvi.getVariables().subList(1,tempvi.getVariables().size()));
                        }
                    }
                    cnt++;
                }
            }
        }
        else{
            ni = ne.getReceiverParameters(predictionLine);
            if (ni != null) {
                vi = ne.getVariableNames(originalLine, ni);
            }
        }
        return new Pair<>(ni, vi);
    }

    /**
     * 抽取抽象表示的API调用语句（在records中的trainPredition.txt）中的receiver以及parameters，用空格隔开
     */
    public NameItem getReceiverParameters(String line){
        NameItem ni =  new NameItem();
        int indexlq = line.indexOf("(");
        if(indexlq < 0){// field access

            int indexpnt = line.lastIndexOf(".");
            if(indexpnt < 0){
                return null;
            }
            ni.setReceiver(line.substring(0, indexpnt));

            ni.setMethod_field(line.substring(indexpnt+1));
            ni.setType("FieldAccess");
            if(isDebug){
                System.out.println(ni);
            }
        }
        else{// method call

            if(line.contains(".new(")){
                int indexpnt = line.indexOf(".new(");
                ni.setReceiver(line.substring(0,indexpnt));
                ni.setMethod_field("new");
                ni.setParameters(extractParameters(line.substring(indexpnt+4),-1));
            }
            else {
                String prelq = line.substring(0, indexlq);
                int indexpnt = prelq.lastIndexOf(".");
                if (indexpnt < 0) {
                    return null;
                }
                ni.setReceiver(line.substring(0, indexpnt));

                ni.setMethod_field(prelq.substring(indexpnt + 1));
                ni.setParameters(extractParameters(line.substring(indexlq, line.length()), -1));
            }
        }
        return ni;
    }

    /**
     * 抽取抽象表示的API调用语句（在records中的trainPredition.txt）中的receiver以及parameters，用空格隔开
     * 此处的函数专门针对于API连续调用的情况
     * 由于API的Field Access混入API连续调用的情况较为复杂，这里不支持解析带有Field Access的API连续调用
     */
    private List<NameItem> getReceiverParametersOfConsecutiveCall(String line) {
        // 这里思考如何将API的连续调用拆分为单独的API调用，再进一步解析
        List<NameItem> nis = new LinkedList<>();
        NameItem ni =  new NameItem();
        ni.setType("ConsecutiveCall");

        int indexlq = line.indexOf("(");
        String prelq = line.substring(0, indexlq);
        int indexpnt = prelq.lastIndexOf(".");
        ni.setReceiver(line.substring(0, indexpnt));
        ni.setMethod_field(prelq.substring(indexpnt + 1));

        int indexrq = line.indexOf(")");
        String fstcall = line.substring(0, indexrq);

        String fstparameters = fstcall.substring(indexlq) + ")";
        ni.setParameters(extractParameters(fstparameters,-1));
        nis.add(ni);

        // 解析剩下的API调用
        String postrq = line.substring(indexrq+1);

        String consecutiveLine = "java.lang.Object" + postrq;

        if(postrq.indexOf("(") != postrq.lastIndexOf("(")){
            List<NameItem> consecutiveNis = getReceiverParametersOfConsecutiveCall(consecutiveLine);
            if(consecutiveLine.length() > 0) {
                nis.addAll(consecutiveNis);
            }
        }
        else{
            NameItem nic = getReceiverParameters(consecutiveLine);
            nic.setType("ConsecutiveCall");
            if(nic != null){
                nis.add(nic);
            }
        }
        return nis;
    }


    /**
     * 根据原始抽象代码行和需要截断的位置处理原始行
     * @param predictLine: 抽象行
     * @param originalLine: 原始行
     * @param cnt: 第几个连用
     * @param nis: 根据抽象行解析出的连用方法
     * */
    private static String getSubLine(String predictLine, String originalLine, int cnt, List<NameItem> nis) {
        String subline = originalLine;
        int index = originalLine.indexOf(" ");
        if(index > 0) {
            String num = subline.substring(0, index);
            String line = subline.substring(index + 1);
            int indexrq0 = 0;
            int indexrq1 = 0;
            int tmpcnt = cnt;
            if(cnt == 1){
                indexrq1 = Util.findNextRightParenthesis(line, indexrq1);
            }
            else{
                while (tmpcnt > 0) {
                    int i = cnt - tmpcnt;
                    String method = nis.get(i).getMethod_field();
                    indexrq0 = line.indexOf("." + method, indexrq0 + 1);
                    indexrq1 = indexrq0 > indexrq1 ? indexrq0 : indexrq1;
                    indexrq1 = Util.findNextRightParenthesis(line, indexrq1);
                    tmpcnt--;
                }
            }
            if(indexrq0 < 0 || indexrq1 < 0){// the original line is invalid
                return null;
            }
            subline = line.substring(indexrq0, indexrq1+1);
            if(subline.startsWith(".")){
                subline  = "object" + subline;
            }
            subline = num + " " + subline;
        }
        return subline;
    }

    /**
     * 抽取参数列表
     * @param line : 以括号括起的参数列表，参数之间用","隔开
     * */
    private LinkedList<String> extractParameters(String line, int goldensize) {
        line = line.substring(1,line.length()-1);
        if(isDebug) {
            System.out.println("extract parameter from: " + line);
        }
        LinkedList<String> parameters = new LinkedList<>();
        String[] candidates = line.split(",");
        boolean isConcatenated = false;
        String concateLabel = "";
        for (int i = 0; i < candidates.length; i++) {
            if (isConcatenated) {// 考虑到被","分割开的字符串参数
                parameters.set(parameters.size() - 1, parameters.getLast() + "," + candidates[i]);
                if(candidates[i].endsWith(concateLabel)) {
                    isConcatenated = false;
                }
            } else if (candidates[i].startsWith("\"")){
                isConcatenated = true;
                if(candidates[i].length() > 0) {
                    parameters.addLast(candidates[i]);
                }
                concateLabel = "\"";
            } else if (candidates[i].startsWith("\'")) {
                isConcatenated = true;
                if(candidates[i].length() > 0) {
                    parameters.addLast(candidates[i]);
                }
                concateLabel = "\'";
            } else {
                if(candidates[i].length() > 0) {
                    parameters.addLast(candidates[i]);
                }
            }
            if(candidates[i].contains("(")){
                isConcatenated = true;
                concateLabel = ")";
            }
        }
        if(isDebug){
            System.out.print("parameters: ");
            for (String p: parameters) {
                System.out.print(p + " ");
            }
            System.out.println();
        }
        if(goldensize > 0){ // heuristic filter
            int psize = parameters.size();
            if(psize < goldensize){
                for (int i = 0; i < psize; i++) {
                    if(parameters.get(i).contains(",")){
                        String[] ps = parameters.get(i).split(",");
                        if(ps.length + psize == goldensize){
                            parameters.set(i, ps[ps.length-1]);
                            for (int j = ps.length-2; j >= 0; j--) {
                                parameters.add(i, ps[j]);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return parameters;
    }

    /**
     * 跳过不解析的数据
     * */
    public boolean isSkip(String line) {
        if(skiptokens.contains(line.trim())) {
            return true;
        }
        for (String postfix: skippostfix) {
            if(line.endsWith(postfix)){
                return true;
            }
        }
        return false;
    }

    public boolean isNeedForParameter(String prediction){
        if(skiptokens.contains(prediction.trim())) {
            return false;
        }
        for (String postfix: skippostfix) {
            if(prediction.endsWith(postfix)){
                return false;
            }
        }
        return true;
    }


    /**
     * 抽取原始表达式中的变量名、参数名、常量（与1.中的API调用有关的），用”空格“（暂定）隔开
     */
    public VariableItem  getVariableNames(String originalLine, NameItem ni) {
        if(originalLine.equals("null")){
            return null;
        }
        VariableItem vi = new VariableItem();

        int index = originalLine.indexOf(" ");
        if(index > 0) {
            String line = originalLine.substring(index+1);

            int indexlq = line.indexOf("(");
            boolean isNeedReceiver = true;
            String method = ni.getMethod_field();
            if(isDebug){
                System.out.println(line);
                System.out.println(method);
            }
            if("FieldAccess".equals(ni.getType())){
                int indexpnt = line.indexOf("." + method);
                if(indexpnt > 0) {
                    String prefix = line.substring(0, indexpnt);
                    int indexlast = prefix.lastIndexOf(" ");
                    int indexlastTmp = prefix.lastIndexOf(",");
                    if (indexlast < indexlastTmp) {
                        indexlast = indexlastTmp;
                    }
                    indexlastTmp = prefix.lastIndexOf("(");
                    if (indexlast < indexlastTmp) {
                        indexlast = indexlastTmp;
                    }
                    vi.addVariable(prefix.substring(indexlast+1));
                }
                // else : ignore case
            }
            else {
                if (indexlq > 0) {
                    if ("new".equals(method)) {
                        String type = ni.getReceiver();
                        type = type.substring(type.lastIndexOf(".") + 1);
                        method = "new " + type;
                        isNeedReceiver = false;
                    }
                    int indexmethod = line.indexOf(method + "(");
                    //System.out.println("index of method:" + indexmethod);
                    int size = ni.getVariableSize();
                    if (!isNeedReceiver) {
                        size--;
                    }
                    LinkedList<String> vs = getVariableNames(line, indexmethod, method, size, isNeedReceiver);
                    while (vs != null && vs.size() != size) {// 参数数量不正确,应继续解析下一个位置的参数
                        indexmethod = line.indexOf(method + "(", indexmethod + 1);
                        //System.out.println("index of method:" + indexmethod);
                        vs = getVariableNames(line, indexmethod, method, size, isNeedReceiver);
                    }
                    if (vs == null) {
                        return null;
                    }
                    vi.addVariable(vs);
                }
            }
        }
        return vi;
    }

    /**
     * 从固定位置开始抽取变量名
     * @param line : 原始语句;
     * @param indexmethod : 固定位置;
     * @param method : 定位的方法;
     * @param isNeedReceiver : 是否需要解析调用者变量名
     * @return : 变量名列表;
     * */
    private LinkedList<String> getVariableNames(String line, int indexmethod, String method, int goldensize, boolean isNeedReceiver) {
        LinkedList<String> vs = new LinkedList<>();
        if(indexmethod < 0){
            return null;
        }

        String postfix = line.substring(indexmethod+method.length());
        if(isNeedReceiver) {
            String prefix = line.substring(0,indexmethod-1);
            int indexlq = prefix.lastIndexOf("(");
            int indexspace = prefix.lastIndexOf(" ");
            if (indexlq > indexspace && indexlq > 0) {
                vs.addLast(prefix.substring(indexlq + 1));
            } else if (indexspace > 0 && indexspace > indexlq) {
                vs.addLast(prefix.substring(indexspace));
            } else {
                vs.addLast(prefix);
            }
        }

        if(isDebug) {
            System.out.println("postfix: " + postfix);
        }
        int indexrq = postfix.indexOf(")");
        int cntrq = Util.getCharacterCntSkipParenthesis(postfix, ')', 0, indexrq+1);
        int cntlq = Util.getCharacterCntSkipParenthesis(postfix, '(', 0, indexrq+1);
        if(isDebug) {
            System.out.println("ir: " + indexrq);
            System.out.println("r: " + cntrq + " l:" + cntlq);
        }
        while(cntlq > cntrq){
            int oldindexrq = indexrq;
            indexrq = postfix.indexOf(")",indexrq+1);
            cntlq = Util.getCharacterCntSkipParenthesis(postfix, '(', 0, indexrq+1);
            if(indexrq>0) {
                cntrq++;
            }
            else{
                indexrq = oldindexrq;
                break;
            }
        }
        if(isDebug) {
            System.out.println("ir: " + indexrq);
            System.out.println("r: " + cntrq + " l:" + cntlq);
        }
        if(indexrq > 0) {
            String sub = postfix.substring(0,indexrq+1);
            if(isDebug) {
                System.out.println("sub: " + sub);
            }
            vs.addAll(extractParameters(sub, goldensize));
        }
        else {
            vs.addAll(extractParameters(postfix,goldensize));
        }
        return vs;
    }

}
