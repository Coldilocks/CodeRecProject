package utils;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.List;

public class AstUtil {

    private static int FAKE_NODE_POS = -1;

    public static void markNodeAsFake(Node node, Node refNode) {
        Position position = new Position(FAKE_NODE_POS,FAKE_NODE_POS);
        Range range = new Range(position,position);
        node.setRange(range);
        DataKey<Node> dataKey = new DataKey<Node>(){};
        node.setData(dataKey,refNode);
    }

    public static <T extends Node> void markNodesAsFake(List<T> nodes, Node refNode) {
        for (T node : nodes) {
            markNodeAsFake(node, refNode);
        }
    }
}
