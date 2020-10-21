package codeAnalysis.processData.processTrainData;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import codeAnalysis.processData.ConstructData;
import config.DataConfig;
import javafx.util.Pair;
import utils.FileUtil;
import utils.GraphWriteUtil;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConstructPreTrainData extends ConstructData {

    private boolean isDebug = false;
    private LinkedList<String> predictions;

    public List<String> getAllVariableNames(Graph graph){
        graph.initAllVariables(graph.getRoot(),new ArrayList<>());
        return graph.getAllVariableNamesList();
    }

    private GraphNode findAncestorUnderControl(GraphNode node) {
        GraphNode parent = null;
        GraphNode tmpnode = node;
        while((parent = tmpnode.getParentNode())!= null){
            if(parent.isControl()){
                return tmpnode;
            }
            tmpnode = parent;
        }
        return null;
    }

    public String filterSigns(String string){
        for (String sign: filterSigns) {
            string = string.replace(sign, "");
        }
        return string;

    }

    public boolean isReassignHoleParent(String label){
        for(String l : holeParentAdjust){
            if(l.equals(label)){
                return true;
            }
        }
        return false;
    }

    // is control node with "condition"
    public boolean isWithConditionNode(String label){
        for (String l: nodeWithCondition) {
            if(l.equals(label)){
                return true;
            }
        }
        return false;
    }

    public void construct(Graph completeGraph, int serialNumber){
        // Make the replica of the completeGraph to avoid destroying.
        Graph graph = util.copyGraph(completeGraph);
        graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());
        // Initialize the blockPrediction
        blockPrediction  = "";
        if(graph.getTotalNumber(new ArrayList<>()) >= serialNumber){
            boolean isSuccess = true;
            List predicts = null;
            if(graph.getTotalNumber(new ArrayList<>()) == 1){// only root node.
                isSuccess = false;
            }
            else if((predicts = remove(graph,serialNumber)) == null
            ){
                isSuccess = false;
            }
            if(isSuccess){
                count++;
                if(isDebug) {
                    System.out.println(count + ". " + predicts);
                }
                predictions.addLast((String)predicts.get(0));
                graphs.addLast(graph);
                classNames.addLast((String)predicts.get(1));
                GraphNode generationNode = (GraphNode)predicts.get(2);
                parents.addLast(generationNode);

                // Add hole node
                util.addHole(graph, generationNode);

                // set as 1.
                holeSize.addLast("1");
                blockPredictions.addLast(blockPrediction.trim());
                if(isDebug) {
                    System.out.println(blockPrediction);
                    // displayGraph(graph, true, blockPrediction);
                }

                // statements
                originalStatements.addLast((String)predicts.get(3));

                // get All variable names.
                List<String> tempVariableNames = getAllVariableNames(graph);
                String variablename = tempVariableNames.size()>0?tempVariableNames.get(0):"";
                for (int i = 1; i < tempVariableNames.size(); i++) {
                    variablename += " " + tempVariableNames.get(i);
                }
                // variable names
                variableNames.addLast(variablename);

                if(isShowGraph) {
                    try {
                        GraphWriteUtil.show(graph.getRoot(), DataConfig.PRE_TEST_OR_TRAIN_GRAPH_DATA_CONSTRUCT_PATH + count);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }



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
                          FileWriter methodNamesWriter,
                          boolean isCompleteFlag){
        LinkedList<LinkedList> result = getConstructTrainingData(graph);
        List<Graph> graphList = result.get(0);
        List<String> predictionList = result.get(1);
        List<String> classList = result.get(2);
        List<GraphNode> generationNodeList = result.get(3);
        List<String> holeSizeList = result.get(4);// record size of hole
        List<String> blockpredictionList = result.get(5);

        List<String> originalStatementsList = result.get(6);
        List<String> variableNameList = result.get(7);
        List<String> methodNameList = result.get(8);
        
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


    /**
     * Construct training graph from the input code graph
     * Return the LinkedList with 2 list:
     * 1. The training graphs with some continuous holes;
     * 2. The predication holes correspondent to the training graphs;
     * */
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
        result.addLast(methodNames);
        return result;
    }


    /**
     * Remove the node with serialNumber in the code graph
     * Remove it, including its children(except the remain node, e.g control)
     * Find the first ancestor node that under control node, remove the next brothers of this ancestor
     * Return its complete method declaration
     * */
    private LinkedList<Object> remove(Graph graph, int serialNumber){
        LinkedList<Object> result = new LinkedList<>();

        GraphNode node =  graph.getGraphNode(serialNumber,new ArrayList<>());
        if(node == null){// Not exist
            return null;
        }
        GraphNode parent = node.getParentNode();
        String label = node.getCompleteMethodDeclaration();

        // tips: 当这个预测节点的condition部分没有API调用的时候呢 移除作为预测的control node.
        if(node.isControl()){
            if(isWithConditionNode(node.getCompleteMethodDeclaration())){
                if (node.getChildNodes().get(0).getChildNodes().size() == 0) {
                    label = label+"+false";
                }
            }
        }

        // 对catch和finally不做预测处理.
        if(label.equals("catch") || label.equals("finally")){
            return null;
        }

        String className = node.getCompleteClassName();
        String statement = node.getStatement();

        // record predictions of this node
        blockPrediction += " " + util.node2String(node, new ArrayList<>());

        // Find the first ancestor node that under control node
        GraphNode ancestor = findAncestorUnderControl(node);

        // Remove the next brother of this ancestor
        GraphNode elderAncestor;
        while(ancestor != null) {
            elderAncestor = ancestor.getParentNode();
            if (elderAncestor != null) {
                int index = elderAncestor.getChildNodes().indexOf(ancestor);
                int nextbrothernum = elderAncestor.getChildNodes().size() - (index + 1);
                for (int i = 1; i <= nextbrothernum; i++) {
                    if("c".equals(elderAncestor.getEdgeMap().get(elderAncestor.getChildNodes().get(index + 1)))||
                            "cd".equals(elderAncestor.getEdgeMap().get(elderAncestor.getChildNodes().get(index + 1)))) {
                        blockPrediction += " " + util.node2String(elderAncestor.getChildNodes().get(index + 1), new ArrayList<>());
                    }
                    util.clearDataEdgeFromNode(elderAncestor.getChildNodes().get(index+1), new ArrayList<>());
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
        graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());
        result.addLast(label);
        result.addLast(filterSigns(className));

        // Reassign hole parent
        if(isReassignHoleParent(label)){
            parent = node.getHoleParentNode();
        }

        if(parent == null) {
            parent = new GraphNode();
            parent.setSerialNumber(0);// the root do not have a parent
        }
        result.addLast(parent);
        result.addLast(statement);

        return result;
    }

}
