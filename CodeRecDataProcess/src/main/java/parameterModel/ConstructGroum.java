package parameterModel;

import codeAnalysis.codeProcess.UserClassProcessing;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import utils.JavaParserUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConstructGroum {
    public int linesCount = 0;
    public int test = 0;
    public List<GroumNode> parameterNodeList = new ArrayList<>();

    public void constructGroum(int count, String filePath, boolean isFilePath, List<String> jdkList, ObjectOutputStream groumWriter, FileWriter traceWriter, boolean holeFlag, String globalPath,
                               List<String> gloveVocabList, List<String> stopWordsList) {
        JavaParserUtil japaAst = new JavaParserUtil(true);
        List<String> tempList = new ArrayList<>();
        CompilationUnit cu = new CompilationUnit();
        try {
            if (isFilePath) {
                cu = StaticJavaParser.parse(new File(filePath));
            } else {
                InputStream in = new ByteArrayInputStream(filePath.getBytes());
                cu = StaticJavaParser.parse(in);
            }
            tempList = new ArrayList<>(japaAst.parse(cu));
        } catch (Exception e) {

        } catch (Error e) {

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
        if (cu.getTypes() != null) {
            for (TypeDeclaration type : cu.getTypes()) {
                if (type instanceof ClassOrInterfaceDeclaration) {
                    //处理field
                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
                    for (Object body : type.getMembers()) {
                        if (body instanceof FieldDeclaration) {
                            FieldDeclaration field = (FieldDeclaration) body;
                            for (int i = 0; i < field.getVariables().size(); i++) {
                                VariableDeclarationExpr expr = new VariableDeclarationExpr();
                                NodeList<VariableDeclarator> list = new NodeList();
                                list.add(field.getVariables().get(i));
                                expr.setAllTypes(field.getCommonType());
                                expr.setVariables(list);
                                fieldExpressionList.add(expr);
                            }
                        }
                    }
                    //处理method
                    for (Object body : type.getMembers()) {
                        if (body instanceof MethodDeclaration) {
                            int lines = countCodeLine((Node)body);
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
                                        InputStream in = new ByteArrayInputStream(contentString.getBytes());
                                        try {
                                            CompilationUnit compilationUnit = StaticJavaParser.parse(in);
                                            Node node = compilationUnit.getTypes().get(0).getMembers().get(0);
                                            ExpressionStmt expression = (ExpressionStmt) node.getChildNodes().get(1).getChildNodes().get(0);
                                            parameterExpressionList.add(expression);
                                        } catch (Exception e) {
                                            continue;
                                        } catch (Error e) {
                                            continue;
                                        }
                                        // String[] strings = parameterList.get(i).toString().split(" ");
                                        // parameterNameList.add(strings[strings.length - 1]);
                                        // typeMapList.add(strings[strings.length - 1] + " " + parameterList.get(i).getType().toString());
                                        //completeTypeMapList.add(parameterList.get(i).getType().toString());
                                    }
                                }
                    /*添加类中的成员变量*/
                                GroumCreator creator = new GroumCreator(globalPath);
                                creator.setUserClassProcessing(userClassProcessing);
                                creator.setStarImportStringList(starImportStringList);
                                List<String> tempUserClassList = new ArrayList<>();
                                for (int i = 0; i < completeClassNameList.size(); i++) {
                                    try {
                                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                                        if (jdkList.contains(completeClassNameList.get(i))) {
                                            creator.getClass_name_map().put(clazz.getSimpleName(), completeClassNameList.get(i));
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
                                }
                    /*get code tree from japa parse*/
                                Groum groum = constructGroumFromAST(completeClassNameList, parameterNameList, typeMapList,
                                        completeTypeMapList, starImportStringList, method, creator, userClassProcessing, holeFlag, globalPath, jdkList,
                                        gloveVocabList, stopWordsList);
                                if (groum != null && groum.getRoot() != null) {
                    /*display the code tree*/
                                    //String functionTrace = method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") ";
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
                                    groum.setFunctionTrace(functionTrace);
                                    linesCount += lines;
                                    System.out.println(groum.getRoot().getChildNodes());
                                } else {
                                    //System.err.println("So " + method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") " + " can not be correctly parsed");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Groum constructGroumFromAST(List<String> completeClassNameList, List<String> parameterNameList,
                                       List<String> typeMapList, List<String> completeTypeMapList,
                                       List<String> starImportStringList, MethodDeclaration method,
                                       GroumCreator fieldCreator, UserClassProcessing userClassProcessing,
                                       boolean holeFlag, String globalPath, List<String> jdkList,
                                       List<String> gloveVocabList, List<String> stopWordsList) {
        try {
            GroumCreator creator = new GroumCreator(completeClassNameList, fieldCreator, globalPath, jdkList);
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
            creator.toGroum(method);
            Groum groum = new Groum();
            groum.setRoot(creator.getGroum().getRoot());
            if (creator.getParsedFlag() && groum.getRoot() != null) {
                //移除没有用到的变量声明结点
                return groum;
            } else {
                return null;
            }
        } catch (Exception e) {
             //e.printStackTrace();
            return null;
        } catch (Error e) {
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

    public Groum getTestGroum(int count, String filePath, boolean isFilePath, List<String> jdkList, ObjectOutputStream groumWriter, FileWriter traceWriter, boolean holeFlag, String globalPath,
                               List<String> gloveVocabList, List<String> stopWordsList) {
        Groum result;
        JavaParserUtil japaAst = new JavaParserUtil(true);
        List<String> tempList = new ArrayList<>();
        CompilationUnit cu = new CompilationUnit();
        try {
            if (isFilePath) {
                cu = StaticJavaParser.parse(new File(filePath));
            } else {
                InputStream in = new ByteArrayInputStream(filePath.getBytes());
                cu = StaticJavaParser.parse(in);
            }
            tempList = new ArrayList<>(japaAst.parse(cu));
        } catch (Exception e) {

        } catch (Error e) {

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
        if (cu.getTypes() != null) {
            for (TypeDeclaration type : cu.getTypes()) {
                if (type instanceof ClassOrInterfaceDeclaration) {
                    List<VariableDeclarationExpr> fieldExpressionList = new ArrayList<>();
                    return  dealClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)type,fieldExpressionList,japaAst,tempList,
                            starImportStringList,filePath,holeFlag,globalPath,jdkList,gloveVocabList,stopWordsList);
                }
            }
        }
        return null;
    }

    public Groum dealClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration type, List<VariableDeclarationExpr> fieldExpressionList, JavaParserUtil japaAst, List<String> tempList,
                                                       List<String> starImportStringList, String filePath, boolean holeFlag, String globalPath, List<String> jdkList,List<String> gloveVocabList, List<String> stopWordsList){
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
                return dealClassOrInterfaceDeclaration((ClassOrInterfaceDeclaration)body,fieldExpressionList,japaAst,tempList,
                        starImportStringList,filePath,holeFlag,globalPath,jdkList,gloveVocabList,stopWordsList);
            }
            if (body instanceof MethodDeclaration || body instanceof ConstructorDeclaration) {
                int lines = countCodeLine(body);
                if (lines >= 0) {
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
                    if(body instanceof ConstructorDeclaration){
                        String constructorDeclaration = body.toString();
                        String str = constructorDeclaration.split("\\(")[0];
                        String str2 = str;
                        String[] strs = str2.split(" +");
                        String str3 = "";
                        for(int i = 0; i < strs.length - 1; i ++){
                            str3 += strs[i];
                            str3 += " ";
                        }
                        str3 += "void ";
                        str3 += strs[strs.length - 1].toLowerCase();
                        constructorDeclaration = constructorDeclaration.replaceFirst(str,str3);
                        constructorDeclaration = "public class Test{" + constructorDeclaration + "}";
                        InputStream in = new ByteArrayInputStream(constructorDeclaration.getBytes());
                        try {
                            CompilationUnit compilationUnit = StaticJavaParser.parse(in);
                            method = (MethodDeclaration) compilationUnit.getTypes().get(0).getMembers().get(0);
                            method.setBody(((ConstructorDeclaration) body).getBody());
                        } catch (Exception e) {
                            continue;
                        } catch (Error e) {
                            continue;
                        }
                    }else{
                        method = (MethodDeclaration) body;
                    }
                    //MethodDeclaration method = (MethodDeclaration) body;
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
                                continue;
                            } catch (Error e) {
                                continue;
                            }
                        }
                    }
                    /*添加类中的成员变量*/
                    GroumCreator creator = new GroumCreator(globalPath);
                    creator.setUserClassProcessing(userClassProcessing);
                    creator.setStarImportStringList(starImportStringList);
                    GroumCreator creator2 = new GroumCreator(globalPath);
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
                    parameterNodeList = new ArrayList<>();
                    if(creator2.getGroum() != null && creator2.getGroum().getRoot() != null){
                        creator2.getGroum().getParameterNodes(creator2.getGroum().getRoot(),parameterNodeList,new ArrayList<String>());
                    }
                    /*get code tree from japa parse*/
                    Groum groum = constructGroumFromAST(completeClassNameList, parameterNameList, typeMapList,
                            completeTypeMapList, starImportStringList, method, creator, userClassProcessing, holeFlag, globalPath, jdkList,
                            gloveVocabList, stopWordsList);
                    if (groum != null && groum.getRoot() != null) {
                        /*display the code tree*/
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
                        groum.setFunctionTrace(functionTrace);
                        linesCount += lines;
                        return groum;
                    } else {
                        return null;
                        //System.err.println("So " + method.getName() + (method.getParameters() == null ? "[]" : method.getParameters()) + " (" + filePath + ") " + " can not be correctly parsed");
                    }
                }
            }
        }
        return null;
    }

}

