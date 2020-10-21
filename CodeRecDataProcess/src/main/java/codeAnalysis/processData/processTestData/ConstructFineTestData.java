package codeAnalysis.processData.processTestData;
import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import codeAnalysis.processData.ConstructData;
import config.DataConfig;
import javafx.util.Pair;
import utils.FileUtil;
import utils.GraphWriteUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import utils.TestDataUtil;

/**
 * 构建完整的测试数据
 */
public class ConstructFineTestData extends ConstructData{

    private String blockStatement;
    private Map<String, String> testcasePool;
    private Map<Integer,String> sourceLines;
    private String trace;
    private String testcaseDir;
    private LinkedList<String> testcaseTraces;
    private LinkedList<String> methodNames;


    public void construct(Graph graph,
                          FileWriter graphWriter,
                          FileWriter predictionWriter,
                          FileWriter classWriter,
                          FileWriter generationNodeWriter,
                          FileWriter graphSentenceWriter,// 图的编号数组表示
                          FileWriter jarWriter,
                          FileWriter holeSizeWriter,
                          FileWriter traceWriter, // trace back
                          FileWriter blockpredictionWriter, // block of predictions (more lines)
                          FileWriter originalStatementsWriter, // original statements
                          FileWriter variableNamesWriter,// variable names
                          FileWriter linesWriter,// lines writer
                          FileWriter vocabWriter,// 新增的
                          FileWriter testCaseTraceWriter,
                          FileWriter methodNamesWriter,
                          String testcaseDir,
                          boolean isCompleteFlag){
        this.testcaseDir = testcaseDir;
        LinkedList<LinkedList> result = getConstructTrainingData(graph);
        List<Graph> graphList = result.get(0);
        List<String> predictionList = result.get(1);
        List<String> classList = result.get(2);
        List<GraphNode> generationNodeList = result.get(3);
        List<String> holeSizeList = result.get(4);// record size of hole
        List<String> blockpredictionList = result.get(5);

        List<String> originalStatementsList = result.get(6);
        List<String> variableNameList = result.get(7);
        List<String> testCaseTraceList = result.get(8); // -- testcase trace
        List<String> methodNameList = result.get(9);
        //FindJarHandler findJarHandler = new FindJarHandler();
        for (int i = 0; i < graphList.size(); i++) {
            try {
                //String jar = findJarHandler.getPackage(classList.get(i));
                //if(jar != null) {
                Graph tempGraph = graphList.get(i);
                //operator.saveRegularizedGraphInFile(operator.regularization(tempGraph), graphWriter);
                Pair<String, String> graphRepresent = util.getGraphStr(tempGraph);
                if(graphRepresent != null) {
                    FileUtil.saveStringInFile(predictionList.get(i), predictionWriter);
                    FileUtil.saveStringInFile(classList.get(i), classWriter);
                    int parentnum = generationNodeList.get(i).getSerialNumber();
                    FileUtil.saveStringInFile(parentnum + " " + ((parentnum != 0) ? generationNodeList.get(i).getCompleteMethodDeclaration() : ""), generationNodeWriter);

                    //Pair<String, String> graphRepresent = getGraphStr(tempGraph);
                    FileUtil.saveStringInFile(graphRepresent.getKey(), graphSentenceWriter);// 记录图表示
                    FileUtil.saveStringInFile(graphRepresent.getValue(), vocabWriter);// 记录词汇表

                    FileUtil.saveStringInFile(holeSizeList.get(i), holeSizeWriter);

                    FileUtil.saveStringInFile(graph.getFunctionTrace(), traceWriter);
                    FileUtil.saveStringInFile(blockpredictionList.get(i), blockpredictionWriter);

                    FileUtil.saveStringInFile(originalStatementsList.get(i), originalStatementsWriter);
                    FileUtil.saveStringInFile(variableNameList.get(i), variableNamesWriter);

                    FileUtil.saveStringInFile((tempGraph.getLines(tempGraph.getRoot(),new ArrayList<>()) + 1) + "", linesWriter);
                    FileUtil.saveStringInFile(testCaseTraceList.get(i),testCaseTraceWriter);
                    FileUtil.saveStringInFile(methodNameList.get(i), methodNamesWriter);
                }
                //}
            } catch (Exception e) {
                //System.err.println(e.getMessage());
            } catch (Error e){
                //System.err.println(e.getMessage());
            }
        }
    }

