package codeAnalysis.processData;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import utils.ConstructGraphUtil;
import utils.FileUtil;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConstructNameOnlyData {

    // The maximum hole number of the continuous holes
    private int MAX_HOLENUM = 5;
    // The construct graphs
    private LinkedList<Graph> graphs;
    // The predication holes correspondent to the constructGraphs;
    private LinkedList<String> predictions;
    // The predicate node's complete class name
    private LinkedList<String> classnames;
    // The node who is the parent of the predicated one
    private LinkedList<GraphNode> parents;
    // GraphOperator
    //private CodeTreeOperation operator;
    // The size of hole
    private  LinkedList<String> holesizes;
    // The block predictions
    private LinkedList<String> blockpredictions;
    private String blockprediction;
    // The original statements
    private LinkedList<String> originalStatements;
    // The variable names
    private LinkedList<String> variableNames;
    // The method names
    private LinkedList<String> methodNames;

    private ConstructGraphUtil util = new ConstructGraphUtil();

    public ConstructNameOnlyData() {
    }

    public void construct(Graph graph,
                          FileWriter graphWriter,
                          FileWriter predictionWriter,
                          FileWriter classWriter,
                          FileWriter generationNodeWriter,
                          FileWriter graphSentenceWriter,
                          FileWriter jarWriter,
                          FileWriter holeSizeWriter,
                          FileWriter traceWriter, // trace back
                          FileWriter blockpredictionWriter, // block of predictions (more lines)
                          FileWriter originalStatementsWriter, // original statements
                          FileWriter variableNamesWriter,// variable names
                          FileWriter linesWriter,// lines writer
                          FileWriter vocabWriter,
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
                Graph tempGraph = graphList.get(i);

                FileUtil.saveStringInFile(predictionList.get(i), predictionWriter);
                FileUtil.saveStringInFile(classList.get(i), classWriter);
                int parentnum = generationNodeList.get(i).getSerialNumber();
                FileUtil.saveStringInFile(parentnum + " " + ((parentnum != 0) ? generationNodeList.get(i).getCompleteMethodDeclaration() : ""), generationNodeWriter);

                FileUtil.saveStringInFile("[[1,4,1]]", graphSentenceWriter);// 记录图表示
                FileUtil.saveStringInFile("{1:'unknown'}", vocabWriter);// 记录词汇表

                FileUtil.saveStringInFile(holeSizeList.get(i), holeSizeWriter);

                FileUtil.saveStringInFile(graph.getFunctionTrace(), traceWriter);
                FileUtil.saveStringInFile(blockpredictionList.get(i), blockpredictionWriter);

                FileUtil.saveStringInFile(originalStatementsList.get(i), originalStatementsWriter);
                FileUtil.saveStringInFile(variableNameList.get(i), variableNamesWriter);

                FileUtil.saveStringInFile("-1" + "", linesWriter);
                FileUtil.saveStringInFile(methodNameList.get(i), methodNamesWriter);
            } catch (Exception e) {
                //System.err.println(e.getMessage());
            } catch (Error e){
                //System.err.println(e.getMessage());
            }
        }
    }


    /**
     * Construct training graph from the input code graph
     * */
    public LinkedList<LinkedList> getConstructTrainingData(Graph completeGraph){
        // Init
        graphs = new LinkedList<>();
        predictions = new LinkedList<>();
        classnames = new LinkedList<>();
        parents = new LinkedList<>();
        holesizes = new LinkedList<>();
        blockpredictions = new LinkedList<>();

        originalStatements = new LinkedList<>();
        variableNames = new LinkedList<>();
        methodNames = new LinkedList<>();

        // Construct
        int count = 1;
        for (int i = 1; i <= 1; i++) {// construct from the first node, construct once
            construct(completeGraph, i);
        }

        // Return
        LinkedList<LinkedList> result = new LinkedList<>();
        result.addLast(graphs);
        result.addLast(predictions);
        result.addLast(classnames);
        result.addLast(parents);
        result.addLast(holesizes);
        result.addLast(blockpredictions);
        result.addLast(originalStatements);
        result.addLast(variableNames);
        result.addLast(methodNames);

        return result;
    }

    /**
     * @param completeGraph: training data from this graph
     * @param serialNumber: node to construct termination mark with this serial number
     * */
    private void construct(Graph completeGraph, int serialNumber){
        // Make the replica of the completeGraph to avoid destroying.
        Graph graph = util.copyGraph(completeGraph);
        graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());

        // Initialize the blockprediction
        blockprediction  = "";

        if(graph.getTotalNumber(new ArrayList<>()) >= serialNumber){
            GraphNode node = graph.getGraphNode(serialNumber, new ArrayList<>());
            predictions.addLast(graph.getRoot().getCompleteMethodDeclaration());
            graphs.addLast(graph);
            classnames.addLast(graph.getRoot().getCompleteClassName());
            parents.addLast(node);
//            addHole(graph, node);
            holesizes.addLast("-1");
            blockprediction = graph.getRoot().getCompleteMethodDeclaration();
            blockpredictions.addLast(blockprediction);
            originalStatements.addLast(node.getStatement());

            graph.setRoot(null);// special operation for name only data
            List<String> previousVariableNames = graph.getUsedClassFieldAndMethodArgumentVariableList();
            String variablename = previousVariableNames.size()>0?previousVariableNames.get(0):"";
            for (int i = 1; i < previousVariableNames.size(); i++) {
                variablename += " " + previousVariableNames.get(i);
            }
            variableNames.addLast(variablename);
            //get method name
            List<String> tempMethodNames = graph.getAllMethodNamesList();
            String methodname = tempMethodNames.size()>0?tempMethodNames.get(0):"";
            for (int i = 1; i < tempMethodNames.size(); i++) {
                methodname += " " + tempMethodNames.get(i);
            }
            // variable names
            methodNames.addLast(methodname);
        }
    }
}
