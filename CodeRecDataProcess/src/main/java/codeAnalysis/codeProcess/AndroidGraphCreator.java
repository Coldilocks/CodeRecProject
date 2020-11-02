package codeAnalysis.codeProcess;

//import codetree.MethodReflection;
//import codetree.UserClassProcessing;
//import constructdata.ConstructVocabulary;
//import japa.parser.ast.Node;
//import japa.parser.ast.body.ConstructorDeclaration;
//import japa.parser.ast.body.MethodDeclaration;
//import japa.parser.ast.body.VariableDeclarator;
//import japa.parser.ast.comments.BlockComment;
//import japa.parser.ast.comments.JavadocComment;
//import japa.parser.ast.comments.LineComment;
//import japa.parser.ast.expr.*;
//import japa.parser.ast.stmt.*;
//import sav.common.core.utils.CollectionUtils;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import codeAnalysis.constructVocab.ConstructVocabulary;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import config.DataConfig;
import utils.CollectionUtils;
import utils.GraphWriteUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class AndroidGraphCreator extends GraphConverter {
    private Graph graph = new Graph();
    private GraphNode lastNode = graph.getRoot();
    private Map<String, String> class_variable = new HashMap<String, String>();
    private List<String> class_variable_list = new ArrayList<>();
    private Map<String, String> class_name_map = new HashMap<>();
    private List<String> completeClassList = new ArrayList<>();
    private Map<String, Boolean> castMap = new HashMap<>();
    private Map<String, Integer> variable_line_map = new HashMap<>();
    private Map<String, Integer> variable_use_map = new HashMap<>();
    private boolean parsedFlag = true;// this field is used to judge whether the method can be correctly parsed
    private String returnType = null;
    private List<String> starImportStringList = new ArrayList<>();
    private UserClassProcessing userClassProcessing;
    //private boolean endFlag.endFlag = true;
    private boolean holeFlag = false;
    private GraphNode endParentNode = null;
    private Map<String, List<GraphNode>> variableNodeMap = new HashMap<>();
    private String globalStatement = null;//用来记录原始语句
    private String globalVariableName = null;//用来记录当前结点的变量名（针对是变量声明结点）
    private String globalType = null;
    private boolean globalFlag = true;
    private List<String> commentList = new ArrayList<>();//用来保存注释信息
    private List<String> allClassFieldAndMethodArgumentVariable = new ArrayList<>();//用来存放函数声明中的变量和类属性变量
    private List<String> jdkList = new ArrayList<>();
    private GraphNode holeParentNode = null;
    private List<GraphNode> removeList = new ArrayList<>();
    private List<GraphNode> binaryNodeList = new ArrayList<>();
    private boolean removeConditionNodeFlag = false;
    private boolean assignFlag = false;
    private boolean binaryFlag = false;
    private boolean conditionAssignFlag = false;
    private boolean conditionFlag = false;
    private GraphNode objectCreationNode = null;
    private String tempViriableName = null;
    private boolean foreachConditionFlag = false;
    private boolean variableDeclarationFlag = false;
    private int scopeIndex = 1;
    private List<String> scopeIndexList = new ArrayList<>();
    private List<String> mutexList = new ArrayList<>();
    private Map<GraphNode, List<String>> mutexMap = new HashMap<>();
    private boolean stopFlag = false;
    private int variableCount = 0;
    private String lastInfo = null;
    private boolean markHole = false;

    public List<String> getMutexList() {
        return mutexList;
    }

    public List<GraphNode> getRemoveList() {
        return removeList;
    }

    public void setRemoveList(List<GraphNode> removeList) {
        this.removeList = removeList;
    }

    public List<String> getUsedClassFieldAndMethodArgumentVariable() {
        return usedClassFieldAndMethodArgumentVariable;
    }

    private List<String> usedClassFieldAndMethodArgumentVariable = new ArrayList<>();//用来存放被用到的函数声明中的变量和类属性变量
    // next node should be added
    // twice time (false and
    // true separately)
    private boolean elseIfFlag = false;//used to judge whether a node is a else if node

    public Map<String, List<GraphNode>> getVariableNodeMap() {
        return variableNodeMap;
    }

    public List<String> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<String> commentList) {
        this.commentList = commentList;
    }

    public void setUserClassProcessing(UserClassProcessing userClassProcessing) {
        this.userClassProcessing = userClassProcessing;
    }

    public Map<String, Integer> getVariable_line_map() {
        return variable_line_map;
    }

    public Map<String, Integer> getVariable_use_map() {
        return variable_use_map;
    }

    public Map<String, String> getClass_variable() {
        return class_variable;
    }

    public List<String> getClass_variable_list() {
        return class_variable_list;
    }

    public Map<String, String> getClass_name_map() {
        return class_name_map;
    }

    public List<String> getStarImportStringList() {
        return starImportStringList;
    }

    public void setStarImportStringList(List<String> starImportStringList) {
        this.starImportStringList = starImportStringList;
    }

    public void setHoleFlag(boolean holeFlag) {
        this.holeFlag = holeFlag;
    }

    public void addClass_variable(String variableName, String type) {
        class_variable.put(variableName, type);
        variable_line_map.put(variableName, 0);
        variable_use_map.put(variableName, 0);
    }

    public void addClass_variable_list(String variable) {
        class_variable_list.add(variable);
    }

    public boolean getParsedFlag() {
        return parsedFlag;
    }

    public void addClass_name_map(String type) {
        if (class_name_map.get(type) == null) {
            class_name_map.put(type, type);
        }
    }

    public AndroidGraphCreator(String globalPath) {
        try {
            File fileClassNameMap = new File(globalPath + "/Extractor/src/main/java/codetree/configs/class_name_map.config");
            //File fileClassNameMap = new File(globalPath + "/class_name_map.config");
            FileInputStream fileInputStream = new FileInputStream(fileClassNameMap);
            Scanner scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Scanner lineScanner = new Scanner(line);
                class_name_map.put(lineScanner.next(), lineScanner.next());
                lineScanner.close();
            }
            scanner.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AndroidGraphCreator(List<String> completeClassNameList, AndroidGraphCreator creator, String globalPath, List<String> jdkList) {
        class_name_map = creator.getClass_name_map();
        class_variable_list = creator.getClass_variable_list();
        class_variable = creator.getClass_variable();
        completeClassList = completeClassNameList;
        variable_use_map = creator.getVariable_use_map();
        variable_line_map = creator.getVariable_line_map();
        this.jdkList = jdkList;
        for (int i = 0; i < completeClassNameList.size(); i++) {
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(completeClassNameList.get(i));
                if (jdkList.contains(completeClassNameList.get(i))) {
                    class_name_map.put(clazz.getSimpleName(), completeClassNameList.get(i));
                }
            } catch (Exception e) {
                if (!(e instanceof ClassNotFoundException)) {
                    parsedFlag = false;//构造数据时得设置回去
                }
                //System.err.println(e.getMessage());
            } catch (Error e) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(e.getMessage());
            }

        }
        for (int i = 0; i < class_variable_list.size(); i++) {
            allClassFieldAndMethodArgumentVariable.add(class_variable_list.get(i));
        }

        try {
            File fileTypeCast = new File(globalPath + "/Extractor/src/main/java/codetree/configs/type_cast.config");
            //File fileTypeCast = new File(globalPath + "/type_cast.config");
            FileInputStream fileInputStream = new FileInputStream(fileTypeCast);
            Scanner scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                castMap.put(scanner.nextLine(), true);
            }

            File fileClassNameMap = new File(globalPath + "/Extractor/src/main/java/codetree/configs/class_name_map.config");
            //File fileClassNameMap = new File(globalPath + "/class_name_map.config");
            fileInputStream = new FileInputStream(fileClassNameMap);
            scanner = new Scanner(fileInputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Scanner lineScanner = new Scanner(line);
                class_name_map.put(lineScanner.next(), lineScanner.next());
                lineScanner.close();
            }
            scanner.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Graph toGraph(Node node) {
        if (node != null && parsedFlag) {
            node.accept(this, null);
            return graph;
        }
        return graph;
    }

    @Override
    protected Graph convert(MethodDeclaration n) {
        toGraph(n.getBody().isPresent() ? n.getBody().get() : null);
        return graph;
    }

    protected Graph convert(ConstructorDeclaration n) {
        toGraph(n.getBody());
        return graph;
    }

    @Override
    protected Graph convert(AssertStmt n) {
        return graph;
    }

    @Override
    protected Graph convert(BlockStmt n) {
        List<Node> stmts = n.getChildNodes();
        return convert(stmts);
    }

    @Override
    protected Graph convert(LineComment n) {
        //commentList.add(n.toString());
        if (holeFlag && graph.getHoleNode(new ArrayList<>()) == null) {
            String str = n.toString();
            str = str.replaceAll("\n", "");
            str = str.replaceAll(" ", "");
            if (str.equals("/*hole*/") || str.equals("//hole") && !stopFlag) {
                stopFlag = true;
                GraphNode tempNode = lastNode;
                GraphNode node = new GraphNode();
                setInfo(node);
                addScope(node);
                node.setCompleteMethodDeclaration("hole");
                addNode(node, "unknown");
                //lastNode = tempNode;
            }
        }
        return graph;
    }

    @Override
    protected Graph convert(BlockComment n) {
        // commentList.add(n.toString());
        return graph;
    }

    @Override
    protected Graph convert(JavadocComment n) {
        // commentList.add(n.toString());
        return graph;
    }

    @Override
    protected Graph convert(BreakStmt n) {
        if (!lastNode.isControl()) {
            GraphNode node = new GraphNode();
            addScope(node);
            node.setStatement(n.toString());
            setNodeClassAndMethod(node, "break", "break", "", "");
            node.setAddMethodName(false);
            node.setExit(false);
            setInfo(node, n);
            boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
            if(addNodeFlag) {
                lastNode = node;
            }
        }
        return graph;
    }

    @Override
    protected Graph convert(ContinueStmt n) {
        if (!lastNode.isControl()) {
            GraphNode node = new GraphNode();
            addScope(node);
            node.setStatement(n.toString());
            setNodeClassAndMethod(node, "continue", "continue", "", "");
            node.setAddMethodName(false);
            node.setExit(false);
            setInfo(node, n);
            boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
            if(addNodeFlag) {
                lastNode = node;
            }
        }
        return graph;
    }

    @Override
    protected Graph convert(DoStmt n) {
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int tempScopeIndex = scopeIndex;
        GraphNode node = new GraphNode();
        addScope(node);
        setNodeClassAndMethod(node, "doWhile", "doWhile", "", "");
        node.setControl(true);
        node.setExit(false);
        setInfo(node, n);
        boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
        //add condition node
        if(addNodeFlag) {
            lastNode = node;
        }
        GraphNode conditionNode = new GraphNode();
        addScope(conditionNode);
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        addNodeFlag = graph.addNode (lastNode, conditionNode, mutexList, "c");
        if(addNodeFlag) {
            lastNode = conditionNode;
        }
        //remove condition node
//        node.getChildNodes().remove(conditionNode);
//        removeNode(conditionNode);
//        conditionNode.setParentNode(null);
//        lastNode = node;
        markHole(n);
        conditionFlag = true;
        dealCondition(null, n.getCondition());
        assignFlag = false;
        binaryFlag = false;
        conditionFlag = false;
        //add end node to represent the end of condition
        //lastNode = conditionNode;
        addConditionEndNode();
        // set the current node to be the parent node for the next node
        lastNode = node;
        addSpecialNode("body");
        // deal with the body in do while
        if (n.getBody() != null && n.getBody().getChildNodes().size() != 0
                && !isAllAnnotationStmt(n.getBody().getChildNodes(),endFlag)) {
            toGraph(n.getBody());
        } else if (n.getBody() instanceof ContinueStmt || n.getBody() instanceof BreakStmt || n.getBody() instanceof ReturnStmt) {
            toGraph(n.getBody());
        } else {
            //todo nothing
        }
        if (((node.getChildNodes().size() == 2) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
            //if (((node.getChildNodes().size() == 1) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
            lastNode = node.getParentNode();
            removeNodeInControlStructure(node, new ArrayList<>());
            if (lastNode != null) {
                lastNode.getChildNodes().remove(node);
                removeNode(node);
            } else {
                graph.setRoot(null);//构造数据时需要设置回去
            }
        } else {
            // add end node
            if (endFlag.endFlag) {
                addEndNode();
            } else {
                endFlag.endFlag = true;
            }
            lastNode = node;
        }
        scopeIndexList.remove(Integer.toString(tempScopeIndex));
        return graph;
    }

    @Override
    protected Graph convert(EmptyStmt n) {
        //System.out.println(n);
        return graph;

    }

    @Override
    public Graph convert(ExpressionStmt n) {
        Expression expr = n.getExpression();
        if (expr != null) {
            if (expr instanceof VariableDeclarationExpr) {
                convert((VariableDeclarationExpr) expr);
            } else if (expr instanceof MethodCallExpr) {
                convert((MethodCallExpr) expr);
            } else if (expr instanceof AssignExpr) {
                convert((AssignExpr) expr);
            } else if (expr instanceof UnaryExpr) {
                convert((UnaryExpr) expr);
            } else if (expr instanceof FieldAccessExpr) {
                convert((FieldAccessExpr) expr);
            } else if (expr instanceof EnclosedExpr) {
                convert((EnclosedExpr) expr);
            } else {
                // to do deal with return variable
            }
        }
        return graph;
    }

    @Override
    protected Graph convert(ForEachStmt n) {
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int tempScopeIndex = scopeIndex;
        ForStmt forStmt = GraphForeachConverter.toForStmt(n);
        if (forStmt != null) {
            if (n.getVariable().getVariables().size() > 1) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n.getVariable() + " " + "can not be parsed");
            } else {
                String temporaryVariable;
                //deal with for each
                GraphNode node = new GraphNode();
                addScope(node);
                setNodeClassAndMethod(node, "for", "for", "", "");
                node.setControl(true);
                node.setExit(false);
                setInfo(node, n);
                boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
                //add condition node
                if(addNodeFlag) {
                    lastNode = node;
                }
                GraphNode conditionNode = new GraphNode();
                addScope(conditionNode);
                setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
                conditionNode.setAddMethodName(false);
                addNodeFlag = graph.addNode (lastNode, conditionNode, mutexList, "c");
                GraphNode newConditionNode = new GraphNode();
                addScope(newConditionNode);
                setNodeClassAndMethod(newConditionNode, "condition", "condition", "", "");
                newConditionNode.setAddMethodName(false);
                if(addNodeFlag) {
                    lastNode = conditionNode;
                }
                // deal with the condition in for each
                /*deal with variable declaration on the left of ":"*/
                /**GraphNode tempNode = lastNode;**/
                removeConditionNodeFlag = true;
                convert(n.getVariable());
                removeConditionNodeFlag = false;
                //下面的两条语句当需要考虑条件时，要注释掉
                //lastNode = lastNode.getParentNode();
                //lastNode.setChildNodes(new ArrayList<>());
                //remove condition node
                /*这里需要先移除condition结点，然后再添加，不然预先处理的init中变量声明结点会被添加上去*/
                node.getChildNodes().remove(conditionNode);
                removeNode(conditionNode);
                conditionNode.setAbsoluteParentNode(null);
                //conditionNode.setParentNode(null);
                temporaryVariable = class_variable_list.get(class_variable_list.size() - 1);
                lastNode = node;
                addNodeFlag = graph.addNode (lastNode, newConditionNode, mutexList, "c");
                if(addNodeFlag) {
                    lastNode = newConditionNode;
                }
                markHole(n);
                List<Expression> listExpr = forStmt.getInitialization();
                if (listExpr != null) {
                    for (Expression conditionExpression : listExpr) {
                        foreachConditionFlag = true;
                        conditionFlag = true;
                        dealCondition(null, conditionExpression);
                        assignFlag = false;
                        binaryFlag = false;
                        conditionFlag = false;
                        foreachConditionFlag = false;
                    }
                }
                addConditionEndNode();
                //deal with the body of for each
                lastNode = node;
                addSpecialNode("body");
                if (forStmt.getBody() != null && forStmt.getBody().getChildNodes().size() != 1
                        && !isAllAnnotationStmt(forStmt.getBody().getChildNodes(),endFlag)) {
                    Statement stmt = forStmt.getBody();
                    if (stmt.getChildNodes().size() > 0) {
                        stmt.getChildNodes().remove(0);
                        toGraph(stmt);
                    }
                    //toGraph(forStmt.getBody());
                } else {
                    /**
                     GraphNode emptyNode = new GraphNode();
                     emptyNode.setAddMethodName(false);
                     setNodeClassAndMethod(emptyNode, "EmptyStatement", "EmptyStatement", "", "");
                     node.getChildNodes().add(emptyNode);
                     lastNode = emptyNode;**/
                }
                if (((node.getChildNodes().size() == 2) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
                    //if (((node.getChildNodes().size() == 1) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
                    lastNode = node.getParentNode();
                    removeNodeInControlStructure(node, new ArrayList<>());
                    if (lastNode != null) {
                        lastNode.getChildNodes().remove(node);
                        removeNode(node);
                    } else {
                        graph.setRoot(null);//构造数据时需要设置回去
                    }
                } else {
                    //add end node
                    if (endFlag.endFlag) {
                        addEndNode();
                    } else {
                        endFlag.endFlag = true;
                    }
                    lastNode = node;
                }
                //remove the temporary variable
                String type = class_variable.get(temporaryVariable);
                class_variable.remove(temporaryVariable, type);
                class_variable_list.remove(class_variable_list.indexOf(temporaryVariable));
                //variable_use_map.remove(temporaryVariable);
                variable_line_map.remove(temporaryVariable);
            }
        } else {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n + " can not be parsed");
        }
        scopeIndexList.remove(Integer.toString(tempScopeIndex));
        return graph;
    }

    @Override
    protected Graph convert(ForStmt n) {
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int tempScopeIndex = scopeIndex;
        GraphNode node = new GraphNode();
        addScope(node);
        setNodeClassAndMethod(node, "for", "for", "", "");
        node.setControl(true);
        node.setExit(false);
        setInfo(node, n);
        boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");

        // add condition node
        if(addNodeFlag) {
            lastNode = node;
        }
        GraphNode conditionNode = new GraphNode();
        addScope(conditionNode);
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        addNodeFlag = graph.addNode (lastNode, conditionNode, mutexList, "c");
        if(addNodeFlag) {
            lastNode = conditionNode;
        }
        GraphNode newConditionNode = new GraphNode();
        addScope(newConditionNode);
        setNodeClassAndMethod(newConditionNode, "condition", "condition", "", "");
        newConditionNode.setAddMethodName(false);
        List<String> variableList = new ArrayList<>();
        markHole(n);
        //deal with condition in for
        /*deal with init*/
        removeConditionNodeFlag = true;
        dealForInitCondition(n, variableList);
        removeConditionNodeFlag = false;
        //remove condition node
        /*这里需要先移除condition结点，然后再添加，不然预先处理的init中变量声明结点会被添加上去*/
        node.getChildNodes().remove(conditionNode);
        removeNode(conditionNode);
        conditionNode.setAbsoluteParentNode(null);
        //conditionNode.setParentNode(null);
        lastNode = node;
        addNodeFlag = graph.addNode (lastNode, newConditionNode, mutexList, "c");
        if(addNodeFlag) {
            lastNode = newConditionNode;
        }
        //处理init
        List<Expression> listExpr = n.getInitialization();
        if (listExpr != null) {
            for (Expression conditionExpression : listExpr) {
                conditionFlag = true;
                dealCondition(null, conditionExpression);
                assignFlag = false;
                binaryFlag = false;
                conditionFlag = false;
            }
        }
        //处理compare
        conditionFlag = true;
        dealCondition(null, n.getCompare().isPresent() ? n.getCompare().get() : null);
        assignFlag = false;
        binaryFlag = false;
        conditionFlag = false;
        //处理update
        List<Expression> listUpdateExpr = n.getUpdate();
        if (listUpdateExpr != null) {
            for (Expression conditionExpression : listUpdateExpr) {
                conditionFlag = true;
                dealCondition(null, conditionExpression);
                assignFlag = false;
                binaryFlag = false;
                conditionFlag = false;
            }
        }
        addConditionEndNode();
        // deal with the body in for
        lastNode = node;
        addSpecialNode("body");
        if (n.getBody() != null && n.getBody().getChildNodes().size() != 0
                && !isAllAnnotationStmt(n.getBody().getChildNodes(),endFlag)) {
            toGraph(n.getBody());
        } else if (n.getBody() instanceof ContinueStmt || n.getBody() instanceof BreakStmt || n.getBody() instanceof ReturnStmt) {
            toGraph(n.getBody());
        } else {
            //todo nothing
        }
        if (((node.getChildNodes().size() == 2) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
            //if (((node.getChildNodes().size() == 1) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
            lastNode = node.getParentNode();
            removeNodeInControlStructure(node, new ArrayList<>());
            if (lastNode != null) {
                lastNode.getChildNodes().remove(node);
                removeNode(node);
            } else {
                graph.setRoot(null);//构造数据时需要设置回去
            }
        } else {
            // add end node
            if (endFlag.endFlag) {
                addEndNode();
            } else {
                endFlag.endFlag = true;
            }
            lastNode = node;
        }
        //remove temporary variales
        if (variableList.size() > 0) {
            for (int i = 0; i < variableList.size(); i++) {
                String type = class_variable.get(variableList.get(i));
                class_variable.remove(variableList.get(i), type);
                class_variable_list.remove(class_variable_list.indexOf(variableList.get(i)));
                variable_line_map.remove(variableList.get(i));
                //variable_use_map.remove(variableList.get(i));
            }
        }
        scopeIndexList.remove(Integer.toString(tempScopeIndex));
        return graph;
    }

    @Override
    protected Graph convert(IfStmt n) {
        boolean flag = false;
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int tempScopeIndex = scopeIndex;
        GraphNode node = new GraphNode();
        addScope(node);
        node.setControl(true);
        //add info
        String info = "";
        info = n.getBegin().get().line + " " + n.getThenStmt().getEnd().get().line + " ";
        boolean addNodeFlag = false;
        if (lastNode == null || lastNode.getClassName() == null) {
            setNodeClassAndMethod(node, "if", "if", "", "");
            addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
            List<String> indexList = new ArrayList<>();
            indexList.add(Integer.toString(scopeIndex));
            mutexMap.put(node, indexList);
            info += "if(";
            flag = true;
        } else {
            if (lastNode.getClassName().contains("if") && lastNode.isControl() && elseIfFlag) {
                setNodeClassAndMethod(node, "elseif", "elseif", "", "");
                addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
                dealScope(node, "if");
                info += "else if(";
            } else {
                flag = true;
                setNodeClassAndMethod(node, "if", "if", "", "");
                addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
                List<String> indexList = new ArrayList<>();
                indexList.add(Integer.toString(scopeIndex));
                mutexMap.put(node, indexList);
                info += "if(";
            }
        }
        info += n.getCondition().toString();
        info += ")";
        info += n.getThenStmt().toString();
        node.setInfo(info);
        node.setExit(false);
        //boolean addNodeFlag = graph.addNode (lastNode, node);
        //add condition node
        if(addNodeFlag) {
            lastNode = node;
        }
        GraphNode conditionNode = new GraphNode();
        addScope(conditionNode);
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        addNodeFlag = graph.addNode (lastNode, conditionNode, mutexList, "c");
        if(addNodeFlag) {
            lastNode = conditionNode;
        }
        //remove condition node
//        node.getChildNodes().remove(conditionNode);
//        removeNode(conditionNode);
//        conditionNode.setParentNode(null);
//        lastNode = node;
        markHole(n);
        conditionFlag = true;
        dealCondition(null, n.getCondition());
        assignFlag = false;
        binaryFlag = false;
        conditionFlag = false;
        //add end node to represent the end of condition
        //lastNode = conditionNode;
        addConditionEndNode();
        // deal with ifthen body
        lastNode = node;
        //if("if".equals(node.getCompleteMethodDeclaration())){
        addSpecialNode("then");
        //}
        elseIfFlag = false;
        if (n.getThenStmt() != null && n.getThenStmt().getChildNodes().size() != 0
                && !isAllAnnotationStmt(n.getThenStmt().getChildNodes(),endFlag)) {
            toGraph(n.getThenStmt());
        } else if (n.getThenStmt() instanceof ContinueStmt || n.getThenStmt() instanceof BreakStmt || n.getThenStmt() instanceof ReturnStmt) {
            toGraph(n.getThenStmt());
        } else {
            //todo nothing
        }
        scopeIndexList.remove(Integer.toString(tempScopeIndex));
        if (node.getCompleteClassName().equals("if")) {
            endParentNode = lastNode;
            holeParentNode = lastNode;
            dealHoleParentNode(node);
            node.setHoleParentNode(holeParentNode);
        } else if (node.getCompleteClassName().equals("elseif")) {
            dealHoleParentNode(node);
            node.setHoleParentNode(holeParentNode);
            if (((node.getChildNodes().size() == 1) && "condition".equals(node.getChildNodes().get(0).getCompleteMethodDeclaration()))
                    || ((node.getChildNodes().size() == 2) && judgeConditionEnd(node))|| isControlUnitWillBeEmpty(node)) {
                // if (((node.getChildNodes().size() == 1) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
                lastNode = node.getParentNode();
                removeNodeInControlStructure(node, new ArrayList<>());
                if (lastNode != null) {
                    lastNode.getChildNodes().remove(node);
                    removeNode(node);
                    node = lastNode;
                } else {
                    parsedFlag = false;//构造数据时得设置回去
                    graph.setRoot(null);//构造数据时需要设置回去
                }
            } else {
                endParentNode = lastNode;
                holeParentNode = lastNode;
            }
        }
        // deal with elsethen body
        if (n.getElseStmt().isPresent()) {
            lastNode = node;
            if (n.getElseStmt().get() instanceof IfStmt) {
                elseIfFlag = true;
                toGraph(n.getElseStmt().get());
                lastNode = node;
                if (lastNode.getCompleteClassName().equals("if")) {
                    if (((lastNode.getChildNodes().size() == 1) && "condition".equals(lastNode.getChildNodes().get(0).getCompleteMethodDeclaration()))
                            || ((lastNode.getChildNodes().size() == 2) && judgeConditionEnd(lastNode))|| isControlUnitWillBeEmpty(lastNode)) {
                        //if (((lastNode.getChildNodes().size() == 1) && judgeConditionEnd(lastNode)) || isControlUnitWillBeEmpty(lastNode)) {
                        lastNode = lastNode.getParentNode();
                        removeNodeInControlStructure(node, new ArrayList<>());
                        if (lastNode != null) {
                        } else {
                            graph.setRoot(null);//构造数据时需要设置回去
                        }
                    }
                }
            } else {
                endFlag.endFlag = true;
                addScopeIndex();
                int tempScopeIndex2 = scopeIndex;
                elseIfFlag = false;
                GraphNode elseNode = new GraphNode();
                addScope(elseNode);
                setNodeClassAndMethod(elseNode, "else", "else", "", "");
                elseNode.setControl(true);
                addNodeFlag = graph.addNode (node, elseNode, mutexList, "c");
                dealScope(elseNode, "if");
                GraphNode tempNode = elseNode;
                dealHoleParentNode(elseNode);
                elseNode.setHoleParentNode(holeParentNode);
                //System.err.println(holeParentNode.toString());
                if(addNodeFlag) {
                    lastNode = elseNode;
                }
                //add info
                String elseInfo = "";
                elseInfo = n.getElseStmt().get().getBegin().get().line + " " + n.getElseStmt().get().getEnd().get().line + " ";
                elseInfo += "else";
                elseInfo += n.getElseStmt().toString();
                elseNode.setInfo(elseInfo);
                if (n.getElseStmt().isPresent() && n.getElseStmt().get().getChildNodes().size() != 0
                        && !isAllAnnotationStmt(n.getElseStmt().get().getChildNodes(), endFlag)) {
                    toGraph(n.getElseStmt().get());
                } else if (n.getElseStmt().get() instanceof ContinueStmt || n.getElseStmt().get() instanceof BreakStmt || n.getElseStmt().get() instanceof ReturnStmt) {
                    toGraph(n.getElseStmt().get());
                } else {
                    //todo nothing
                }
                scopeIndexList.remove(Integer.toString(tempScopeIndex2));
                if ((elseNode.getChildNodes().size() == 0) || isControlUnitWillBeEmpty(elseNode)) {
                    GraphNode parentNode = elseNode.getParentNode();
                    lastNode = parentNode;
                    removeNodeInControlStructure(elseNode, new ArrayList<>());
                    lastNode.getChildNodes().remove(elseNode);
                    removeNode(elseNode);
                    if (((lastNode.getChildNodes().size() == 1) && "condition".equals(lastNode.getChildNodes().get(0).getCompleteMethodDeclaration()))
                            || ((lastNode.getChildNodes().size() == 2) && judgeConditionEnd(lastNode))|| isControlUnitWillBeEmpty(lastNode)) {
//                    if (((lastNode.getChildNodes().size() == 1) && judgeConditionEnd(lastNode)) || isControlUnitWillBeEmpty(lastNode)) {
                        GraphNode removeNode = lastNode.getParentNode();
                        removeNodeInControlStructure(lastNode, new ArrayList<>());
                        if (removeNode != null) {
                            removeNode.getChildNodes().remove(lastNode);
                            removeNode(lastNode);
                            lastNode = removeNode;
                        } else {
                            lastNode = null;
                            graph.setRoot(null);//构造数据时需要设置回去
                        }
                    } else {
                        if (endFlag.endFlag) {
                            lastNode = endParentNode;
                            if (endParentNode != null) {
                                addEndNode();
                            } else {
                                parsedFlag = false;//构造数据时得设置回去
                            }
                        } else {
                            endFlag.endFlag = true;
                        }
                        lastNode = node;
                    }
                } else {
                    //add end node
                    if (endFlag.endFlag) {
                        addEndNode();
                    } else {
                        endFlag.endFlag = true;
                    }
                    //lastNode = elseNode;
                    lastNode = node;
                }
            }
        } else {
            if (((node.getChildNodes().size() == 1) && "condition".equals(node.getChildNodes().get(0).getCompleteMethodDeclaration()))
                    || ((node.getChildNodes().size() == 2) && judgeConditionEnd(node))|| isControlUnitWillBeEmpty(node)) {
//            if (((node.getChildNodes().size() == 1) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
                lastNode = node.getParentNode();
                removeNodeInControlStructure(node, new ArrayList<>());
                if (lastNode != null) {
                    lastNode.getChildNodes().remove(node);
                    removeNode(node);
                } else {
                    graph.setRoot(null);//构造数据时需要设置回去
                }
            } else {
                //add end node
                if (endFlag.endFlag) {
                    lastNode = endParentNode;
                    if (lastNode != null) {
                        addEndNode();
                    } else {
                        parsedFlag = false;//构造数据时得设置回去
                    }
                } else {
                    endFlag.endFlag = true;
                }
                lastNode = node;
                elseIfFlag = false;
            }
        }
        if (node.getCompleteMethodDeclaration().equals("if")) {
            mutexList.remove(node);
        }
        return graph;
    }

    @Override
    protected Graph convert(LabeledStmt n) {
        parsedFlag = false;//构造数据时得设置回去
        //System.err.println(n + " " + "can not be parsed");
        return graph;
    }

    @Override
    protected Graph convert(ReturnStmt n) {
        boolean infoFlag = false;
        if (lastInfo == null) {
            lastInfo = n.getBegin().get().line + " " + n.getEnd().get().line + " " + n.toString();
            infoFlag = true;
        }
        GraphNode tempNode = lastNode;
        //处理return的返回语句
        dealReturnExpr(n.getExpression().get());
        //判断是否增加return结点
        if (lastNode != null && !lastNode.equals(tempNode)) {
            if (!lastNode.isControl()) {
                GraphNode node = new GraphNode();
                addScope(node);
                node.setStatement(n.toString());
                setNodeClassAndMethod(node, "return", "return", "", "");
                node.setAddMethodName(false);
                node.setExit(true);
                setInfo(node, n);
                boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
                if(addNodeFlag) {
                    lastNode = node;
                }
            }
        }
        if (infoFlag) {
            lastInfo = null;
        }
        return graph;
    }

    @Override
    protected Graph convert(SynchronizedStmt n) {
        parsedFlag = false;//构造数据时得设置回去
        //System.err.println(n + " " + "can not be parsed");
        return graph;
    }

    @Override
    protected Graph convert(TryStmt n) {
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int tryScopeIndex = scopeIndex;
        boolean flag = true;
        //add try node
        GraphNode tryNode = new GraphNode();
        addScope(tryNode);
        setNodeClassAndMethod(tryNode, "try", "try", "", "");
        tryNode.setControl(true);
        //add try info
        String tryInfo = "";
        tryInfo = n.getBegin().get().line + " " + n.getEnd().get().line + " ";
        tryInfo += "try";
        tryInfo += n.getTryBlock().toString();
        tryNode.setInfo(tryInfo);
        boolean addNodeFlag = graph.addNode (lastNode, tryNode, mutexList, "c");
        List<String> indexList = new ArrayList<>();
        indexList.add(Integer.toString(scopeIndex));
        mutexMap.put(tryNode, indexList);
        //get the body of try node
        if (n.getResources().size() == 0) {
            if (n.getTryBlock().getChildNodes().size() == 0 || isAllAnnotationStmt(n.getTryBlock().getChildNodes(),endFlag)) {
                //todo nothing
            } else {
                if(addNodeFlag) {
                    lastNode = tryNode;
                }
                toGraph(n.getTryBlock());
            }
            scopeIndexList.remove(Integer.toString(tryScopeIndex));
            holeParentNode = lastNode;
            dealHoleParentNode(tryNode);
            tryNode.setHoleParentNode(holeParentNode);
            lastNode = tryNode;
            //deal catch clause
            List<CatchClause> catchList = n.getCatchClauses();
            if (catchList != null) {
                for (int i = 0; i < catchList.size(); i++) {
                    endFlag.endFlag = true;
                    addScopeIndex();
                    int catchScopeIndex = scopeIndex;
                    GraphNode catchNode = new GraphNode();
                    addScope(catchNode);
                    setNodeClassAndMethod(catchNode, "catch", "catch", "", "");
                    catchNode.setControl(true);
                    //add catch info
                    String catchInfo = "";
                    catchInfo = catchList.get(i).getBegin().get().line + " " + catchList.get(i).getEnd().get().line + " ";
                    catchInfo += "catch(";
                    catchInfo += catchList.get(i).getParameter().toString();
                    catchInfo += ")";
                    catchInfo += catchList.get(i).getBody();
                    catchNode.setInfo(catchInfo);
                    addNodeFlag = graph.addNode (lastNode, catchNode, mutexList, "c");
                    dealScope(catchNode, "try");
                    if(addNodeFlag) {
                        lastNode = catchNode;
                    }
                    //将catch(Exception e)中的e当做用户自定义类处理
                    List<String> variableList = new ArrayList<>();
                    if (catchList.get(i).getParameter().getName() != null) {
                        String catchVariable = catchList.get(i).getParameter().getName().toString();
                        class_variable_list.add(catchVariable);
                        class_variable.put(catchVariable, "userDefinedClass");
                        variableList.add(class_variable_list.get(class_variable_list.size() - 1));
                        if (!userClassProcessing.getUserClassList().contains("userDefinedClass")) {
                            userClassProcessing.addUserClass("userDefinedClass");
                        }
                    }
                    toGraph(catchList.get(i).getBody());
                    scopeIndexList.remove(Integer.toString(catchScopeIndex));
                    dealHoleParentNode(catchNode);
                    catchNode.setHoleParentNode(holeParentNode);
                    //remove temporary variables
                    if (variableList.size() > 0) {
                        for (int k = 0; k < variableList.size(); k++) {
                            String type = class_variable.get(variableList.get(k));
                            class_variable.remove(variableList.get(k), type);
                            class_variable_list.remove(class_variable_list.indexOf(variableList.get(k)));
                        }
                    }
                    if (catchNode.getChildNodes().size() != 0 && !isControlUnitWillBeEmpty(catchNode)) {
                        endParentNode = lastNode;
                        holeParentNode = lastNode;
                        lastNode = catchNode;
                        flag = false;
                    } else {
                        lastNode = catchNode.getParentNode();
                        removeNodeInControlStructure(catchNode, new ArrayList<>());
                        if (lastNode != null) {
                            lastNode.getChildNodes().remove(catchNode);
                            removeNode(catchNode);
                        } else {
                            graph.setRoot(null);//构造数据时需要设置回去
                            parsedFlag = false;//构造数据时得设置回去
                        }
                    }
                }
            }
            // add finally node if exits
            /*lastNode = catchNode;*/
            if (n.getFinallyBlock() != null) {
                addScopeIndex();
                int finallyScopeIndex = scopeIndex;
                GraphNode finallyNode = new GraphNode();
                addScope(finallyNode);
                setNodeClassAndMethod(finallyNode, "finally", "finally", "", "");
                finallyNode.setControl(true);
                //add finally info
                String finallyInfo = "";
                finallyInfo = n.getFinallyBlock().get().getBegin().get().line + " " + n.getFinallyBlock().get().getEnd().get().line + " ";
                finallyInfo += "finally";
                finallyInfo += n.getFinallyBlock().toString();
                finallyNode.setInfo(finallyInfo);
                addNodeFlag = graph.addNode (lastNode, finallyNode, mutexList, "c");
                dealScope(finallyNode, "try");
                if(addNodeFlag) {
                    lastNode = finallyNode;
                }
                if (n.getFinallyBlock().get().getChildNodes().size() == 0 || isAllAnnotationStmt(n.getFinallyBlock().get().getChildNodes(),endFlag)) {
                    //todo nothing
                } else {
                    toGraph(n.getFinallyBlock().get());
                }
                scopeIndexList.remove(Integer.toString(finallyScopeIndex));
                dealHoleParentNode(finallyNode);
                finallyNode.setHoleParentNode(holeParentNode);
                if (finallyNode.getChildNodes().size() != 0 && !isControlUnitWillBeEmpty(finallyNode)) {
                    if (finallyNode.getParentNode().getCompleteClassName().equals("try")) {
                        tryNode.getChildNodes().remove(finallyNode);
                        removeNode(finallyNode);
                        GraphNode catchNode = new GraphNode();
                        addScope(catchNode);
                        setNodeClassAndMethod(catchNode, "catch", "catch", "", "");
                        catchNode.setControl(true);
                        catchNode.setHoleParentNode(finallyNode.getHoleParentNode());
                        finallyNode.setHoleParentNode(catchNode);
                        addNodeFlag = graph.addNode (tryNode, catchNode, mutexList, "c");
                        addNodeFlag = graph.addNode (catchNode, finallyNode, mutexList, "c");
                    } else if (finallyNode.getParentNode().getCompleteClassName().equals("catch")) {
                        //to do nothing
                    } else {
                        parsedFlag = false;//构造数据时得设置回去
                    }
                    if (endFlag.endFlag) {
                        addEndNode();
                    } else {
                        endFlag.endFlag = true;
                    }
                    lastNode = tryNode;
                    flag = false;
                } else {
                    lastNode = finallyNode.getParentNode();
                    removeNodeInControlStructure(finallyNode, new ArrayList<>());
                    if (lastNode != null) {
                        lastNode.getChildNodes().remove(finallyNode);
                        removeNode(finallyNode);
                        if (lastNode.getCompleteClassName().equals("catch")) {
                            lastNode = endParentNode;
                            if (endFlag.endFlag) {
                                addEndNode();
                            } else {
                                endFlag.endFlag = true;
                            }
                            lastNode = tryNode;
                            flag = false;
                        }
                    } else {
                        graph.setRoot(null);//构造数据时需要设置回去
                        parsedFlag = false;//构造数据时得设置回去
                    }
                }
            } else {
                if (lastNode.getCompleteClassName().equals("catch")) {
                    lastNode = endParentNode;
                    if (endFlag.endFlag) {
                        addEndNode();
                    } else {
                        endFlag.endFlag = true;
                    }
                    lastNode = tryNode;
                    flag = false;
                }
            }
            //judge whether to remove try node or not (if preserve try whether need to add catch node)
            if (tryNode.getChildNodes().size() != 0 && !isControlUnitWillBeEmpty(tryNode)) {
                if (flag) {//add catch node
                    GraphNode catchNode = new GraphNode();
                    addScope(catchNode);
                    setNodeClassAndMethod(catchNode, "catch", "catch", "", "");
                    catchNode.setControl(true);
                    catchNode.setHoleParentNode(tryNode.getHoleParentNode());
                    addNodeFlag = graph.addNode (tryNode, catchNode, mutexList, "c");
                    lastNode = catchNode;
                    if (endFlag.endFlag) {
                        addEndNode();
                    } else {
                        endFlag.endFlag = true;
                    }
                    lastNode = tryNode;
                }
            } else {
                lastNode = tryNode.getParentNode();
                removeNodeInControlStructure(tryNode, new ArrayList<>());
                if (lastNode != null) {
                    lastNode.getChildNodes().remove(tryNode);
                    removeNode(tryNode);
                } else {
                    graph.setRoot(null);//构造数据时需要设置回去
                }
            }
        } else {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n + " " + "can not be parsed");
        }
        mutexList.remove(tryNode);
        return graph;
    }