    public LinkedList<LinkedList> getConstructTrainingData(Graph completeGraph){
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
        //MAX_HOLENUM = completeGraph.getTotalNumber(); // set max hole number
        for (int i = 1; i <= MAX_HOLENUM; i++) {
            construct(completeGraph, 1, i);// the serial number of root is 1.
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
    
    
    
    /**
     * @param completeGraph: training data from this graph
     * @param serialNumber: the first node to be remove is with this serial number
     * @param holeNumber: the number of continuous holes is holeNumber
     * */
    private void construct(Graph completeGraph, int serialNumber, int holeNumber){
        // Make the replica of the completeGraph to avoid destroying.
        Graph graph;
        graph = util.copyGraph(completeGraph);
        graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());

        // Initialize the blockPrediction
        blockPrediction  = "";
        blockStatement = "";

        // The source code
        trace = TestDataUtil.getTrace(completeGraph);
        sourceLines = TestDataUtil.getSourceLines(trace);

        if(holeNumber > 0 && graph.getTotalNumber(new ArrayList<>()) >= serialNumber){
            boolean isSuccess = true;
            List predicts = null;
            GraphNode node =  graph.getGraphNode(serialNumber,new ArrayList<>());
            GraphNode constrainParent = null;
            if(node != null){
                constrainParent = node.getControlParentNode();
            }
            int loopSerialNumber = serialNumber;
            int i = 0;
            for (i = 0; i < holeNumber; i++) {
                if(graph.getTotalNumber(new ArrayList<>()) == 1){// only root node.
                    isSuccess = false;
                    break;
                }
                List temp = null;
                if(i == 0){
                    if((predicts = remove(graph,constrainParent,loopSerialNumber, true)) == null
                    ){
                        isSuccess = false;
                        break;
                    }
                }
                else if((temp = remove(graph,constrainParent,loopSerialNumber, false)) == null){
                    isSuccess = false;
                    break;
                }
                // Consider the condition case will change the serial number!(remove end, -1)
                if(i == 0){
                    loopSerialNumber = Integer.parseInt(predicts.get(3).toString());
                }
                else if (temp != null){
                    loopSerialNumber = Integer.parseInt(temp.get(3).toString());
                }
            }
            if(isSuccess){
                String testcase = TestDataUtil.convert2SourceCode(sourceLines);
                String predict = (String) predicts.get(0);
                if((!testcasePool.containsValue(testcase)) || isSpecialLabel(predict)) { // real, success when testcase pool do not contains the test case
                    // For debug
                    count++;
                    predictions.addLast((String) predicts.get(0));
                    graphs.addLast(graph);
                    classNames.addLast((String) predicts.get(1));
                    GraphNode generationNode = (GraphNode) predicts.get(2);
                    parents.addLast(generationNode);

                    holeSize.addLast("" + holeNumber);
                    blockPredictions.addLast(blockPrediction.trim());
                    blockStatement = "";

                    // statements
                    originalStatements.addLast((String) predicts.get(4));

                    // get All variable names.
                    List<String> tempVariableNames = getAllVariableNames(graph);
                    String variablename = tempVariableNames.size()>0?tempVariableNames.get(0):"";
                    for (int k = 1; k < tempVariableNames.size(); k++) {
                        variablename += " " + tempVariableNames.get(k);
                    }
                    // variable names
                    variableNames.addLast(variablename);

                    //get method name
                    List<String> tempMethodNames = graph.getAllMethodNamesList();
                    String methodname = tempMethodNames.size()>0?tempMethodNames.get(0):"";
                    for (int k = 1; k < tempMethodNames.size(); k++) {
                        methodname += " " + tempMethodNames.get(k);
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
                    String testcaseTrace = saveTestCase(testcase, count, trace, b + "to" + e, "FineGrain");
                    testcasePool.put(testcaseTrace,testcase);
                    // test case trace
                    testcaseTraces.addLast(testcaseTrace);
                    if (isShowGraph) {
                        try {
                            GraphWriteUtil.show(graph.getRoot(), DataConfig.FINE_TEST_GRAPH_DATA_CONSTRUCT_PATH + count);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }

            }
            serialNumber++;
            construct(completeGraph, serialNumber, holeNumber);
        }
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


    /**
     * Remove the node with serialNumber in the code graph
     * Set its parent as its children's parent
     * Return its complete method declaration, classname, and the parent node
     *
     * @param graph: the code graph
     * @param constrainParent: the parent of this node
     * @param serialNumber: the serial number of this node
     * */
    private LinkedList<Object> remove(Graph graph, GraphNode constrainParent, int serialNumber, boolean isAddHole){
        LinkedList<Object> result = new LinkedList<>();

        GraphNode node =  graph.getGraphNode(serialNumber,new ArrayList<>());
        if(node == null){// Not exist
            return null;
        }
        // 考虑这个位置已经是个hole node的情况.
        else if(node.getClassName().equals("hole")){
            // 找到下一个remove的位置.
            if(node.getChildNodes() != null && node.getChildNodes().size() > 0){
                node = graph.getGraphNode(node.getChildNodes().get(0).getSerialNumber(),new ArrayList<>());
            }
            else {
                // sibling
                GraphNode tmpParent = node.getControlParentNode();
                if(tmpParent != null) {
                    List<GraphNode> parentschildren = tmpParent.getChildNodes();
                    if (parentschildren.size() > 0) {
                        int tmpIndex = parentschildren.indexOf(node);
                        if (tmpIndex < parentschildren.size() - 1) {
                            node = parentschildren.get(tmpIndex + 1);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
                else {
                    return null;
                }
            }
        }

        String label = node.getCompleteMethodDeclaration();
        // tips: 当这个预测节点的condition部分没有API调用的时候呢 移除作为预测的control node.
        if(isAddHole && node.isControl()){
            if(isWithConditionNode(node.getCompleteMethodDeclaration())){
                if (node.getChildNodes().get(0).getChildNodes().size() == 0) {
                    label = label+"+false";
                }
            }
        }

        GraphNode parent = node.getControlParentNode();
        GraphNode holeNode = util.createHoleNode();

        // 考虑parent可以为null的情况 [[[
        if(parent != null && !parent.equals(constrainParent) && !parent.getClassName().equals("hole")){
            return null;// Stop remove when the parent is not the constrain one
        }
        // ]]]

        List<GraphNode> parents = node.getParentNodes();
        List<GraphNode> children = node.getChildNodes();
        String className = node.getCompleteClassName();

        // 对catch和finally不做预测处理.
        if(label.equals("catch") || label.equals("finally")){
            return null;
        }

        String statement = node.getStatement();

        // When removing a node, add the block predictions
        blockPrediction += getBlockPredictions(node);
        constructTestCase(node,false, new ArrayList<>());

        // leaf node / inner node with end mark
        if (children.isEmpty() || children.get(0).getCompleteMethodDeclaration().equals(ENDMARK)) {
            int tmpIndex = -1;
            if(parent != null && isAddHole){
                tmpIndex = parent.getChildNodes().indexOf(node);
            }

            if (parents != null) {
                for (GraphNode p : parents) {// 切断node的parent与node的联系
                    p.getChildNodes().remove(node);// 切断点联系
                    p.getEdgeMap().remove(node);// 切断边联系
                }
            }

            if(tmpIndex < 0){
                return null;
            }
            if(isAddHole){
                if(tmpIndex > 0) {
                    if(tmpIndex >= parent.getChildNodes().size()){
                        parent.getChildNodes().add(holeNode);
                    }
                    else {
                        parent.getChildNodes().set(tmpIndex, holeNode);
                    }
                }
                else if(tmpIndex == 0){
                    parent.getChildNodes().add(holeNode);
                }
                holeNode.setParentNode(parent);
                parent.getEdgeMap().put(holeNode, "unknown");
            }
        }
        else { // root with children / inner node
            // Control node
            if (node.isControl()) {// get the successor to append to the parent
                children = new ArrayList<GraphNode>();
                GraphNode successor = getSuccessor(node);
                if(successor != null){
                    children.add(successor);
                }
            }

            List<GraphNode> transChildren = new ArrayList<>();// 记录要移植的child

            // set parent
            for (GraphNode child : children) {
                child.getParentNodes().remove(node);// 切断child与node的点联系
                // 再根据node到child的边联系，决定是否将该孩子移植到parent上
                if(!node.getEdgeMap().get(child).equals("d")){// 如果这个child跟node是数据流之外的边联系，则需要移植
                    if(!isAddHole) {
                        child.setParentNode(parent);// 把node的parent设为child的parent
                    }
                    else{
                        child.setParentNode(holeNode);
                    }
                    transChildren.add(child);
                }
            }

            // set children
            if (parent != null) {
                // the index of node
                int index = parent.getChildNodes().indexOf(node);//这里默认只有一个successor节点移植过来咯

                if(index < 0){
                    return null;
                }

                if (transChildren.size() > 0) { // transplant
                    if(!isAddHole) {
                        // 这里分为两种情况:
                        // 1.successor节点与parent没有数据流依赖,此时将node所在的位置转换为successor节点
                        if (!parent.getChildNodes().contains(transChildren.get(0))) {
                            // transplant the child to the parent
                            parent.getChildNodes().set(index, transChildren.get(0));
                            // 此时parent与child之间的新边定为c
                            if(!parent.getClassName().equals("hole")) {
                                parent.getEdgeMap().put(transChildren.get(0), "c");
                            }
                            else {
                                parent.getEdgeMap().put(transChildren.get(0), "unknown");
                            }
                        }
                        // 2.successor节点与parent原本就存在数据流依赖,此时,移除node节点
                        //   同时,将原本存在的数据流依赖边d替换为cd类型(控制流+数据流)
                        else {
                            // transplant the child to the parent
                            parent.getChildNodes().remove(node);
                            // 此时parent与child之间的新边定为c
                            parent.getEdgeMap().put(transChildren.get(0), "cd");
                        }
                    }
                    else {
                        parent.getChildNodes().set(index, holeNode);
                        holeNode.setParentNode(parent);
                        parent.getEdgeMap().put(holeNode, "unknown");

                        holeNode.getChildNodes().add(transChildren.get(0));
                        transChildren.get(0).setParentNode(holeNode);
                        holeNode.getEdgeMap().put(transChildren.get(0), "unknown");
                    }
                }
                else{// moving-brothers
                    if(!isAddHole) {
                        if (index != -1) {
                            for (int i = index; i < parent.getChildNodes().size() - 1; i++) {
                                GraphNode bro = parent.getChildNodes().get(i + 1);
                                parent.getChildNodes().set(i, bro);
                            }
                            GraphNode cn = parent.getChildNodes().get(parent.getChildNodes().size() - 1);
                            parent.getChildNodes().remove(cn);
                        }
                        // 此时parent与bro之间的边类型不变
                    }
                    else {
                        parent.getChildNodes().set(index, holeNode);
                        holeNode.setParentNode(parent);
                        parent.getEdgeMap().put(holeNode, "unknown");
                    }
                }
                // 移除旧边
                parents = node.getParents();
                for (GraphNode p: parents) {
                    p.getEdgeMap().remove(node);
                    p.getChildNodes().remove(node);
                }
            } else {// root
                if(transChildren.size() > 0 && !transChildren.get(0).getCompleteMethodDeclaration().equals("else") && !transChildren.get(0).getCompleteMethodDeclaration().equals("elseif")) {
                    if(!isAddHole) {
                        graph.setRoot(transChildren.get(0));
                    }
                    else {
                        holeNode.getChildNodes().add(transChildren.get(0));
                        transChildren.get(0).setParentNode(holeNode);
                        holeNode.getEdgeMap().put(transChildren.get(0), "unknown");
                        graph.setRoot(holeNode);
                    }                }
                else{
                    graph.setRoot(new GraphNode());
                    return null;// the graph is not exist now.
                }
            }
        }

        // Reorder nodes
        graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());
        result.addLast(label);
        result.addLast(filterSigns(className));

        if(parent == null) {
            parent = new GraphNode();
            parent.setSerialNumber(0);// the root do not have a parent
        }
        result.addLast(parent);
        result.addLast(serialNumber);// set the serial number

        result.addLast(statement);
        return result;
    }

    // Get multi-lines of predictions when removing this node
    private String getBlockPredictions(GraphNode node) {
        String buff = "";

        // record label of the first node
        String label = node.getCompleteMethodDeclaration();
        buff += " " + label;

        // take the nodes inner control-node into consideration
        if(node.isControl()){
            // append body/then part
            if(label.equals("if") && node.getChildNodes().size() > 1){
                buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>()); // condition
                buff += " " + util.node2String(node.getChildNodes().get(1), new ArrayList<>()); // then
                if(node.getChildNodes().size() > 2) {
                    String secondChildLabel = node.getChildNodes().get(2).getCompleteMethodDeclaration();
                    if (secondChildLabel.equals("else") || secondChildLabel.equals("elseif")) {
                        buff += " " + util.node2String(node.getChildNodes().get(2), new ArrayList<>());
                    }
                }
            }
            else if(label.equals("else")){
                if(node.getChildNodes().size() > 0) {
                    buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>());
                }
            }
            else if(label.equals("elseif")){
                buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>()); // condition
                if(node.getChildNodes().size() > 1) {
                    buff += " " + util.node2String(node.getChildNodes().get(1), new ArrayList<>());
                }
            }
            else if (label.equals("for") || label.equals("while") || label.equals("foreach") || label.equals("doWhile")) {
                buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>()); // condition
                if(node.getChildNodes().size() > 1) {// body
                    buff += " " + util.node2String(node.getChildNodes().get(1), new ArrayList<>());
                }
            }
            else if (label.equals("switch")){
                buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>()); // condition
                for (int i = 0; i < node.getChildNodes().size(); i++) {
                    String childLabel = node.getChildNodes().get(i).getCompleteMethodDeclaration();
                    if(childLabel.equals(CASEMARK) || childLabel.equals(DEFAULTMARK)){
                        buff += " " + util.node2String(node.getChildNodes().get(i), new ArrayList<>());
                    }
                }
            }
            else if (label.equals("case") || label.equals("default")){
                if(node.getChildNodes().size() > 0) {
                    buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>());
                }
            }
            else if (label.equals("try")){ // try (xxx) catch xxx catch xxx finally xxx (successor)
                for (int i = 0; i < node.getChildNodes().size(); i++) {
                    String childLabel = node.getChildNodes().get(i).getCompleteMethodDeclaration();
                    if(childLabel.equals("catch") || childLabel.equals("finally")){
                        buff += " " + util.node2String(node.getChildNodes().get(i), new ArrayList<>());
                    }
                    else if(i != (node.getChildNodes().size() - 1)){
                        buff += " " + util.node2String(node.getChildNodes().get(i), new ArrayList<>());
                    }
                }
            }
            else if (label.equals("catch")){// catch xxx
                if(node.getChildNodes().size() > 0) {
                    buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>());
                }
            }
            else if (label.equals("finally")){// finally xxx
                if(node.getChildNodes().size() > 0) {
                    buff += " " + util.node2String(node.getChildNodes().get(0), new ArrayList<>());
                }
            }
        }
        return buff;
    }

