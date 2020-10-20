package utils;

import codeAnalysis.codeRepresentation.GraphNode;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class GraphWriteUtil {

    public static void show(GraphNode root, String filePath) throws Exception{
        if (root == null || filePath == null){
            return;
        }
        FileUtil.createFileParent(new File(filePath));
        PrintWriter out = null;
        try {
            out = new PrintWriter(filePath);
            String header = "digraph callGraph {\n" + "\tnode [shape=rectangle]\n";
            StringBuilder sbNode= new StringBuilder(), sbEdge = new StringBuilder();
            Queue<GraphNode> queue = new LinkedList<>();
            HashMap<Integer, GraphNode> recorder = new HashMap<>();
            queue.add(root);
            while (!queue.isEmpty()){
                GraphNode node = queue.poll();
                if (!recorder.containsKey(node.hashCode())) {
                    sbNode.append("\t" + node.hashCode() + "  [label=\""
                            +node.getCompleteMethodDeclaration().replace("\"", "\\\"") + " " + node.getVariableName() + " " + node.isVariableDeclaration() + " " + node.isAssign() +" " + node.getSerialNumber() + "\"]\n");
                    recorder.put(node.hashCode(), node);
                    for (GraphNode child : node.getChildNodes()){
                        String edgeSymbol = node.hashCode() + " -> " + child.hashCode();
                        String edgeLabel = "";
                        sbEdge.append("\t" + edgeSymbol + "[ label=\"" + edgeLabel + "\" ]" + "\n");
                        queue.add(child);
                    }
                }
            }
            out.print(header + sbNode.toString() + sbEdge.toString() + "}");
            out.flush();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (out != null){
                out.close();
            }
            //Runtime rt = Runtime.getRuntime();
            //rt.exec("dot -Tpng " + filePath + " -o " + filePath + ".png");
        }
    }
}
