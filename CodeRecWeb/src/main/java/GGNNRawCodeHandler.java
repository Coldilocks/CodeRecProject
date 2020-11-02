import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import parameterModel.ConstructGroum;
import parameterModel.Groum;
import parameterModel.GroumNode;
import parameterModel.NaturalnessPrediction;
import parameterModel.util.Pair;
//import predict.GraphPredictNew;
import test.Predict;
import utils.NameExtractor;
import utils.NameItem;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class GGNNRawCodeHandler {
    private boolean isControl = false;
    private List<Double> probabilities = new ArrayList<>();
    private int MaxRec = 1;
    private int addLine = 1;
    private int line = 0;
    private List<String> jdkList = new ArrayList<>();
    private List<String> gloveVocabList = new ArrayList<>();
    private List<String> stopWordsList = new ArrayList<>();
    private String space = "";
    private List<List<String>> importInfoList = new ArrayList<>();
    private List<String> tipList = new ArrayList<>();

    public List<List<String>> getImportInfoList() {
        return importInfoList;
    }

    public List<String> getTipList() {
        return tipList;
    }

    public GGNNRawCodeHandler(List<String> jdkList, List<String> gloveVocabList, List<String> stopWordsList, String space) {
        this.jdkList = jdkList;
        this.gloveVocabList = gloveVocabList;
        this.stopWordsList = stopWordsList;
        this.space = space;
    }


    // Handle the raw code here.
    public List<String> handleRawCode(String rawcode) {
//        System.out.println(rawcode);
        List<String> result = new ArrayList<>();
        String completecode = rawcode;
        String removeCommentCode = rawcode;
        int holeIndex = removeCommentCode.indexOf("$hole$");
        if (holeIndex >= 0) {
            line = getLine(removeCommentCode);
            String code = rawcode.replace("$hole$", "/*hole*/");
            completecode = code;
        }
        String parsedCode = convertRawCode2ParsedCode(completecode);
        String newParsedCode = parsedCode;
        newParsedCode = newParsedCode.replace("/*hole*/", "true == true/*hole*/");
        //newParsedCode = newParsedCode.replace("}","}//\n");
        //System.out.println(newParsedCode);
        long st = System.currentTimeMillis();
        Graph codeGraph =  getCodeGraph(parsedCode).get(0);
        long ed = System.currentTimeMillis();
        System.out.println("parse time:" + (ed - st));
        if (codeGraph == null) {
            codeGraph = getCodeGraph(newParsedCode).get(0);
            if (codeGraph == null) {
                newParsedCode = newParsedCode.replace("}", "}//\n");
                codeGraph = getCodeGraph(newParsedCode).get(0);
                if(codeGraph == null){
                    return null;
                }
            }
            codeGraph.processConditionHole(codeGraph.getRoot(), new ArrayList<>());
            codeGraph.setSerialNumberofNode(codeGraph.getRoot(), new ArrayList<>());
            int serialNumber = 0;
            /**get the original prediction*/
            List<String> predictions;
            predictions = getPredictedResult(newParsedCode, serialNumber, new GraphNode(), codeGraph);
            result = predictions;
            for (int i = 0; i < result.size(); i++) {
                result.set(i, result.get(i).replace(";", ""));
            }
        } else {
            int serialNumber = 0;
            GraphNode childNode = new GraphNode();
            /**get the original prediction*/
            codeGraph.setSerialNumberofNode(codeGraph.getRoot(), new ArrayList<>());
            List<String> predictions;
            predictions = getPredictedResult(parsedCode, serialNumber, childNode, codeGraph);
            result = predictions;
        }
//        Graph codeGraph = getCodeGraph(newParsedCode).get(0);
//        if(codeGraph == null) {
//            newParsedCode = newParsedCode.replace("}", "}//\n");
//            codeGraph = getCodeGraph(newParsedCode).get(0);
//        }
//        if (codeGraph == null) {
//            codeGraph = getCodeGraph(parsedCode).get(0);
//            if (codeGraph == null) {
//                return null;
//            }
//            int serialNumber = 0;
//            GraphNode childNode = new GraphNode();
//            /**get the original prediction*/
//            codeGraph.setSerialNumberofNode(codeGraph.getRoot(), new ArrayList<>());
//            List<String> predictions;
//            predictions = getPredictedResult(parsedCode, serialNumber, childNode, codeGraph);
//            result = predictions;
//        } else {
//            codeGraph.processConditionHole(codeGraph.getRoot(), new ArrayList<>());
//            codeGraph.setSerialNumberofNode(codeGraph.getRoot(), new ArrayList<>());
//            int serialNumber = 0;
//            /**get the original prediction*/
//            List<String> predictions;
//            predictions = getPredictedResult(newParsedCode, serialNumber, new GraphNode(), codeGraph);
//            result = predictions;
//            for (int i = 0; i < result.size(); i++) {
//                result.set(i, result.get(i).replace(";", ""));
//            }
//        }
        return result;
    }

    //convert raw code to the code that we can parse
    private String convertRawCode2ParsedCode(String rawCode) {
        /**get the serial number of hole node's parent node **/
//        int startIndex = 0;
//        if (rawCode.contains("package")) {
//            startIndex = rawCode.indexOf("package");
//        } else if (rawCode.contains("import")) {
//            startIndex = rawCode.indexOf("import");
//        }
//        String parsedCode = rawCode.substring(startIndex, rawCode.length());
//        return parsedCode;
        return rawCode;
    }

    // Scan and complete the brackets
//    private String scanAndComplete(String precontext) {
//        int length = precontext.length();
//        Stack<String> signs = new Stack<>();
//        boolean isInQuotation = false;
//        for (int i = 0; i < length; i++) {
//            char temp = precontext.charAt(i);
//            if (temp == '\"' || temp == '\'') {
//                isInQuotation = !isInQuotation;
//            }
//            if (!isInQuotation) {
//                if (temp == '{') {
//                    signs.push("}");
//                } else if (temp == '(') {
//                    signs.push(")");
//                } else if (temp == '}' || temp == ')') {
//                    signs.pop();
//                }
//            }
//        }
//        while (signs.size() > 0) {
//            precontext = precontext + signs.pop();
//        }
//        return precontext;
//    }

//    public List<Object> getHoleNode(String parsedCode) {
//        List result = new ArrayList();
//        String globalPath = "/Users/lingxiaoxia/IdeaProjects/CodeRecommendation";
//        try {
//            GraphPredictNew predict = new GraphPredictNew();
//            List<Graph> codeGraphList = predict.getCodeGraph(parsedCode, false, true, globalPath, jdkList, gloveVocabList, stopWordsList);
//            Graph codeGraph = codeGraphList.get(0);
//            codeGraph.setSerialNumberofNode(codeGraph.getRoot(),new ArrayList<>());
//            GraphNode holeNode = codeGraph.getHoleNode(new ArrayList<>());
//            if (holeNode.getParentNode() != null) {
//                isControl = holeNode.getParentNode().isControl();
//                if (isControl) {
//                    if (holeNode.getSerialNumber() == holeNode.getParentNode().getSerialNumber() + 2) {
//                        isControl = false;
//                    } else if (holeNode.getSerialNumber() == holeNode.getParentNode().getSerialNumber() + 1) {
//                        isControl = false;
//                    }
//                }
//                result.add(holeNode.getParentNode().getSerialNumber());
//                if (holeNode.getChildNodes().size() != 0) {
//                    result.add(holeNode.getChildNodes().get(0));
//                } else {
//                    result.add(null);
//                }
//                return result;
//            } else {
//                result.add(0);
//                result.add(null);
//            }
//        } catch (Exception e) {
//            result.add(0);
//            result.add(null);
//        }
//        return result;
//    }

    public List<Graph> getCodeGraph(String content) {
        List<Graph> result = new ArrayList<>();
        //String globalPath = "/Users/lingxiaoxia/IdeaProjects/CodeRecommendation";
        String globalPath = "/home/x/mydisk/IdeaProjects/CodeRecommendation";
        //String globalPath = "/mydisk/fudan_se_dl/code_recommendation";
        try {
            //GraphPredictNew predict = new GraphPredictNew();
            Predict predict = new Predict();
            //result = predict.getCodeGraph(content, false, true, globalPath, jdkList, gloveVocabList, stopWordsList);
            result = predict.getAndroidCodeGraph(content, false, true, globalPath, jdkList, gloveVocabList, stopWordsList);

        } catch (Exception e) {
            result.add(null);
        }
        return result;
    }

    public List<String> getPredictedResult(String content, int serialNumber, GraphNode childNode, Graph codeGraph) {
        List<String> result = new ArrayList<>();
        //String globalPath = "/Users/lingxiaoxia/IdeaProjects/CodeRecommendation";
        String globalPath = "/home/x/mydisk/IdeaProjects/CodeRecommendation";
        //String globalPath = "/mydisk/fudan_se_dl/code_recommendation";
        List<String> top5Result = new ArrayList<>();
        try {
            //GraphPredictNew predict = new GraphPredictNew();
            Predict predict = new Predict();
            predict.setSerialNumberString(Integer.toString(serialNumber));
            predict.setChildNode(childNode);
            top5Result = predict.predict(codeGraph, globalPath);// pathList.get(0)
//            top5Result = filter(top5Result,codeGraph);
            for(int i = 0; i < top5Result.size(); i ++){
                String prediction = top5Result.get(i).split(" +")[0];
                if(prediction.contains(".new(")){
                    String[] strs = prediction.split("\\.new\\(");
                    String completeClassName = strs[0];
                    String[] names = completeClassName.split("\\.");
                    String simpleClassName = "." + names[names.length - 1] + "(";
                    prediction = prediction.replace(".new(",simpleClassName);
                }
                if(LoadSource.tipMap.containsKey(prediction)){
                    tipList.add(LoadSource.tipMap.get(prediction));
                }else{
                    tipList.add("No API Description");
                }
            }
            try {
                long st = System.currentTimeMillis();
                ConstructGroum constructGroum = new ConstructGroum();
                Groum groum = constructGroum.getTestGroum(0, content, false, jdkList,
                        null,
                        null,
                        true, globalPath,
                        new ArrayList<>(), new ArrayList<>());
                // groum.processConditionHole(groum.getRoot(), new ArrayList<>());
                groum.processConditionHole();
                // System.out.println(top5Result);
                NaturalnessPrediction naturalnessPrediction = new NaturalnessPrediction();
                naturalnessPrediction.getNaturalnessPrediction_1(groum, top5Result, LoadSource.map);
                Map<String, List<Pair<GroumNode, Double>>> parameterMap = naturalnessPrediction.parameterMap;
                sortParameter(parameterMap);
                for (int i = 0; i < top5Result.size(); i++) {
                    result.add(recommendParameter(parameterMap, top5Result.get(i), constructGroum.parameterNodeList));
                    //result.add(recommendParameter2(top5Result.get(i)));
                }
                long ed = System.currentTimeMillis();
                System.out.println("parameter time:" + (ed-st));
            }catch(Exception e){
                for (int i = 0; i < top5Result.size(); i++) {
                    result.add(recommendParameter2(top5Result.get(i)));
                }
            }catch(Error e){
                for (int i = 0; i < top5Result.size(); i++) {
                    result.add(recommendParameter2(top5Result.get(i)));
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            result = new ArrayList<>();
        } catch (Error e) {
            //e.printStackTrace();
            result = new ArrayList<>();
        }
        return result;
    }

    public void sortParameter(Map<String, List<Pair<GroumNode, Double>>> parameterMap) {
        for (String key : parameterMap.keySet()) {
            List<Pair<GroumNode, Double>> list = parameterMap.get(key);
            Pair<GroumNode, Double> temp = null;
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = 0; j < list.size() - 1 - i; j++) {
                    if (list.get(j + 1).b.doubleValue() > list.get(j).b.doubleValue()) {
                        temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                    } else if (list.get(j + 1).b.doubleValue() == list.get(j).b.doubleValue()) {
                        if (list.get(j + 1).a.getVariableCount() < list.get(j).a.getVariableCount()) {
                            temp = list.get(j);
                            list.set(j, list.get(j + 1));
                            list.set(j + 1, temp);
                        }
                    }
                }
            }
        }
    }

    public boolean judgeRelationshipOfCast(String sourceClassName, String objectClassName) {
        if (sourceClassName != null && objectClassName != null) {
            if (LoadSource.castMap.get(sourceClassName + "2" + objectClassName) != null) {
                return true;
            } else if (LoadSource.castMap.get(objectClassName + "2" + sourceClassName) != null) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public int getLine(String text) {
        int i = 0;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String buff = scanner.nextLine();
            i++;
            if (buff.equals("$hole$")) {
                scanner.close();
                return i;
            }
        }
        scanner.close();
        return -1;
    }
    public String recommendParameter(Map<String, List<Pair<GroumNode, Double>>> parameterMap, String label, List<GroumNode> parameterNodeList) {
        List<String> importInfo = new ArrayList<>();
        String append = "";
        String prediction = label.split(" +")[0];
        String replace = "";
        if (prediction.contains(".new(")) {
            replace = prediction.replace(".new", " ");
            replace = replace.split(" +")[0];
            prediction = prediction.replaceFirst(replace, "woshireplace");
        }else{
            String r = handleSpecialPrediction(prediction, importInfo);
            if(!r.equals(prediction+";")){
                importInfoList.add(importInfo);
                return r;
            }
        }
        String expression = prediction;
        String probability = label.split(" +")[1];
        GroumNode temp = new GroumNode();
        for (int i = 0; i < parameterNodeList.size() - 1; i++) {
            for (int j = 0; j < parameterNodeList.size() - 1 - i; j++) {
                if (parameterNodeList.get(j + 1).getVariableCount() < parameterNodeList.get(j).getVariableCount()) {
                    temp = parameterNodeList.get(j);
                    parameterNodeList.set(j, parameterNodeList.get(j + 1));
                    parameterNodeList.set(j + 1, temp);
                }
            }
        }
        List<String> result = new ArrayList<>();
        String key = label.split(" +")[0];
        if (parameterMap.containsKey(key)) {
            //for(String key: parameterMap.keySet()){
            NameExtractor ne = new NameExtractor();
            ne.init();
            if (ne.isNeedForParameter(key)) {
                NameItem ni = ne.getReceiverParameters(key);
                String[] types = ni.toString().split(" +");
                List<Pair<GroumNode, Double>> list = parameterMap.get(key);
                List<String> allVariables = new ArrayList<>();//used to stor all variables ordered by probability
                for (Pair<GroumNode, Double> pair : list) {
                    if (pair.a.getVariableName() != null && !allVariables.contains(pair.a.getVariableName())) {
                        allVariables.add(pair.a.getVariableName());
                    }
                }
                Map<String, List<String>> parameterNameMap = new HashMap<>();
                /*get the types together with names in parameterMap*/
                for (int i = 0; i < list.size(); i++) {
                    String type = list.get(i).a.getType();
                    String name = list.get(i).a.getVariableName();
                    if (parameterNameMap.containsKey(type)) {
                        if (!parameterNameMap.get(type).contains(name)) {
                            parameterNameMap.get(type).add(name);
                        }
                    } else {
                        List<String> nameList = new ArrayList<>();
                        nameList.add(name);
                        parameterNameMap.put(type, nameList);
                    }
                }
                /*add field and method signature parameter*/
                for (GroumNode node : parameterNodeList) {
                    if (node.getVariableName() != null && !allVariables.contains(node.getVariableName())) {
                        allVariables.add(node.getVariableName());
                        if (parameterNameMap.containsKey(node.getType())) {
                            parameterNameMap.get(node.getType()).add(node.getVariableName());
                        } else {
                            List<String> nameList = new ArrayList<>();
                            nameList.add(node.getVariableName());
                            parameterNameMap.put(node.getType(), nameList);
                        }
                    }
                }
                /*choose the correct variable*/
                List<String> oneAppendNodeParameterList = new ArrayList<>();
                for (int i = 0; i < types.length; i++) {
                    String type = types[i];
                    if (i == 0) {
                        try {
                            boolean flag = false;
                            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                            Method[] methods = clazz.getMethods();
                            Field[] fields = clazz.getFields();
                            String methodSignature = prediction.replaceFirst(type,"");
                            for (int j = 0; j < methods.length; j++) {
                                if (methods[j].toString().contains(methodSignature)) {
                                    String completeReturnType = methods[j].getReturnType().getCanonicalName();
                                    addImportInfo(importInfo, handleType(completeReturnType));
                                    String returnType = methods[j].getReturnType().getSimpleName();
                                    if (!parameterNameMap.containsKey(completeReturnType)) {
                                        if (!key.contains(".append(") && !returnType.contains("Builder")) {
                                            append = returnType + " " + returnType.toLowerCase().replaceAll("\\[\\]", "") + " = ";
                                        }
                                    } else {
                                        if (parameterNameMap.get(completeReturnType).size() > 0 && !key.contains(".append(") && !returnType.contains("Builder")) {
                                            append = parameterNameMap.get(completeReturnType).get(0) + " = ";
                                        } else if (!key.contains(".append(") && !returnType.contains("Builder")) {
                                            append = returnType + " " + returnType.toLowerCase().replaceAll("\\[\\]", "") + " = ";
                                        }
                                    }
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag) {
                                for (int j = 0; j < fields.length; j++) {
                                    if (fields[j].toString().contains(methodSignature)) {
                                        String completeReturnType = fields[j].getType().getCanonicalName();
                                        addImportInfo(importInfo, handleType(completeReturnType));
                                        String returnType = fields[j].getType().getSimpleName();
                                        if (!parameterNameMap.containsKey(completeReturnType)) {
                                            if (!key.contains(".append(") && !returnType.contains("Builder")) {
                                                append = returnType + " " + returnType.toLowerCase().replaceAll("\\[\\]", "") + " = ";
                                            }
                                        } else {
                                            if (parameterNameMap.get(completeReturnType).size() > 0 && !key.contains(".append(") && !returnType.contains("Builder")) {
                                                append = parameterNameMap.get(completeReturnType).get(0) + " = ";
                                            } else if (!key.contains(".append(") && !returnType.contains("Builder")) {
                                                append = returnType + " " + returnType.toLowerCase().replaceAll("\\[\\]", "") + " = ";
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            //todo nothing
                        }
                    }
                    String originalType = type;
                    String[] judge = type.split("\\.");
                    String add = "";
                    int typeIndex = 0;
                    if (judge.length > 1) {
                        type = "";
                        for (int index = judge.length - 1; index > 0; index--) {
                            if (Character.isUpperCase(judge[index].charAt(0)) && Character.isLowerCase(judge[index - 1].charAt(0))) {
                                typeIndex = index;
                                break;
                            } else {
                                add += ".";
                                add += judge[index];
                            }
                        }
                        for (int index = 0; index <= typeIndex; index++) {
                            if (index > 0) {
                                type += ".";
                            }
                            type += judge[index];
                        }
                    }
                    addImportInfo(importInfo, handleType(type));
                    if (type.equals("java.lang.Object") || type.equals("java.lang.Object[]")) {
                        if (allVariables.size() > 0) {
                            //type = handleType(type);
                            originalType = handleType(originalType);
                            prediction = prediction.replaceFirst(originalType, allVariables.get(0) + add);
                            oneAppendNodeParameterList.add(allVariables.get(0));
                            String name = allVariables.get(0);
                            for (String typeKey : parameterNameMap.keySet()) {
                                List<String> nameList = parameterNameMap.get(typeKey);
                                if (nameList.contains(name)) {
                                    nameList.remove(name);
                                    break;
                                }
                            }
                            allVariables.remove(0);
                        } else {
                            //type = handleType(type);
                            originalType = handleType(originalType);
                            String[] strs = originalType.split("\\.");
                            prediction = prediction.replaceFirst(originalType, strs[strs.length - 1]);
                            //prediction = prediction.replaceFirst(originalType, "constant");
                            oneAppendNodeParameterList.add("constant");
                        }
                        continue;
                    }
                    if (parameterNameMap.containsKey(type)) {
                        List<String> nameList = parameterNameMap.get(type);
                        if (nameList.size() > 0) {
                            //type = handleType(type);
                            originalType = handleType(originalType);
                            prediction = prediction.replaceFirst(originalType, nameList.get(0) + add);
                            oneAppendNodeParameterList.add(nameList.get(0));
                            allVariables.remove(nameList.get(0));
                            nameList.remove(0);
                        } else {
                            //type = handleType(type);
                            originalType = handleType(originalType);
                            String[] strs = originalType.split("\\.");
                            prediction = prediction.replaceFirst(originalType, strs[strs.length - 1]);
                            // prediction = prediction.replaceFirst(originalType, "constant");
                            oneAppendNodeParameterList.add("constant");
                        }
                    } else {
                        boolean flag = false;
                        for (String typeKey : parameterNameMap.keySet()) {
                            try {
                                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                                Class clazz2 = Thread.currentThread().getContextClassLoader().loadClass(typeKey);
                                if (clazz2.isAssignableFrom(clazz) || clazz.isAssignableFrom(clazz2) || judgeRelationshipOfCast(type, typeKey)) {
                                    flag = true;
                                    List<String> nameList = parameterNameMap.get(typeKey);
                                    if (nameList.size() > 0) {
                                        //type = handleType(type);
                                        originalType = handleType(originalType);
                                        prediction = prediction.replaceFirst(originalType, nameList.get(0) + add);
                                        oneAppendNodeParameterList.add(nameList.get(0));
                                        allVariables.remove(nameList.get(0));
                                        nameList.remove(0);
                                    } else {
                                        //type = handleType(type);
                                        originalType = handleType(originalType);
                                        String[] strs = originalType.split("\\.");
                                        prediction = prediction.replaceFirst(originalType, strs[strs.length - 1]);
                                        //prediction = prediction.replaceFirst(originalType, "constant");
                                        oneAppendNodeParameterList.add("constant");
                                    }
                                    break;
                                }
                            } catch (Exception e) {
                                continue;
                            }
                        }
                        if (!flag) {
                            if (i == 0) {
                                type = handleType(type);
                                originalType = handleType(originalType);
                                String[] strs = type.split("\\.");
                                prediction = prediction.replaceFirst(originalType, strs[strs.length - 1] + add);
                                oneAppendNodeParameterList.add(strs[strs.length - 1]);
                            } else {
                                // type = handleType(type);
                                originalType = handleType(originalType);
                                String[] strs = originalType.split("\\.");
                                prediction = prediction.replaceFirst(originalType, strs[strs.length - 1]);
                                //prediction = prediction.replaceFirst(originalType, "constant");
                                oneAppendNodeParameterList.add("constant");
                            }
                        }
                    }
                }
                result = oneAppendNodeParameterList;
            } else {
                result.add("controlordeclarationnoneed");
            }
        } else {
            result.add("controlordeclarationnoneed");
        }
        prediction = prediction.replace("woshireplace", replace);
        prediction = handleSpecialPrediction(prediction, importInfo);
        append = handleAppend(append);
        importInfoList.add(importInfo);
        return append + prediction;
        //return prediction + " " + probability;
    }

    public String recommendParameter2(String label) {
        List<String> importInfo = new ArrayList<>();
        String append = "";
        String prediction = label.split(" +")[0];
        String replace = "";
        if (prediction.contains(".new(")) {
            replace = prediction.replace(".new", " ");
            replace = replace.split(" +")[0];
            prediction = prediction.replaceFirst(replace, "woshireplace");
        }else{
            String r = handleSpecialPrediction(prediction, importInfo);
            if(!r.equals(prediction+";")){
                importInfoList.add(importInfo);
                return r;
            }
        }
        String key = label.split(" +")[0];
        NameExtractor ne = new NameExtractor();
        ne.init();
        if (ne.isNeedForParameter(key)) {
            NameItem ni = ne.getReceiverParameters(key);
            String[] types = ni.toString().split(" +");
            for (int i = 0; i < types.length; i++) {
                String type = types[i];
                if (i == 0) {
                    try {
                        boolean flag = false;
                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                        Method[] methods = clazz.getMethods();
                        Field[] fields = clazz.getFields();
                        for (int j = 0; j < methods.length; j++) {
                            String methodSignature = prediction.replaceFirst(type,"");
                            if (methods[j].toString().contains(methodSignature)) {
                                String completeReturnType = methods[j].getReturnType().getCanonicalName();
                                addImportInfo(importInfo, handleType(completeReturnType));
                                String returnType = methods[j].getReturnType().getSimpleName();
                                if (!key.contains(".append(") && !returnType.contains("Builder")) {
                                    //append = returnType + " " + returnType.toLowerCase().replaceAll("\\[\\]", "") + " = ";
                                    append = returnType + " " + toLowerCaseFirstOne(returnType).replaceAll("\\[\\]", "") + " = ";
                                }
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            for (int j = 0; j < fields.length; j++) {
                                String methodSignature = prediction.replaceFirst(type,"");
                                if (fields[j].toString().contains(methodSignature)) {
                                    String completeReturnType = fields[j].getType().getCanonicalName();
                                    addImportInfo(importInfo, handleType(completeReturnType));
                                    String returnType = fields[j].getType().getSimpleName();
                                    if (!key.contains(".append(") && !returnType.contains("Builder")) {
                                        //append = returnType + " " + returnType.toLowerCase().replaceAll("\\[\\]", "") + " = ";
                                        append = returnType + " " + toLowerCaseFirstOne(returnType).replaceAll("\\[\\]", "") + " = ";

                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        //todo nothing
                    }
                }
                String originalType = type;
                String[] judge = type.split("\\.");
                String add = "";
                int typeIndex = 0;
                if (judge.length > 1) {
                    type = "";
                    for (int index = judge.length - 1; index > 0; index--) {
                        if (Character.isUpperCase(judge[index].charAt(0)) && Character.isLowerCase(judge[index - 1].charAt(0))) {
                            typeIndex = index;
                            break;
                        } else {
                            add += ".";
                            add += judge[index];
                        }
                    }
                    for (int index = 0; index <= typeIndex; index++) {
                        if (index > 0) {
                            type += ".";
                        }
                        type += judge[index];
                    }
                }
                addImportInfo(importInfo, handleType(type));
                originalType = handleType(originalType);
                String[] strs = originalType.split("\\.");
                prediction = prediction.replaceFirst(originalType, strs[strs.length - 1]);
            }
        }
        prediction = prediction.replace("woshireplace", replace);
        prediction = handleSpecialPrediction(prediction, importInfo);
        append = handleAppend(append);
        importInfoList.add(importInfo);
        return append + prediction;
    }


    public String handleType(String type) {
//        if(type.contains("[]")){
//            String str = "\\[\\]";
//            type = type.replace("[]",str);
//        }
        String s = "\\(";
        String s1 = "\\)";
        String s2 = "\\<";
        String s3 = "\\>";
        String s4 = "\\[";
        String s5 = "\\]";
        type = type.replace("(", s);
        type = type.replace(")", s1);
        type = type.replace("<", s2);
        type = type.replace(">", s3);
        type = type.replace("[", s4);
        type = type.replace("]", s5);
        return type;
    }

    public String handleSpecialPrediction(String prediction, List<String> importInfo) {
        String result = "";
        if (prediction.equals("if") || prediction.equals("while")
                || prediction.equals("catch") || prediction.equals("switch")) {
            result = prediction;
            result += "(){\n" + space + "}";
        } else if (prediction.equals("for")) {
            result += "for(;;){\n" + space + "}";
        } else if (prediction.equals("foreach")) {
            result += "for(:){\n" + space + "}";
        } else if (prediction.equals("elseif")) {
            result = "else if(){\n" + space + "}";
        } else if (prediction.equals("doWhile")) {
            result = "do{\n" + space + "}while();";
        } else if (prediction.equals("case")) {
            result = "case:";
        } else if (prediction.equals("finally") || prediction.equals("default")
                || prediction.equals("else")) {
            result = prediction;
            result += "{\n" + space + "}";
        } else if (prediction.equals("try")) {
            result = prediction;
            result += "{\n" + space + "}";
            result += "catch(Exception e){\n" + space + "}";
            result += "finally{\n" + space + "}";
        } else if (prediction.endsWith(".Declaration")) {
            prediction = prediction.replace(".Declaration", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + ";";
        } else if (prediction.endsWith(".Null")) {
            prediction = prediction.replace(".Null", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + " = null;";
        } else if (prediction.endsWith(".Constant")) {
            prediction = prediction.replace(".Constant", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + " = constant;";
        } else if(prediction.endsWith(".DeclarationConstant")){
            prediction = prediction.replace(".DeclarationConstant", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + " = constant;";
        } else if(prediction.endsWith(".DeclarationNull")){
            prediction = prediction.replace(".DeclarationNull", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + " = null;";
        } else if(prediction.endsWith(".DeclarationCast")){
            prediction = prediction.replace(".DeclarationCast", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + ";";
        } else if (prediction.endsWith(".ArrayDeclaration[]")) {
            prediction = prediction.replace(".ArrayDeclaration[]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + ";";
        } else if (prediction.endsWith(".ArrayDeclaration[][]")) {
            prediction = prediction.replace(".ArrayDeclaration[][]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + ";";
        } else if (prediction.endsWith(".ArrayNull[]")) {
            prediction = prediction.replace(".ArrayNull[]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = null;";
        } else if (prediction.endsWith(".ArrayNull[][]")) {
            prediction = prediction.replace(".ArrayNull[][]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = null;";
        } else if (prediction.endsWith(".ArrayConstant[]")) {
            prediction = prediction.replace(".ArrayConstant[]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = constant;";
        } else if (prediction.endsWith(".ArrayConstant[][]")) {
            prediction = prediction.replace(".ArrayConstant[][]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = constant;";
        } else if (prediction.endsWith(".ArrayInit[]{}")) {
            prediction = prediction.replace(".ArrayInit[]{}", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = {};";
        } else if (prediction.endsWith(".ArrayInit[][]{}")) {
            prediction = prediction.replace(".ArrayInit[][]{}", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = {};";
        } else if(prediction.endsWith(".DeclarationArrayNull[]")){
            prediction = prediction.replace(".DeclarationArrayNull[]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = null;";
        } else if(prediction.endsWith(".DeclarationArrayNull[][]")){
            prediction = prediction.replace(".DeclarationArrayNull[][]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = null;";
        } else if(prediction.endsWith(".DeclarationArrayConstant[]")){
            prediction = prediction.replace(".DeclarationArrayConstant[]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = constant;";
        } else if(prediction.endsWith(".DeclarationArrayConstant[][]")){
            prediction = prediction.replace(".DeclarationArrayConstant[][]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = constant;";
        } else if(prediction.endsWith(".DeclarationArrayInit[]{}")){
            prediction = prediction.replace(".DeclarationArrayInit[]{}", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = {};";
        } else if(prediction.endsWith(".DeclarationArrayInit[][]{}")){
            prediction = prediction.replace(".DeclarationArrayInit[][]{}", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = {};";
        }
        else if (prediction.endsWith(".new[]")) {
            prediction = prediction.replace(".new[]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[] " + toLowerCase(prediction) + "Array" + " = new " + prediction + "[size]";
        } else if (prediction.endsWith(".new[][]")) {
            prediction = prediction.replace(".new[][]", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + "[][] " + toLowerCase(prediction) + "Array" + " = new " + prediction + "[size][size]";
        } else if (prediction.endsWith(".Cast")) {
            prediction = prediction.replace(".Cast", "");
            addImportInfo(importInfo, handleType(prediction));
            String[] strs = prediction.split("\\.");
            prediction = strs[strs.length - 1];
            result = prediction + " " + toLowerCase(prediction) + ";";
        } else if (prediction.contains(".new(")) {
            prediction = prediction.replace(".new", " ");
            String[] strs = prediction.split(" +");
            prediction = strs[0];
            addImportInfo(importInfo, handleType(prediction));
            String[] types = prediction.split("\\.");
            String type = types[types.length - 1];
            result = type + " " + toLowerCase(type) + " = new " + type + strs[1] + ";";
        } else {
            result = prediction;
            result += ";";
        }
        return result;
    }

    public String handleAppend(String append) {
        String result = "";
        if (append.split(" +").length == 3) {
            if (append.startsWith("short") || append.startsWith("byte") || append.startsWith("int")
                    || append.startsWith("long") || append.startsWith("float") || append.startsWith("double")
                    || append.startsWith("char") || append.startsWith("Short")
                    || append.startsWith("Byte") || append.startsWith("Integer") || append.startsWith("Long")
                    || append.startsWith("Float") || append.startsWith("Double") || append.startsWith("Character")) {
                result = "";
                String str1 = append.split(" +")[0];
                String str2 = append.split(" +")[1] + "_variable";
                if (str2.contains("[]")) {
                    str2 = str2.replace("[]", "");
                    str2 += "Array";
                }
                result = str1 + " " + str2 + " = ";
            } else if (append.startsWith("boolean") || append.startsWith("Boolean") || append.startsWith("void")) {
                result = "";
            } else {
                result = append;
            }
        } else {
            result = append;
        }
        return result;
    }

    public String toLowerCase(String prediction) {
        if (Character.isLowerCase(prediction.charAt(0))) {
            prediction = prediction.toLowerCase() + "_variable";
        } else {
            //prediction = prediction.toLowerCase();
            prediction = toLowerCaseFirstOne(prediction);
        }
        prediction = prediction.replaceAll("\\[\\]", "");
        prediction = prediction.replaceAll("\\\\\\[\\\\\\]", "");
        return prediction;
    }

    public static String toLowerCaseFirstOne(String s){
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    public void addImportInfo(List<String> importInfo, String type) {
        if (!importInfo.contains(type) && type.contains(".")) {
            if (!"java.lang.String".equals(type)) {
                type = type.replaceAll("\\[\\]", "");
                type = type.replaceAll("\\\\\\[\\\\\\]", "");
                try {
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                    String str = clazz.toGenericString();
                    if (str.contains("public ")) {
                        importInfo.add(type);
                    }

                } catch (Exception ex) {

                } catch (Error ex) {

                }
                //importInfo.add(type);
            }
        }
    }

    public List<String> filter(List<String> softmaxs, Graph graph) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < softmaxs.size(); i++) {
            String str = softmaxs.get(i);
            String[] strs = str.split(" +");
            String api = strs[0];
            if (!isFilter(api)) {
//                list.add(str);
                //if (api.contains(".new(") || isControl(api)) {
                    list.add(str);
                //    continue;
               // }
                //filterNoClass(api,str,list,graph);
                //filterDuplicated(api, str, list, graph, label);
                if (list.size() == 10) {
                    return list;
                }
            }
        }

        return list;
    }


    public static void filterNoClass(String api, String str, List<String> list, Graph graph) {
        NameExtractor ne = new NameExtractor();
        ne.init();
        if (ne.isNeedForParameter(api)) {
            NameItem ni = ne.getReceiverParameters(api);
            String[] types = ni.toString().split(" +");
            if (types.length > 0) {
                String type = types[0];
                try {
                    if (graph != null) {
                        Map<String, String> map = graph.getClass_name_map();
                        List<String> typeList = new ArrayList<>();
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            value = value.replaceAll("\\[\\]", "");
                            typeList.add(value);
                        }
                        type = type.replaceAll("\\[\\]", "");
                        String[] ss = type.split("\\.");
                        String type2 = ss[ss.length - 1];
                        boolean flag = false;
                        for (int j = 0; j < typeList.size(); j++) {
                            String t = typeList.get(j);
                            flag = judgeExtension(t, type);
                            if (flag) {
                                break;
                            } else {
                                String t2 = "";
                                for (int k = 0; k < ss.length - 1; k++) {
                                    if (k > 0) {
                                        t2 += ".";
                                    }
                                    t2 += ss[k];
                                }
                                t2 += "." + t;
                                flag = judgeExtension(t2, type);
                                if (flag) {
                                    break;
                                }
                            }
                        }
                        if (typeList.contains(type) || typeList.contains(type2) || flag) {
//                                    if(api.equals(label) && !typeList.contains(type) && !typeList.contains(type2)) {
//                                        System.out.println(label + " " + type + " " + type2 + " " + typeList + " " + trace);
                            //}
                            // || typeList.contains(type2) || api.equals(label)
                            list.add(str);
                        } else {
                            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                            Method[] methods = clazz.getMethods();
                            Field[] fields = clazz.getFields();
                            for (int j = 0; j < methods.length; j++) {
                                if (methods[j].toString().contains(api)) {
                                    if (methods[j].toString().contains("static ")) {
                                        list.add(str);
                                        break;
                                    }
                                }
                            }
                            for (int j = 0; j < fields.length; j++) {
                                if (fields[j].toString().contains(api)) {
                                    if (fields[j].toString().contains("static ")) {
                                        list.add(str);
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        list.add(str);
                    }
                } catch (Exception e) {
                    list.add(str);
                    //e.printStackTrace();
                } catch (Error e) {
                    list.add(str);
                    //e.printStackTrace();
                }
            } else {
                list.add(str);
            }
        } else {
            list.add(str);
        }
    }


    public static boolean judgeExtension(String type, String type2) {
        if (type.equals("java.lang.Object") || type2.equals("java.lang.Object")) {
            return false;
        }
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
            Class clazz2 = Thread.currentThread().getContextClassLoader().loadClass(type2);
            if (clazz.isAssignableFrom(clazz2)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        } catch (Error e) {
            return false;
        }
    }

    public  boolean isFilter(String str) {
        if (
                str.endsWith(".ArrayNull[]") ||
                        str.endsWith(".ArrayNull[][]") ||
                        str.endsWith(".ArrayConstant[]") ||
                        str.endsWith(".ArrayConstant[][]") ||
                        str.endsWith(".ArrayInit[]{}") ||
                        str.endsWith(".ArrayInit[][]{}") ||
                        str.endsWith(".ArrayDeclaration[]") ||
                        str.endsWith(".ArrayDeclaration[][]") ||
                        str.endsWith(".Null") ||
                        str.endsWith(".Constant") ||
                        str.endsWith(".Declaration") ||
                        str.endsWith(".Cast") ||
                        str.endsWith(".new[]") ||
                        str.endsWith(".new[][]") ||
                        str.endsWith(".length") ||
                        str.contains("[index]") ||
                        str.contains(").") ||
                        str.equals("return") ||
                        str.equals("break") ||
                        str.equals("continue")) {
            return true;
        } else if (str.contains(".new(")) {
            return false;
        } else if (isControl(str)) {
            return false;
        }
        else if(isSpecialControl(str)){
            return true;
        }
        else {
            NameExtractor ne = new NameExtractor();
            ne.init();
            if (ne.isNeedForParameter(str)) {
                NameItem ni = ne.getReceiverParameters(str);
                String[] types = ni.toString().split(" +");
                if (types.length > 0) {
                    String type = types[0];
                    try {
                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                        return false;
                    } catch (Exception e) {
                        return true;
                    } catch (Error e) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public  boolean isSpecialControl(String prediction){
        List<String> list = new ArrayList<>();
        //list.add("if");
        list.add("elseif");
        list.add("else");
        //list.add("for");
        //list.add("while");
        list.add("doWhile");
        //list.add("foreach");
        list.add("try");
        list.add("catch");
        list.add("finally");
        list.add("switch");
        list.add("case");
        list.add("deafult");
        list.add("break");
        list.add("continue");
        list.add("return");
        list.add("end");
        list.add("termination");
        list.add("conditionEnd");
        if (list.contains(prediction)) {
            return true;
        }
        return false;
    }

    public  boolean isControl(String prediction) {
        List<String> list = new ArrayList<>();
        list.add("if");
//        list.add("elseif");
//        list.add("else");
        list.add("for");
        list.add("while");
//        list.add("doWhile");
        list.add("foreach");
//        list.add("try");
//        list.add("catch");
//        list.add("finally");
//        list.add("switch");
//        list.add("case");
//        list.add("deafult");
//        list.add("break");
//        list.add("continue");
//        list.add("return");
//        list.add("end");
//        list.add("termination");
//        list.add("conditionEnd");
        if (list.contains(prediction)) {
            return true;
        }
        return false;
    }

}