//    @Override
//    protected Graph convert(TypeDeclarationStmt n) {
//        return graph;
//    }

    @Override
    protected Graph convert(WhileStmt n) {
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int tempScopeIndex = scopeIndex;
        GraphNode node = new GraphNode();
        addScope(node);
        setNodeClassAndMethod(node, "while", "while", "", "");
        node.setControl(true);
        node.setExit(false);
        //add info
        setInfo(node, n);
        boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
        //add condition node
        lastNode = node;
        GraphNode conditionNode = new GraphNode();
        addScope(conditionNode);
        setNodeClassAndMethod(conditionNode, "condition", "condition", "", "");
        conditionNode.setAddMethodName(false);
        addNodeFlag = graph.addNode (lastNode, conditionNode, mutexList, "c");
        if(addNodeFlag) {
            lastNode = conditionNode;
        }
        //remove condition node
//        node.getChildNodes().remove(conditionNode);
//        removeNode(conditionNode);
//        conditionNode.setParentNode(null);
//        lastNode = node;
        markHole(n);
        conditionFlag = true;
        dealCondition(null, n.getCondition());
        assignFlag = false;
        binaryFlag = false;
        conditionFlag = false;
        //add end node to represent the end of condition
        //lastNode = conditionNode;
        addConditionEndNode();
        lastNode = node;
        addSpecialNode("body");
        if (n.getBody() != null && n.getBody().getChildNodes().size() != 0
                && !isAllAnnotationStmt(n.getBody().getChildNodes(),endFlag)) {
            toGraph(n.getBody());
        } else if (n.getBody() instanceof ContinueStmt || n.getBody() instanceof BreakStmt || n.getBody() instanceof ReturnStmt) {
            toGraph(n.getBody());
        } else {
            //todo nothing
        }
        if (((node.getChildNodes().size() == 2) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
            //if (((node.getChildNodes().size() == 1) && judgeConditionEnd(node)) || isControlUnitWillBeEmpty(node)) {
            lastNode = node.getParentNode();
            removeNodeInControlStructure(node, new ArrayList<>());
            if (lastNode != null) {
                lastNode.getChildNodes().remove(node);
                removeNode(node);
            } else {
                graph.setRoot(null);//构造数据时需要设置回去
            }
        } else {
            //add end node
            if (endFlag.endFlag) {
                addEndNode();
            } else {
                endFlag.endFlag = true;
            }
            lastNode = node;
        }
        //   contorlStatementFlag = true;
        //   controlNode = node;
        scopeIndexList.remove(Integer.toString(tempScopeIndex));
        return graph;
    }

    @Override
    protected Graph convert(ExplicitConstructorInvocationStmt n) {
        parsedFlag = false;//构造数据时得设置回去
        //System.err.println(n + " " + "can not be parsed");
        return graph;
    }

    @Override
    protected Graph convert(SwitchStmt n) {
        EndFlag endFlag = new EndFlag();
        addScopeIndex();
        int switchScopeIndex = scopeIndex;
        List<GraphNode> removeNodeList = new ArrayList<>();
        GraphNode switchNode = new GraphNode();
        addScope(switchNode);
        setNodeClassAndMethod(switchNode, "switch", "switch", "", "");
        switchNode.setControl(true);
        //add switch info
        setInfo(switchNode, n);
        boolean addNodeFlag = graph.addNode (lastNode, switchNode, mutexList, "c");
        List<String> indexList = new ArrayList<>();
        //   indexList.add(Integer.toString(scopeIndex));
        mutexMap.put(switchNode, indexList);
        //deal condition in switch
        /*add condition node*/
        lastNode = switchNode;
        GraphNode switchConditionNode = new GraphNode();
        addScope(switchConditionNode);
        setNodeClassAndMethod(switchConditionNode, "condition", "condition", "", "");
        switchConditionNode.setAddMethodName(false);
        addNodeFlag = graph.addNode (lastNode, switchConditionNode, mutexList, "c");
        lastNode = switchConditionNode;
        //remove condition node
//        switchNode.getChildNodes().remove(switchConditionNode);
//        removeNode(switchConditionNode);
//        switchConditionNode.setParentNode(null);
        /*deal condition*/
//        lastNode = switchNode;
        conditionFlag = true;
        markHole(n);
        dealCondition(null, n.getSelector());
        assignFlag = false;
        binaryFlag = false;
        conditionFlag = false;
        addConditionEndNode();
        lastNode = switchNode;
        holeParentNode = lastNode;
        if (n.getEntries().size() > 8) {
            parsedFlag = false;//构造数据时得设置回去
            //return null;
            //System.err.println("too many case nodes");
        }
        //add case and default node
        for (SwitchEntry entry : n.getEntries()) {
            if (entry.getLabels() != null) {
                GraphNode conditionNode = new GraphNode();
                setNodeClassAndMethod(conditionNode, "case", "case", "", "");
                conditionNode.setControl(true);
                addNodeFlag = graph.addNode (switchNode, conditionNode, mutexList, "c");
                //switchNode.getChildNodes().add(conditionNode);
                //conditionNode.setParentNode(switchNode);
            } else {
                GraphNode conditionNode = new GraphNode();
                setNodeClassAndMethod(conditionNode, "default", "default", "", "");
                conditionNode.setControl(true);
                addNodeFlag = graph.addNode (switchNode, conditionNode, mutexList, "c");
                //switchNode.getChildNodes().add(conditionNode);
                //conditionNode.setParentNode(switchNode);
            }
        }
        //deal case and default body
        for (int i = 1; i < switchNode.getChildNodes().size(); i++) {
            lastNode = switchNode.getChildNodes().get(i);
            setInfo(switchNode.getChildNodes().get(i), n.getEntries().get(i - 1));
            List<Node> childrenNodes = n.getEntries().get(i - 1).getChildNodes();
            //deal with case node, if it is finally node, do nothing
            if (lastNode.getCompleteClassName().equals("case")) {
                if (childrenNodes.size() == 0) {
                    parsedFlag = false;//构造数据时得设置回去
                } else {
                    childrenNodes.remove(0);
                }
            }
            endFlag.endFlag = true;
            if (n.getEntries().get(i - 1).getStatements() != null && !isAllAnnotationStmt(childrenNodes,endFlag)) {
                addScopeIndex();
                int tempIndex = scopeIndex;
                addScope(switchNode.getChildNodes().get(i));
                dealScope(switchNode.getChildNodes().get(i), "switch");
                convert(childrenNodes);
                scopeIndexList.remove(Integer.toString(tempIndex));
                dealHoleParentNode(switchNode.getChildNodes().get(i));
                switchNode.getChildNodes().get(i).setHoleParentNode(holeParentNode);
                if (switchNode.getChildNodes().get(i).getChildNodes().size() != 0 && !isControlUnitWillBeEmpty(switchNode.getChildNodes().get(i))) {
                    endParentNode = lastNode;
                    holeParentNode = lastNode;
                } else {
                    removeNodeList.add(switchNode.getChildNodes().get(i));
                }
            } else {
                removeNodeList.add(switchNode.getChildNodes().get(i));
            }
        }
        for (int i = 0; i < removeNodeList.size(); i++) {
            switchNode.getChildNodes().remove(removeNodeList.get(i));
            removeNode(removeNodeList.get(i));
            removeNodeInControlStructure(removeNodeList.get(i), new ArrayList<>());
            removeNodeList.get(i).setParentNode(null);
            switchNode.getEdgeMap().remove(removeNodeList.get(i));
        }
        if (((switchNode.getChildNodes().size() == 1) && switchNode.getChildNodes().get(0).getCompleteMethodDeclaration().equals("condition")) || isControlUnitWillBeEmpty(switchNode)) {
            //if (((switchNode.getChildNodes().size() == 1) && judgeConditionEnd(switchNode)) || isControlUnitWillBeEmpty(switchNode)) {
            lastNode = switchNode.getParentNode();
            removeNodeInControlStructure(switchNode, new ArrayList<>());
            if (lastNode != null) {
                lastNode.getChildNodes().remove(switchNode);
                removeNode(switchNode);
            } else {
                graph.setRoot(null);//构造数据时需要设置回去
            }
        } else {
            //add end node
            if (endFlag.endFlag) {
                lastNode = endParentNode;
                if (lastNode == null) {
                    lastNode = switchNode;
                }
                addEndNode();
            } else {
                endFlag.endFlag = true;
            }
            lastNode = switchNode;
        }
        //      contorlStatementFlag = true;
        //      controlNode = switchNode;
        scopeIndexList.remove(Integer.toString(switchScopeIndex));
        mutexMap.remove(switchNode);
        return graph;
    }

    @Override
    protected Graph convert(ThrowStmt n) {
        return graph;
    }

    @Override
    protected Graph newInstance(Node n) {
        return graph;
    }

    private Graph convert(List<Node> stmts) {
        for (Node stmt : CollectionUtils.nullToEmpty(stmts)) {
            String str = stmt.toString();
            str = str.replaceAll(" ", "");
            if ((str.startsWith("/*hole*/") || str.startsWith("//hole")) && holeFlag && !stopFlag) {
                stopFlag = true;
                if ((graph.getRoot() != null && graph.getHoleNode(new ArrayList<>()) == null) || graph.getRoot() == null) {
                    GraphNode tempNode = lastNode;
                    GraphNode node = new GraphNode();
                    addScope(node);
                    setInfo(node);
                    node.setCompleteMethodDeclaration("hole");
                    addNode(node, "unknown");
                    //lastNode = tempNode;
                }
            }
            toGraph(stmt);
        }
        return graph;
    }

    public Graph convert(VariableDeclarationExpr n) {
        //add info
        boolean infoFlag = false;
        if (lastInfo == null) {
            lastInfo = constructInfo(n);
            infoFlag = true;
        }
        for (int i = 0; i < n.getVariables().size(); i++) {
            variableCount += 1;
            binaryFlag = false;
            variableDeclarationFlag = true;
            String variableName = null;
            //boolean verifyFlag = true;
            String type1 = n.getCommonType().toString();
            String type2 = n.getCommonType().toString();
            String type3 = n.getCommonType().toString();
            if (n.getCommonType().toString().contains("<")) {
                int index = type1.indexOf("<");
                type1 = type1.substring(0, index);
                type2 = type1;
                type3 = type1;
                if (n.getCommonType().toString().contains("[")) {
                    parsedFlag = false;//构造数据时得设置回去
                    //System.err.println(n.toString() + " can not be parsed");
                }
            }
            if (type1.contains(".")) {
                String[] strs = type1.split("\\.");
                type1 = strs[strs.length - 1];
                type2 = type1;
            }
            if (n.getVariables().get(i).getName().toString().contains("[")) {
                int index = n.getVariables().get(i).getName().toString().indexOf("[");
                String str = n.getVariables().get(i).getName().toString().substring(index, n.getVariables().get(i).getName().toString().length());
                class_variable.put(n.getVariables().get(i).getName().toString(), type1 + str);
                variableName = n.getVariables().get(i).getName().toString();
                variable_line_map.put(n.getVariables().get(i).getName().toString(), n.getEnd().get().line);
                //variable_use_map.put(n.getVars().get(i).getId().toString(),0);
                if (!class_variable_list.contains(n.getVariables().get(i).getName().toString())) {
                    class_variable_list.add(n.getVariables().get(i).getName().toString());
                } else {
                    setModifiedFalse(variableName);
                }
                type1 += str;
            } else {
                class_variable.put(n.getVariables().get(i).getName().toString(), type1);
                variableName = n.getVariables().get(i).getName().toString();
                variable_line_map.put(n.getVariables().get(i).getName().toString(), n.getEnd().get().line);
                //variable_use_map.put(n.getVars().get(i).getId().toString(),0);
                if (!class_variable_list.contains(n.getVariables().get(i).getName().toString())) {
                    class_variable_list.add(n.getVariables().get(i).getName().toString());
                } else {
                    setModifiedFalse(variableName);
                }
                if (type2.contains("[")) {
                    int index = type2.indexOf("[");
                    type2 = type2.substring(0, index);
                }
            }
            String str = type3.replaceAll("\\[\\]", "");
            if (class_name_map.get(n.getCommonType().toString()) == null && !jdkList.contains(str)) {
                if (n.getCommonType().toString().contains("<")) {
                    class_name_map.put(n.getCommonType().toString(), class_name_map.get(type2));
                    String temp = n.getCommonType().toString().replaceAll("\\<\\>", "");
                    if (class_name_map.get(temp) == null) {
                        userClassProcessing.addUserClass(n.getCommonType().toString());
                        userClassProcessing.addUserClass(temp);
                    }
                } else if (n.getCommonType().toString().contains("[")) {
                    class_name_map.put(n.getCommonType().toString(), n.getCommonType().toString());
                    String temp = n.getCommonType().toString().replaceAll("\\[\\]", "");
                    if (class_name_map.get(temp) == null) {
                        userClassProcessing.addUserClass(n.getCommonType().toString());
                        userClassProcessing.addUserClass(temp);
                    }
                } else {
                    class_name_map.put(n.getCommonType().toString(), n.getCommonType().toString());
                    userClassProcessing.addUserClass(n.getCommonType().toString());
                }
            } else if (class_name_map.get(n.getCommonType().toString()) == null && jdkList.contains(str)) {
                if (str.contains(".")) {
                    parsedFlag = false;//构造数据时得设置回去
                }
            }
            GraphNode node = new GraphNode();
            setInfo(node);
            addScope(node);
            node.setControl(false);
            node.setExit(false);
            node.setVariableDeclaration(true);
            String bracket = "";
            if (type1.contains("[")) {
                int index = type1.indexOf("[");
                bracket = type1.substring(index, type1.length());
            }
            setGlobalStatementAndVariable(n.getBegin().get().line + " " + n.toString(), variableName, class_name_map.get(type2) + bracket);
            setNodeStatementAndVariable(node);
            //node.setVariableName(globalVariableName);
            setNodeClass(node, type1, class_name_map.get(type2));
            node.setVariableName(globalVariableName, globalType, variableCount);
            GraphNode judgeNode = lastNode;
            tempViriableName = variableName;
            if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                checkVariableUsed(n.getVariables().get(i).getInitializer().isPresent() ? n.getVariables().get(i).getInitializer().get() : null, false, null,true,true);
                dealVariableDeclarationExpr(type1, variableName, node,
                        n.getVariables().get(i).getInitializer().isPresent() ? n.getVariables().get(i).getInitializer().get() : null,
                        n, i);
                if (lastNode != null && !lastNode.equals(judgeNode) && userClassProcessing.isUserClassProcessing(lastNode.getCompleteClassName())) {
                    if (variableNodeMap.get(variableName).contains(lastNode)) {
                        variableNodeMap.get(variableName).remove(lastNode);
                    }
                    if (lastNode.getParentNode() != null) {
                        lastNode.getParentNode().getChildNodes().remove(lastNode);
                        removeNode(lastNode);
                        lastNode = lastNode.getParentNode();
                    } else {
                        lastNode = null;
                        graph.setRoot(null);//构造数据时需要设置回去
                    }

                }
            } else {
                dealVariableDeclarationExpr(type1, variableName, node,
                        n.getVariables().get(i).getInitializer().isPresent() ? n.getVariables().get(i).getInitializer().get() : null,
                        n, i);
                if (lastNode != null && !lastNode.equals(judgeNode)) {
                    lastNode.setVariableDeclaration(true);
                    lastNode.setVariableName(globalVariableName, globalType, variableCount);
                    addVariableToNodeMap(variableName, lastNode);
                    if (removeConditionNodeFlag) {
                        if (variableNodeMap.get(variableName) != null) {
                            variableNodeMap.get(variableName).removeAll(variableNodeMap.get(variableName));
                        }
                        //variableNodeMap.get(variableName).remove(lastNode);
                    }
                }
            }
            globalFlag = true;
            binaryNodeList.removeAll(binaryNodeList);
            binaryFlag = false;
            tempViriableName = null;
            variableDeclarationFlag = false;
        }
        //System.out.println(lastNode.isVariableDeclaration());
        if (infoFlag) {
            lastInfo = null;
        }
        return graph;
    }


    protected Graph convert(MethodCallExpr n) {
        //add info
        boolean infoFlag = false;
        if (lastInfo == null) {
            lastInfo = constructInfo(n);
            infoFlag = true;
        }
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        node.setVariablePreserved(true);
        setGlobalStatementAndVariable(n.getBegin().get().line + " " + n.toString(), globalVariableName, globalType);
        setNodeStatementAndVariable(node);
        dealMethodExpr(n, node);
        //addNode(node);
        if (node.getCompleteClassName() != null && !node.getCompleteClassName().equals("userDefinedClass")) {
            boolean flag = addNode(node, "c");
            checkVariableUsed(n, true, node,true, flag);
            //returnType = getMethodReturnType(node);
        } else {
            checkVariableUsed(n, false, null,true,true);
            //加下面这段代码是因为当methodcall的参数是API时，就算methodcall是userDefinedClass,也会被作为一个结点加到图中
            removeUserDefinedClassNode(node);
        }
        globalFlag = true;
        if (infoFlag) {
            lastInfo = null;
        }
        return graph;

    }

    protected Graph convert(AssignExpr n) {
        //add info
        boolean infoFlag = false;
        if (lastInfo == null) {
            lastInfo = constructInfo(n);
            infoFlag = true;
        }
        variableCount += 1;
        assignFlag = true;
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setControl(false);
        node.setExit(false);
        GraphNode tempNode = lastNode;
        binaryNodeList.removeAll(binaryNodeList);
        dealAssignExpr(n, node);
        String target = n.getTarget().toString();
        if (target.contains("[")) {
            String[] strs = target.split("\\[");
            target = strs[0];
            for (int length = 0; length < strs.length - 1; length++) {
                target += "[]";
            }
        }
        if (!class_variable_list.contains(target)) {
            if (class_variable.get(filterSquareBracket(target)) != null) {
                target = filterSquareBracket(target);
            } else if (class_variable.get(filterSquareBracket(target) + "[]") != null) {
                target = filterSquareBracket(target) + "[]";
            } else if (class_variable.get(filterSquareBracket(target) + "[][]") != null) {
                target = filterSquareBracket(target) + "[][]";
            } else {
                parsedFlag = false;//构造数据时得设置回去
            }
        }
        tempViriableName = target;
        if (lastNode != null && !lastNode.equals(tempNode)) {
            if (!binaryNodeList.contains(lastNode)) {
                binaryNodeList.add(lastNode);
            }
            List<GraphNode> tempBinaryNodeList = new ArrayList<>();
            for (GraphNode graphNode : binaryNodeList) {
                if (variableNodeMap.get(target) != null) {
                    for (int index = 0; index < variableNodeMap.get(target).size(); index++) {
                        if (!graphNode.equals(variableNodeMap.get(target).get(index)) && !tempBinaryNodeList.contains(variableNodeMap.get(target).get(index))) {
                            boolean addNodeFlag = graph.addNode (variableNodeMap.get(target).get(index), graphNode, mutexList, "d");
                        } else if (graphNode.equals(variableNodeMap.get(target).get(index))) {
                            break;
                        } else {
                            //todo nothing
                        }
                    }
                }
                tempBinaryNodeList.add(graphNode);
            }
            //if (variableNodeMap.get(target) != null && !variableNodeMap.get(target).contains(lastNode)
            if (variableNodeMap.get(target) != null
                    && variableNodeMap.get(target).size() > 0) {
                if (!variableNodeMap.get(target).get(0).isAssign()) {
                    variableNodeMap.get(target).get(0).setVariablePreserved(true);
                }
            }
            tempBinaryNodeList = null;
        }
        assignFlag = false;
        binaryNodeList.removeAll(binaryNodeList);
        tempViriableName = null;
        if (infoFlag) {
            lastInfo = null;
        }
        return graph;
    }

    protected Graph convert(ObjectCreationExpr n) {
        GraphNode node = new GraphNode();
        setInfo(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        addScope(node);
        node.setVariablePreserved(true);
        setNodeStatementAndVariable(node);
        String type = n.getType().getNameAsString();
        if (type.contains("<")) {
            int index = type.indexOf("<");
            type = type.substring(0, index);
        }
        if (userClassProcessing.isUserClassProcessing(class_name_map.get(type))) {
            checkVariableUsed(n.getArguments(), false, null,true,true);
            //addNode(userClassProcessing.createObjectCreationExprNode());
        } else {
            setNodeClass(node, type, class_name_map.get(type));
            node.setControl(false);
            node.setExit(false);
            if (n.getArguments() != null) {
                List<Expression> args = n.getArguments();
                String arguments = new String("");
                node.setMethodName("new" + "(" + getArguments(args, arguments, node) + ")");
            } else {
                node.setMethodName("new" + "()");
            }
            // this fragment code is used to compare whether the node.toSting() is consistent with method declaration
            if (!verifyMethodNameAndParameter(node, n.getArguments())) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n.toString() + ": can not be parsed");
                return null;
            }
            boolean flag = addNode(node, "c");
            checkVariableUsed(n.getArguments(), true, node,true,flag);
        }
        return graph;
    }

    protected Graph convert(ArrayCreationExpr n) {
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        setNodeStatementAndVariable(node);
        setNodeClass(node, n.getElementType().toString(), class_name_map.get(n.getElementType().toString()));
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(class_name_map.get(n.getElementType().toString()))) {
            //addNode(userClassProcessing.createArrayCreationExprNode());
        } else {
            int squareBracketCount = 0;
            //List<Expression> args = n.getDimensions();
            for (int i = 0; i < n.toString().length(); i++) {
                if (n.toString().charAt(i) == '[') {
                    squareBracketCount++;
                }
            }
            String methodArguments = new String("");
            String completeMethodArguments = new String("");
            //for (int i = 0; i < argumentsList.length; i++) {
            for (int i = 0; i < squareBracketCount; i++) {
                methodArguments += "[]";
                completeMethodArguments += ("[]");
            }
            setNodeMethod(node, "new" + methodArguments, "new" + completeMethodArguments);
            if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n.toString() + ": can not be parsed");
                return null;
            }
            addNode(node, "c");
        }
        return graph;
    }

    protected Graph convert(ArrayAccessExpr n) {
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        setNodeStatementAndVariable(node);
        dealArrayAccessExprVariableType(n, node);
        if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n.toString() + ": can not be parsed");
            return null;
        } else if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
            addNode(node, "c");
        } else if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            // nothing to do
        }
        return graph;
    }

    protected Graph convert(ArrayInitializerExpr n, String type) {
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        setNodeStatementAndVariable(node);
        setNodeClassAndMethod(node, type, class_name_map.get(filterSquareBracket(type)), "ArrayInit" + preserveSquareBracket(type) + "{}", "ArrayInit" + preserveSquareBracket(type) + "{}");
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(class_name_map.get(filterSquareBracket(type)))) {
            // addNode(userClassProcessing.createArrayInitExprNode());
        } else {
            if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n.toString() + ": can not be parsed");
                return null;
            }
            addNode(node, "c");
        }
        return graph;
    }

    protected Graph convert(CastExpr n) {
        if (n.getExpression() instanceof MethodCallExpr) {
            MethodCallExpr expr = (MethodCallExpr) n.getExpression();
            convert(expr);
        } else if (n.getExpression() instanceof ObjectCreationExpr) {
            ObjectCreationExpr expr = (ObjectCreationExpr) n.getExpression();
            convert(expr);
        } else if (n.getExpression() instanceof ArrayCreationExpr) {
            ArrayCreationExpr expr = (ArrayCreationExpr) n.getExpression();
            convert(expr);
        } else if (n.getExpression() instanceof ArrayAccessExpr) {
            ArrayAccessExpr expr = (ArrayAccessExpr) n.getExpression();
            convert(expr);
        } else if (n.getExpression() instanceof FieldAccessExpr) {
            FieldAccessExpr expr = (FieldAccessExpr) n.getExpression();
            convert(expr);
        } else if (n.getExpression() instanceof EnclosedExpr) {
            EnclosedExpr expr = (EnclosedExpr) n.getExpression();
            convert(expr);
        } else {
            GraphNode node = new GraphNode();
            setInfo(node);
            addScope(node);
            node.setVariableName(globalVariableName, globalType, variableCount);
            setNodeStatementAndVariable(node);
            setNodeClassAndMethod(node, n.getType().toString(), class_name_map.get(filterSquareBracket(n.getType().toString())), "Cast", "Cast");
            node.setControl(false);
            node.setExit(false);
            if (userClassProcessing.isUserClassProcessing(class_name_map.get(filterSquareBracket(n.getType().toString())))) {
                //addNode(userClassProcessing.createCastExprNode());
            } else {
                if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                    parsedFlag = false;//构造数据时得设置回去
                    //System.err.println(n.toString() + ": can not be parsed");
                    //return null;
                }
                addNode(node, "c");
            }
        }
        return graph;
    }

    protected Graph convert(FieldAccessExpr n) {
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        node.setVariablePreserved(true);
        // setGlobalStatementAndVariable(n.toString(),globalVariableName);
        setNodeStatementAndVariable(node);
        dealFieldAccessExpr(n, node);
        //addNode(node);
        if (node.getCompleteClassName() != null) {
            if (!userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !(node.getCompleteClassName().contains("[]"))) {
                //returnType = getMethodReturnType(node);
                boolean flag = addNode(node, "c");
                checkVariableUsed(n, true, node, true,flag);
            } else if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                //returnType = "userDefinedClass";
                checkVariableUsed(n, false, null,true,true);
                removeUserDefinedClassNode(node);
            } else {
                //returnType = "int";//represent the return type of String[].length,int[].length etc..
                boolean flag = addNode(node, "c");
                checkVariableUsed(n, true, node,true,flag);
            }
        } else {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n + " can not be parsed");
        }
        // globalFlag = true;
        return graph;
    }

    protected Graph convert(UnaryExpr n) {
        return graph;
    }

    protected Graph convert(EnclosedExpr n) {
        if (n.getInner() instanceof VariableDeclarationExpr) {
            convert((VariableDeclarationExpr) n.getInner());
        } else if (n.getInner() instanceof MethodCallExpr) {
            convert((MethodCallExpr) n.getInner());
        } else if (n.getInner() instanceof FieldAccessExpr) {
            convert((FieldAccessExpr) n.getInner());
        } else if (n.getInner() instanceof ObjectCreationExpr) {
            convert((ObjectCreationExpr) n.getInner());
        } else if (n.getInner() instanceof ArrayAccessExpr) {
            convert((ArrayAccessExpr) n.getInner());
        } else if (n.getInner() instanceof ArrayCreationExpr) {
            convert((ArrayCreationExpr) n.getInner());
        } else if (n.getInner() instanceof CastExpr) {
            convert((CastExpr) n.getInner());
        } else if (n.getInner() instanceof UnaryExpr) {
            convert((UnaryExpr) n.getInner());
        } else if (n.getInner() instanceof EnclosedExpr) {
            convert((EnclosedExpr) n.getInner());
        } else {
            parsedFlag = false;//构造数据时得设置回去
        }
        return graph;
    }

    protected void dealClassNameMap(String type) {
        if (type != null && class_name_map.get(type) == null) {
            for (int i = 0; i < starImportStringList.size(); i++) {
                String className = starImportStringList.get(i).replace("*", type);
                try {
                    if (Thread.currentThread().getContextClassLoader().loadClass(className) != null) {
                        class_name_map.put(type, className);
                    }
                } catch (Exception e) {
                    if (!(e instanceof ClassNotFoundException)) {
                        parsedFlag = false;//构造数据时得设置回去
                        //System.err.println(e.getMessage());
                    }
                    // nothing to do
                } catch (Error e) {
                    parsedFlag = false;//构造数据时得设置回去
                    //System.err.println(e.getMessage());
                }
            }
        }
    }

    protected void dealForInitCondition(ForStmt n, List<String> variableList) {
        if (n.getInitialization() != null) {
            if (n.getInitialization().size() > 1) {
                //todo nothing
            } else if (n.getInitialization().size() == 1 && !(n.getInitialization().get(0) instanceof VariableDeclarationExpr)) {
                /**dealCondition(n.getInit().get(0), lastNode);**/
            } else if (n.getInitialization().size() == 1 && n.getInitialization().get(0) instanceof VariableDeclarationExpr) {
                VariableDeclarationExpr expr = (VariableDeclarationExpr) n.getInitialization().get(0);
                if (expr.getVariables().size() == 1) {
                    /** GraphNode node = lastNode;**/
                    convert(expr);
                    //下面的两条语句当需要考虑条件时，要注释掉
                    //lastNode = lastNode.getParentNode();
                    //lastNode.setChildNodes(new ArrayList<>());
                    variableList.add(class_variable_list.get(class_variable_list.size() - 1));
                } else {
                    for (int i = 0; i < expr.getVariables().size(); i++) {
                        VariableDeclarationExpr singleVariableDeclarationExpr = new VariableDeclarationExpr();
                        singleVariableDeclarationExpr.setAllTypes(expr.getCommonType());
                        NodeList list = new NodeList();
                        list.add(expr.getVariables().get(i));
                        singleVariableDeclarationExpr.setVariables(list);
                        /** GraphNode node = lastNode;**/
                        convert(singleVariableDeclarationExpr);
                        //下面的两条语句当需要考虑条件时，要注释掉
                        //lastNode = lastNode.getParentNode();
                        //lastNode.setChildNodes(new ArrayList<>());
                        variableList.add(class_variable_list.get(class_variable_list.size() - 1));
                    }
                }
            } else {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n + " " + "can not be parsed");
            }
        } else {
            //todo nothing
        }
    }

    protected void dealCondition(String variableName, Expression n) {
        if (n != null) {
            if (n instanceof MethodCallExpr) {
                dealMethodCallCondition(variableName, n);
            } else if (n instanceof FieldAccessExpr) {
                dealFieldAccessCondition(variableName, n);
            } else if (n instanceof ObjectCreationExpr) {
                GraphNode tempNode = lastNode;
                convert((ObjectCreationExpr) n);
                if (lastNode != null && !lastNode.equals(tempNode)) {
                    lastNode.setCondition(true);
                    addVariableToNodeMap(variableName, lastNode);
                    if (conditionAssignFlag) {
                        binaryNodeList.add(lastNode);
                    }
                }
            } else if (n instanceof BinaryExpr) {
                dealBinaryCondition(variableName, n);
            } else if (n instanceof EnclosedExpr) {
                Expression expr = ((EnclosedExpr) n).getInner();
                dealCondition(variableName, expr);
            } else if (n instanceof CastExpr) {
                Expression expr = ((CastExpr) n).getExpression();
                dealCondition(variableName, expr);
            } else if (n instanceof AssignExpr) {
                Expression expr = ((AssignExpr) n).getValue();
                binaryNodeList.removeAll(binaryNodeList);
                if (!foreachConditionFlag) {
                    assignFlag = true;
                    conditionAssignFlag = true;
                    String target = ((AssignExpr) n).getTarget().toString();
                    if (target.contains("[")) {
                        String[] strs = target.split("\\[");
                        target = strs[0];
                        for (int length = 0; length < strs.length - 1; length++) {
                            target += "[]";
                        }
                    }
                    if (!class_variable_list.contains(target)) {
                        if (class_variable.get(filterSquareBracket(target)) != null) {
                            target = filterSquareBracket(target);
                        } else if (class_variable.get(filterSquareBracket(target) + "[]") != null) {
                            target = filterSquareBracket(target) + "[]";
                        } else if (class_variable.get(filterSquareBracket(target) + "[][]") != null) {
                            target = filterSquareBracket(target) + "[][]";
                        } else {
                            parsedFlag = false;//构造数据时得设置回去
                        }
                    }
                    tempViriableName = target;
                    GraphNode tempNode = lastNode;
                    dealCondition(target, expr);
                    if (lastNode != null && !lastNode.equals(tempNode)) {
                        if (!binaryNodeList.contains(lastNode)) {
                            binaryNodeList.add(lastNode);
                        }
                        List<GraphNode> tempBinaryNodeList = new ArrayList<>();
                        for (GraphNode graphNode : binaryNodeList) {
                            if (variableNodeMap.get(target) != null) {
                                for (int index = 0; index < variableNodeMap.get(target).size(); index++) {
                                    if (!graphNode.equals(variableNodeMap.get(target).get(index)) && !tempBinaryNodeList.contains(variableNodeMap.get(target).get(index))) {
                                        boolean addNodeFlag = graph.addNode (variableNodeMap.get(target).get(index), graphNode, mutexList, "d");
                                    } else if (graphNode.equals(variableNodeMap.get(target).get(index))) {
                                        break;
                                    } else {
                                        //todo nothing
                                    }
                                }
                            }
                            tempBinaryNodeList.add(graphNode);
                        }
                        //if (variableNodeMap.get(target) != null && !variableNodeMap.get(target).contains(lastNode)
                        if (variableNodeMap.get(target) != null
                                && variableNodeMap.get(target).size() > 0) {
                            if (!variableNodeMap.get(target).get(0).isAssign()) {
                                variableNodeMap.get(target).get(0).setVariablePreserved(true);
                            }
                        }
                        tempBinaryNodeList = null;
                    }
                    binaryNodeList.removeAll(binaryNodeList);
                    assignFlag = false;
                    conditionAssignFlag = false;
                    tempViriableName = null;
                } else {
                    binaryFlag = false;
                    variableDeclarationFlag = true;
                    String target = ((AssignExpr) n).getTarget().toString();
                    if (target.contains("[")) {
                        String[] strs = target.split("\\[");
                        target = strs[0];
                        for (int length = 0; length < strs.length - 1; length++) {
                            target += "[]";
                        }
                    }
                    if (!class_variable_list.contains(target)) {
                        if (class_variable.get(filterSquareBracket(target)) != null) {
                            target = filterSquareBracket(target);
                        } else if (class_variable.get(filterSquareBracket(target) + "[]") != null) {
                            target = filterSquareBracket(target) + "[]";
                        } else if (class_variable.get(filterSquareBracket(target) + "[][]") != null) {
                            target = filterSquareBracket(target) + "[][]";
                        } else {
                            parsedFlag = false;//构造数据时得设置回去
                        }
                    }
                    tempViriableName = target;
                    dealCondition(target, expr);
                    binaryFlag = false;
                    tempViriableName = null;
                    variableDeclarationFlag = false;
                }
            } else if (n instanceof VariableDeclarationExpr) {
                List<VariableDeclarator> list = ((VariableDeclarationExpr) n).getVariables();
                for (int i = 0; i < list.size(); i++) {
                    binaryFlag = false;
                    variableDeclarationFlag = true;
                    Expression expr = list.get(i).getInitializer().isPresent() ? list.get(i).getInitializer().get() : null;
                    String target = list.get(i).getName().toString();
                    if (target.contains("[")) {
                        String[] strs = target.split("\\[");
                        target = strs[0];
                        for (int length = 0; length < strs.length - 1; length++) {
                            target += "[]";
                        }
                    }
                    if (!class_variable_list.contains(target)) {
                        if (class_variable.get(filterSquareBracket(target)) != null) {
                            target = filterSquareBracket(target);
                        } else if (class_variable.get(filterSquareBracket(target) + "[]") != null) {
                            target = filterSquareBracket(target) + "[]";
                        } else if (class_variable.get(filterSquareBracket(target) + "[][]") != null) {
                            target = filterSquareBracket(target) + "[][]";
                        } else {
                            parsedFlag = false;//构造数据时得设置回去
                        }
                    }
                    tempViriableName = target;
                    dealCondition(target, expr);
                    binaryFlag = false;
                    tempViriableName = null;
                    variableDeclarationFlag = false;
                }

            }else if (n instanceof UnaryExpr){
                Expression expr = ((UnaryExpr) n).getExpression();
                dealCondition(variableName, expr);
            }
            if (markHole && n.toString().contains("true == true")) {
                replaceHoleString();
            }
        }
    }

    protected void dealMethodCallCondition(String variableName, Expression n) {//已修改好
        MethodCallExpr expr = (MethodCallExpr) n;
        GraphNode conditionContentNode = new GraphNode();
        addScope(conditionContentNode);
        conditionContentNode.setVariablePreserved(true);
        conditionContentNode.setCondition(true);
        dealMethodExpr(expr, conditionContentNode);
        if (conditionContentNode.getCompleteClassName() != null && !conditionContentNode.getCompleteClassName().equals("userDefinedClass")) {
            boolean flag = addNode(conditionContentNode, "c");
            if (variableName != null && flag) {
                addVariableToNodeMap(variableName, conditionContentNode);
            }
            if (conditionAssignFlag && conditionContentNode != null && flag) {
                binaryNodeList.add(conditionContentNode);
            }
            checkVariableUsed(n, true, conditionContentNode, true, flag);
        } else {
            checkVariableUsed(n, false, null,true,true);
            removeUserDefinedClassNode(conditionContentNode);
        }
    }

    protected void dealFieldAccessCondition(String variableName, Expression n) {//已修改好
        FieldAccessExpr expr = (FieldAccessExpr) n;
        GraphNode conditionContentNode = new GraphNode();
        addScope(conditionContentNode);
        conditionContentNode.setVariablePreserved(true);
        conditionContentNode.setCondition(true);
        dealFieldAccessExpr(expr, conditionContentNode);
        if (conditionContentNode.getCompleteClassName() != null) {
            if (!userClassProcessing.isUserClassProcessing(conditionContentNode.getCompleteClassName()) && !(conditionContentNode.getCompleteClassName().contains("[]"))) {
                boolean flag = addNode(conditionContentNode, "c");
                if (variableName != null && flag) {
                    addVariableToNodeMap(variableName, conditionContentNode);
                }
                if (conditionAssignFlag && conditionContentNode != null && flag) {
                    binaryNodeList.add(conditionContentNode);
                }
                checkVariableUsed(n, true, conditionContentNode,true, flag);
            } else if (userClassProcessing.isUserClassProcessing(conditionContentNode.getCompleteClassName())) {
                checkVariableUsed(n, false, null,true,true);
                removeUserDefinedClassNode(conditionContentNode);
            } else {
                boolean flag = addNode(conditionContentNode, "c");
                if (variableName != null && flag) {
                    addVariableToNodeMap(variableName, conditionContentNode);
                }
                if (conditionAssignFlag && conditionContentNode != null && flag) {
                    binaryNodeList.add(conditionContentNode);
                }
                checkVariableUsed(n, true, conditionContentNode,true,flag);
            }
        } else {
            parsedFlag = false;//构造数据时得设置回去
        }
    }


    protected void dealBinaryCondition(String variableName, Expression n) {//已修改好
        BinaryExpr expr = (BinaryExpr) n;
        Expression leftExpr = expr.getLeft();
        Expression rightExpr = expr.getRight();
        dealCondition(variableName, leftExpr);
        binaryFlag = true;
        dealCondition(variableName, rightExpr);
    }

    protected void dealMethodReturnType(String variable, String type) {
        if (type != null && type.contains(".")) {
            String[] strs = type.split("\\.");
            String simpleType = strs[strs.length - 1];
            if (!class_name_map.containsKey(simpleType)) {
                class_name_map.put(simpleType, type);
            }
            if (variable != null && class_variable.containsKey(variable)) {
                setVariableType(variable, getVariableType(class_variable.get(variable), true), simpleType);
            }
        } else if (type != null && class_variable.containsKey(variable)) {
            if (!class_name_map.containsKey(type)) {
                class_name_map.put(type, type);
            }
            if (variable != null) {
                setVariableType(variable, getVariableType(class_variable.get(variable), true), type);
            }
        }
    }

    protected String getMethodReturnType(GraphNode node) {
        String type = node.getCompleteClassName();
        if (node.getCompleteClassName() != null && !node.getCompleteClassName().contains("[]")) {
            MethodReflection methodReflection = new MethodReflection(node.getCompleteClassName());
            Map<String, String> map = methodReflection.getAllMethodsReturnTypeMap(node.getCompleteClassName());
            if (map.containsKey(node.toString()) && !map.get(node.toString()).equals("void")) {
                type = map.get(node.toString());
            } else if (!map.containsKey(node.toString()) && node.toString().contains(".new(")) {
                //todo nothing
            } else {
                type = "userDefinedClass";
            }
        } else if (node.getCompleteClassName() == null) {
            type = null;
        } else {
            type = "userDefinedClass";
        }
        return type;
    }

    protected String dealBinaryReturnType(List<String> list) {
        if (list.contains("null")) {
            return "null";
        } else if (list.contains("String")) {
            return "String";
        } else if (list.contains("double")) {
            return "double";
        } else if (list.contains("int")) {
            return "int";
        } else if (list.contains("boolean")) {
            return "boolean";
        } else if (list.contains("char")) {
            return "char";
        } else {
            return "null";
        }
    }

    protected void dealArrayAccessExprVariableType(ArrayAccessExpr n, GraphNode node) {
        String expressionString = n.toString();
        String expressionNameString = new String("");
        String expressionWithoutIndexString = new String("");
        String expressionWithoutIndexAndNameString = new String("");
        /*the following code is used to filter out the index (b[1][2] -> b[][])*/
        boolean flag = true;
        for (int i = 0; i < expressionString.length(); i++) {
            char ch = expressionString.charAt(i);
            if (flag) {
                expressionWithoutIndexString += ch;
                expressionNameString += ch;
            }
            if (ch == '[') {
                expressionWithoutIndexAndNameString += "[index";
                expressionNameString = expressionNameString.substring(0, expressionNameString.length() - 1);
                flag = false;
            } else if (ch == ']') {
                expressionWithoutIndexString += ch;
                expressionWithoutIndexAndNameString += ']';
                flag = true;
            }
        }
        /*the following code is used to construct tree node*/
        node.setControl(false);
        setNodeMethod(node, expressionWithoutIndexAndNameString, expressionWithoutIndexAndNameString);
        if (class_variable.get(expressionWithoutIndexString) != null) {
            setNodeClass(node, getVariableType(class_variable.get(expressionWithoutIndexString), false), class_name_map.get(filterSquareBracket(getVariableType(class_variable.get(expressionWithoutIndexString), false))));
            returnType = class_name_map.get(getVariableType(class_variable.get(expressionWithoutIndexString), false));
            node.getArgumentList().add(expressionWithoutIndexString);
        } else if (class_variable.get(expressionNameString) != null) {
            node.getArgumentList().add(expressionNameString);
            String type = getVariableType(class_variable.get(expressionNameString), false);
            String type2 = getVariableType(class_variable.get(expressionNameString), true);
            if (type.contains("[")) {
                int firstIndexOfLeftSquareBracket = type.indexOf('[');
                type = type.substring(0, firstIndexOfLeftSquareBracket);
            }
            int count = type2.split("\\[").length - expressionWithoutIndexString.split("\\[").length;
            setNodeClass(node, type, class_name_map.get(filterSquareBracket(type)));
            returnType = class_name_map.get(type);
            for (int i = 0; i < count; i++) {
                setNodeMethod(node, node.getMethodName() + "[]", node.getCompleteMethodName() + "[]");
                returnType = class_name_map.get(type) + "[]";
            }
        } else if (class_variable.get(expressionWithoutIndexString + "[]") != null) {
            node.getArgumentList().add(expressionWithoutIndexString + "[]");
            String type = getVariableType(class_variable.get(expressionWithoutIndexString + "[]"), false);
            if (type.contains("[")) {
                int firstIndexOfLeftSquareBracket = type.indexOf('[');
                type = type.substring(0, firstIndexOfLeftSquareBracket);
            }
            String completeType = class_name_map.get(filterSquareBracket(type)) + "[]";
            setNodeClass(node, type, class_name_map.get(filterSquareBracket(type)));
            setNodeMethod(node, node.getMethodName() + "[]", node.getCompleteMethodName() + "[]");
            returnType = completeType;
        } else {
            //parsedFlag = false;
            //System.err.println(n + " " + "can not be parsed");
        }
        node.setExit(false);
    }

    protected void dealFieldAccessExprScope(Expression expression, FieldAccessExpr n, GraphNode fieldAccessNode) {
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setControl(false);
        node.setExit(false);
        if (expression instanceof NameExpr) {
            String fieldAccessScope = expression.toString();
            dealClassNameMap(class_variable.get(expression.toString()));
            dealClassNameMap(expression.toString());
            if ((class_variable.get(fieldAccessScope) != null && !getVariableType(class_variable.get(fieldAccessScope), true).contains("[]"))) {
                fieldAccessNode.getArgumentList().add(fieldAccessScope);
                if (class_variable.get(fieldAccessScope).contains("[][]")) {
                    String completeType = class_name_map.get(class_variable.get(fieldAccessScope).replaceAll("\\[\\]", "")) + "[][]";
                    setNodeClassAndMethod(node, class_variable.get(fieldAccessScope), completeType, n.getName().toString(), n.getName().toString());
                } else if (class_variable.get(fieldAccessScope).contains("[]")) {
                    String completeType = class_name_map.get(class_variable.get(fieldAccessScope).replaceAll("\\[\\]", "")) + "[]";
                    setNodeClassAndMethod(node, class_variable.get(fieldAccessScope), completeType, n.getName().toString(), n.getName().toString());
                } else {
                    setNodeClassAndMethod(node, class_variable.get(fieldAccessScope), class_name_map.get(class_variable.get(fieldAccessScope)), n.getName().toString(), n.getName().toString());
                }
            } else if ((class_variable.get(fieldAccessScope) != null && !getVariableType(fieldAccessScope, true).contains("[]"))) {
                fieldAccessNode.getArgumentList().add(fieldAccessScope);
                if (class_variable.get(fieldAccessScope).contains("[][]")) {
                    String completeType = class_name_map.get(class_variable.get(fieldAccessScope).replaceAll("\\[\\]", "")) + "[][]";
                    setNodeClassAndMethod(node, class_variable.get(fieldAccessScope), completeType, n.getName().toString(), n.getName().toString());
                } else if (class_variable.get(fieldAccessScope).contains("[]")) {
                    String completeType = class_name_map.get(class_variable.get(fieldAccessScope).replaceAll("\\[\\]", "")) + "[]";
                    setNodeClassAndMethod(node, class_variable.get(fieldAccessScope), completeType, n.getName().toString(), n.getName().toString());
                } else {
                    setNodeClassAndMethod(node, class_variable.get(fieldAccessScope), class_name_map.get(class_variable.get(fieldAccessScope)), n.getName().toString(), n.getName().toString());
                }
            } else if (class_variable.get(fieldAccessScope) == null && class_variable.get(fieldAccessScope + "[]") == null && class_variable.get(fieldAccessScope + "[][]") == null && class_name_map.get(fieldAccessScope) == null) {
                setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", n.getName().toString(), n.getName().toString());
            } else if (class_variable.get(fieldAccessScope) == null && class_name_map.get(fieldAccessScope) != null) {
                setNodeClassAndMethod(node, fieldAccessScope, class_name_map.get(fieldAccessScope), n.getName().toString(), n.getName().toString());
            } else if (class_variable.get(fieldAccessScope + "[]") != null) {
                fieldAccessNode.getArgumentList().add(fieldAccessScope + "[]");
                String type = getVariableType(class_variable.get(fieldAccessScope + "[]"), false);
                String completeType = class_name_map.get(type.replaceAll("\\[\\]", "")) + "[]";
                //variable_use_map.put(n.getScope().toString() + "[]",//variable_use_map.get(n.getScope().toString() + "[]") + 1);
                if (!type.contains("[]")) {
                    type += "[]";
                }
                if (n.getName().toString().equals("length")) {
                    setNodeClassAndMethod(node, type, completeType, n.getName().toString(), n.getName().toString());
                } else {
                    parsedFlag = false;//构造数据时得设置回去
                    //System.err.println(n.getField() + " " + "can not be parsed");
                }
            } else if (class_variable.get(fieldAccessScope + "[][]") != null) {
                fieldAccessNode.getArgumentList().add(fieldAccessScope + "[][]");
                String type = getVariableType(class_variable.get(fieldAccessScope + "[][]"), false);
                String completeType = class_name_map.get(type.replaceAll("\\[\\]", "")) + "[][]";
                //variable_use_map.put(n.getScope().toString() + "[][]",//variable_use_map.get(n.getScope().toString() + "[][]") + 1);
                if (!type.contains("[]")) {
                    type += "[][]";
                }
                if (n.getName().toString().equals("length")) {
                    setNodeClassAndMethod(node, type, completeType, n.getName().toString(), n.getName().toString());
                } else {
                    parsedFlag = false;//构造数据时得设置回去
                    //System.err.println(n.getField() + " " + "can not be parsed");
                }
            } else if (jdkList.contains(fieldAccessScope)) {
                fieldAccessNode.getArgumentList().add(fieldAccessScope);
                String[] strs = fieldAccessScope.split("\\.");
                setNodeClassAndMethod(node, strs[strs.length - 1], fieldAccessScope, n.getName().toString(), n.getName().toString());
            } else {
                fieldAccessNode.getArgumentList().add(fieldAccessScope);
                String type = getVariableType(class_variable.get(fieldAccessScope), true);
                //variable_use_map.put(n.getScope().toString(),//variable_use_map.get(n.getScope().toString()) + 1);
                if (type != null) {
                    int index = type.indexOf('[');
                    String completeType = class_name_map.get(type.substring(0, index)) + type.substring(index, type.length());
                    if (n.getName().toString().equals("length")) {
                        setNodeClassAndMethod(node, type, completeType, n.getName().toString(), n.getName().toString());
                    } else {
                        parsedFlag = false;//构造数据时得设置回去
                        //System.err.println(n.getField() + " " + "can not be parsed");
                    }
                } else {
                    parsedFlag = false;//构造数据时得设置回去
                    //System.err.println(n.toString() + " " + "can not be parsed");
                }
            }
        } else if (expression instanceof FieldAccessExpr) {
            if (jdkList.contains(expression.toString())) {
                parsedFlag = false;//构造数据时得设置回去
            } else {
                dealFieldAccessExpr((FieldAccessExpr) expression, fieldAccessNode);
                if (returnType != null) {
                    String[] strs = returnType.split("\\.");
                    setNodeClass(node, strs[strs.length - 1], returnType);
                    if (class_name_map.get(strs[strs.length - 1]) == null) {
                        class_name_map.put(strs[strs.length - 1], returnType);
                    }
                } else {
                    returnType = "userDefinedClass";
                    setNodeClass(node, "userDefinedClass", "userDefinedClass");
                }
                setNodeMethod(node, n.getName().toString(), n.getName().toString());
            }
            //dealLastFieldOfFieldAccess(n, node);
        } else if (expression instanceof MethodCallExpr) {
            dealMethodExpr((MethodCallExpr) expression, fieldAccessNode);
            if (returnType != null) {
                String[] strs = returnType.split("\\.");
                setNodeClass(node, strs[strs.length - 1], returnType);
                if (class_name_map.get(strs[strs.length - 1]) == null) {
                    class_name_map.put(strs[strs.length - 1], returnType);
                }
            } else {
                returnType = "userDefinedClass";
                setNodeClass(node, "userDefinedClass", "userDefinedClass");
            }
            setNodeMethod(node, n.getName().toString(), n.getName().toString());
        } else if (expression instanceof ArrayAccessExpr) {
            dealArrayAccessExprVariableType((ArrayAccessExpr) expression, node);
            String[] strs = node.getCompleteClassName().split("\\.");
            if (!n.getName().toString().equals("length")) {
                setNodeClass(node, strs[strs.length - 1], node.getCompleteClassName());
            }
            setNodeMethod(node, n.getName().toString(), n.getName().toString());
        } else if (expression instanceof EnclosedExpr) {
            EnclosedExpr expr = (EnclosedExpr) expression;
            dealFieldAccessExprScope(expr.getInner(), n, fieldAccessNode);
            node = null;
        } else if (expression instanceof ObjectCreationExpr) {
            GraphNode tempNode = lastNode;
            convert((ObjectCreationExpr) expression);
            if (lastNode != null && !lastNode.equals(tempNode)) {
                if (!fieldAccessNode.getDataDependency().contains(lastNode)) {
                    fieldAccessNode.getDataDependency().add(lastNode);
                }
//                for (GraphNode graphNode : lastNode.getDataDependency()) {
//                    if (!fieldAccessNode.getDataDependency().contains(graphNode)) {
//                        fieldAccessNode.getDataDependency().add(graphNode);
//                    }
//                }
                for (GraphNode graphNode : fieldAccessNode.getDataDependency()) {
                    boolean addNodeFlag = graph.addNode (graphNode, fieldAccessNode, mutexList, "d");
                }
                if (conditionFlag || assignFlag || variableDeclarationFlag) {
                    if (tempViriableName != null) {
                        objectCreationNode = lastNode;
                        if (lastNode != null) {
                            addVariableToNodeMap(tempViriableName, objectCreationNode);
                            if (!variableDeclarationFlag) {
                                binaryNodeList.add(objectCreationNode);
                            }
                        }
                        objectCreationNode = null;
                        if (variableDeclarationFlag) {
                            binaryFlag = true;
                        }
                    } else {
                        parsedFlag = false;//构造数据时得设置回去
                    }
                }
                String type = getMethodReturnType(lastNode);
                if (type != null) {
                    String[] strs = type.split("\\.");
                    String simpleType = strs[strs.length - 1];
                    setNodeClassAndMethod(node, simpleType, type, n.getName().toString(), n.getName().toString());
                } else {
                    setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "FieldAccess", "FieldAccess");
                }
            } else {
                setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "FieldAccess", "FieldAccess");
            }
        } else {
            setNodeClassAndMethod(node, "", "", "", "");
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n + " " + "can not be parsed");
        }
        //判断field access是否正确
        if (node != null) {
            //先处理node中的classname,判断是否为用户自定义类
            if (node.getCompleteClassName() != null) {
                String str = node.getCompleteClassName();
                str = str.replaceAll("\\[\\]", "");
                if (!jdkList.contains(str)) {
                    setNodeClass(node, "userDefinedClass", "userDefinedClass");
                }
            } else {
                parsedFlag = false;//构造数据时得设置回去
            }
            //判断是否为一个正确的API调用
            if (node.getCompleteClassName() != null && !node.getCompleteClassName().equals("userDefinedClass")) {
                if (fieldAccessNode.getClassName() == null) {
                    setNodeClass(fieldAccessNode, node.getClassName(), node.getCompleteClassName());
                }
                if (!verifyFieldAccess(node) && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName()) && !node.toString().endsWith("[].length")) {
                    parsedFlag = false;//构造数据时得设置回去
                } else {
                    if (fieldAccessNode.getMethodName() == null) {
                        setNodeMethod(fieldAccessNode, node.getMethodName(), node.getCompleteMethodName());
                    } else {
                        setNodeMethod(fieldAccessNode, fieldAccessNode.getMethodName() + "." + node.getMethodName(), fieldAccessNode.getCompleteMethodName() + "." + node.getCompleteMethodName());
                    }
                    returnType = getMethodReturnType(node);
                }
            } else if (node.getCompleteClassName() == null) {
                parsedFlag = false;//构造数据时得设置回去
            } else if (node.getCompleteClassName().equals("userDefinedClass")) {
                //if (methodNode.getClassName() == null) {
                setNodeClass(fieldAccessNode, "userDefinedClass", "userDefinedClass");
                returnType = "userDefinedClass";
                //}
            } else {
                parsedFlag = false;//构造数据时得设置回去
            }
        }
    }

    protected void dealReturnExpr(Expression expr) {
        if (expr != null) {
            if (expr instanceof MethodCallExpr) {
                convert((MethodCallExpr) expr);
            } else if (expr instanceof ObjectCreationExpr) {
                convert((ObjectCreationExpr) expr);
            } else if (expr instanceof FieldAccessExpr) {
                convert((FieldAccessExpr) expr);
            } else if (expr instanceof EnclosedExpr) {
                dealReturnExpr(((EnclosedExpr) expr).getInner());
            } else if (expr instanceof CastExpr) {
                dealReturnExpr(((CastExpr) expr).getExpression());
            } else if (expr instanceof BinaryExpr) {
                BinaryExpr binaryExpr = (BinaryExpr) expr;
                dealReturnExpr(binaryExpr.getRight());
                dealReturnExpr(binaryExpr.getLeft());
            } else if (expr instanceof UnaryExpr) {
                convert((UnaryExpr) expr);
            }
        }
    }

    protected void dealVariableDeclarationExpr(String type1, String variableName, GraphNode node, Expression declareExpression, VariableDeclarationExpr n, int i) {
        boolean verifyFlag = true;
        if (declareExpression != null) {
            if (!declareExpression.toString().equals("null")) {
                String type = new String("");
                if (type1.contains("[")) {
                    type += "Array";
                }
                if (declareExpression instanceof BinaryExpr) {
                    boolean flag = true;
                    flag = dealBinaryExprInVariableDeclarationAndAssignExpr(variableName, declareExpression, flag);
                    binaryFlag = false;
                    if (flag) {
                        String constant = handleConstant(declareExpression.toString());
                        type += "Declaration" + constant + preserveSquareBracket(type1);
                        setNodeMethod(node, type, type);
                        addNode(node, "c");
                        addVariableToNodeMap(variableName, node);
                        verifyFlag = false;
                    }
                } else if (declareExpression.getChildNodes().size() == 0 && !declareExpression.toString().contains("{")) {//this condition is equivalent the condition "n.getVars().get(0).getInit() instanceof IntegerExpr || DoubleExpr"
                    String constant = handleConstant(declareExpression.toString());
                    type += "Declaration" + constant + preserveSquareBracket(type1);
                    setNodeMethod(node, type, type);
                    addNode(node, "c");
                    addVariableToNodeMap(variableName, node);
                    verifyFlag = false;
                } else if (declareExpression instanceof MethodCallExpr) {
                    MethodCallExpr expr = (MethodCallExpr) declareExpression;
                    GraphNode methodNode = new GraphNode();
                    setInfo(methodNode);
                    addScope(methodNode);
                    methodNode.setVariableName(globalVariableName, globalType, variableCount);
                    methodNode.setVariablePreserved(true);
                    setNodeStatementAndVariable(methodNode);
                    dealMethodExpr(expr, methodNode);
                    if (methodNode.getCompleteClassName() != null && !methodNode.getCompleteClassName().equals("userDefinedClass")) {
                        returnType = getMethodReturnType(methodNode);
                    }
                    dealMethodReturnType(n.getVariables().get(i).getName().toString(), returnType);
                    if (userClassProcessing.isUserClassProcessing(methodNode.getCompleteClassName())) {
                        String constant = handleConstant(declareExpression.toString());
                        type += "Declaration" + constant + preserveSquareBracket(type1);
                        setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), type, type);
                        //setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Declaration", "Declaration");
                        boolean flag = addNode(node, "c");
                        if(flag) {
                            addVariableToNodeMap(variableName, node);
                        }
                        checkVariableUsed(n, false, null,true,flag);
                        removeUserDefinedClassNode(methodNode);
                    } else {
                        boolean flag = addNode(methodNode, "c");
                        checkVariableUsed(expr, true, methodNode,true,flag);
                    }
                } else if (declareExpression instanceof ObjectCreationExpr) {
                    ObjectCreationExpr expr = (ObjectCreationExpr) declareExpression;
                    setVariableType(n.getVariables().get(i).getName().toString(), getVariableType(class_variable.get(n.getVariables().get(i).getName().toString()), true), expr.getType().toString());
                    //GraphNode tempNode = lastNode;
                    convert(expr);
                } else if (declareExpression instanceof ArrayCreationExpr) {
                    ArrayCreationExpr expr = (ArrayCreationExpr) declareExpression;
                    setVariableType(n.getVariables().get(i).getName().toString(), getVariableType(class_variable.get(n.getVariables().get(i).getName().toString()), true), expr.getElementType().toString());
                    GraphNode tempNode = lastNode;
                    // tempNode.setVariableName(globalVariableName,globalType);
                    convert(expr);
                    setVariableDeclaration(tempNode, variableName);
                } else if (declareExpression instanceof CastExpr) {
                    CastExpr expr = (CastExpr) declareExpression;
                    GraphNode tempNode = lastNode;
                    // tempNode.setVariableName(globalVariableName,globalType);
                    convert(expr);
                    if ((lastNode != null && lastNode.equals(tempNode)) || lastNode == null) {
                        setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "DeclarationCast", "DeclarationCast");
                        addNode(node, "c");
                        addVariableToNodeMap(variableName, node);
                    } else if (lastNode != null && !lastNode.equals(tempNode)) {
                        if (expr.getExpression() instanceof MethodCallExpr ||
                                expr.getExpression() instanceof FieldAccessExpr ||
                                expr.getExpression() instanceof ObjectCreationExpr) {
                        } else {
                            setVariableDeclaration(tempNode, variableName);
                        }
                    }
                    setVariableType(n.getVariables().get(i).getName().toString(), getVariableType(class_variable.get(n.getVariables().get(i).getName().toString()), true), expr.getType().toString());
                } else if (declareExpression instanceof ArrayAccessExpr) {
                    ArrayAccessExpr expr = (ArrayAccessExpr) declareExpression;
                    GraphNode arrayAccessNode = new GraphNode();
                    setInfo(arrayAccessNode);
                    addScope(arrayAccessNode);
                    arrayAccessNode.setVariableName(globalVariableName, globalType, variableCount);
                    arrayAccessNode.setVariableDeclaration(true);
                    setNodeStatementAndVariable(arrayAccessNode);
                    dealArrayAccessExprVariableType(expr, arrayAccessNode);
                    if (!userClassProcessing.isUserClassProcessing(arrayAccessNode.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(arrayAccessNode, arrayAccessNode.getClassName())) {
                        parsedFlag = false;//构造数据时得设置回去
                        //System.err.println(n.toString() + ": can not be parsed");
                        addNode(arrayAccessNode, "c");
                        //return null;
                    } else if (!userClassProcessing.isUserClassProcessing(arrayAccessNode.getCompleteClassName()) && verifyMethodNameAndParameterOfSpecial(arrayAccessNode, arrayAccessNode.getClassName())) {
                        GraphNode tempNode = lastNode;
                        addNode(arrayAccessNode, "c");
                        setVariableDeclaration(tempNode, variableName);
                    } else {
                        setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Declaration", "Declaration");
                        addNode(node, "c");
                        addVariableToNodeMap(variableName, node);
                        verifyFlag = false;
                    }
                    dealMethodReturnType(n.getVariables().get(i).getName().toString(), returnType);
                } else if (declareExpression instanceof ArrayInitializerExpr) {
                    ArrayInitializerExpr expr = (ArrayInitializerExpr) declareExpression;
                    if (n.getCommonType().toString().contains("[]")) {
                        GraphNode tempNode = lastNode;
                        convert(expr, n.getCommonType().toString());
                        setVariableDeclaration(tempNode, variableName);
                    } else if (type1.contains("[]")) {
                        GraphNode tempNode = lastNode;
                        convert(expr, type1);
                        setVariableDeclaration(tempNode, variableName);
                    } else {
                        parsedFlag = false;//构造数据时得设置回去
                        //System.err.println(n.toString() + " can not be parsed");
                    }
                } else if (declareExpression instanceof FieldAccessExpr) {
                    FieldAccessExpr expr = (FieldAccessExpr) declareExpression;
                    GraphNode fieldNode = new GraphNode();
                    setInfo(fieldNode);
                    addScope(fieldNode);
                    fieldNode.setVariableName(globalVariableName, globalType, variableCount);
                    fieldNode.setVariablePreserved(true);
                    setNodeStatementAndVariable(fieldNode);
                    dealFieldAccessExpr(expr, fieldNode);
                    if (fieldNode.getCompleteClassName() != null) {
                        if (!userClassProcessing.isUserClassProcessing(fieldNode.getCompleteClassName()) && !(fieldNode.getCompleteClassName().contains("[]"))) {
                            returnType = getMethodReturnType(fieldNode);
                            boolean flag = addNode(fieldNode, "c");
                            checkVariableUsed(expr, true, fieldNode,true,flag);
                        } else if (userClassProcessing.isUserClassProcessing(fieldNode.getCompleteClassName())) {
                            returnType = "userDefinedClass";
                            String constant = handleConstant(declareExpression.toString());
                            type += constant + preserveSquareBracket(type1);
                            setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), type, type);
                            //setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Declaration", "Declaration");
                            boolean flag = addNode(node, "c");
                            if(flag) {
                                addVariableToNodeMap(variableName, node);
                            }
                            checkVariableUsed(expr, false, null,true,flag);
                            removeUserDefinedClassNode(fieldNode);
                        } else {
                            returnType = "int";//represent the return type of String[].length,int[].length etc..
                            boolean flag = addNode(fieldNode, "c");
                            checkVariableUsed(expr, true, fieldNode,true,flag);
                        }
                        dealMethodReturnType(n.getVariables().get(i).getName().toString(), returnType);
                    } else {
                        parsedFlag = false;//构造数据时得设置回去
                        //System.err.println(n + " can not be parsed");
                    }
                } else if (declareExpression instanceof EnclosedExpr) {
                    EnclosedExpr expr = (EnclosedExpr) declareExpression;
                    dealVariableDeclarationExpr(type1, variableName, node, expr.getInner(), n, i);
                } else if (declareExpression instanceof UnaryExpr) {
                    UnaryExpr expr = (UnaryExpr) declareExpression;
                    convert(expr);
                } else {
                    String constant = handleConstant(declareExpression.toString());
                    type += "Declaration" + constant + preserveSquareBracket(type1);
                    setNodeMethod(node, type, type);
                    addNode(node, "c");
                    addVariableToNodeMap(variableName, node);
                    verifyFlag = false;
                }
                returnType = null;
            } else {
                if (type1.contains("[")) {
                    setNodeMethod(node, "DeclarationArrayNull" + preserveSquareBracket(type1), "DeclarationArrayNull" + preserveSquareBracket(type1));
                } else {
                    setNodeMethod(node, "DeclarationNull", "DeclarationNull");
                }
                addNode(node, "c");
                addVariableToNodeMap(variableName, node);
                verifyFlag = false;
            }
        } else {
            if (type1.contains("[")) {
                setNodeMethod(node, "ArrayDeclaration" + preserveSquareBracket(type1), "ArrayDeclaration" + preserveSquareBracket(type1));
            } else {
                setNodeMethod(node, "Declaration", "Declaration");
            }
            addNode(node, "c");
            addVariableToNodeMap(variableName, node);
            verifyFlag = false;
        }
        if (!verifyFlag && !userClassProcessing.isUserClassProcessing(node.getCompleteClassName()) && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n.toString() + ": can not be parsed");
        }

    }

    protected void dealAssignExpr(String type, String variableName, String target, GraphNode node, Expression assignExpression) {
        boolean verifyFlag = true;
        if (assignExpression instanceof ObjectCreationExpr) {
            ObjectCreationExpr expr = (ObjectCreationExpr) assignExpression;
            setVariableType(target, getVariableType(type, true), expr.getType().toString());
            convert(expr);
        } else if (assignExpression instanceof MethodCallExpr) {
            GraphNode temp = lastNode;
            MethodCallExpr expr = (MethodCallExpr) assignExpression;
            convert(expr);
            if(temp != null && temp.equals(lastNode) && !userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                String constant = "";
                if (type.contains("[")) {
                    constant += "Array";
                }
                constant += handleConstant(constant);
                constant += preserveSquareBracket(type);
                setNodeMethod(node,constant,constant);
                addNode(node, "c");
                lastNode.setAssign(true);
                addVariableToNodeMap(variableName, node);
            }
            //dealMethodReturnType(target, returnType);
            returnType = null;
            //verifyFlag = false;
        } else if (assignExpression instanceof ArrayCreationExpr) {
            ArrayCreationExpr expr = (ArrayCreationExpr) assignExpression;
            setVariableType(target, getVariableType(type, true), expr.getElementType().toString());
            GraphNode tempNode = lastNode;
            convert(expr);
            lastNode.setAssign(true);
            setVariableDeclaration(tempNode, variableName);
            //verifyFlag = false;
        } else if (assignExpression.toString().equals("null")) {
            if (getVariableType(type, true) != null) {
                type = getVariableType(type, true);
                if (getVariableType(type, true).contains("[")) {
                    setNodeMethod(node, "ArrayNull" + preserveSquareBracket(type), "ArrayNull" + preserveSquareBracket(type));
                } else if (target.contains("[")) {
                    setNodeMethod(node, "ArrayNull" + preserveSquareBracket(target), "ArrayNull" + preserveSquareBracket(target));
                } else {
                    setNodeMethod(node, "Null", "Null");
                }
                addNode(node, "c");
                lastNode.setAssign(true);
                addVariableToNodeMap(variableName, node);
            } else {
                //parsedFlag = false;
                //System.err.println(n + " can not be parsed");
            }
            verifyFlag = false;
        } else if (assignExpression instanceof CastExpr) {
            CastExpr expr = (CastExpr) assignExpression;
            GraphNode tempNode = lastNode;
            convert(expr);
            if ((lastNode != null && lastNode.equals(tempNode)) || lastNode == null) {
                setNodeClassAndMethod(node, node.getClassName(), node.getCompleteClassName(), "Cast", "Cast");
                addNode(node, "c");
                lastNode.setAssign(true);
                addVariableToNodeMap(variableName, node);
            } else if (lastNode != null && !lastNode.equals(tempNode)) {
                if (expr.getExpression() instanceof MethodCallExpr ||
                        expr.getExpression() instanceof FieldAccessExpr ||
                        expr.getExpression() instanceof ObjectCreationExpr) {
                    //todo nothing
                } else {
                    lastNode.setAssign(true);
                    setVariableDeclaration(tempNode, variableName);
                }
            }
            setVariableType(target, getVariableType(type, true), expr.getType().toString());
            //verifyFlag = false;
        } else if (assignExpression instanceof ArrayAccessExpr) {
            ArrayAccessExpr expr = (ArrayAccessExpr) assignExpression;
            GraphNode tempNode = lastNode;
            convert(expr);
            lastNode.setAssign(true);
            setVariableDeclaration(tempNode, variableName);
            //dealMethodReturnType(target, returnType);
            returnType = null;
            //verifyFlag = false;
        } else if (assignExpression instanceof FieldAccessExpr) {
            GraphNode temp = lastNode;
            FieldAccessExpr expr = (FieldAccessExpr) assignExpression;
            convert(expr);
            if(temp != null && temp.equals(lastNode) && !userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
                String constant = "";
                if (type.contains("[")) {
                    constant += "Array";
                }
                constant += handleConstant(constant);
                constant += preserveSquareBracket(type);
                setNodeMethod(node,constant,constant);
                addNode(node, "c");
                lastNode.setAssign(true);
                addVariableToNodeMap(variableName, node);
            }
            //dealMethodReturnType(target, returnType);
            returnType = null;
            //verifyFlag = false;
        } else if (assignExpression instanceof EnclosedExpr) {
            EnclosedExpr expr = (EnclosedExpr) assignExpression;
            dealAssignExpr(type, variableName, target, node, expr.getInner());
        } else if (assignExpression instanceof UnaryExpr) {
            UnaryExpr expr = (UnaryExpr) assignExpression;
            convert(expr);
            //dealAssignExpr(type,variableName,target,node,expr.getExpr());
        } else if (assignExpression instanceof BinaryExpr) {
            boolean flag = true;
            flag = dealBinaryExprInVariableDeclarationAndAssignExpr(variableName, assignExpression, flag);
            binaryFlag = false;
            if (flag) {
                if (getVariableType(type, true) != null) {
                    type = getVariableType(type, true);
                    if (type.contains("[")) {
                        setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(type), "ArrayConstant" + preserveSquareBracket(type));
                    } else if (target.contains("[")) {
                        setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(target), "ArrayConstant" + preserveSquareBracket(target));
                    } else {
                        setNodeMethod(node, "Constant", "Constant");
                    }
                } else {
                    String constant = handleConstant(assignExpression.toString());
                    setNodeMethod(node, constant, constant);
                }
                addNode(node, "c");
                addVariableToNodeMap(variableName, node);
                verifyFlag = false;
            }
        } else {
            if (getVariableType(type, true) != null) {
                type = getVariableType(type, true);
                if (type.contains("[")) {
                    setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(type), "ArrayConstant" + preserveSquareBracket(type));
                } else if (target.contains("[")) {
                    setNodeMethod(node, "ArrayConstant" + preserveSquareBracket(target), "ArrayConstant" + preserveSquareBracket(target));
                } else {
                    setNodeMethod(node, "Constant", "Constant");
                }
            } else {
                String constant = handleConstant(assignExpression.toString());
                setNodeMethod(node, constant, constant);
            }
            addNode(node, "c");
            addVariableToNodeMap(variableName, node);
            verifyFlag = false;
        }
        if (!verifyFlag && !verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n.toString() + ": can not be parsed");
        }
    }

    protected void dealAssignExpr(AssignExpr n, GraphNode node) {
        String variableName = null;
        //处理value
        //boolean verifyFlag = true;
        String target = n.getTarget().toString();
        if (target.contains("[")) {
            String[] strs = target.split("\\[");
            target = strs[0];
            for (int length = 0; length < strs.length - 1; length++) {
                target += "[]";
            }
        }
        String type = "";
        String bracket = "";
        if (!class_variable_list.contains(target)) {
            if (class_variable.get(filterSquareBracket(target)) != null) {
                type = class_variable.get(filterSquareBracket(target));
                target = filterSquareBracket(target);
            } else if (class_variable.get(filterSquareBracket(target) + "[]") != null) {
                type = class_variable.get(filterSquareBracket(target) + "[]");
                target = filterSquareBracket(target) + "[]";
                bracket = "[]";
            } else if (class_variable.get(filterSquareBracket(target) + "[][]") != null) {
                type = class_variable.get(filterSquareBracket(target) + "[][]");
                target = filterSquareBracket(target) + "[][]";
                bracket = "[][]";
            } else {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n.toString() + " can not be parsed");
            }
        } else {
            type = class_variable.get(target);
        }
        variableName = target;
        tempViriableName = variableName;
        setGlobalStatementAndVariable(n.getBegin().get().line + " " + n.toString(), variableName, type + bracket);
        setNodeStatementAndVariable(node);
        node.setVariableName(globalVariableName, globalType, variableCount);
        setNodeClass(node, getVariableType(type, true), class_name_map.get(filterSquareBracket(getVariableType(type, true))));
        GraphNode judgeNode = lastNode;
        if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            checkVariableUsed(n.getValue(), false, null,true,true);
            dealAssignExpr(type, variableName, target, node, n.getValue());
            if (lastNode != null && !lastNode.equals(judgeNode) && userClassProcessing.isUserClassProcessing(lastNode.getCompleteClassName())) {
                if (variableNodeMap.get(variableName).contains(lastNode)) {
                    variableNodeMap.get(variableName).remove(lastNode);
                }
                if (lastNode.getParentNode() != null) {
                    lastNode.getParentNode().getChildNodes().remove(lastNode);
                    removeNode(lastNode);
                    lastNode = lastNode.getParentNode();
                } else {
                    lastNode = null;
                    graph.setRoot(null);//构造数据时需要设置回去
                }

            }
        } else {
            dealAssignExpr(type, variableName, target, node, n.getValue());
            if (lastNode != null && !lastNode.equals(judgeNode)) {
                addVariableToNodeMap(variableName, lastNode);
            }
        }
        globalFlag = true;
        tempViriableName = null;
    }

    protected void dealFieldAccessExpr(FieldAccessExpr n, GraphNode node) {
        Expression expression = n.getScope();
        dealFieldAccessExprScope(expression, n, node);
    }

    protected void dealUnaryExpr(UnaryExpr n, GraphNode node) {
        Expression expr = n.getExpression();
        String operator = n.getOperator().toString();
        //handel operator
        switch (operator) {
            case "posIncrement":
                operator = "++p";
                break;
            case "preIncrement":
                operator = "++";
                break;
            case "posDecrement":
                operator = "--p";
                break;
            case "preDecrement":
                operator = "--";
                break;
            default:
                operator = "UnaryExprOperator";
        }
        //handle expr
        if (expr instanceof ArrayAccessExpr) {
            dealArrayAccessExprVariableType((ArrayAccessExpr) expr, node);
            setNodeMethod(node, operator, operator);
        } else if (expr instanceof NameExpr) {
            setNodeClassAndMethod(node, getVariableType(class_variable.get(((NameExpr) expr).getName()), false), class_name_map.get(getVariableType(class_variable.get(((NameExpr) expr).getName()), false)), operator, operator);
        } else if (expr instanceof MethodCallExpr) {
            parsedFlag = false;//构造数据时得设置回去
        } else {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(n + " " + "can not be parsed");
        }
        node.setControl(false);
        node.setExit(false);
        if (userClassProcessing.isUserClassProcessing(node.getCompleteClassName())) {
            //addNode(userClassProcessing.createUnaryExprNode());
        } else {
            if (!verifyMethodNameAndParameterOfSpecial(node, node.getClassName())) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(n.toString() + ": can not be parsed");
            }
        }
    }

    protected void dealMethodExpr(MethodCallExpr n, GraphNode node) {
        Expression expr = n.getScope().isPresent() ? n.getScope().get() : null;
        List<Expression> list = n.getArguments();
        String methodName = n.getNameAsString();
        node.setControl(false);
        node.setExit(false);
        node.setVariableName(globalVariableName, globalType, variableCount);
        list = dealContinuedMethodCall(expr, methodName, list, node);
    }

    protected List<Expression> dealContinuedMethodCall(Expression n, String methodName, List<Expression> list, GraphNode methodNode) {
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setControl(false);
        node.setExit(false);
        List<Expression> args = new ArrayList<>();
        if (n != null) {
            /*get the type of scope*/
            if (n instanceof StringLiteralExpr) {
                setNodeClass(node, "String", "java.lang.String");
                methodNode.getArgumentList().add(n.toString());
            } else if (n instanceof CharLiteralExpr) {
                setNodeClass(node, "char", "char");
                methodNode.getArgumentList().add(n.toString());
            } else if (n instanceof IntegerLiteralExpr) {
                setNodeClass(node, "Integer", "java.lang.Integer");
                methodNode.getArgumentList().add(n.toString());
            } else if (n instanceof DoubleLiteralExpr) {
                setNodeClass(node, "Double", "java.lang.Double");
                methodNode.getArgumentList().add(n.toString());
            } else if (n instanceof BooleanLiteralExpr) {
                setNodeClass(node, "Boolean", "java.lang.Boolean");
                methodNode.getArgumentList().add(n.toString());
            } else if (n instanceof NameExpr) {
                if (class_name_map.containsKey(n.toString())) {
                    setNodeClass(node, n.toString(), class_name_map.get(n.toString()));
                    methodNode.getArgumentList().add(n.toString());
                } else {
                    String variableName = n.toString();
                    String className = new String("");
                    //variable_use_map.put(n.toString(),//variable_use_map.get(n.toString()) + 1);
                    if (class_variable.get(variableName) != null) {
                        className = class_variable.get(variableName);
                        className = getVariableType(className, false);
                        if (className.contains("<")) {
                            int index = className.indexOf("<");
                            className = className.substring(0, index);
                        }
                        setNodeClass(node, className, class_name_map.get(className));
                        methodNode.getArgumentList().add(variableName);
                    } else {
                        setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "Method", "Method");
                        //parsedFlag = false;
                    }
                }
            } else if (n instanceof ArrayAccessExpr) {
                ArrayAccessExpr expr = (ArrayAccessExpr) n;
                dealArrayAccessExprVariableType(expr, node);
                String[] strs = node.getCompleteClassName().split("\\.");
                setNodeClass(node, strs[strs.length - 1], node.getCompleteClassName());
                setNodeMethod(node, "", "");
                for(String s : node.getArgumentList()){
                    methodNode.getArgumentList().add(s);
                }
            } else if (n instanceof FieldAccessExpr) {
                FieldAccessExpr expr = (FieldAccessExpr) n;
                if (jdkList.contains(expr.toString())) {
                    parsedFlag = false;//构造数据时得设置回去
                } else {
                    dealFieldAccessExpr(expr, methodNode);
                    if (returnType != null) {
                        String[] strs = returnType.split("\\.");
                        setNodeClass(node, strs[strs.length - 1], returnType);
                        if (class_name_map.get(strs[strs.length - 1]) == null) {
                            class_name_map.put(strs[strs.length - 1], returnType);
                        }
                    } else {
                        returnType = "userDefinedClass";
                        setNodeClass(node, "userDefinedClass", "userDefinedClass");
                    }
                }
            } else if (n instanceof MethodCallExpr) {
                MethodCallExpr expr = (MethodCallExpr) n;
                List<Expression> tempList = dealContinuedMethodCall(expr.getScope().isPresent() ? expr.getScope().get() : null, expr.getNameAsString(), expr.getArguments(), methodNode);
                if (returnType != null) {
                    String[] strs = returnType.split("\\.");
                    setNodeClass(node, strs[strs.length - 1], returnType);
                    if (class_name_map.get(strs[strs.length - 1]) == null) {
                        class_name_map.put(strs[strs.length - 1], returnType);
                    }
                } else {
                    returnType = "userDefinedClass";
                    setNodeClass(node, "userDefinedClass", "userDefinedClass");
                }
            } else if (n instanceof EnclosedExpr) {
                Expression expr = ((EnclosedExpr) n).getInner();
                //methodName = null用来标记EnclosedExpr
                List<Expression> tempList = dealContinuedMethodCall(expr, methodName, list, methodNode);
                node = null;
            } else if (n instanceof ObjectCreationExpr) {
                GraphNode tempNode = lastNode;
                convert((ObjectCreationExpr) n);
                if (lastNode != null && !lastNode.equals(tempNode)) {
                    if (!methodNode.getDataDependency().contains(lastNode)) {
                        methodNode.getDataDependency().add(lastNode);
                    }
//                    for (GraphNode graphNode : lastNode.getDataDependency()) {
//                        if (!methodNode.getDataDependency().contains(graphNode)) {
//                            methodNode.getDataDependency().add(graphNode);
//                        }
//                    }
                    for (GraphNode graphNode : methodNode.getDataDependency()) {
                        boolean addNodeFlag = graph.addNode (graphNode, methodNode, mutexList, "d");
                    }
                    if (conditionFlag || assignFlag || variableDeclarationFlag) {
                        if (tempViriableName != null) {
                            objectCreationNode = lastNode;
                            if (lastNode != null) {
                                addVariableToNodeMap(tempViriableName, objectCreationNode);
                                if (!variableDeclarationFlag) {
                                    binaryNodeList.add(objectCreationNode);
                                }
                            }
                            objectCreationNode = null;
                            if (variableDeclarationFlag) {
                                binaryFlag = true;
                            }
                        } else {
                            parsedFlag = false;//构造数据时得设置回去
                        }
                    }
                    String type = getMethodReturnType(lastNode);
                    if (type != null) {
                        String[] strs = type.split("\\.");
                        String simpleType = strs[strs.length - 1];
                        setNodeClass(node, simpleType, type);
                        if (class_name_map.get(simpleType) == null) {
                            class_name_map.put(simpleType, returnType);
                        }
                    } else {
                        setNodeClass(node, "userDefinedClass", "userDefinedClass");
                    }
                } else {
                    setNodeClass(node, "userDefinedClass", "userDefinedClass");
                }

            } else {
                parsedFlag = false;//构造数据时得设置回去
            }
            /*get the type of each argument*/
            if (node != null) {
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        args.add(list.get(i));
                    }
                    String arguments = new String("");
                    if (methodName != null) {//当methodName == null时，表示这时候解析的是enclosedexpr，不需要将methodName加进去
                        if (node.getMethodName() != null && !node.getMethodName().equals("")) {
                            node.setMethodName(node.getMethodName() + "." + methodName + "(" + getArguments(list, arguments, methodNode) + ")");
                        } else {
                            node.setMethodName(methodName + "(" + getArguments(list, arguments, methodNode) + ")");
                        }
                    }
                } else {
                    if (methodName != null) {
                        if (node.getMethodName() != null && !node.getMethodName().equals("")) {
                            if (!(n.getParentNode().get() instanceof FieldAccessExpr)) {
                                node.setMethodName(node.getMethodName() + "." + methodName + "()");
                            } else {
                                node.setMethodName(node.getMethodName() + "." + methodName);
                            }
                        } else {
                            if (!(n.getParentNode().get() instanceof FieldAccessExpr)) {
                                node.setMethodName(methodName + "()");
                            } else {
                                node.setMethodName(methodName);
                            }
                        }
                    }
                }
            }
        } else {
            setNodeClassAndMethod(node, "userDefinedClass", "userDefinedClass", "Method", "Method");
        }
        //判断node是否是一个正确的API调用
        if (node != null) {
            //先处理node中的classname,判断是否为用户自定义类
            if (node.getCompleteClassName() != null) {
                String str = node.getCompleteClassName();
                str = str.replaceAll("\\[\\]", "");
                if (!jdkList.contains(str)) {
                    setNodeClass(node, "userDefinedClass", "userDefinedClass");
                }
            } else {
                parsedFlag = false;//构造数据时得设置回去
            }
            //判断是否为一个正确的API调用
            if (node.getCompleteClassName() != null && !node.getCompleteClassName().equals("userDefinedClass")) {
                if (methodNode.getClassName() == null) {
                    setNodeClass(methodNode, node.getClassName(), node.getCompleteClassName());
                }
                if (!verifyMethodNameAndParameter(node, list)) {
                    parsedFlag = false;//构造数据时得设置回去
                } else {
                    if (methodNode.getMethodName() == null) {
                        setNodeMethod(methodNode, node.getMethodName(), node.getCompleteMethodName());
                    } else {
                        setNodeMethod(methodNode, methodNode.getMethodName() + "." + node.getMethodName(), methodNode.getCompleteMethodName() + "." + node.getCompleteMethodName());
                    }
                    returnType = getMethodReturnType(node);
                }
            } else if (node.getCompleteClassName() == null) {
                parsedFlag = false;//构造数据时得设置回去
            } else if (node.getCompleteClassName().equals("userDefinedClass")) {
                //if (methodNode.getClassName() == null) {
                setNodeClass(methodNode, "userDefinedClass", "userDefinedClass");
                returnType = "userDefinedClass";
                //}
            } else {
                parsedFlag = false;//构造数据时得设置回去
            }
        }
        return args;
    }

    protected String getArguments(List<Expression> args, String arguments, GraphNode methodNode) {
        if (args.size() > 0) {
            for (int i = 0; i < args.size(); i++) {
                GraphNode judgeNode = lastNode;
                if (i > 0) {
                    arguments += ",";
                }
                if (args.get(i).getClass().getSimpleName().equals("StringLiteralExpr")) {
                    arguments += "String";
                } else if (args.get(i).getClass().getSimpleName().equals("CharLiteralExpr")) {
                    arguments += "char";
                } else if (args.get(i).getClass().getSimpleName().equals("IntegerLiteralExpr")) {
                    arguments += "int";
                } else if (args.get(i).getClass().getSimpleName().equals("DoubleLiteralExpr")) {
                    arguments += "double";
                } else if (args.get(i).getClass().getSimpleName().equals("BooleanLiteralExpr")) {
                    arguments += "boolean";
                } else if (args.get(i).getClass().getSimpleName().equals("UnaryExpr")) {
                    GraphNode node = new GraphNode();
                    setInfo(node);
                    addScope(node);
                    setNodeStatementAndVariable(node);
                    dealUnaryExpr((UnaryExpr) args.get(i), node);
                    arguments += node.getClassName();
                    //node.setVariableName("null",node.getCommonType(),node.getVariableCount());
                    node.setDataDependencyFlag("null");
                } else if (args.get(i).getClass().getSimpleName().equals("NameExpr")) {
                    if (class_variable.get(args.get(i).toString()) != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString()), true);
                        //variable_use_map.put(args.get(i).toString(),//variable_use_map.get(args.get(i).toString()) + 1);
                    } else if (class_variable.get(args.get(i).toString() + "[]") != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[]"), true);
                        //variable_use_map.put(args.get(i).toString() + "[]",//variable_use_map.get(args.get(i).toString() + "[]") + 1);
                    } else if (class_variable.get(args.get(i).toString() + "[][]") != null) {
                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[][]"), true);
                        //variable_use_map.put(args.get(i).toString() + "[][]",//variable_use_map.get(args.get(i).toString() + "[][]") + 1);
                    } else {
                        arguments += "null";
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("BinaryExpr")) {
                    if (((BinaryExpr) args.get(i)).getOperator().toString().equals("times") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("divide")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("remainder")) {
                        parsedFlag = false;//构造数据时得设置回去
                        //System.err.println(args.get(i) + " " + "can not be parsed");
                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("less") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("lessEquals")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greater") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greaterEquals")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("equals") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("notEquals")) {
                        arguments += "null";
                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("or") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("and")
                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("not")) {
                        arguments += "boolean";
                    } else {
                        BinaryExpr expr = (BinaryExpr) args.get(i);
                        List<String> typeList = new ArrayList<>();
                        List<Expression> rightExpressionList = new ArrayList<>();
                        rightExpressionList.add(expr.getRight());
                        String rightType = new String("");
                        rightType = getArguments(rightExpressionList, rightType, methodNode);
                        List<Expression> leftExpressionList = new ArrayList<>();
                        leftExpressionList.add(expr.getLeft());
                        String leftType = new String("");
                        leftType = getArguments(leftExpressionList, leftType, methodNode);
                        typeList.add(rightType);
                        typeList.add(leftType);
                        String binaryType = dealBinaryReturnType(typeList);
                        arguments += binaryType;
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("ConditionalExpr")) {
                    arguments += "null";
                } else if (args.get(i).getClass().getSimpleName().equals("EnclosedExpr")) {
                    EnclosedExpr expr = (EnclosedExpr) args.get(i);
                    List<Expression> expressionList = new ArrayList<>();
                    expressionList.add(expr.getInner());
                    arguments = getArguments(expressionList, arguments, methodNode);
                } else if (args.get(i).getClass().getSimpleName().equals("ArrayAccessExpr")) {
                    GraphNode node = new GraphNode();
                    setInfo(node);
                    addScope(node);
                    setNodeStatementAndVariable(node);
                    ArrayAccessExpr expr = (ArrayAccessExpr) args.get(i);
                    dealArrayAccessExprVariableType(expr, node);
                    arguments += filterSquareBracket(node.getClassName());
                    //node.setVariableName("null",node.getCommonType(),node.getVariableCount());
                    node.setDataDependencyFlag("null");
                } else if (args.get(i).getClass().getSimpleName().equals("CastExpr")) {
                    CastExpr expr = (CastExpr) args.get(i);
                    arguments += expr.getType();
                } else if (args.get(i).getClass().getSimpleName().equals("ObjectCreationExpr")) {
                    ObjectCreationExpr expr = (ObjectCreationExpr) args.get(i);
                    GraphNode tempNode = lastNode;
                    convert(expr);
                    if (lastNode != null && !lastNode.equals(tempNode)) {
                        if (tempViriableName != null) {
                            addVariableToNodeMap(tempViriableName, lastNode);
                            if (!variableDeclarationFlag) {
                                binaryNodeList.add(lastNode);
                            }
                        }
                        if (!userClassProcessing.isUserClassProcessing(methodNode.getCompleteClassName())) {
                            if (!methodNode.getDataDependency().contains(lastNode)) {
                                methodNode.getDataDependency().add(lastNode);
                            }
//                            for (GraphNode graphNode : lastNode.getDataDependency()) {
//                                if (!methodNode.getDataDependency().contains(graphNode)) {
//                                    methodNode.getDataDependency().add(graphNode);
//                                }
//                            }
                            for (GraphNode graphNode : methodNode.getDataDependency()) {
                                boolean addNodeFlag = graph.addNode (graphNode, methodNode, mutexList, "d");
                            }
                        }
                        //lastNode.setVariableName("null",lastNode.getCommonType(),lastNode.getVariableCount());
                        lastNode.setDataDependencyFlag("null");
                    }
                    arguments += expr.getType();
                } else if (args.get(i).getClass().getSimpleName().equals("MethodCallExpr")) {
                    GraphNode node = new GraphNode();
                    setInfo(node);
                    addScope(node);
                    setNodeStatementAndVariable(node);
                    node.setVariablePreserved(true);
                    MethodCallExpr n = (MethodCallExpr) args.get(i);
                    dealMethodExpr(n, node);
                    if (node.getCompleteClassName() != null && !node.getCompleteClassName().equals("userDefinedClass")) {
                        boolean flag = addNode(node, "c");
                        checkVariableUsed(n, true, node,true,flag);
                        if (tempViriableName != null) {
                            addVariableToNodeMap(tempViriableName, node);
                            if (!variableDeclarationFlag) {
                                binaryNodeList.add(node);
                            }
                        }
                        if (!userClassProcessing.isUserClassProcessing(methodNode.getCompleteClassName())) {
                            if (!methodNode.getDataDependency().contains(node)) {
                                methodNode.getDataDependency().add(node);
                            }
//                            for (GraphNode graphNode : node.getDataDependency()) {
//                                if (!methodNode.getDataDependency().contains(graphNode)) {
//                                    methodNode.getDataDependency().add(graphNode);
//                                }
//                            }
                            for (GraphNode graphNode : methodNode.getDataDependency()) {
                                boolean addNodeFlag = graph.addNode (graphNode, methodNode, mutexList, "d");
                            }
                        }
                        //node.setVariableName("null",node.getCommonType(),node.getVariableCount());
                        node.setDataDependencyFlag("null");
                    } else {
                        checkVariableUsed(n, false, null,true,true);
                    }
                    String methodReturnType = getMethodReturnType(node);
                    if (methodReturnType != null && methodReturnType.contains(".")) {
                        String[] strs = methodReturnType.split("\\.");
                        methodReturnType = strs[strs.length - 1];
                    }
                    if (methodReturnType != null) {
                        arguments += methodReturnType;
                    } else {
                        arguments += "null";
                    }
                } else if (args.get(i).getClass().getSimpleName().equals("FieldAccessExpr")) {
                    GraphNode node = new GraphNode();
                    setInfo(node);
                    addScope(node);
                    node.setVariablePreserved(true);
                    setNodeStatementAndVariable(node);
                    FieldAccessExpr n = (FieldAccessExpr) args.get(i);
                    dealFieldAccessExpr(n, node);
                    if (!node.getCompleteClassName().equals("userDefinedClass")) {
                        boolean flag = addNode(node, "c");
                        checkVariableUsed(n, true, node,true,flag);
                        if (tempViriableName != null) {
                            addVariableToNodeMap(tempViriableName, node);
                            if (!variableDeclarationFlag) {
                                binaryNodeList.add(node);
                            }
                        }
                        if (!userClassProcessing.isUserClassProcessing(methodNode.getCompleteClassName())) {
                            if (!methodNode.getDataDependency().contains(node)) {
                                methodNode.getDataDependency().add(node);
                            }
//                            for (GraphNode graphNode : node.getDataDependency()) {
//                                if (!methodNode.getDataDependency().contains(graphNode)) {
//                                    methodNode.getDataDependency().add(graphNode);
//                                }
//                            }
                            for (GraphNode graphNode : methodNode.getDataDependency()) {
                                boolean addNodeFlag = graph.addNode (graphNode, methodNode, mutexList, "d");
                            }
                        }
                        //node.setVariableName("null",node.getCommonType(),node.getVariableCount());
                        node.setDataDependencyFlag("null");
                    } else {
                        checkVariableUsed(n, false, null,true,true);
                    }
                    String methodReturnType = getMethodReturnType(node);
                    if (methodReturnType != null && methodReturnType.contains(".")) {
                        String[] strs = methodReturnType.split("\\.");
                        methodReturnType = strs[strs.length - 1];
                    }
                    if (methodReturnType != null) {
                        arguments += methodReturnType;
                    } else {
                        arguments += "null";
                    }
                } else {
                    arguments += "null";
                }
                if (lastNode != null && !lastNode.equals(judgeNode)) {
                    binaryFlag = true;
                }
            }
        }
        return arguments;
    }


