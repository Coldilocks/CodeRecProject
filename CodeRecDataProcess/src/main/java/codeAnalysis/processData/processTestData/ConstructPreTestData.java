package codeAnalysis.processData.processTestData;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import codeAnalysis.processData.ConstructData;
import config.DataConfig;
import javafx.util.Pair;
import utils.ConstructGraphUtil;
import utils.FileUtil;
import utils.GraphWriteUtil;
import utils.TestDataUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ConstructPreTestData extends ConstructData {

    private boolean isDebug = false;
    private boolean isShowGraph = false;
    // testcase trace
    private LinkedList<String> testcaseTraces;
    // testcase pool: <trace, testcase>
    private Map<String, String> testcasePool;
    private Map<Integer, String> sourceLines;
    private String trace;
    private String testcaseDir;
    private String blockStatement;
    private LinkedList<String> predictions;
    // test
    private int count = 0;
    private LinkedList<String> methodNames;
    private ConstructGraphUtil util = new ConstructGraphUtil();

    public void construct(Graph graph,
                          FileWriter graphWriter,
                          FileWriter predictionWriter,
                          FileWriter classWriter,
                          FileWriter generationNodeWriter,
                          FileWriter graphSentenceWriter,// 图的编号数组表示
                          FileWriter jarWriter,
                          FileWriter holeSizeWriter,
                          FileWriter traceWriter, // trace back
                          FileWriter blockPredictionWriter, // block of predictions (more lines)
                          FileWriter originalStatementsWriter, // original statements
                          FileWriter variableNamesWriter,// variable names
                          FileWriter linesWriter,// lines writer
                          FileWriter vocabWriter,// 新增的
                          FileWriter testCaseTraceWriter,
                          FileWriter methodNamesWriter,
                          String testcaseDir,
                          boolean isCompleteFlag) {
        this.testcaseDir = testcaseDir;
        LinkedList<LinkedList> result = getConstructTrainingData(graph);
        List<Graph> graphList = result.get(0);
        List<String> predictionList = result.get(1);
        List<String> classList = result.get(2);
        List<GraphNode> generationNodeList = result.get(3);
        List<String> holeSizeList = result.get(4);// record size of hole
        List<String> blockPredictionList = result.get(5);

        List<String> originalStatementsList = result.get(6);
        List<String> variableNameList = result.get(7);
        List<String> testCaseTraceList = result.get(8);
        List<String> methodNameList = result.get(9);
        
        //FindJarHandler findJarHandler = new FindJarHandler();
        for (int i = 0; i < graphList.size(); i++) {
            try {
                //String jar = findJarHandler.getPackage(classList.get(i));
                //if(jar != null) {
                Graph tempGraph = graphList.get(i);
                //operator.saveRegularizedGraphInFile(operator.regularization(tempGraph), graphWriter);
                Pair<String, String> graphRepresent = util.getGraphStr(tempGraph);
                if (graphRepresent != null) {
                    FileUtil.saveStringInFile(predictionList.get(i), predictionWriter);
                    FileUtil.saveStringInFile(classList.get(i), classWriter);
                    int parentnum = generationNodeList.get(i).getSerialNumber();
                    FileUtil.saveStringInFile(parentnum + " " + ((parentnum != 0) ? generationNodeList.get(i).getCompleteMethodDeclaration() : ""), generationNodeWriter);

                    //Pair<String, String> graphRepresent = getGraphStr(tempGraph);
                    FileUtil.saveStringInFile(graphRepresent.getKey(), graphSentenceWriter);// 记录图表示
                    FileUtil.saveStringInFile(graphRepresent.getValue(), vocabWriter);// 记录词汇表

                    FileUtil.saveStringInFile(holeSizeList.get(i), holeSizeWriter);

                    FileUtil.saveStringInFile(graph.getFunctionTrace(), traceWriter);
                    FileUtil.saveStringInFile(blockPredictionList.get(i), blockPredictionWriter);

                    FileUtil.saveStringInFile(originalStatementsList.get(i), originalStatementsWriter);
                    FileUtil.saveStringInFile(variableNameList.get(i), variableNamesWriter);

                    FileUtil.saveStringInFile((tempGraph.getLines(tempGraph.getRoot(), new ArrayList<>()) + 1) + "", linesWriter);
                    FileUtil.saveStringInFile(testCaseTraceList.get(i), testCaseTraceWriter);
                    FileUtil.saveStringInFile(methodNameList.get(i), methodNamesWriter);
                }
                //}
            } catch (Exception e) {
                // System.err.println(e.getMessage());
            } catch (Error e) {
                // System.err.println(e.getMessage());
            }
        }
    }


    /**
     * Construct training graph from the input code graph
     * Return the LinkedList with 2 list:
     * 1. The training graphs with some continuous holes;
     * 2. The predication holes correspondent to the training graphs;
     */
    public LinkedList<LinkedList> getConstructTrainingData(Graph completeGraph) {
        // Init
        graphs = new LinkedList<>();
        predictions = new LinkedList<>();
        classNames = new LinkedList<>();
        parents = new LinkedList<>();
        holeSize = new LinkedList<>();
        blockPredictions = new LinkedList<>();
        originalStatements = new LinkedList<>();
        variableNames = new LinkedList<>();
        methodNames = new LinkedList<>();
        testcaseTraces = new LinkedList<>();
        testcasePool = new HashMap<>(); // -- for test case
        sourceLines = new HashMap<>();

        // Construct
        int count = completeGraph.getTotalNumber(new ArrayList<>());
        for (int i = 2; i <= count; i++) {// construct from the second node
            construct(completeGraph, i);
        }

        // Return
        LinkedList<LinkedList> result = new LinkedList<>();
        result.addLast(graphs);
        result.addLast(predictions);
        result.addLast(classNames);
        result.addLast(parents);
        result.addLast(holeSize);
        result.addLast(blockPredictions);
        result.addLast(originalStatements);
        result.addLast(variableNames);
        result.addLast(testcaseTraces);
        result.addLast(methodNames);
        return result;
    }

    private void construct(Graph completeGraph, int serialNumber) {
        // Make the replica of the completeGraph to avoid destroying.
        Graph graph = util.copyGraph(completeGraph);
        graph.setSerialNumberofNode(graph.getRoot(), new ArrayList<>());

        // Initialize the blockPrediction
        blockPrediction = "";
        blockStatement = "";

        // The source code
        trace = TestDataUtil.getTrace(completeGraph);
        sourceLines = TestDataUtil.getSourceLines(trace);

        if (graph.getTotalNumber(new ArrayList<>()) >= serialNumber) {
            boolean isSuccess = true;
            List predicts = null;
            if (graph.getTotalNumber(new ArrayList<>()) == 1) {// only root node.
                isSuccess = false;
            } else if ((predicts = remove(graph, serialNumber)) == null
            ) {
                isSuccess = false;
            }
            if (isSuccess) {
                String testcase = TestDataUtil.convert2SourceCode(sourceLines);
                String predict = (String) predicts.get(0);
                if ((!testcasePool.containsValue(testcase)) || isSpecialLabel(predict)) { // real, success when testcase pool do not contains the test case
                    count++;
                    if (isDebug) {
                        System.out.println(count + ". " + predicts);
                    }
                    predictions.addLast((String) predicts.get(0));
                    graphs.addLast(graph);
                    classNames.addLast((String) predicts.get(1));
                    GraphNode generationNode = (GraphNode) predicts.get(2);
                    parents.addLast(generationNode);

                    // Add hole node
                    util.addHole(graph, generationNode);

                    // set as 1.
                    holeSize.addLast("1");
                    blockPredictions.addLast(blockPrediction.trim());
                    if (isDebug) {
                        System.out.println(blockPrediction);
                        // displayGraph(graph, true, blockPrediction);
                    }

                    blockStatement = "";

                    // statements
                    originalStatements.addLast((String) predicts.get(3));

                    // get All variable names.
                    List<String> tempVariableNames = getAllVariableNames(graph);
                    String variablename = tempVariableNames.size() > 0 ? tempVariableNames.get(0) : "";
                    for (int i = 1; i < tempVariableNames.size(); i++) {
                        variablename += " " + tempVariableNames.get(i);
                    }
                    // variable names
                    variableNames.addLast(variablename);

                    //get method name
                    List<String> tempMethodNames = graph.getAllMethodNamesList();
                    String methodname = tempMethodNames.size()>0?tempMethodNames.get(0):"";
                    for (int i = 1; i < tempMethodNames.size(); i++) {
                        methodname += " " + tempMethodNames.get(i);
                    }
                    // variable names
                    methodNames.addLast(methodname);

                    // extract the function
                    String methodinfo = graph.getMethodInfo();
                    int b = Integer.parseInt(methodinfo.split(" ")[0]);
                    int e = Integer.parseInt(methodinfo.split(" ")[1]);
                    String functionblock = TestDataUtil.convert2SourceCode(sourceLines, b, e);

                    // construct the testcase: testcase = sourceinfo + functionblock + }
                    String sourceinfo = graph.getSourceInfo();
                    testcase = sourceinfo + functionblock + "}";

                    // test case save
                    String testcaseTrace = saveTestCase(testcase, count, trace, b + "to" + e, "PreContext");
                    testcasePool.put(testcaseTrace,testcase);
                    // test case trace
                    testcaseTraces.addLast(testcaseTrace);
                    if (isShowGraph) {
                        try {
                            GraphWriteUtil.show(graph.getRoot(), DataConfig.PRE_TEST_OR_TRAIN_GRAPH_DATA_CONSTRUCT_PATH + count);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove the node with serialNumber in the code graph
     * Remove it, including its children(except the remain node, e.g control)
     * Find the first ancestor node that under control node, remove the next brothers of this ancestor
     * Return its complete method declaration
     */
    private LinkedList<Object> remove(Graph graph, int serialNumber) {
        LinkedList<Object> result = new LinkedList<>();

        GraphNode node = graph.getGraphNode(serialNumber, new ArrayList<>());
        if (node == null) {// Not exist
            return null;
        }

        String label = node.getCompleteMethodDeclaration();
        // tips: 当这个预测节点的condition部分没有API调用的时候呢 移除作为预测的control node.
        if(node.isControl()){
            if(isWithConditionNode(node.getCompleteMethodDeclaration())){
                if (node.getChildNodes().get(0).getChildNodes().size() == 0) {
                    label = label +"+false";
                }
            }
        }

        GraphNode parent = node.getParentNode();

        // 对catch和finally不做预测处理.
        if(label.equals("catch") || label.equals("finally")){
            return null;
        }
        String className = node.getCompleteClassName();
        String statement = node.getStatement();
        // record predictions of this node
        blockPrediction += " " + util.node2String(node, new ArrayList<>());
        constructTestCase(node,new ArrayList<>());
        // Find the first ancestor node that under control node
        GraphNode ancestor = findAncestorUnderControl(node);
        // Remove the next brother of this ancestor
        GraphNode elderAncestor;
        while (ancestor != null) {
            elderAncestor = ancestor.getParentNode();
            if (elderAncestor != null) {
                int index = elderAncestor.getChildNodes().indexOf(ancestor);
                int nextbrothernum = elderAncestor.getChildNodes().size() - (index + 1);
                for (int i = 1; i <= nextbrothernum; i++) {
                    if ("c".equals(elderAncestor.getEdgeMap().get(elderAncestor.getChildNodes().get(index + 1))) ||
                            "cd".equals(elderAncestor.getEdgeMap().get(elderAncestor.getChildNodes().get(index + 1)))) {
                        blockPrediction += " " + util.node2String(elderAncestor.getChildNodes().get(index + 1), new ArrayList<>());
                    }
                    constructTestCase(elderAncestor.getChildNodes().get(index + 1),new ArrayList<>());
                    util.clearDataEdgeFromNode(elderAncestor.getChildNodes().get(index + 1), new ArrayList<>());
                    elderAncestor.getChildNodes().remove(index + 1);
                }
            }
            ancestor = findAncestorUnderControl(elderAncestor);
        }

        // Remove the node
        if (parent != null) {
            parent.getChildNodes().remove(node);
            util.clearDataEdgeFromNode(node, new ArrayList<>());
        }

        // Reorder nodes
        //operator.setSerialNumberofEachNode(graph);
        graph.setSerialNumberofNode(graph.getRoot(), new ArrayList<>());
        result.addLast(label);
        result.addLast(filterSigns(className));

        // Reassign hole parent
        if (isReassignHoleParent(label)) {
            parent = node.getHoleParentNode();
        }

        if (parent == null) {
            parent = new GraphNode();
            parent.setSerialNumber(0);// the root do not have a parent
        }
        result.addLast(parent);
        result.addLast(statement);

        return result;
    }


    private String saveTestCase(String testcase, int count, String trace, String lines, String flag) {
        String path = testcaseDir + trace.replace("/", "_")+"_"+lines+"_"+count+"_"+flag+".java";
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(testcase);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private GraphNode findAncestorUnderControl(GraphNode node) {
        GraphNode parent = null;
        GraphNode tmpnode = node;
        while ((parent = tmpnode.getParentNode()) != null) {
            if (parent.isControl()) {
                return tmpnode;
            }
            tmpnode = parent;
        }
        return null;
    }

    private boolean isSpecialLabel(String label){
        List<String> l = Arrays.stream(specialLabel).collect(Collectors.toList());
        return l.contains(label);
    }


    // Replace the begin~end lines as holes to construct testcase, including all its children.
    private void constructTestCase(GraphNode node,ArrayList<GraphNode> list) {
        if(!list.contains(node)) {
            list.add(node);
            String scinfo = node.getInfo();
            if (scinfo == null || node.isCondition()) {
                return;
            }

            int begin = TestDataUtil.getBeginLine(node);
            int end = TestDataUtil.getEndLine(node);
            String stmt = TestDataUtil.getStmt(node);

            if ((!node.isControl()) && begin == end) {
                blockStatement = blockStatement + " " + TestDataUtil.replaceStmt(begin, stmt, sourceLines, " /*hole*/ ");
            } else {
                blockStatement = blockStatement + " " + TestDataUtil.replaceLine2Line(begin, end, sourceLines, " /*hole*/ ");
            }
            if (isDebug) {
                System.out.println("replacing " + node.getCompleteMethodDeclaration());
                System.out.println("b: " + begin + "; e: " + end);
                System.out.println(stmt);
                System.out.println();
                System.out.println(TestDataUtil.convert2SourceCode(sourceLines));
            }
            List<GraphNode> children = node.getChildNodes();
            for (int i = 0; i < children.size(); i++) {
                constructTestCase(children.get(i),list);
            }
        }
    }

}