    /**
     * 获取node节点(控制节点)的后继节点successor
     * 根据控制结构的不同,寻找对应控制结构的后继节点successor
     * 寻找过程中剔除的节点需进行{clearDataEdgeFromNode}操作
     * */
    private GraphNode getSuccessor(GraphNode node){
        String label = node.getCompleteMethodDeclaration();
        List<GraphNode> children = node.getChildNodes();
        if(label.equals("if")){
            if(children.size() == 4) {// There exist successor part
                for (int i = 0; i < 3; i++) {
                    util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
                }
                return children.get(3);
            }
            else if(children.size() == 3){
                String childLabel = children.get(2).getCompleteClassName();
                if(childLabel.equals("else") || childLabel.equals("elseif")){// "false" part
                    for (int i = 0; i < 3; i++) {
                        util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
                    }
                    return null;// no successor part
                }
                else{// There exist successor part
                    for (int i = 0; i < 2; i++) {
                        util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
                    }
                    return children.get(2);
                }
            }
            else if(children.size() == 2){// no successor part
                for (int i = 0; i < 2; i++) {
                    util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
                }
                return null;
            }
        }
        else if(label.equals("elseif")){
            for (int i = 0; i < 2; i++) {
                util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
            }
            if(children.size() == 3){// "successor" part - else/elseif
                return children.get(2);
            }
        }
        else if (label.equals("for") || label.equals("while") || label.equals("foreach") || label.equals("doWhile") ) {
            for (int i = 0; i < 2; i++) {
                util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
            }
            if (children.size() == 3) {// There exist successor part
                return children.get(2);
            }
        }
        else if (label.equals("switch")) {
            if (children.size() > 1) {
                String tmpLabel = children.get(children.size() - 1).getCompleteMethodDeclaration();
                if (!tmpLabel.equals(DEFAULTMARK) && !tmpLabel.equals(CASEMARK)) {
                    for (int i = 0; i < children.size() - 1; i++) {
                        util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
                    }
                    return children.get(children.size() - 1);
                }
                else{
                    util.clearDataEdgeFromNode(node, new ArrayList<>());
                }
            }
        }
        else if (label.equals("try")) {
            if (children.size() > 0) {
                String tmpLabel = children.get(children.size() - 1).getCompleteMethodDeclaration();
                if(!tmpLabel.equals("catch") && !tmpLabel.equals("finally")){
                    for (int i = 0; i < children.size() - 1; i++) {
                        util.clearDataEdgeFromNode(children.get(i), new ArrayList<>());
                    }
                    return children.get(children.size() - 1);
                }
                else {
                    util.clearDataEdgeFromNode(node, new ArrayList<>());
                }
            }
        }
        else if (label.equals("catch")){
            util.clearDataEdgeFromNode(children.get(0), new ArrayList<>());
            if(children.size() == 2) {
                return children.get(1);// "successor" part - catch/finally
            }
        }
        return null;
    }


