package codeAnalysis.codeProcess;

import codeAnalysis.codeRepresentation.Graph;
import codeAnalysis.codeRepresentation.GraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将图转换为点边点的格式
 */
public class GraphToNodeEdgeSet {

    private Map<String, String> map = new HashMap<>();

    public GraphToNodeEdgeSet(Graph graph) {
        graph.setSerialNumberOfNode(graph.getRoot(), new ArrayList<>());
        map.put("c", "1");
        map.put("d", "2");
        map.put("cd", "3");
        map.put("unknown", "4");
    }

    public List<List<String>> getSet(GraphNode root, List<GraphNode> reList) {
        boolean flag = false;
        List<List<String>> result = new ArrayList<>();
        List<String> nodeEdges = new ArrayList<>();
        List<String> words = new ArrayList<>();
        List<GraphNode> nodeList = new ArrayList<>();
        if (root.getChildNodes().size() == 0) {
            words.add(root.getSerialNumber() + " " + root.getCompleteMethodDeclaration());
            String nodeEdgeNode = "";
            nodeEdgeNode = root.getSerialNumber() + "," + map.get("unknown") + "," + root.getSerialNumber();
            nodeEdges.add(nodeEdgeNode);
        } else {
            nodeList.add(root);
            while (nodeList.size() > 0) {
                List<GraphNode> tempList = new ArrayList<GraphNode>();
                for (int index = 0; index < nodeList.size(); index++) {
                    GraphNode node = nodeList.get(index);
                    if (!reList.contains(node)) {
                        reList.add(node);
                        words.add(node.getSerialNumber() + " " + node.getCompleteMethodDeclaration());
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            String nodeEdgeNode = "";
                            //nodeEdgeNode = node.getCompleteMethodDeclaration() + " " + node.getEdgeMap().get(node.getChildNodes().get(j))+ " " + node.getChildNodes().get(j).getCompleteMethodDeclaration();
                            if (map.get(node.getEdgeMap().get(node.getChildNodes().get(j))) != null) {
                                nodeEdgeNode = node.getSerialNumber() + "," + map.get(node.getEdgeMap().get(node.getChildNodes().get(j))) + "," + node.getChildNodes().get(j).getSerialNumber();
                                nodeEdges.add(nodeEdgeNode);
                            } else {
                                nodeEdgeNode = node.getSerialNumber() + "," + "1" + "," + node.getChildNodes().get(j).getSerialNumber();
                                nodeEdges.add(nodeEdgeNode);
                                //flag = true;
                                //break;
                            }
                            //if (!"d".equals(node.getEdgeMap().get(node.getChildNodes().get(j)))) {
                            tempList.add(node.getChildNodes().get(j));
                            //}
                        }
                    }
                }
                nodeList.removeAll(nodeList);
                nodeList = tempList;
            }
        }
        result.add(nodeEdges);
        result.add(words);
        if (flag) {
            result = null;
        }
        return result;
    }

}
