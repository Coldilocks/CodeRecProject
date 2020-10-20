package utils;

import codeAnalysis.codeProcess.GraphToNodeEdgeSet;
import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConstructGraphUtil {

    public Pair<String,String> getGraphStr(Graph tempGraph) {
        StringBuilder graphsb = new StringBuilder("[");
        StringBuilder vocabsb = new StringBuilder("{");
        GraphToNodeEdgeSet graphToNodeEdgeSet = new GraphToNodeEdgeSet(tempGraph);
        List<List<String>> r = graphToNodeEdgeSet.getSet(tempGraph.getRoot(),new ArrayList<>());
        if(r != null) {
            for (int h = 0; h < r.get(0).size(); h++) {
                if (h > 0) {
                    graphsb.append(",");
                }
                graphsb.append("[").append(r.get(0).get(h)).append("]");
            }
            graphsb.append("]");
            for (int h = 0; h < r.get(1).size(); h++) {
                if (h > 0) {
                    vocabsb.append(",");
                }
                String word = r.get(1).get(h);
                int indexOfFstSpace = word.indexOf(" ");
                vocabsb.append(word, 0, indexOfFstSpace).append(":'").append(word.substring(indexOfFstSpace + 1)).append("'");
            }
            vocabsb.append("}");
            return new Pair<>(graphsb.toString(), vocabsb.toString());
        }else{
            return null;
        }
    }

    /**
     * ClearDataEdgeFromNode:清理节点与父节点之间的数据流关系
     * 从node节点开始对每个孩子节点,检查这个节点和父节点的关系,如果是数据流边,切断之
     * */
    public void clearDataEdgeFromNode(GraphNode node, ArrayList<GraphNode> list){
        if(!list.contains(node)) {
            list.add(node);
            List<GraphNode> parents = node.getParents();
            for (int i = 0; i < parents.size(); i++) {
                GraphNode parent = parents.get(i);
                if (parent != null) {
                    if ("d".equals(parent.getEdgeMap().get(node))) {
                        parent.getEdgeMap().remove(node);
                        parent.getChildNodes().remove(node);
                    }
                }
            }
            List<GraphNode> children = node.getChildNodes();
            // 这里采用迭代的方式处理孩子节点,保证这个孩子节点的后继节点与父节点的关系被清理干净
            for (int i = 0; i < children.size(); i++) {
                clearDataEdgeFromNode(children.get(i), list);
            }
        }
    }

    // DFS node to string
    public String node2String(GraphNode graphNode, ArrayList<GraphNode> list) {
        if(!list.contains(graphNode)) {
            list.add(graphNode);
            String buff = graphNode.getCompleteMethodDeclaration();
            List<GraphNode> children = new ArrayList<>();
            for (int i = 0; i < graphNode.getChildNodes().size(); i++) {
                if ("c".equals(graphNode.getEdgeMap().get(graphNode.getChildNodes().get(i))) ||
                        "cd".equals(graphNode.getEdgeMap().get(graphNode.getChildNodes().get(i)))) {
                    children.add(graphNode.getChildNodes().get(i));
                }
            }
            for (int i = 0; i < children.size(); i++) {
                buff += " " + node2String(children.get(i), list);
            }
            // for (int i = 0; i < graphNode.getChildNodes().size(); i++) {
            //     buff += " " + node2String(graphNode.getChildNodes().get(i));
            // }
            return buff.trim();
        }
        else {
            return "";
        }
    }

    public Graph copyGraph(Graph completeGraph) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(completeGraph);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            Graph graph = (Graph) ois.readObject();
            return graph;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addHole(Graph graph, GraphNode generationNode) {
        graph.addNode(graph.getGraphNode(generationNode.getSerialNumber(),new ArrayList<>()), createHoleNode(),new ArrayList<>(),"unknown");
        graph.setSerialNumberofNode(graph.getRoot(),new ArrayList<>());
    }

    public GraphNode createHoleNode(){
        GraphNode hole = new GraphNode();
        hole.setClassName("hole");
        hole.setCompleteClassName("hole");
        hole.setMethodName("");
        hole.setCompleteMethodName("");
        hole.setAddMethodName(false);
        return hole;
    }

}