    private boolean isSpecialLabel(String label){
        List<String> l = Arrays.stream(specialLabel).collect(Collectors.toList());
        return l.contains(label);
    }

    // Replace the begin~end lines as holes to construct testcase.
    private void constructTestCase(GraphNode node, boolean isThrough, ArrayList<GraphNode> list) {
        if(!list.contains(node)) {
            list.add(node);
            String label = node.getCompleteMethodDeclaration();
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
            if (node.isControl()) {
                if (label.equals("if") && node.getChildNodes().size() > 1) {
                    if (node.getChildNodes().size() > 2) {// else/ elseif
                        GraphNode secondChildNode = node.getChildNodes().get(2);
                        String secondChildLabel = secondChildNode.getCompleteMethodDeclaration();
                        if (secondChildLabel.equals("else") || secondChildLabel.equals("elseif")) {
                            constructTestCase(secondChildNode, true, new ArrayList<>());
                        }
                    }
                } else if (isThrough && label.equals("elseif")) {
                    if (node.getChildNodes().size() == 3) {// "successor" part - else/elseif
                        constructTestCase(node.getChildNodes().get(2), true, new ArrayList<>());
                    }
                } else if (label.equals("else") || label.equals("elseif") || label.equals("for") || label.equals("while") || label.equals("foreach") || label.equals("doWhile")) {
                    // replace all...
                } else if (label.equals("switch")) {
                    // replace all...
                } else if (label.equals("try")) {
                    List<GraphNode> children = node.getChildNodes();
                    if (children.size() > 0) {
                        for (int i = 0; i < children.size(); i++) {
                            String tmpLabel = children.get(i).getCompleteMethodDeclaration();
                            if (tmpLabel.equals("catch") || tmpLabel.equals("finally")) {
                                constructTestCase(children.get(i), false, new ArrayList<>());
                            }
                        }
                    }
                } else if (label.equals("catch")) {
                    // replace all...
                } else if (label.equals("finally")) {
                    // replace all...
                }
            }
        }
    }


}
