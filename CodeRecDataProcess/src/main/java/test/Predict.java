package test;


import codeAnalysis.codeProcess.AndroidGraphCreator;
import codeAnalysis.codeProcess.GraphCreator;
import codeAnalysis.codeProcess.UserClassProcessing;
import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import config.DataConfig;
import javafx.util.Pair;
import utils.ConstructGraphUtil;
import utils.JavaParserUtil;

import codeAnalysis.codeRepresentation.GraphNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchi on 19/9/17.
 */
public class Predict {
    private String serialNumberString;
    public GraphNode childNode;
    public GraphNode parentNode;
    public boolean isThrowException;
    public GraphNode holeParentNode = null;
    public GraphNode holeChildNode = null;

    public void setChildNode(GraphNode childNode) {
        this.childNode = childNode;
    }

    public GraphNode getChildNode(){return childNode;}

    public String getSerialNumberString() {
        return serialNumberString;
    }

    public void setSerialNumberString(String serialNumberString) {
        this.serialNumberString = serialNumberString;
    }


    public List<String> predict(Graph codeGraph, String globalPath) {//String path
        codeGraph.setSerialNumberofNode(codeGraph.getRoot(),new ArrayList<>());
        //remove old hole node
//        GraphNode holeNode = codeGraph.getHoleNode(new ArrayList<>());
//        codeGraph.removeNode(holeNode,new ArrayList<>());
//        //add new hole node
//        GraphNode hole = new GraphNode();
//        hole.setClassName("hole");
//        hole.setCompleteClassName("hole");
//        hole.setMethodName("");
//        hole.setCompleteMethodName("");
//        hole.setAddMethodName(false);
//        hole.setSerialNumber(codeGraph.getTotalNumber(new ArrayList<>()) + 1);
//        //holeNumber = hole.getSerialNumber();
//        codeGraph.addNode(codeGraph.getGraphNode(Integer.parseInt(serialNumberString),new ArrayList<>()), hole, new ArrayList<>(),"unknown");
//        codeGraph.setSerialNumberofNode(codeGraph.getRoot(),new ArrayList<>());
        serialNumberString = "0";
        //save parameter in file
        ConstructGraphUtil util = new ConstructGraphUtil();
        Pair<String, String> graphRepresent = util.getGraphStr(codeGraph);
        String nodeEdgeSet = graphRepresent.getKey();
        String words = graphRepresent.getValue();
        codeGraph.initAllVariables(codeGraph.getRoot(),new ArrayList<>());
        List<String> allVariableNames = codeGraph.getAllVariableNamesList();
        String variableNameSet = allVariableNames.size()>0?allVariableNames.get(0):"";
        for (int i = 1; i < allVariableNames.size(); i++) {
            variableNameSet += " " + allVariableNames.get(i);
        }

        //nodeEdgeSet = "[[1,1,2],[2,3,3],[3,1,4],[3,2,5],[4,1,6],[4,2,5],[4,2,7],[5,1,8],[5,2,7],[6,1,9],[6,2,7],[6,2,10],[7,1,11],[9,1,5],[9,1,7],[9,1,10],[10,3,12],[12,4,13],[1,1,14],[14,1,15]]";
        //words = "{1:'try',2:'java.io.FileReader.new(java.lang.String)',3:'java.io.BufferedReader.new(java.io.Reader)',4:'java.lang.String.Declaration',5:'java.io.BufferedReader.readLine()',6:'java.lang.StringBuilder.new()',7:'java.lang.StringBuilder.append(java.lang.String)',8:'conditionEnd',9:'while',10:'java.lang.StringBuilder.toString()',11:'end',12:'java.lang.String.getBytes()',13:'hole',14:'catch',15:'end'}";
        //variableNameSet = "path digest algorithm reader br str message builder origin datum";
        //System.out.println(nodeEdgeSet + "\r\n");
        //System.out.println(words + "\r\n");
        //System.out.println(variableNameSet + "\r\n");

//        Runtime run_0 = Runtime.getRuntime();
//        String[] cmd_0 = new String[] {"source","activate","tf_gpu_1_14_0"};
//        try {
//            Process p = run_0.exec(cmd_0);
//        }catch (Exception e) {
//            System.out.println(e.getMessage());
//            // System.err.println("python error");
//        }catch(Error e){
//            System.out.println(e.getMessage());
//
//        }
        long st = System.currentTimeMillis();
        Runtime run = Runtime.getRuntime();
        String[] cmd = new String[] {"python3",globalPath + "/ggnn/Client.py",nodeEdgeSet,words,variableNameSet};
//        System.out.println(globalPath + "/ggnn/Client.py");
//        System.out.println(nodeEdgeSet);
//        System.out.println(words);
//        System.out.println(variableNameSet);
        List<String> top5Result = new ArrayList<>();
        try {
            Process p = run.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
            String line;
            boolean flag = false;
            while ((line = inBr.readLine()) != null) {
                if(line.equals("startrecord")){
                    flag = true;
                    continue;
                }
                if (flag) {
                    line = line.replaceAll("\r","");
                    line = line.replaceAll("\n","");
                    String prediciton = line.split(" +")[0];
//                    System.out.println(line);
                    line = line.replace("+false","");
                    //SyntaxChecker syntaxChecker = new SyntaxChecker();
                    //if(top5Result.size() < 5){
                    if(top5Result.size() < 10 && !prediciton.equals("conditionEnd") && !prediciton.equals("end") && !prediciton.equals("termination")){
                        top5Result.add(line);
                        //System.out.println(line);
                    }
                    /*
                    if(top5Result.size() < 10 && syntaxChecker.check(codeTree,Integer.parseInt(serialNumberString),line)){
                        top5Result.add(line);
                        System.out.println(line);
                    }*/
                }
            }
            //System.out.println();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
            // System.err.println("python error");
        }catch(Error e){
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
        //the following code is used to make sure that there are five candidates
        while(top5Result.size() < 10){
            top5Result.add("termination 0.01");
            //top5Result.add("termination");
        }
        //String prediction = top5Result.get(0).split(" ")[0];
        //serialNumberString = operateCodeTree(codeTree, prediction);
        long ed = System.currentTimeMillis();
        System.out.println("predict time:" + (ed-st));
        return top5Result;
    }

    public List<Graph> getAndroidCodeGraph(String filePath, boolean isFilePath, boolean holeFlag, String globalPath, List<String> jdkList,List<String> gloveVocabList, List<String> stopWordsList) throws Exception {
        JavaParserUtil japaAst = new JavaParserUtil(true);
        List<String> tempList = new ArrayList<>();
        CompilationUnit cu = new CompilationUnit();
        //System.out.println(filePath);
        try {
            if (isFilePath) {
                cu = StaticJavaParser.parse(new File(filePath));
            } else {
                InputStream in = new ByteArrayInputStream(filePath.getBytes());
                cu = StaticJavaParser.parse(in);
            }
            tempList = new ArrayList<>(japaAst.parse(cu));
        } catch (Exception e) {
            List<Graph> result = new ArrayList<>();
            result.add(null);
            return result;
        } catch (Error e) {
            List<Graph> result = new ArrayList<>();
            result.add(null);
            return result;
        }
        //如果Import的包中带有*号，那么得到含有*号的这个import
        List importList = cu.getImports();
        List<String> starImportStringList = new ArrayList<>();
        if (importList != null) {
            for (int i = 0; i < importList.size(); i++) {
                if (importList.get(i).toString().contains("*")) {
                    String str = importList.get(i).toString();
                    int index = str.indexOf("import");
                    str = str.substring(index);
                    String[] strs = str.split(" ");
                    str = strs[strs.length - 1];//得到Import的包的信息
                    str = str.replace(" ", ""); //替换掉空格" "
                    str = str.replace(";", ""); //去除;
                    starImportStringList.add(str);
                }
            }
        }
        //开始分析程序
        List<Graph> result = new ArrayList<>();
        if (cu.getTypes() != null) {
            for (TypeDeclaration type : cu.getTypes()) {
                if (type instanceof ClassOrInterfaceDeclaration) {
                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
                    result = dealAndroidClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)type,fieldExpressionList,japaAst,tempList,result,
                            starImportStringList,filePath,holeFlag,globalPath,jdkList,gloveVocabList,stopWordsList);
                    if(result.size() > 0){
                        break;
                    }
                }
            }
        }
        return result;
    }

    public List<Graph> getCodeGraph(String filePath, boolean isFilePath, boolean holeFlag, String globalPath, List<String> jdkList,List<String> gloveVocabList, List<String> stopWordsList) throws Exception {
        JavaParserUtil japaAst = new JavaParserUtil(true);
        List<String> tempList = new ArrayList<>();
        CompilationUnit cu = new CompilationUnit();
        //System.out.println(filePath);
        try {
            if (isFilePath) {
                cu = StaticJavaParser.parse(new File(filePath));
            } else {
                InputStream in = new ByteArrayInputStream(filePath.getBytes());
                cu = StaticJavaParser.parse(in);
            }
            tempList = new ArrayList<>(japaAst.parse(cu));
        } catch (Exception e) {
            List<Graph> result = new ArrayList<>();
            result.add(null);
            return result;
        } catch (Error e) {
            List<Graph> result = new ArrayList<>();
            result.add(null);
            return result;
        }
        //如果Import的包中带有*号，那么得到含有*号的这个import
        List importList = cu.getImports();
        List<String> starImportStringList = new ArrayList<>();
        if (importList != null) {
            for (int i = 0; i < importList.size(); i++) {
                if (importList.get(i).toString().contains("*")) {
                    String str = importList.get(i).toString();
                    int index = str.indexOf("import");
                    str = str.substring(index);
                    String[] strs = str.split(" ");
                    str = strs[strs.length - 1];//得到Import的包的信息
                    str = str.replace(" ", ""); //替换掉空格" "
                    str = str.replace(";", ""); //去除;
                    starImportStringList.add(str);
                }
            }
        }
        //开始分析程序
        List<Graph> result = new ArrayList<>();
        if (cu.getTypes() != null) {
            for (TypeDeclaration type : cu.getTypes()) {
                if (type instanceof ClassOrInterfaceDeclaration) {
                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
                    result = dealClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)type,fieldExpressionList,japaAst,tempList,result,
                            starImportStringList,filePath,holeFlag,globalPath,jdkList,gloveVocabList,stopWordsList);
                    if(result.size() > 0){
                        break;
                    }
                }
            }
        }
        return result;
    }

    public List<Graph> dealAndroidClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration type, List<VariableDeclarationExpr> fieldExpressionList, JavaParserUtil japaAst, List<String> tempList, List<Graph> result,
                                                                  List<String> starImportStringList, String filePath, boolean holeFlag, String globalPath, List<String> jdkList,List<String> gloveVocabList, List<String> stopWordsList){
        //处理field
        for (BodyDeclaration body : type.getMembers()) {
            if (body instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) body;
                for (int i = 0; i < field.getVariables().size(); i++) {
                    VariableDeclarationExpr expr = new VariableDeclarationExpr();
                    NodeList<VariableDeclarator> list = new NodeList<>();
                    list.add(field.getVariables().get(i));
                    expr.setAllTypes(field.getCommonType());
                    expr.setVariables(list);
                    fieldExpressionList.add(expr);
                }
            }
        }
        //处理method
        for (BodyDeclaration body : type.getMembers()) {
            if(body instanceof ClassOrInterfaceDeclaration){
                return dealAndroidClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)body,fieldExpressionList,japaAst,tempList,result,
                        starImportStringList,filePath,holeFlag,globalPath,jdkList,gloveVocabList,stopWordsList);
            }
            if (body instanceof MethodDeclaration || body instanceof ConstructorDeclaration) {
//                List<Comment> comments = new ArrayList<>();
//                if(body instanceof MethodDeclaration){
//                    comments= ((MethodDeclaration)body).getAllContainedComments();
//                }else{
//                    comments= ((ConstructorDeclaration)body).getAllContainedComments();
//                }
//                String all_comment = "";
//                for(Comment c:comments){
//                    all_comment += c.toString();
//                }
//                if(!all_comment.contains("/*hole*/")){
//                    continue;
//                }
                int lines = 3;
                if (lines >= 2) {
                    List<String> completeClassNameList = new ArrayList<>();
                    for (String str : tempList) {
                        completeClassNameList.add(str);
                    }
                    List userClassList = new ArrayList();
                    for (String str : japaAst.getUserDefinedClassNames()) {
                        userClassList.add(str);
                    }
                    UserClassProcessing userClassProcessing = new UserClassProcessing();
                    userClassProcessing.setUserClassList(userClassList);
                    userClassProcessing.setJdkList(jdkList);
                    userClassList.add("userDefinedClass");
                    MethodDeclaration method;
                    if (body instanceof ConstructorDeclaration) {
                        String constructorDeclaration = body.toString();
                        String str = constructorDeclaration.split("\\(")[0];
                        String str2 = str;
                        String[] strs = str2.split(" +");
                        String str3 = "";
                        for (int i = 0; i < strs.length - 1; i++) {
                            str3 += strs[i];
                            str3 += " ";
                        }
                        str3 += "void ";
                        str3 += strs[strs.length - 1].toLowerCase();
                        constructorDeclaration = constructorDeclaration.replaceFirst(str, str3);
                        constructorDeclaration = "public class Test{" + constructorDeclaration + "}";
                        InputStream in = new ByteArrayInputStream(constructorDeclaration.getBytes());
                        try {
                            CompilationUnit compilationUnit = StaticJavaParser.parse(in);
                            method = (MethodDeclaration) compilationUnit.getTypes().get(0).getMembers().get(0);
                            method.setBody(((ConstructorDeclaration) body).getBody());
                        } catch (Exception e) {
                            result.add(null);
                            return null;
                            //continue;
                        } catch (Error e) {
                            result.add(null);
                            return null;
                            //continue;
                        }
                    } else {
                        method = (MethodDeclaration) body;
                    }
                    //System.out.println(method.getName() + " " + method.getParameters());
                    List<String> parameterNameList = new ArrayList<>();
                    List<String> typeMapList = new ArrayList<>();
                    List<String> completeTypeMapList = new ArrayList<>();
                    List<ExpressionStmt> parameterExpressionList = new ArrayList<>();
                    if (method.getParameters() != null) {
                        List<Parameter> parameterList = method.getParameters();
                        for (int i = 0; i < parameterList.size(); i++) {
                            String contentString = "public class Test{public void test(){$}}";
                            String parameterString = parameterList.get(i).toString() + ";";
                            contentString = contentString.replaceAll("\\$", parameterString);
                            InputStream in = new ByteArrayInputStream(contentString.getBytes());
                            try {
                                CompilationUnit compilationUnit = StaticJavaParser.parse(in);
                                Node node = compilationUnit.getTypes().get(0).getMembers().get(0);
                                ExpressionStmt expression = (ExpressionStmt) node.getChildNodes().get(1).getChildNodes().get(0);
                                parameterExpressionList.add(expression);
                            } catch (Exception e) {
                                result.add(null);
                                return result;
                            } catch (Error e) {
                                result.add(null);
                                return result;
                            }
                        }
                    }
                    /*添加类中的成员变量*/
                    AndroidGraphCreator creator = new AndroidGraphCreator(globalPath);
                    creator.setUserClassProcessing(userClassProcessing);
                    creator.setStarImportStringList(starImportStringList);
                    AndroidGraphCreator creator2 = new AndroidGraphCreator(globalPath);
                    creator2.setUserClassProcessing(userClassProcessing);
                    creator2.setStarImportStringList(starImportStringList);
                    List<String> tempUserClassList = new ArrayList<>();
                    for (int i = 0; i < completeClassNameList.size(); i++) {
                        try {
                            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                            if (jdkList.contains(completeClassNameList.get(i))) {
                                creator.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
                                creator2.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
                            } else {
                                tempUserClassList.add(completeClassNameList.get(i));
                                userClassList.add(completeClassNameList.get(i));
                            }
                        } catch (Exception e) {
                            tempUserClassList.add(completeClassNameList.get(i));
                            userClassList.add(completeClassNameList.get(i));
                        } catch (Error e) {
                            //System.err.println(e.getCause());
                            tempUserClassList.add(completeClassNameList.get(i));
                            userClassList.add(completeClassNameList.get(i));
                        }

                    }
                    //过滤掉反射不到的类
                    for (int i = 0; i < tempUserClassList.size(); i++) {
                        completeClassNameList.remove(tempUserClassList.get(i));
                    }
                    tempUserClassList.removeAll(tempUserClassList);
                    //处理field
                    for (int i = 0; i < fieldExpressionList.size(); i++) {
                        creator.convert(fieldExpressionList.get(i));
                    }
                    //处理method中的parameter
                    for (int i = 0; i < parameterExpressionList.size(); i++) {
                        creator.convert(parameterExpressionList.get(i));
                        creator2.convert(parameterExpressionList.get(i));
                    }
                    for (int i = 0; i < fieldExpressionList.size(); i++) {
                        creator2.convert(fieldExpressionList.get(i));
                    }

                    /*get code tree from japa parse*/
                    Graph graph = constructAndroidGraphFromAST(completeClassNameList, parameterNameList, typeMapList,
                            completeTypeMapList, starImportStringList, method, creator, userClassProcessing, holeFlag, globalPath, jdkList,
                            gloveVocabList, stopWordsList);
                    if (graph != null && graph.getRoot() != null) {
                        /*display the code tree*/
                        //String functionTrace = method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") ";
                        String functionTrace = method.getNameAsString();
                        functionTrace += "[";
                        int parameterCount = 0;
                        if (method.getThrownExceptions() != null && method.getThrownExceptions().size() > 0) {
                            isThrowException = true;
                        }
                        if (method.getParameters() != null) {
                            for (Parameter parameter : method.getParameters()) {
                                parameterCount++;
                                if (parameterCount > 1) {
                                    functionTrace += ", ";
                                }
                                functionTrace += parameter.getType().toString();
                                functionTrace += " " + parameter.getName();
                            }
                        }
                        functionTrace += "]";
                        functionTrace += " (" + filePath + ") ";
                        functionTrace = functionTrace.replaceAll("\r", "");
                        functionTrace = functionTrace.replaceAll("\n", "");
                        graph.setFunctionTrace(functionTrace);
                        graph.setHoleNodeEdgeToUnknown(graph.getRoot(), new ArrayList<>());
                        // graph.dealHoleNode(new ArrayList<>(),new ArrayList<>());
                        // System.out.println(holeParentNode + " " + holeChildNode);
                        result.add(graph);
                        return result;
                        //displayTree(graph,false);
                        /*store the code tree in mongodb*/
                        //storeTreeInDB(graph);
                        /*construct training tree data */
                        //constructTrainingData(graph, treeWriter, predictionWriter, classWriter, generationNodeWriter, treeSentenceWriter, jarWriter, holeSizeWriter, traceWriter, blockpredictionWriter,originalStatementsWriter,variableNamesWriter, true);
                    } else {
                        result.add(null);
                        return result;
                        //System.err.println("So " + method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") " + " can not be correctly parsed");
                    }
                }
            }
        }
        return result;
    }

    public List<Graph> dealClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration type, List<VariableDeclarationExpr> fieldExpressionList, JavaParserUtil japaAst, List<String> tempList, List<Graph> result,
                                                             List<String> starImportStringList, String filePath, boolean holeFlag, String globalPath, List<String> jdkList,List<String> gloveVocabList, List<String> stopWordsList){
        //处理field
        for (BodyDeclaration body : type.getMembers()) {
            if (body instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) body;
                for (int i = 0; i < field.getVariables().size(); i++) {
                    VariableDeclarationExpr expr = new VariableDeclarationExpr();
                    NodeList<VariableDeclarator> list = new NodeList<>();
                    list.add(field.getVariables().get(i));
                    expr.setAllTypes(field.getCommonType());
                    expr.setVariables(list);
                    fieldExpressionList.add(expr);
                }
            }
        }
        //处理method
        for (BodyDeclaration body : type.getMembers()) {
            if(body instanceof ClassOrInterfaceDeclaration){
                return dealClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)body,fieldExpressionList,japaAst,tempList,result,
                        starImportStringList,filePath,holeFlag,globalPath,jdkList,gloveVocabList,stopWordsList);
            }
            if (body instanceof MethodDeclaration || body instanceof ConstructorDeclaration) {
//                List<Comment> comments = new ArrayList<>();
//                if(body instanceof MethodDeclaration){
//                    comments= ((MethodDeclaration)body).getAllContainedComments();
//                }else{
//                    comments= ((ConstructorDeclaration)body).getAllContainedComments();
//                }
//                String all_comment = "";
//                for(Comment c:comments){
//                    all_comment += c.toString();
//                }
//                if(!all_comment.contains("/*hole*/")){
//                    continue;
//                }
                int lines = 3;
                if (lines >= 2) {
                    List<String> completeClassNameList = new ArrayList<>();
                    for (String str : tempList) {
                        completeClassNameList.add(str);
                    }
                    List userClassList = new ArrayList();
                    for (String str : japaAst.getUserDefinedClassNames()) {
                        userClassList.add(str);
                    }
                    UserClassProcessing userClassProcessing = new UserClassProcessing();
                    userClassProcessing.setUserClassList(userClassList);
                    userClassProcessing.setJdkList(jdkList);
                    userClassList.add("userDefinedClass");
                    MethodDeclaration method;
                    if (body instanceof ConstructorDeclaration) {
                        String constructorDeclaration = body.toString();
                        String str = constructorDeclaration.split("\\(")[0];
                        String str2 = str;
                        String[] strs = str2.split(" +");
                        String str3 = "";
                        for (int i = 0; i < strs.length - 1; i++) {
                            str3 += strs[i];
                            str3 += " ";
                        }
                        str3 += "void ";
                        str3 += strs[strs.length - 1].toLowerCase();
                        constructorDeclaration = constructorDeclaration.replaceFirst(str, str3);
                        constructorDeclaration = "public class Test{" + constructorDeclaration + "}";
                        InputStream in = new ByteArrayInputStream(constructorDeclaration.getBytes());
                        try {
                            CompilationUnit compilationUnit = StaticJavaParser.parse(in);
                            method = (MethodDeclaration) compilationUnit.getTypes().get(0).getMembers().get(0);
                            method.setBody(((ConstructorDeclaration) body).getBody());
                        } catch (Exception e) {
                            result.add(null);
                            return null;
                            //continue;
                        } catch (Error e) {
                            result.add(null);
                            return null;
                            //continue;
                        }
                    } else {
                        method = (MethodDeclaration) body;
                    }
                    //System.out.println(method.getName() + " " + method.getParameters());
                    List<String> parameterNameList = new ArrayList<>();
                    List<String> typeMapList = new ArrayList<>();
                    List<String> completeTypeMapList = new ArrayList<>();
                    List<ExpressionStmt> parameterExpressionList = new ArrayList<>();
                    if (method.getParameters() != null) {
                        List<Parameter> parameterList = method.getParameters();
                        for (int i = 0; i < parameterList.size(); i++) {
                            String contentString = "public class Test{public void test(){$}}";
                            String parameterString = parameterList.get(i).toString() + ";";
                            contentString = contentString.replaceAll("\\$", parameterString);
                            InputStream in = new ByteArrayInputStream(contentString.getBytes());
                            try {
                                CompilationUnit compilationUnit = StaticJavaParser.parse(in);
                                Node node = compilationUnit.getTypes().get(0).getMembers().get(0);
                                // todo: check the number
                                ExpressionStmt expression = (ExpressionStmt) node.getChildNodes().get(3).getChildNodes().get(0);
                                parameterExpressionList.add(expression);
                            } catch (Exception e) {
                                e.printStackTrace();
                                result.add(null);
                                return result;
                            } catch (Error e) {
                                e.printStackTrace();
                                result.add(null);
                                return result;
                            }
                        }
                    }
                    /*添加类中的成员变量*/
                    GraphCreator creator = new GraphCreator();
                    creator.setUserClassProcessing(userClassProcessing);
                    creator.setStarImportStringList(starImportStringList);
                    GraphCreator creator2 = new GraphCreator();
                    creator2.setUserClassProcessing(userClassProcessing);
                    creator2.setStarImportStringList(starImportStringList);
                    List<String> tempUserClassList = new ArrayList<>();
                    for (int i = 0; i < completeClassNameList.size(); i++) {
                        try {
                            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                            if (jdkList.contains(completeClassNameList.get(i))) {
                                creator.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
                                creator2.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
                            } else {
                                tempUserClassList.add(completeClassNameList.get(i));
                                userClassList.add(completeClassNameList.get(i));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            tempUserClassList.add(completeClassNameList.get(i));
                            userClassList.add(completeClassNameList.get(i));
                        } catch (Error e) {
                            //System.err.println(e.getCause());
                            e.printStackTrace();
                            tempUserClassList.add(completeClassNameList.get(i));
                            userClassList.add(completeClassNameList.get(i));
                        }

                    }
                    //过滤掉反射不到的类
                    for (int i = 0; i < tempUserClassList.size(); i++) {
                        completeClassNameList.remove(tempUserClassList.get(i));
                    }
                    tempUserClassList.removeAll(tempUserClassList);
                    //处理field
                    for (int i = 0; i < fieldExpressionList.size(); i++) {
                        creator.convert(fieldExpressionList.get(i));
                    }
                    //处理method中的parameter
                    for (int i = 0; i < parameterExpressionList.size(); i++) {
                        creator.convert(parameterExpressionList.get(i));
                        creator2.convert(parameterExpressionList.get(i));
                    }
                    for (int i = 0; i < fieldExpressionList.size(); i++) {
                        creator2.convert(fieldExpressionList.get(i));
                    }

                    /*get code tree from japa parse*/
                    Graph graph = constructGraphFromAST(completeClassNameList, parameterNameList, typeMapList,
                            completeTypeMapList, starImportStringList, method, creator, userClassProcessing, holeFlag, globalPath, jdkList,
                            gloveVocabList, stopWordsList);
                    if (graph != null && graph.getRoot() != null) {
                        /*display the code tree*/
                        //String functionTrace = method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") ";
                        String functionTrace = method.getNameAsString();
                        functionTrace += "[";
                        int parameterCount = 0;
                        if (method.getThrownExceptions() != null && method.getThrownExceptions().size() > 0) {
                            isThrowException = true;
                        }
                        if (method.getParameters() != null) {
                            for (Parameter parameter : method.getParameters()) {
                                parameterCount++;
                                if (parameterCount > 1) {
                                    functionTrace += ", ";
                                }
                                functionTrace += parameter.getType().toString();
                                functionTrace += " " + parameter.getName();
                            }
                        }
                        functionTrace += "]";
                        functionTrace += " (" + filePath + ") ";
                        functionTrace = functionTrace.replaceAll("\r", "");
                        functionTrace = functionTrace.replaceAll("\n", "");
                        graph.setFunctionTrace(functionTrace);
                        graph.setHoleNodeEdgeToUnknown(graph.getRoot(), new ArrayList<>());
                        // graph.dealHoleNode(new ArrayList<>(),new ArrayList<>());
                        // System.out.println(holeParentNode + " " + holeChildNode);
                        result.add(graph);
                        return result;
                        //displayTree(graph,false);
                        /*store the code tree in mongodb*/
                        //storeTreeInDB(graph);
                        /*construct training tree data */
                        //constructTrainingData(graph, treeWriter, predictionWriter, classWriter, generationNodeWriter, treeSentenceWriter, jarWriter, holeSizeWriter, traceWriter, blockpredictionWriter,originalStatementsWriter,variableNamesWriter, true);
                    } else {
                        result.add(null);
                        return result;
                        //System.err.println("So " + method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") " + " can not be correctly parsed");
                    }
                }
            }
        }
        return result;
    }

    public Graph constructAndroidGraphFromAST(List<String> completeClassNameList, List<String> parameterNameList,
                                                  List<String> typeMapList, List<String> completeTypeMapList,
                                                  List<String> starImportStringList, MethodDeclaration method,
                                                  AndroidGraphCreator fieldCreator, UserClassProcessing userClassProcessing,
                                                  boolean holeFlag, String globalPath, List<String> jdkList,
                                                  List<String> gloveVocabList, List<String> stopWordsList) {
        try {
            AndroidGraphCreator creator = new AndroidGraphCreator(completeClassNameList, fieldCreator, globalPath, jdkList);
            creator.setHoleFlag(holeFlag);
            for (int i = 0; i < parameterNameList.size(); i++) {
                creator.addClass_variable_list(parameterNameList.get(i));
            }
            for (int i = 0; i < typeMapList.size(); i++) {
                String[] strings = typeMapList.get(i).split(" ");
                creator.addClass_variable(strings[0], strings[1]);
            }
            for (int i = 0; i < completeTypeMapList.size(); i++) {
                creator.addClass_name_map(completeTypeMapList.get(i));
            }
            creator.setStarImportStringList(starImportStringList);
            creator.setUserClassProcessing(userClassProcessing);
            creator.toGraph(method);
//            if(creator.getGraph() != null && creator.getGraph().getRoot() != null &&
//                    "hole".equals(creator.getGraph().getRoot().getCompleteMethodDeclaration())){
//                Graph graph = new Graph();
//                GraphNode root = new GraphNode();
//                root.setCompleteMethodDeclaration("unknown");
//                graph.setRoot(root);
//                //添加类属性变量和函数声明中的变量
//                dealGraph(graph,creator,method,gloveVocabList,stopWordsList);
//                return graph;
//            }
            if(creator.getGraph() != null && creator.getParsedFlag() && creator.getGraph().getRoot() != null
                    && (creator.getGraph().getRoot().getCompleteMethodDeclaration().equals("hole") || userClassProcessing.isUserClassProcessing(creator.getGraph().getRoot().getCompleteClassName()))
                    && creator.getGraph().getTotalNumber(new ArrayList<>()) == 1){
                Graph graph = new Graph();
                GraphNode root = new GraphNode();
                root.setCompleteMethodDeclaration("hole");
                graph.setRoot(root);
                //添加类属性变量和函数声明中的变量
                dealAndroidGraph(graph,creator,method,gloveVocabList,stopWordsList);
                return graph;
            }
            else {
                Graph graph = new Graph();
                graph.setRoot(creator.getGraph().getRoot());
                if (creator.getParsedFlag()) {
                    //添加类属性变量和函数声明中的变量
                    if(graph.getRoot() != null) {
                        dealAndroidGraph(graph, creator, method, gloveVocabList, stopWordsList);
                        return graph;
                    }else{
                        GraphNode root = new GraphNode();
                        root.setCompleteMethodDeclaration("hole");
                        graph.setRoot(root);
                        //添加类属性变量和函数声明中的变量
                        dealAndroidGraph(graph,creator,method,gloveVocabList,stopWordsList);
                        return graph;
                    }

                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        } catch (Error e) {
            return null;
        }
    }

    public Graph constructGraphFromAST(List<String> completeClassNameList, List<String> parameterNameList,
                                             List<String> typeMapList, List<String> completeTypeMapList,
                                             List<String> starImportStringList, MethodDeclaration method,
                                             GraphCreator fieldCreator, UserClassProcessing userClassProcessing,
                                             boolean holeFlag, String globalPath, List<String> jdkList,
                                             List<String> gloveVocabList, List<String> stopWordsList) {
        try {
            GraphCreator creator = new GraphCreator(completeClassNameList, fieldCreator, jdkList);
            creator.setHoleFlag(holeFlag);
            for (int i = 0; i < parameterNameList.size(); i++) {
                creator.addClass_variable_list(parameterNameList.get(i));
            }
            for (int i = 0; i < typeMapList.size(); i++) {
                String[] strings = typeMapList.get(i).split(" ");
                creator.addClass_variable(strings[0], strings[1]);
            }
            for (int i = 0; i < completeTypeMapList.size(); i++) {
                creator.addClass_name_map(completeTypeMapList.get(i));
            }
            creator.setStarImportStringList(starImportStringList);
            creator.setUserClassProcessing(userClassProcessing);
            creator.toGraph(method);
//            if(creator.getGraph() != null && creator.getGraph().getRoot() != null &&
//                    "hole".equals(creator.getGraph().getRoot().getCompleteMethodDeclaration())){
//                Graph graph = new Graph();
//                GraphNode root = new GraphNode();
//                root.setCompleteMethodDeclaration("unknown");
//                graph.setRoot(root);
//                //添加类属性变量和函数声明中的变量
//                dealGraph(graph,creator,method,gloveVocabList,stopWordsList);
//                return graph;
//            }
            if(creator.getGraph() != null && creator.getParsedFlag() && creator.getGraph().getRoot() != null
                    && (creator.getGraph().getRoot().getCompleteMethodDeclaration().equals("hole") || userClassProcessing.isUserClassProcessing(creator.getGraph().getRoot().getCompleteClassName()))
                    && creator.getGraph().getTotalNumber(new ArrayList<>()) == 1){
                Graph graph = new Graph();
                GraphNode root = new GraphNode();
                root.setCompleteMethodDeclaration("hole");
                graph.setRoot(root);
                //添加类属性变量和函数声明中的变量
                dealGraph(graph,creator,method,gloveVocabList,stopWordsList);
                return graph;
            }
            else {
                Graph graph = new Graph();
                graph.setRoot(creator.getGraph().getRoot());
                if (creator.getParsedFlag()) {
                    //添加类属性变量和函数声明中的变量
                    if(graph.getRoot() != null) {
                        dealGraph(graph, creator, method, gloveVocabList, stopWordsList);
                        return graph;
                    }else{
                        GraphNode root = new GraphNode();
                        root.setCompleteMethodDeclaration("hole");
                        graph.setRoot(root);
                        //添加类属性变量和函数声明中的变量
                        dealGraph(graph,creator,method,gloveVocabList,stopWordsList);
                        return graph;
                    }

                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (Error e) {
            e.printStackTrace();
            return null;
        }
    }

    public void dealAndroidGraph(Graph graph, AndroidGraphCreator creator, MethodDeclaration method,List<String> gloveVocabList, List<String> stopWordsList){
//        Map<String, List<GraphNode>> map = creator.getVariableNodeMap();
//        for (String key : map.keySet()) {
//            List<GraphNode> list = map.get(key);
//            for (int i = 0; i < list.size(); i++) {
//                if ((list.get(i).isPrimitive() && !list.get(i).isVariablePreserved())) {
//                    if (!graph.removeNode(list.get(i), new ArrayList<>())) {
//                        //return null;
//                    }
//                }
//            }
//        }
        GraphNode node = new GraphNode();
        //node.setPreviousVariableNames(creator.getClass_variable_list());
        node.setPreviousVariableNames(creator.getUsedClassFieldAndMethodArgumentVariable());
        node.getPreviousVariableNames().add(0, method.getNameAsString());//添加方法名
        //添加所有参数名
        if (method.getParameters() != null) {
            List<Parameter> parameterList = method.getParameters();
            for (int i = 0; i < parameterList.size(); i++) {
                String parameterName = parameterList.get(i).getNameAsString();
                //node.getPreviousVariableNames().add(parameterList.get(i).getId().getName());
                if(!creator.isVariableBelong2UserDefinedClass(parameterName)) {
                    node.getPreviousVariableNames().add(parameterName);
                }
            }
        }
        for (int i = 0; i < node.getPreviousVariableNames().size(); i++) {
            String variableName = node.getPreviousVariableNames().get(i);
            variableName = variableName.replaceAll("\r", "");
            variableName = variableName.replaceAll("\n", "");
            node.getPreviousVariableNames().set(i, variableName);
        }
        graph.processRootVariables(node, gloveVocabList, stopWordsList);
        graph.setUsedClassFieldAndMethodArgumentVariableList(node.getPreviousVariableNames());
        graph.dealNodeVariables(graph.getRoot(), gloveVocabList, stopWordsList, new ArrayList<>());
        //过滤掉原始语句中的\r\n符号
        graph.filterSpecialCharacterInOriginalStatement(graph.getRoot(), new ArrayList<>());
        graph.setClass_name_map(creator.getClass_name_map());
        graph.setClass_variable(creator.getClass_variable());
        graph.setClass_variable_list(creator.getClass_variable_list());
        graph.setVariable_use_map(creator.getVariable_use_map());
        graph.setVariable_line_map(creator.getVariable_line_map());
    }

    public void dealGraph(Graph graph, GraphCreator creator, MethodDeclaration method,List<String> gloveVocabList, List<String> stopWordsList){
//        Map<String, List<GraphNode>> map = creator.getVariableNodeMap();
//        for (String key : map.keySet()) {
//            List<GraphNode> list = map.get(key);
//            for (int i = 0; i < list.size(); i++) {
//                if ((list.get(i).isPrimitive() && !list.get(i).isVariablePreserved())) {
//                    if (!graph.removeNode(list.get(i), new ArrayList<>())) {
//                        //return null;
//                    }
//                }
//            }
//        }
        GraphNode node = new GraphNode();
        //node.setPreviousVariableNames(creator.getClass_variable_list());
        node.setPreviousVariableNames(creator.getUsedClassFieldAndMethodArgumentVariable());
        node.getPreviousVariableNames().add(0, method.getNameAsString());//添加方法名
        //添加所有参数名
        if (method.getParameters() != null) {
            List<Parameter> parameterList = method.getParameters();
            for (int i = 0; i < parameterList.size(); i++) {
                String parameterName = parameterList.get(i).getNameAsString();
                //node.getPreviousVariableNames().add(parameterList.get(i).getId().getName());
                if(!creator.isVariableBelong2UserDefinedClass(parameterName)) {
                    node.getPreviousVariableNames().add(parameterName);
                }
            }
        }
        for (int i = 0; i < node.getPreviousVariableNames().size(); i++) {
            String variableName = node.getPreviousVariableNames().get(i);
            variableName = variableName.replaceAll("\r", "");
            variableName = variableName.replaceAll("\n", "");
            node.getPreviousVariableNames().set(i, variableName);
        }
        graph.processRootVariables(node, gloveVocabList, stopWordsList);
        graph.setUsedClassFieldAndMethodArgumentVariableList(node.getPreviousVariableNames());
        graph.dealNodeVariables(graph.getRoot(), gloveVocabList, stopWordsList, new ArrayList<>());
        //过滤掉原始语句中的\r\n符号
        graph.filterSpecialCharacterInOriginalStatement(graph.getRoot(), new ArrayList<>());
        graph.setClass_name_map(creator.getClass_name_map());
        graph.setClass_variable(creator.getClass_variable());
        graph.setClass_variable_list(creator.getClass_variable_list());
        graph.setVariable_use_map(creator.getVariable_use_map());
        graph.setVariable_line_map(creator.getVariable_line_map());
    }

}

