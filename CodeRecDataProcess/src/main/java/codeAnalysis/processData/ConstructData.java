package codeAnalysis.processData;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import utils.ConstructGraphUtil;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ConstructData {

    protected final boolean isShowGraph = false;
    // The marks
    protected final String ENDMARK = "end";
    protected final String CASEMARK = "case";
    protected final String DEFAULTMARK = "default";
    protected final String[] filterSigns = {"[","]"};
    protected final String[] holeParentAdjust = {"else","elseif","catch","finally","case","default"};
    protected final String[] nodeWithCondition = {"if","while","for","foreach","elseif","doWhile"};
    protected final String[] specialLabel = {"end", "conditionEnd"};

    // The maximum hole number of the continuous holes
    public int MAX_HOLENUM = 5;
    // The construct graphs
    protected LinkedList<Graph> graphs;
    // The predication holes correspondent to the constructGraphs;
    protected LinkedList<String> predictions;
    // The predicate node's complete class name
    protected LinkedList<String> classNames;
    // The node who is the parent of the predicated one
    protected LinkedList<GraphNode> parents;
    // GraphOperator
    // private CodeGraphOperation operator;
    // The size of hole
    protected LinkedList<String> holeSize;
    // The block predictions
    protected LinkedList<String> blockPredictions;
    protected String blockPrediction;
    // The original statements
    protected LinkedList<String> originalStatements;
    // The variable names
    protected LinkedList<String> variableNames;
    // test
    protected int count = 0;
    protected LinkedList<String> methodNames;

    public ConstructGraphUtil util = new ConstructGraphUtil();

    public List<String> getAllVariableNames(Graph graph){
        graph.initAllVariables(graph.getRoot(),new ArrayList<>());
        return graph.getAllVariableNamesList();
    }

    /**
     * Remove the node with serialNumber in the code graph
     * Set its parent as its children's parent
     * Return its complete method declaration, classname, and the parent node
     * */

    // filter the signs
    public String filterSigns(String string){
        for (String sign: filterSigns) {
            string = string.replace(sign, "");
        }
        return string;
    }

    // Reassign hole parent.
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

}
