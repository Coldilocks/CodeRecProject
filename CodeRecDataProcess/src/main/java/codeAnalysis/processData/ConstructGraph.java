package codeAnalysis.processData;

import codeAnalysis.codeProcess.GraphCreator;
import codeAnalysis.codeProcess.UserClassProcessing;
import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import codeAnalysis.processData.processTrainData.ConstructFineTrainData;
import codeAnalysis.processData.processTrainData.ConstructPreTrainData;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import utils.JavaParserUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstructGraph {

    public int linesCount = 0;
    public int test = 0;
    public List<GraphNode> parameterNodeList = new ArrayList<>();

    public void constructGraph(int count, String filePath, boolean isFilePath,
                               List<String> jdkList, boolean holeFlag,
                               List<String> gloveVocabList, List<String> stopWordsList) {
        JavaParserUtil javaParserUtil = new JavaParserUtil(true);
        List<String> tempList = new ArrayList<>();
        CompilationUnit cu = new CompilationUnit();
        try {
            if (isFilePath) {
                cu = StaticJavaParser.parse(new File(filePath));
            } else {
                InputStream in = new ByteArrayInputStream(filePath.getBytes());
                cu = StaticJavaParser.parse(in);
            }
            tempList = new ArrayList<>(javaParserUtil.parse(cu));
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 如果Import的包中带有*号，那么得到含有*号的这个import
         * todo: 改成visitor模式
         */
        List<ImportDeclaration> importList = cu.getImports();
        List<String> starImportStringList = new ArrayList<>();
        if (importList != null) {
            /*
             *  todo: 识别错误，会算上import语句上方的注释中的*
             *  todo: 过滤方法错误，得到的str包含换行符\n
             *  todo: 把Object改成ImportDeclaration，然后getName
             */
            for (Object o : importList) {
                if (o.toString().contains("*")) {
                    String str = o.toString();
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
        if (cu.getTypes() != null) {
            for (TypeDeclaration type : cu.getTypes()) {
                if (type instanceof ClassOrInterfaceDeclaration) {
                    //处理field
                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
                    for (BodyDeclaration body : (NodeList<BodyDeclaration>) type.getMembers()) {
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
                    for (BodyDeclaration body : (NodeList<BodyDeclaration>) type.getMembers()) {
                        if (body instanceof MethodDeclaration) {
//                            int lines = countCodeLine(body);
                            MethodDeclaration _methodDeclaration = (MethodDeclaration) body;
                            int beginLine = _methodDeclaration.getBegin().isPresent() ? _methodDeclaration.getBegin().get().line: 0;
                            int endLine = _methodDeclaration.getEnd().isPresent() ? _methodDeclaration.getEnd().get().line: 0;
                            int lines = endLine - beginLine;
//                            if(lines > 100){
//                                continue;
//                            }
                            if (lines >= 2) {

                                List<String> completeClassNameList = new ArrayList<>(tempList);
                                List<String> userClassList = new ArrayList<>(javaParserUtil.getUserDefinedClassNames());
                                UserClassProcessing userClassProcessing = new UserClassProcessing();
                                userClassProcessing.setUserClassList(userClassList);
                                userClassProcessing.setJdkList(jdkList);
                                userClassList.add("userDefinedClass");
                                MethodDeclaration method = (MethodDeclaration) body;
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
                                        try {
                                            CompilationUnit compilationUnit = StaticJavaParser.parse(contentString);
                                            Node node = compilationUnit.getTypes().get(0).getMembers().get(0);
                                            // todo: node.getChildNodes().get(1) modified to node.getChildNodes().get(3)
                                            ExpressionStmt expression = (ExpressionStmt) node.getChildNodes().get(3).getChildNodes().get(0);
                                            parameterExpressionList.add(expression);
                                        } catch (Exception | Error e) {
                                        }
                                    }
                                }
                                /*添加类中的成员变量*/
                                GraphCreator creator = new GraphCreator();
                                creator.setUserClassProcessing(userClassProcessing);
                                creator.setStarImportStringList(starImportStringList);
                                List<String> tempUserClassList = new ArrayList<>();
                                for (int i = 0; i < completeClassNameList.size(); i++) {
                                    try {
                                        /* 判断是JDK中的类还是用户自定义类 */
                                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                                        if (jdkList.contains(completeClassNameList.get(i))) {
                                            creator.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
                                        } else {
                                            tempUserClassList.add(completeClassNameList.get(i));
                                            userClassList.add(completeClassNameList.get(i));
                                        }
                                    } catch (Exception | Error e) {
                                        tempUserClassList.add(completeClassNameList.get(i));
                                        userClassList.add(completeClassNameList.get(i));
                                    }

                                }
                                //过滤掉反射不到的类
                                completeClassNameList.removeAll(tempUserClassList);

                                // todo: why called on itself?
                                tempUserClassList.removeAll(tempUserClassList);

                                //处理field
                                for (VariableDeclarationExpr variableDeclarationExpr : fieldExpressionList) {
                                    creator.convert(variableDeclarationExpr);
                                }
                                //处理method中的parameter
                                for (ExpressionStmt expressionStmt : parameterExpressionList) {
                                    creator.convert(expressionStmt);
                                }
                                /*get code tree from japa parse*/
                                Graph graph = constructGraphFromAST(completeClassNameList, parameterNameList, typeMapList,
                                        completeTypeMapList, starImportStringList, method, creator, userClassProcessing, holeFlag, jdkList,
                                        gloveVocabList, stopWordsList);
                                if (graph != null && graph.getRoot() != null) {
                                    String functionTrace = method.getName().toString();
                                    functionTrace += "[";
                                    int parameterCount = 0;
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
                                    graph.setLineCount(lines);
                                    linesCount += lines;
                                    ConstructFineTrainData constructFineTrainData = new ConstructFineTrainData();
                                    constructFineTrainData.construct(graph, true);
                                    ConstructPreTrainData constructPreTrainData = new ConstructPreTrainData();
                                    constructPreTrainData.construct(graph,true);
                                    ConstructNameOnlyData constructNameOnlyData = new ConstructNameOnlyData();
                                    constructNameOnlyData.construct(graph, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Graph constructGraphFromAST(List<String> completeClassNameList, List<String> parameterNameList,
                                       List<String> typeMapList, List<String> completeTypeMapList,
                                       List<String> starImportStringList, MethodDeclaration method,
                                       GraphCreator fieldCreator, UserClassProcessing userClassProcessing,
                                       boolean holeFlag, List<String> jdkList,
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
            Graph graph = new Graph();
            graph.setRoot(creator.getGraph().getRoot());
            if (creator.getParsedFlag() && graph.getRoot() != null) {
                //移除没有用到的变量声明结点
                Map<String, List<GraphNode>> map = creator.getVariableNodeMap();
                for (String key : map.keySet()) {
                    List<GraphNode> list = map.get(key);
                    for (int i = 0; i < list.size(); i++) {
                        if ((list.get(i).isPrimitive() && !list.get(i).isVariablePreserved())) {
                            if (!graph.removeNode(list.get(i), new ArrayList<>())) {
                                return null;
                            }
                        }
                    }
                }
                //添加类属性变量和函数声明中的变量
                GraphNode node = new GraphNode();
                node.setPreviousVariableNames(creator.getUsedClassFieldAndMethodArgumentVariable());
                node.getPreviousVariableNames().add(0,method.getName().toString());//添加方法名
                graph.setMethodInfo(method.getName().toString());
                //添加所有参数名
                if (method.getParameters() != null) {
                    List<Parameter> parameterList = method.getParameters();
                    for (int i = 0; i < parameterList.size(); i++) {
                        String parameterName = parameterList.get(i).getName().toString();
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
                graph.dealNodeVariables(graph.getRoot(),gloveVocabList,stopWordsList,new ArrayList<>());
                graph.setAllMethodNamesList(graph.processVariables(method.getName().toString(),gloveVocabList,stopWordsList));
                graph.filterSpecialCharacterInOriginalStatement(graph.getRoot(), new ArrayList<>());
                return graph;
            } else {
                return null;
            }
        } catch (Exception | Error e) {
            //e.printStackTrace();
            return null;
        }
    }

    public int countCodeLine(Node node) {
        int result = 0;
        if (node instanceof Statement && !(node instanceof BlockStmt)) {
            result += 1;
            if (node instanceof IfStmt) {
                if (((IfStmt) node).getElseStmt().isPresent() && !(((IfStmt) node).getElseStmt().get() instanceof IfStmt)) {
                    result += 1;
                }
            } else if (node instanceof TryStmt) {
                if (((TryStmt) node).getFinallyBlock().isPresent()) {
                    result += 1;
                }
            }
        } else if (node instanceof CatchClause) {
            result += 1;
        }
        if (node.getChildNodes() != null) {
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                result += countCodeLine(node.getChildNodes().get(i));
            }
        }
        return result;
    }

//    public Graph getTestGraph(int count, String filePath, boolean isFilePath, List<String> jdkList,
//                              ObjectOutputStream graphWriter, FileWriter traceWriter, boolean holeFlag,
//                              List<String> gloveVocabList, List<String> stopWordsList) {
//        Graph result;
//        JavaParserUtil javaParserUtil = new JavaParserUtil(true);
//        List<String> tempList = new ArrayList<>();
//        CompilationUnit cu = new CompilationUnit();
//        try {
//            if (isFilePath) {
//                cu = StaticJavaParser.parse(new File(filePath));
//            } else {
//                InputStream in = new ByteArrayInputStream(filePath.getBytes());
//                cu = StaticJavaParser.parse(in);
//            }
//            tempList = javaParserUtil.parse(cu);
//        } catch (Exception e) {
//
//        } catch (Error e) {
//
//        }
//        //如果Import的包中带有*号，那么得到含有*号的这个import
//        List importList = cu.getImports();
//        List<String> starImportStringList = new ArrayList<>();
//        if (importList != null) {
//            for (int i = 0; i < importList.size(); i++) {
//                if (importList.get(i).toString().contains("*")) {
//                    String str = importList.get(i).toString();
//                    int index = str.indexOf("import");
//                    str = str.substring(index);
//                    String[] strs = str.split(" ");
//                    str = strs[strs.length - 1];//得到Import的包的信息
//                    str = str.replace(" ", ""); //替换掉空格" "
//                    str = str.replace(";", ""); //去除;
//                    starImportStringList.add(str);
//                }
//            }
//        }
//        //开始分析程序
//        if (cu.getTypes() != null) {
//            for (TypeDeclaration type : cu.getTypes()) {
//                if (type instanceof ClassOrInterfaceDeclaration) {
//                    //处理field
//                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
//                    for (BodyDeclaration body : (NodeList<BodyDeclaration>)type.getMembers()) {
//                        if (body instanceof FieldDeclaration) {
//                            FieldDeclaration field = (FieldDeclaration) body;
//                            for (int i = 0; i < field.getVariables().size(); i++) {
//                                VariableDeclarationExpr expr = new VariableDeclarationExpr();
//                                NodeList list = new NodeList();
//                                list.add(field.getVariables().get(i));
//                                expr.setAllTypes(field.getCommonType());
//                                expr.setVariables(list);
//                                fieldExpressionList.add(expr);
//                            }
//                        }
//                    }
//                    //处理method
//                    for (BodyDeclaration body : (NodeList<BodyDeclaration>)type.getMembers()) {
//                        if (body instanceof MethodDeclaration) {
//                            int lines = countCodeLine(body);
//                            if (lines >= 2) {
//                                List<String> completeClassNameList = new ArrayList<>();
//                                for (String str : tempList) {
//                                    completeClassNameList.add(str);
//                                }
//                                List userClassList = new ArrayList();
//                                JavaParserUtil javaParserUtil1 = new JavaParserUtil();
//                                for (String str : javaParserUtil1.getFilternames()) {
//                                    userClassList.add(str);
//                                }
//                                UserClassProcessing userClassProcessing = new UserClassProcessing();
//                                userClassProcessing.setUserClassList(userClassList);
//                                userClassProcessing.setJdkList(jdkList);
//                                userClassList.add("userDefinedClass");
//                                MethodDeclaration method = (MethodDeclaration) body;
//                                //System.out.println(method.getName() + " " + method.getParameters());
//                                List<String> parameterNameList = new ArrayList<>();
//                                List<String> typeMapList = new ArrayList<>();
//                                List<String> completeTypeMapList = new ArrayList<>();
//                                List<ExpressionStmt> parameterExpressionList = new ArrayList<>();
//                                if (method.getParameters() != null) {
//                                    List<Parameter> parameterList = method.getParameters();
//                                    for (int i = 0; i < parameterList.size(); i++) {
//                                        String contentString = "public class Test{public void test(){$}}";
//                                        String parameterString = parameterList.get(i).toString() + ";";
//                                        contentString = contentString.replaceAll("\\$", parameterString);
//                                        InputStream in = new ByteArrayInputStream(contentString.getBytes());
//                                        try {
//                                            CompilationUnit compilationUnit = StaticJavaParser.parse(in);
//                                            Node node = compilationUnit.getTypes().get(0).getMembers().get(0);
//                                            ExpressionStmt expression = (ExpressionStmt) node.getChildNodes().get(1).getChildNodes().get(0);
//                                            parameterExpressionList.add(expression);
//                                        } catch (Exception e) {
//                                            continue;
//                                        }
//                                    }
//                                }
//                                /*添加类中的成员变量*/
//                                GraphCreator creator = new GraphCreator();
//                                creator.setUserClassProcessing(userClassProcessing);
//                                creator.setStarImportStringList(starImportStringList);
//                                GraphCreator creator2 = new GraphCreator();
//                                creator2.setUserClassProcessing(userClassProcessing);
//                                creator2.setStarImportStringList(starImportStringList);
//                                List<String> tempUserClassList = new ArrayList<>();
//                                for (int i = 0; i < completeClassNameList.size(); i++) {
//                                    try {
//                                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
//                                        if (jdkList.contains(completeClassNameList.get(i))) {
//                                            creator.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
//                                            creator2.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
//                                        } else {
//                                            tempUserClassList.add(completeClassNameList.get(i));
//                                            userClassList.add(completeClassNameList.get(i));
//                                        }
//                                    } catch (Exception e) {
//                                        tempUserClassList.add(completeClassNameList.get(i));
//                                        userClassList.add(completeClassNameList.get(i));
//                                    } catch (Error e) {
//                                        //System.err.println(e.getCause());
//                                        tempUserClassList.add(completeClassNameList.get(i));
//                                        userClassList.add(completeClassNameList.get(i));
//                                    }
//
//                                }
//                                //过滤掉反射不到的类
//                                for (int i = 0; i < tempUserClassList.size(); i++) {
//                                    completeClassNameList.remove(tempUserClassList.get(i));
//                                }
//                                tempUserClassList.removeAll(tempUserClassList);
//                                //处理field
//                                for (int i = 0; i < fieldExpressionList.size(); i++) {
//                                    creator.convert(fieldExpressionList.get(i));
//                                }
//                                //处理method中的parameter
//                                for (int i = 0; i < parameterExpressionList.size(); i++) {
//                                    creator.convert(parameterExpressionList.get(i));
//                                    creator2.convert(parameterExpressionList.get(i));
//                                }
//                                for (int i = 0; i < fieldExpressionList.size(); i++) {
//                                    creator2.convert(fieldExpressionList.get(i));
//                                }
//                                parameterNodeList = new ArrayList<>();
//                                if (creator2.getGraph() != null && creator2.getGraph().getRoot() != null) {
//                                    creator2.getGraph().getParameterNodes(creator2.getGraph().getRoot(), parameterNodeList, new ArrayList<String>());
//                                }
//                                /*get code tree from japa parse*/
//                                Graph graph = constructGraphFromAST(completeClassNameList, parameterNameList, typeMapList,
//                                        completeTypeMapList, starImportStringList, method, creator, userClassProcessing, holeFlag, jdkList,
//                                        gloveVocabList, stopWordsList);
//                                if (graph != null && graph.getRoot() != null) {
//                                    String functionTrace = method.getName().toString();
//                                    functionTrace += "[";
//                                    int parameterCount = 0;
//                                    if (method.getParameters() != null) {
//                                        for (Parameter parameter : method.getParameters()) {
//                                            parameterCount++;
//                                            if (parameterCount > 1) {
//                                                functionTrace += ", ";
//                                            }
//                                            functionTrace += parameter.getType().toString();
//                                            functionTrace += " " + parameter.getName();
//                                        }
//                                    }
//                                    functionTrace += "]";
//                                    functionTrace += " (" + filePath + ") ";
//                                    functionTrace = functionTrace.replaceAll("\r", "");
//                                    functionTrace = functionTrace.replaceAll("\n", "");
//                                    graph.setFunctionTrace(functionTrace);
//                                    linesCount += lines;
//                                    return graph;
//                                } else {
//                                    return null;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

}