//    protected String getArguments2(List<Expression> args, String arguments) {
//        if (args.size() > 0) {
//            for (int i = 0; i < args.size(); i++) {
//                if (i > 0) {
//                    arguments += ",";
//                }
//                if (args.get(i).getClass().getSimpleName().equals("StringLiteralExpr")) {
//                    arguments += "String";
//                } else if (args.get(i).getClass().getSimpleName().equals("CharLiteralExpr")) {
//                    arguments += "char";
//                } else if (args.get(i).getClass().getSimpleName().equals("IntegerLiteralExpr")) {
//                    arguments += "int";
//                } else if (args.get(i).getClass().getSimpleName().equals("DoubleLiteralExpr")) {
//                    arguments += "double";
//                } else if (args.get(i).getClass().getSimpleName().equals("BooleanLiteralExpr")) {
//                    arguments += "boolean";
//                } else if (args.get(i).getClass().getSimpleName().equals("UnaryExpr")) {
//                    GraphNode node = new GraphNode();
//                    setInfo(node);
//                    addScope(node);
//                    dealUnaryExpr((UnaryExpr) args.get(i), node);
//                    arguments += node.getClassName();
//                } else if (args.get(i).getClass().getSimpleName().equals("NameExpr")) {
//                    if (class_variable.get(args.get(i).toString()) != null) {
//                        arguments += getVariableType(class_variable.get(args.get(i).toString()), true);
//                        //variable_use_map.put(args.get(i).toString(),//variable_use_map.get(args.get(i).toString()) + 1);
//                    } else if (class_variable.get(args.get(i).toString() + "[]") != null) {
//                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[]"), true);
//                        //variable_use_map.put(args.get(i).toString() + "[]",//variable_use_map.get(args.get(i).toString() + "[]") + 1);
//                    } else if (class_variable.get(args.get(i).toString() + "[][]") != null) {
//                        arguments += getVariableType(class_variable.get(args.get(i).toString() + "[][]"), true);
//                        //variable_use_map.put(args.get(i).toString() + "[][]",//variable_use_map.get(args.get(i).toString() + "[][]") + 1);
//                    } else {
//                        arguments += "null";
//                    }
//                } else if (args.get(i).getClass().getSimpleName().equals("BinaryExpr")) {
//                    if (((BinaryExpr) args.get(i)).getOperator().toString().equals("times") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("divide")
//                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("remainder")) {
//                        parsedFlag = false;//构造数据时得设置回去
//                        //System.err.println(args.get(i) + " " + "can not be parsed");
//                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("less") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("lessEquals")
//                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greater") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("greaterEquals")
//                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("equals") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("notEquals")) {
//                        arguments += "null";
//                    } else if (((BinaryExpr) args.get(i)).getOperator().toString().equals("or") || ((BinaryExpr) args.get(i)).getOperator().toString().equals("and")
//                            || ((BinaryExpr) args.get(i)).getOperator().toString().equals("not")) {
//                        arguments += "boolean";
//                    } else {
//                        BinaryExpr expr = (BinaryExpr) args.get(i);
//                        List<String> typeList = new ArrayList<>();
//                        List<Expression> rightExpressionList = new ArrayList<>();
//                        rightExpressionList.add(expr.getRight());
//                        String rightType = new String("");
//                        rightType = getArguments2(rightExpressionList, rightType);
//                        List<Expression> leftExpressionList = new ArrayList<>();
//                        leftExpressionList.add(expr.getLeft());
//                        String leftType = new String("");
//                        leftType = getArguments2(leftExpressionList, leftType);
//                        typeList.add(rightType);
//                        typeList.add(leftType);
//                        String binaryType = dealBinaryReturnType(typeList);
//                        arguments += binaryType;
//                    }
//                } else if (args.get(i).getClass().getSimpleName().equals("ConditionalExpr")) {
//                    arguments += "null";
//                } else if (args.get(i).getClass().getSimpleName().equals("EnclosedExpr")) {
//                    EnclosedExpr expr = (EnclosedExpr) args.get(i);
//                    List<Expression> expressionList = new ArrayList<>();
//                    expressionList.add(expr.getInner());
//                    arguments = getArguments2(expressionList, arguments);
//                } else if (args.get(i).getClass().getSimpleName().equals("ArrayAccessExpr")) {
//                    GraphNode node = new GraphNode();
//                    setInfo(node);
//                    addScope(node);
//                    ArrayAccessExpr expr = (ArrayAccessExpr) args.get(i);
//                    dealArrayAccessExprVariableType(expr, node);
//                    arguments += filterSquareBracket(node.getClassName());
//                } else if (args.get(i).getClass().getSimpleName().equals("CastExpr")) {
//                    CastExpr expr = (CastExpr) args.get(i);
//                    arguments += expr.getCommonType();
//                } else if (args.get(i).getClass().getSimpleName().equals("ObjectCreationExpr")) {
//                    ObjectCreationExpr expr = (ObjectCreationExpr) args.get(i);
//                    //convert(expr);
//                    arguments += expr.getCommonType();
//                } else if (args.get(i).getClass().getSimpleName().equals("MethodCallExpr")) {
//                    GraphNode node = new GraphNode();
//                    setInfo(node);
//                    addScope(node);
//                    MethodCallExpr n = (MethodCallExpr) args.get(i);
//                    dealMethodExpr(n, node);
//                    String methodReturnType = getMethodReturnType(node);
//                    if (methodReturnType != null && methodReturnType.contains(".")) {
//                        String[] strs = methodReturnType.split("\\.");
//                        methodReturnType = strs[strs.length - 1];
//                    }
//                    if (methodReturnType != null) {
//                        arguments += methodReturnType;
//                    } else {
//                        arguments += "null";
//                    }
//                } else if (args.get(i).getClass().getSimpleName().equals("FieldAccessExpr")) {
//                    GraphNode node = new GraphNode();
//                    setInfo(node);
//                    addScope(node);
//                    FieldAccessExpr n = (FieldAccessExpr) args.get(i);
//                    dealFieldAccessExpr(n, node);
//                    String methodReturnType = getMethodReturnType(node);
//                    if (methodReturnType != null && methodReturnType.contains(".")) {
//                        String[] strs = methodReturnType.split("\\.");
//                        methodReturnType = strs[strs.length - 1];
//                    }
//                    if (methodReturnType != null) {
//                        arguments += methodReturnType;
//                    } else {
//                        arguments += "null";
//                    }
//                } else {
//                    arguments += "null";
//                }
//            }
//        }
//        return arguments;
//    }

    protected void setVariableType(String variable, String parentType, String childType) {
        String type;
        if (parentType != null) {
            if (parentType.equals(childType)) {
                type = parentType;
            } else {
                if ("userDefinedClass".equals(childType)) {
                    String tempType = parentType.replaceAll("\\[\\]", "");
                    if (!userClassProcessing.isUserClassProcessing(class_name_map.get(tempType))) {
                        type = parentType;
                    } else {
                        type = parentType + " " + childType;
                    }
                } else {
                    if("Object".equals(childType) || "Object[]".equals(childType) || "Object[][]".equals(childType)){
                        type = parentType;
                    }else {
                        type = parentType + " " + childType;
                    }
                }
            }
            class_variable.replace(variable, type);
        } else {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println("null exception");
        }
    }

    protected String getVariableType(String str, boolean isParameterFlag) {
        String type = null;
        if (str != null) {
            if (str.contains(" ")) {
                String[] types = str.split(" ");
                if (isParameterFlag) {
                    type = types[0];
                } else {
                    type = types[1];
                }
            } else {
                type = str;
            }
        } else {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println( "variable type  can not be parsed");
        }
        return type;
    }

    protected boolean verifyMethodNameAndParameterOfSpecial(GraphNode node, String className) {
        List<String> list = new ArrayList<>();
        ConstructVocabulary constructVocabulary = new ConstructVocabulary();
        constructVocabulary.addSpecialVocabulary(className, list);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null && list.get(i).equals(node.toString())) {
                return true;
            }
        }
        return false;
    }

    protected boolean verifyFieldAccess(GraphNode node) {
        //先判断是否为单独的FieldAccess
        MethodReflection methodReflection_1 = new MethodReflection();
        List<String> fieldAccessList = methodReflection_1.getAllCompleteStaticFields(class_name_map.get(node.getClassName()));
        for (int i = 0; i < fieldAccessList.size(); i++) {
            if (fieldAccessList.get(i).equals(node.getCompleteMethodDeclaration())) {
                return true;
            }
        }
        return false;
    }

    // this method is used to compare whether the node.toSting() is consistent with method declaration
    protected boolean verifyMethodNameAndParameter(GraphNode node, List<Expression> arguments) {
        MethodReflection methodReflection = new MethodReflection();
        List<String> methodDeclarationList = methodReflection.getAllMethodDeclaration(class_name_map.get(node.getClassName()));
        Map<String, List<String>> methodAndParameterTypeMap = methodReflection.getMethodAndParameterTypeMap();
        Map<String, List<String>> methodAndParameterCompleteTypeMap = methodReflection.getMethodAndParameterCompleteTypeMap();
        Map<String, String> simpleToCompleteNameMap = methodReflection.getSimpleToCompleteName(class_name_map.get(node.getClassName()));
        if (methodAndParameterTypeMap.get(node.toString()) == null) {
            String methodDeclaration = node.toString();
            List<String> matchedList = new ArrayList<>();
            methodDeclaration = methodDeclaration.substring(0, methodDeclaration.indexOf('('));
            for (int i = 0; i < methodDeclarationList.size(); i++) {
                int index = methodDeclarationList.get(i).indexOf('(');
                if (methodDeclaration.equals(methodDeclarationList.get(i))) {
                    matchedList.removeAll(matchedList);
                    matchedList.add(methodDeclarationList.get(i));
                    break;
                } else if (index != -1 && methodDeclaration.equals(methodDeclarationList.get(i).substring(0, index))) {
                    matchedList.add(methodDeclarationList.get(i));
                }
            }
            if (matchedList.size() == 1) {
                modifyMethodDeclarationOfNode(matchedList.get(0), node, simpleToCompleteNameMap);
                return true;
            } else if (matchedList.size() > 1) {
                String result = chooseTheCorrectMethodDeclaration(matchedList, arguments, node, methodAndParameterTypeMap, methodAndParameterCompleteTypeMap);
                if (result.equals("")) {
                    return false;
                } else {
                    modifyMethodDeclarationOfNode(result, node, simpleToCompleteNameMap);
                    return true;
                }
            } else {
                return false;
            }
        } else {
            modifyMethodDeclarationOfNode(node.toString(), node, simpleToCompleteNameMap);
            return true;
        }
    }

    //this method is used modify the method name and parameter in node methodName
    protected void modifyMethodDeclarationOfNode(String str, GraphNode node, Map<String, String> map) {
        int startIndex = str.indexOf('.') + 1;
        int endIndex = str.length();
        String methodNameAndArguments = str.substring(startIndex, endIndex);
        String completeNameAndArguments = map.get(str);
        setNodeMethod(node, methodNameAndArguments, completeNameAndArguments);
    }

    // this method is used to choose the method declaration that is the most likely to be the correct one
    protected String chooseTheCorrectMethodDeclaration(List<String> matchedList, List<Expression> arguments, GraphNode node,Map<String, List<String>> methodAndParameterTypeMap, Map<String, List<String>> methodAndParameterCompleteTypeMap) {
        String methodDeclaration = new String("");
        List<String> candidateMethodList = new ArrayList<>();
        //init the candidate list of possible matched method declarations
        for (int i = 0; i < matchedList.size(); i++) {
            candidateMethodList.add(matchedList.get(i));
        }
        String str = node.toString();
        int index = str.indexOf('(');
        str = str.substring(index + 1,str.length() - 1);
        String[] types = str.split(",");
        //judge the correct method declaration
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < matchedList.size(); i++) {
            List<String> parameterList = methodAndParameterTypeMap.get(matchedList.get(i));
            List<String> parameterCompleteList = methodAndParameterCompleteTypeMap.get(matchedList.get(i));
            if (parameterList.size() == arguments.size()) {
                int matchedTypeCount = 0;
                for (int j = 0; j < arguments.size(); j++) {
//                    List<Expression> list = new ArrayList<>();
//                    list.add(arguments.get(j));
//                    String parameterType = new String("");
//                    parameterType = getArguments2(list, parameterType);
                    String parameterType = types[j];
                    if (parameterList.get(j).equals(parameterType)) {
                        matchedTypeCount++;
                    } else if (judgeRelationshipOfCast(parameterType, parameterList.get(j))) {//judge whether exits cast relationship
                        matchedTypeCount++;
                    } else if (judgeRelationshipOfExtend(parameterType, parameterCompleteList.get(j))) {//judge whether is parent and child class relationship
                        matchedTypeCount++;
                    } else if (parameterType.equals("null")) {
                        matchedTypeCount++;
                    } else if (parameterList.get(j).equals("Object") || parameterList.get(j).equals("java.lang.Object")) {
                        matchedTypeCount++;
                    } else if (parameterType.equals("userDefinedClass")) {
                        matchedTypeCount++;
                    } else if (parameterType.equals("Object") || parameterType.equals("java.lang.Object")) {
                        matchedTypeCount++;
                    }
                }
                if (matchedTypeCount == 0) {
                    candidateMethodList.remove(matchedList.get(i));
                } else {
                    map.put(matchedList.get(i), matchedTypeCount);
                }
            } else {
                candidateMethodList.remove(matchedList.get(i));
            }
        }

        // choose the only correct one method declaration
        int lastCompletelyMatchedTypeCount = 0;
        for (int i = 0; i < candidateMethodList.size(); i++) {
            List<String> parameterList = methodAndParameterTypeMap.get(candidateMethodList.get(i));
            int completelyMatchedTypeCount = 0;
            for (int j = 0; j < arguments.size(); j++) {
//                List<Expression> list = new ArrayList<>();
//                list.add(arguments.get(j));
//                String parameterType = new String("");
//                parameterType = getArguments2(list, parameterType);
                String parameterType = types[j];
                if (parameterList.get(j).equals(parameterType)) {
                    completelyMatchedTypeCount++;
                }
            }
            if (completelyMatchedTypeCount == arguments.size()) {
                methodDeclaration = candidateMethodList.get(i);
                break;
            } else if (map.get(candidateMethodList.get(i)) == arguments.size()) {
                methodDeclaration = candidateMethodList.get(i);
                break;
            } else {
                if (completelyMatchedTypeCount > lastCompletelyMatchedTypeCount) {
                    methodDeclaration = candidateMethodList.get(i);
                    lastCompletelyMatchedTypeCount = completelyMatchedTypeCount;
                } else if (completelyMatchedTypeCount == lastCompletelyMatchedTypeCount) {
                    // the following code need to be discussed
                    if (map.get(methodDeclaration) != null) {
                        if (map.get(methodDeclaration) < map.get(candidateMethodList.get(i))) {
                            methodDeclaration = candidateMethodList.get(i);
                        }
                    } else if (map.get(methodDeclaration) == null) {
                        methodDeclaration = candidateMethodList.get(i);
                    } else if (!candidateMethodList.get(i).contains("null")) {
                        methodDeclaration = candidateMethodList.get(i);
                    }
                }
            }
        }
        if(methodDeclaration.equals("")){
            if(matchedList.size() > 0) {
                methodDeclaration = matchedList.get(0);
            }
        }
        return methodDeclaration;
    }

    protected boolean judgeRelationshipOfExtend(String childClassName, String parentClassName) {
        try {
            if (parentClassName.contains(".") && class_name_map.get(childClassName) != null && class_name_map.get(childClassName).contains(".") && !parentClassName.contains("[")) {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(class_name_map.get(childClassName));
                Class clazz2 = Thread.currentThread().getContextClassLoader().loadClass(parentClassName);
                if (clazz2.isAssignableFrom(clazz)) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            if (!(e instanceof ClassNotFoundException)) {
                parsedFlag = false;//构造数据时得设置回去
                //System.err.println(e.getMessage());
            }
        } catch (Error e) {
            parsedFlag = false;//构造数据时得设置回去
            //System.err.println(e.getMessage());
        }
        return false;
    }

    protected boolean judgeRelationshipOfCast(String sourceClassName, String objectClassName) {
        if (class_name_map.get(sourceClassName) != null && class_name_map.get(objectClassName) != null) {
            if (castMap.get(class_name_map.get(sourceClassName) + "2" + class_name_map.get(objectClassName)) != null) {
                return true;
            } else {
                return false;
            }
        } else if (class_name_map.get(sourceClassName) != null && class_name_map.get(objectClassName) == null) {
            if (castMap.get(class_name_map.get(sourceClassName) + "2" + objectClassName) != null) {
                return true;
            } else {
                return false;
            }
        } else if (class_name_map.get(sourceClassName) == null && class_name_map.get(objectClassName) != null) {
            if (castMap.get(sourceClassName + "2" + class_name_map.get(objectClassName)) != null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (castMap.get(sourceClassName + "2" + objectClassName) != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    protected void setNodeClassAndMethod(GraphNode node, String clazz, String completeClazz, String method, String completeMethod) {
        node.setClassName(clazz);
        node.setCompleteClassName(completeClazz);
        node.setMethodName(method);
        node.setCompleteMethodName(completeMethod);
    }

    protected void setNodeClass(GraphNode node, String clazz, String completeClazz) {
        node.setClassName(clazz);
        node.setCompleteClassName(completeClazz);
    }

    protected void setNodeMethod(GraphNode node, String method, String completeMethod) {
        node.setMethodName(method);
        node.setCompleteMethodName(completeMethod);
    }

    protected String filterSquareBracket(String type) {
        if (type != null && type.contains("[")) {
            int index = type.indexOf("[");
            type = type.substring(0, index);
        }
        return type;
    }

    protected String preserveSquareBracket(String type) {
        if (type.contains("[")) {
            int index = type.indexOf("[");
            type = type.substring(index, type.length());
        } else {
            type = "";
        }
        return type;
    }


    protected boolean isAllAnnotationStmt(List list, EndFlag endFlag) {
        boolean flag = true;
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i).toString();
            str = str.replaceAll("\n", "");
            str = str.replaceAll(" ", "");
            if (list.get(i) instanceof LineComment || list.get(i) instanceof BlockComment) {
                if ((str.equals("//hole") || str.startsWith("/*hole*/")) && holeFlag) {
                    endFlag.endFlag = false;
                    flag = false;
                } else {
                    continue;
                }
            } else {
                flag = false;
            }
        }
        return flag;
    }

    protected String handleConstant(String constant) {
        String result = "Constant";
        return result;
    }

    //used to check whether declared variable is used
    protected void checkVariableUsed(Expression n, boolean variablePreserved, GraphNode node, boolean addDependencyFlag, boolean flag) {
        if (!removeConditionNodeFlag) {
            if (n instanceof MethodCallExpr) {
                MethodCallExpr expr = (MethodCallExpr) n;
                checkVariableUsed(expr.getArguments(), variablePreserved, node, addDependencyFlag, flag);
                if (expr.getScope().isPresent()) {
                    List<Expression> list = new ArrayList<>();
                    list.add(expr.getScope().get());
                    checkVariableUsed(list, variablePreserved, node, addDependencyFlag, flag);
                }
            } else if (n instanceof FieldAccessExpr) {
                FieldAccessExpr expr = (FieldAccessExpr) n;
                if (expr.getScope() != null) {
                    List<Expression> list = new ArrayList<>();
                    list.add(expr.getScope());
                    checkVariableUsed(list, variablePreserved, node, addDependencyFlag, flag);
                }
            } else if (n instanceof NameExpr || n instanceof ArrayAccessExpr) {
                String arg = filterSquareBracket(n.toString());
                if (variableNodeMap.containsKey(arg)) {
                    updateVariableState(arg, variablePreserved);
                    addDataDependencyNode(variableNodeMap.get(arg), node);
                } else if (variableNodeMap.containsKey(arg + "[]")) {
                    updateVariableState(arg + "[]", variablePreserved);
                    addDataDependencyNode(variableNodeMap.get(arg + "[]"), node);
                } else if (variableNodeMap.containsKey(arg + "[][]")) {
                    updateVariableState(arg + "[][]", variablePreserved);
                    addDataDependencyNode(variableNodeMap.get(arg + "[][]"), node);
                } else {
                    if (variablePreserved) {
                        String variableName = judgeIsVariableInAllClassFieldAndMethodArgument(arg);
                        if (variableName != null && !usedClassFieldAndMethodArgumentVariable.contains(variableName) && !isVariableBelong2UserDefinedClass(variableName)) {
                            usedClassFieldAndMethodArgumentVariable.add(variableName);
                        }
                    }
                }
            } else if (n instanceof BinaryExpr) {
                Expression right = ((BinaryExpr) n).getRight();
                Expression left = ((BinaryExpr) n).getLeft();
                if (right != null) {
                    checkVariableUsed(right, variablePreserved, node,addDependencyFlag, flag);
                }
                if (left != null) {
                    checkVariableUsed(left, variablePreserved, node,addDependencyFlag, flag);
                }

            } else if (n instanceof EnclosedExpr) {
                EnclosedExpr expr = (EnclosedExpr) n;
                checkVariableUsed(expr.getInner(), variablePreserved, node,addDependencyFlag, flag);
            } else if (n instanceof ObjectCreationExpr) {
                ObjectCreationExpr expr = (ObjectCreationExpr) n;
                checkVariableUsed(((ObjectCreationExpr) n).getArguments(), variablePreserved, node, addDependencyFlag, flag);
            }
        }
    }

    //used to check whether declared variable is used
    protected void checkVariableUsed(List<Expression> args, boolean variablePreserved, GraphNode node, boolean addDataDependencyFlag, boolean flag) {
        if (!removeConditionNodeFlag) {
            if (args != null) {
                for (int i = 0; i < args.size(); i++) {
                    if (args.get(i) instanceof NameExpr || args.get(i) instanceof ArrayAccessExpr) {
                        String arg = filterSquareBracket(args.get(i).toString());
                        //System.err.println(arg + " " + node.getCompleteMethodDeclaration() + " " + addDataDependencyFlag);
                        if (variableNodeMap.containsKey(arg)) {
                            updateVariableState(arg, variablePreserved);
                            if(addDataDependencyFlag && flag) {
                                addDataDependencyNode(variableNodeMap.get(arg), node);
                            }
                        } else if (variableNodeMap.containsKey(arg + "[]")) {
                            updateVariableState(arg + "[]", variablePreserved);
                            if(addDataDependencyFlag && flag) {
                                addDataDependencyNode(variableNodeMap.get(arg + "[]"), node);
                            }
                        } else if (variableNodeMap.containsKey(arg + "[][]")) {
                            updateVariableState(arg + "[][]", variablePreserved);
                            if(addDataDependencyFlag && flag) {
                                addDataDependencyNode(variableNodeMap.get(arg + "[][]"), node);
                            }
                        } else {
                            if (variablePreserved) {
                                String variableName = judgeIsVariableInAllClassFieldAndMethodArgument(arg);
                                if (variableName != null && !usedClassFieldAndMethodArgumentVariable.contains(variableName) && !isVariableBelong2UserDefinedClass(variableName)) {
                                    usedClassFieldAndMethodArgumentVariable.add(variableName);
                                }
                            }
                        }
                    } else if (args.get(i) instanceof MethodCallExpr) {
                        MethodCallExpr expr = (MethodCallExpr) args.get(i);
                        checkVariableUsed(expr.getArguments(), variablePreserved, node, false, flag);
                        if (expr.getScope().isPresent()) {
                            List<Expression> list = new ArrayList<>();
                            list.add(expr.getScope().get());
                            checkVariableUsed(list, variablePreserved, node, false, flag);
                        }
                    } else if (args.get(i) instanceof FieldAccessExpr) {
                        FieldAccessExpr expr = (FieldAccessExpr) args.get(i);
                        if (expr.getScope() != null) {
                            List<Expression> list = new ArrayList<>();
                            list.add(expr.getScope());
                            checkVariableUsed(list, variablePreserved, node, false, flag);
                        }
                    } else if (args.get(i) instanceof BinaryExpr) {
                        Expression right = ((BinaryExpr) args.get(i)).getRight();
                        Expression left = ((BinaryExpr) args.get(i)).getLeft();
                        if (right != null) {
                            checkVariableUsed(right, variablePreserved, node, false, flag);
                        }
                        if (left != null) {
                            checkVariableUsed(left, variablePreserved, node, false, flag);
                        }
                    } else if (args.get(i) instanceof EnclosedExpr) {
                        EnclosedExpr expr = (EnclosedExpr) args.get(i);
                        checkVariableUsed(expr.getInner(), variablePreserved, node,false, flag);
                    } else if (args.get(i) instanceof ObjectCreationExpr) {
                        ObjectCreationExpr expr = (ObjectCreationExpr) args.get(i);
                        checkVariableUsed(expr, variablePreserved, node,false, flag);
                    }
                }
            }
        }
    }

    protected void setModifiedFalse(String variableName) {
        if (variableNodeMap.containsKey(variableName)) {
            for (int i = 0; i < variableNodeMap.get(variableName).size(); i++) {
                variableNodeMap.get(variableName).get(i).setModified(false);
            }
        }
    }

    //update declared variable state
    protected void updateVariableState(String variableName, boolean variablePreserved) {
        for (int i = 0; i < variableNodeMap.get(variableName).size(); i++) {
            if (variableNodeMap.get(variableName).get(i).isModified()) {
                variableNodeMap.get(variableName).get(i).setUsed(true);
                if (variablePreserved) {
                    variableNodeMap.get(variableName).get(i).setVariablePreserved(variablePreserved);
                }
            }
        }
    }


    //map declared variable to its related node
    protected void setVariableDeclaration(GraphNode node, String variableName) {
        if (lastNode != null && !lastNode.equals(node)) {
            addVariableToNodeMap(variableName, lastNode);
        }
    }

    //map declared variable to its related node
    protected void addVariableToNodeMap(String variableName, GraphNode node) {
        if (variableName != null) {
            node.setVariableDeclaration(true);
            //node.setVariableName(variableName,node.getCompleteClassName());
            node.setVariableName(globalVariableName, globalType, variableCount);
            node.setPrimitive(isPrimitive(node.getCompleteClassName()));
            if (assignFlag || binaryFlag || conditionAssignFlag) {
                node.setAssign(true);
            }
            if (variableNodeMap.containsKey(variableName)) {
                if (!variableNodeMap.get(variableName).contains(node) && !node.isAssign() && variableNodeMap.get(variableName).size() == 0) {
                    variableNodeMap.get(variableName).add(node);
                } else if (!variableNodeMap.get(variableName).contains(node) && !node.isAssign() && variableNodeMap.get(variableName).size() > 0) {
                    for (GraphNode graphNode : variableNodeMap.get(variableName)) {
                        removeList.add(graphNode);
                    }
                    variableNodeMap.get(variableName).removeAll(variableNodeMap.get(variableName));
                    variableNodeMap.get(variableName).add(node);
                } else if (!variableNodeMap.get(variableName).contains(node) && node.isAssign()) {
                    variableNodeMap.get(variableName).add(node);
                }
            } else {
                List<GraphNode> list = new ArrayList<>();
                list.add(node);
                variableNodeMap.put(variableName, list);
            }
        }
    }

    //用来判断结构体中是否都是无用需要移除的变量声明语句
    protected boolean isControlUnitWillBeEmpty(GraphNode node) {
        int totalCount = node.countNodeChildrenExcludeConditionAndEnd(node, new ArrayList<>());
        int removeCount = 0;
        if (node != null) {
            for (String key : variableNodeMap.keySet()) {
                List<GraphNode> list = variableNodeMap.get(key);
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).isPrimitive() && node.isContainRemoveNode(node, list.get(i), new ArrayList<>()) && !list.get(i).isVariablePreserved()) {
                            removeCount++;
                        }
                    }
                }
            }
            if (removeCount == totalCount) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    protected boolean dealBinaryExprInVariableDeclarationAndAssignExpr(String variableName, Expression declareExpression, boolean flag) {
        Expression left = declareExpression;
        while (left != null) {
            Expression right;
            if (left instanceof BinaryExpr) {
                right = ((BinaryExpr) left).getRight();
                left = ((BinaryExpr) left).getLeft();
            } else {
                right = left;
                left = null;
            }
            if (right instanceof MethodCallExpr) {
                GraphNode epxrNode = new GraphNode();
                setInfo(epxrNode);
                addScope(epxrNode);
                epxrNode.setVariableName(globalVariableName, globalType, variableCount);
                epxrNode.setVariablePreserved(true);
                setNodeStatementAndVariable(epxrNode);
                dealMethodExpr((MethodCallExpr) right, epxrNode);
                if (!"userDefinedClass".equals(epxrNode.getCompleteClassName())) {
                    //addNode(assignNew);
                    boolean fl = addNode(epxrNode, "c");
                    if(fl) {
                        addVariableToNodeMap(variableName, epxrNode);
                    }
                    //将API method作为variable declaration结点
                    //addVariableToNodeMap(variableName,epxrNode);
                    //预先一次判断这个API method是都可以保留
                    //judgeAndSetAPIPreserved(right,epxrNode);
                    checkVariableUsed((MethodCallExpr) right, true, epxrNode,true,fl);
                    flag = false;
                    binaryFlag = true;
                    binaryNodeList.add(epxrNode);
                    //break;
                } else {
                    checkVariableUsed((MethodCallExpr) right, false, null,true,true);
                    removeUserDefinedClassNode(epxrNode);
                }
            } else if (right instanceof FieldAccessExpr) {
                GraphNode epxrNode = new GraphNode();
                setInfo(epxrNode);
                addScope(epxrNode);
                epxrNode.setVariableName(globalVariableName, globalType, variableCount);
                epxrNode.setVariablePreserved(true);
                setNodeStatementAndVariable(epxrNode);
                dealFieldAccessExpr((FieldAccessExpr) right, epxrNode);
                if (!"userDefinedClass".equals(epxrNode.getCompleteClassName())) {
                    //addNode(assignNew);
                    boolean fl = addNode(epxrNode, "c");
                    if(fl) {
                        addVariableToNodeMap(variableName, epxrNode);
                    }
                    checkVariableUsed((FieldAccessExpr) right, true, epxrNode,true,fl);
                    //addVariableToNodeMap(variableName,epxrNode);
                    //judgeAndSetAPIPreserved(right,epxrNode);
                    flag = false;
                    binaryFlag = true;
                    binaryNodeList.add(epxrNode);
                    //break;
                } else {
                    checkVariableUsed((FieldAccessExpr) right, false, null,true,true);
                    removeUserDefinedClassNode(epxrNode);
                }
            } else if (right instanceof EnclosedExpr) {
                flag = dealBinaryExprInVariableDeclarationAndAssignExpr(variableName, ((EnclosedExpr) right).getInner(), flag);
            } else if (right instanceof BinaryExpr) {
                flag = dealBinaryExprInVariableDeclarationAndAssignExpr(variableName, right, flag);
            } else if (right instanceof ObjectCreationExpr) {
                ObjectCreationExpr expr = (ObjectCreationExpr) right;
                GraphNode tempNode = lastNode;
                convert(expr);
                if (lastNode != null && !lastNode.equals(tempNode)) {
                    addVariableToNodeMap(variableName, lastNode);
                    flag = false;
                    binaryFlag = true;
                    binaryNodeList.add(lastNode);
                }
            } else if (right instanceof CastExpr) {
                CastExpr expr = (CastExpr) right;
                if (expr.getExpression() instanceof MethodCallExpr ||
                        expr.getExpression() instanceof FieldAccessExpr ||
                        expr.getExpression() instanceof ObjectCreationExpr) {
                    GraphNode tempNode = lastNode;
                    convert(expr);
                    if (lastNode != null && !lastNode.equals(tempNode) && lastNode.getCompleteMethodDeclaration().endsWith(".Cast")) {
                        GraphNode parentNode = lastNode.getParentNode();
                        if (parentNode != null) {
                            lastNode.setParentNode(null);
                            parentNode.getChildNodes().remove(lastNode);
                            removeNode(lastNode);
                            lastNode = parentNode;
                        } else {
                            lastNode = null;
                            graph.setRoot(null);//构造数据时需要设置回去
                        }
                    } else if (lastNode != null && !lastNode.equals(tempNode)) {
                        addVariableToNodeMap(variableName, lastNode);
                        flag = false;
                        binaryFlag = true;
                        binaryNodeList.add(lastNode);
                    }
                }
            }
        }
        return flag;
    }

    protected String judgeIsVariableInAllClassFieldAndMethodArgument(String variableName) {
        variableName = filterSquareBracket(variableName);
        if (allClassFieldAndMethodArgumentVariable.contains(variableName)) {
            return variableName;
        } else if (allClassFieldAndMethodArgumentVariable.contains(variableName + "[]")) {
            return variableName + "[]";
        } else if (allClassFieldAndMethodArgumentVariable.contains(variableName + "[][]")) {
            return variableName + "[][]";
        } else {
            return null;
        }
    }

    public boolean isVariableBelong2UserDefinedClass(String variableName) {
        variableName = filterSquareBracket(variableName);
        if (allClassFieldAndMethodArgumentVariable.contains(variableName)) {
            String type = class_variable.get(variableName);
            String completeType = class_name_map.get(type);
            if(userClassProcessing.isUserClassProcessing(completeType)){
                return true;
            }else{
                return false;
            }
        } else if (allClassFieldAndMethodArgumentVariable.contains(variableName + "[]")) {
            String type = class_variable.get(variableName + "[]");
            String completeType = class_name_map.get(type);
            if(userClassProcessing.isUserClassProcessing(completeType)){
                return true;
            }else{
                return false;
            }
        } else if (allClassFieldAndMethodArgumentVariable.contains(variableName + "[][]")) {
            String type = class_variable.get(variableName + "[][]");
            String completeType = class_name_map.get(type);
            if(userClassProcessing.isUserClassProcessing(completeType)){
                return true;
            }else{
                return false;
            }
        } else {
            return true;
        }
    }

    protected boolean isPrimitive(String type) {
        if (type != null) {
            List<String> list = new ArrayList<>();
            list.add("short");
            list.add("byte");
            list.add("int");
            list.add("long");
            list.add("float");
            list.add("double");
            list.add("char");
            list.add("boolean");
            list.add("java.lang.String");
            for (int i = 0; i < list.size(); i++) {
                if (type.equals(list.get(i)) || type.equals(list.get(i) + "[]") || type.equals(list.get(i) + "[][]")) {
                    return true;
                }
            }
        }
        return false;
    }


    protected void addEndNode() {
//        GraphNode endNode = new GraphNode();
//        addScope(endNode);
//        setNodeClassAndMethod(endNode, "end", "end", "", "");
//        endNode.setAddMethodName(false);
//        if (lastNode.getCompleteClassName() != null) {
//            if (!lastNode.getCompleteClassName().equals("break") && !lastNode.getCompleteClassName().equals("continue")
//                    && !lastNode.getCompleteClassName().equals("return")) {
//                boolean addNodeFlag = graph.addNode (lastNode, endNode, mutexList, "c");
//                endParentNode = null;
//            }
//        }
    }

    protected void addConditionEndNode() {
//        GraphNode endNode = new GraphNode();
//        addScope(endNode);
//        setNodeClassAndMethod(endNode, "conditionEnd", "conditionEnd", "", "");
//        endNode.setAddMethodName(false);
//        if (lastNode.getCompleteClassName() != null) {
//            if (!lastNode.getCompleteClassName().equals("break") && !lastNode.getCompleteClassName().equals("continue")
//                    && !lastNode.getCompleteClassName().equals("return")) {
//                boolean addNodeFlag = graph.addNode (lastNode, endNode, mutexList, "c");
//            }
//        }
    }

    protected void addSpecialNode(String str) {
        GraphNode node = new GraphNode();
        addScope(node);
        setNodeClassAndMethod(node, str, str, "", "");
        node.setAddMethodName(false);
        boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, "c");
        lastNode = node;
    }

    protected boolean judgeConditionEnd(GraphNode node) {
        if (node.getChildNodes().size() > 0) {
            if (node.getChildNodes().get(0).getCompleteMethodDeclaration().equals("condition")
                    && (node.getChildNodes().get(1).getCompleteMethodDeclaration().equals("body")
                    ||node.getChildNodes().get(1).getCompleteMethodDeclaration().equals("then"))
                    && node.getChildNodes().get(0).getChildNodes().size() == 0
                    && node.getChildNodes().get(1).getChildNodes().size() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
//        if (node.getChildNodes().size() > 0) {
//            if (node.getChildNodes().get(0).getCompleteMethodDeclaration().equals("conditionEnd")) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return true;
//        }
    }


    protected void setGlobalStatementAndVariable(String statement, String variableName, String type) {
        if (globalFlag) {
            globalStatement = statement;
            globalVariableName = variableName;
            globalType = type;
            globalFlag = false;
        }
    }

    protected void dealHoleParentNode(GraphNode node) {
        if (holeParentNode != null) {
            while (holeParentNode != null) {
                if ((holeParentNode.isPrimitive() && !holeParentNode.isVariablePreserved())) {
                    holeParentNode = holeParentNode.getParentNode();
                } else {
                    break;
                }
            }
        }
//        if (holeParentNode != null) {
//            //System.out.println(node.getCompleteMethodDeclaration() + "  " + holeParentNode.getCompleteMethodDeclaration());
//        }
    }

    protected void addDataDependencyNode(List<GraphNode> list, GraphNode node) {
        if (list != null && node != null) {
            for (GraphNode tempNode : list) {
                if (!node.getDataDependency().contains(tempNode)) {
                    node.getDataDependency().add(tempNode);
                }
            }
            for (GraphNode tempNode : node.getDataDependency()) {
                if(tempNode.getVariableName() != null){
                    if(!tempNode.getDataDependencyFlag().equals("null")) {
                        boolean addNodeFlag = graph.addNode (tempNode, node, mutexList, "d");
                    }
                }
//                if(tempNode.getVariableName() != null){
//                    if(!tempNode.getVariableName().equals("null")) {
//                        boolean addNodeFlag = graph.addNode (tempNode, node, mutexList, "d");
//                    }
//                }
            }
        }
    }

    protected void setNodeStatementAndVariable(GraphNode node) {
//        if(node.getVariableName() != null){
//            globalVariableName = node.getVariableName();
//        }
//        if(node.getCommonType() != null){
//           globalType = node.getCommonType();
//        }
//        node.setVariableName(globalVariableName,globalType);
        node.setStatement(globalStatement);
    }

    protected boolean addNode(GraphNode node, String edgeType) {
        boolean addNodeFlag = graph.addNode (lastNode, node, mutexList, edgeType);
        if(addNodeFlag) {
            lastNode = node;
        }
        return addNodeFlag;
    }

    protected void removeNodeInControlStructure(GraphNode node, List<GraphNode> judgeList) {
        List<GraphNode> list = new ArrayList<>();
        list.add(node);
        while (list.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int index = 0; index < list.size(); index++) {
                if (!judgeList.contains(list.get(index))) {
                    judgeList.add(list.get(index));
                    List<GraphNode> parents = list.get(index).getParents();
                    for (int i = 0; i < parents.size(); i++) {
                        if (parents.get(i) != null) {
                            parents.get(i).getChildNodes().remove(list.get(index));
                            removeNode(list.get(index));
                        }
                    }
                    for (String key : variableNodeMap.keySet()) {
                        if (variableNodeMap.get(key).contains(list.get(index))) {
                            variableNodeMap.get(key).remove(list.get(index));
                        }
                    }
                    List<GraphNode> children = list.get(index).getChildNodes();
                    for (int i = 0; i < children.size(); i++) {
                        tempList.add(children.get(i));
                    }
                }
            }
            list.removeAll(list);
            list = tempList;
        }
    }

    protected void addScope(GraphNode node) {
        for (int i = 0; i < scopeIndexList.size(); i++) {
            if (!node.getScopeList().contains(scopeIndexList.get(i))) {
                node.getScopeList().add(scopeIndexList.get(i));
            }
        }
    }

    protected void addScopeIndex() {
        scopeIndex++;
        scopeIndexList.add(Integer.toString(scopeIndex));
    }

    protected void dealScope(GraphNode node, String controlName) {
        GraphNode tempNode = node;
        while (tempNode.getParentNode() != null) {
            if (tempNode.getParentNode().getCompleteMethodDeclaration().equals(controlName)) {
                mutexMap.get(tempNode.getParentNode()).add(Integer.toString(scopeIndex));
                List<String> list = mutexMap.get(tempNode.getParentNode());
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < list.size(); j++) {
                        if (i != j) {
                            if (!mutexList.contains(list.get(i) + list.get(j))) {
                                mutexList.add(list.get(i) + list.get(j));
                            }
                        }
                    }
                }
                break;
            } else {
                tempNode = tempNode.getParentNode();
            }
        }
    }

    private void setInfo(GraphNode node,SwitchEntry entry){
        String info = "";
        info = entry.getBegin().get().line + " " + entry.getEnd().get().line + " ";
        info += entry.toString();
        node.setInfo(info);
    }

    protected void setInfo(GraphNode node, Statement stmt) {
        String info = "";
        info = stmt.getBegin().get().line + " " + stmt.getEnd().get().line + " ";
        info += stmt.toString();
        node.setInfo(info);
    }

    protected void setInfo(GraphNode node) {
        if (lastInfo != null && !node.isControl() && !node.isCondition()) {
            node.setInfo(lastInfo);
        }
    }

    protected String constructInfo(Expression stmt) {
        String info = "";
        info = stmt.getBegin().get().line + " " + stmt.getEnd().get().line + " ";
        info += stmt.toString();
        info += ";";
        return info;
    }

    protected void removeNode(GraphNode node) {
        for (GraphNode parentNode : node.getParentNodes()) {
            parentNode.getChildNodes().remove(node);
        }
    }

    protected void removeUserDefinedClassNode(GraphNode node) {
        List<GraphNode> parents = node.getParents();
        for (GraphNode parent : parents) {
            if (parent != null) {
                parent.getChildNodes().remove(node);
                parent.getEdgeMap().remove(node);
            }
        }
        node.getParentNodes().clear();
        node.setParentNode(null);
    }

    protected void replaceHoleString() {
        GraphNode tempNode = lastNode;
        GraphNode node = new GraphNode();
        setInfo(node);
        addScope(node);
        node.setCompleteMethodDeclaration("hole");
        addNode(node, "unknown");
        //lastNode = tempNode;
        markHole = false;
    }
    protected void markHole(Statement n) {
        if (n.toString().contains("true == true") && n.getAllContainedComments() != null && n.getAllContainedComments().get(0).getContent().equals("hole")) {
            markHole = true;
        }
    }

}
