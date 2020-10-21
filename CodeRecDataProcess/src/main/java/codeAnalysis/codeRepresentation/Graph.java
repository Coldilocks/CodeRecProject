package codeAnalysis.codeRepresentation;

import codeAnalysis.textTokenProcess.Stemming;
import codeAnalysis.textTokenProcess.Tokenize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph implements Serializable {

    private static final long serialVersionUID = -2205872838993746704L;
    private GraphNode root;
    private int totalNumber = 0; // used to store the total number of nodes
    private Map<String, String> class_variable = new HashMap<String, String>();
    private List<String> class_variable_list = new ArrayList<>();
    private Map<String, String> class_name_map = new HashMap<>();
    private Map<String, Integer> variable_line_map = new HashMap<>();
    private Map<String, Integer> variable_use_map = new HashMap<>();
    private String functionTrace;
    private String sourceCodeTrace;
    private String sourceInfo;
    private String methodInfo;
    private int maxSerialNumber = 0;
    private int lineCount = 0;
    private List<String> usedClassFieldAndMethodArgumentVariableList = new ArrayList<>();
    private List<String> allVariableNamesList = new ArrayList<>();
    private List<String> allMethodNamesList = new ArrayList<>();

    public void setAllMethodNamesList(List<String> allMethodNamesList) {
        this.allMethodNamesList = allMethodNamesList;
    }

    public List<String> getAllMethodNamesList() {
        return allMethodNamesList;
    }

    public String getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(String methodInfo) {
        this.methodInfo = methodInfo;
    }

    public String getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(String sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public String getSourceCodeTrace() {
        return sourceCodeTrace;
    }

    public void setSourceCodeTrace(String sourceCodeTrace) {
        this.sourceCodeTrace = sourceCodeTrace;
    }

    public List<String> getAllVariableNamesList() {
        return allVariableNamesList;
    }

    public List<String> getUsedClassFieldAndMethodArgumentVariableList() {
        return usedClassFieldAndMethodArgumentVariableList;
    }

    public void setUsedClassFieldAndMethodArgumentVariableList(List<String> usedClassFieldAndMethodArgumentVariableList) {
        this.usedClassFieldAndMethodArgumentVariableList = usedClassFieldAndMethodArgumentVariableList;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int lineCount) {
        this.lineCount = lineCount;
    }

    public int getMaxSerialNumber() {
        return maxSerialNumber;
    }

    public void setMaxSerialNumber(int maxSerialNumber) {
        this.maxSerialNumber = maxSerialNumber;
    }

    public String getFunctionTrace() {
        return functionTrace;
    }

    public void setFunctionTrace(String functionTrace) {
        this.functionTrace = functionTrace;
    }

    public Map<String, Integer> getVariable_line_map() {
        return variable_line_map;
    }

    public void setVariable_line_map(Map<String, Integer> variable_line_map) {
        this.variable_line_map = variable_line_map;
    }

    public Map<String, Integer> getVariable_use_map() {
        return variable_use_map;
    }

    public void setVariable_use_map(Map<String, Integer> variable_use_map) {
        this.variable_use_map = variable_use_map;
    }

    public Map<String, String> getClass_variable() {
        return class_variable;
    }

    public void setClass_variable(Map<String, String> class_variable) {
        this.class_variable = class_variable;
    }

    public List<String> getClass_variable_list() {
        return class_variable_list;
    }

    public void setClass_variable_list(List<String> class_variable_list) {
        this.class_variable_list = class_variable_list;
    }

    public Map<String, String> getClass_name_map() {
        return class_name_map;
    }

    public void setClass_name_map(Map<String, String> class_name_map) {
        this.class_name_map = class_name_map;
    }

    public GraphNode getRoot() {
        return root;
    }

    public int getTotalNumber(List<GraphNode> list) {
        totalNumber = 0;
        computeTotalNumberOfNodes(root, list);
        return totalNumber;
    }

    public void setRoot(GraphNode root) {
        this.root = root;
    }

    public Graph() {
        root = null;
    }

    public void addDataDependencyToControlNode(GraphNode node, GraphNode newNode) {
        if (!node.getChildNodes().contains(newNode) && !newNode.getParents().contains(node)
                && !node.equals(newNode)) {
            node.getChildNodes().add(newNode);
            newNode.setParentNode(node);
        }
    }

    public boolean addNode(GraphNode node, GraphNode newNode, List<String> mutexList, String edgeType) {
        if (node == null) {
            if (root == null) {
                root = newNode;
            }
        } else {
            if(node.getCompleteMethodDeclaration().equals("hole")){
                edgeType = "unknown";
            }
            if (!newNode.isContainRemoveNode(newNode, node, new ArrayList<>())) {
                boolean flag = true;
                List<String> parentScopeList = node.getScopeList();
                List<String> childScopeList = newNode.getScopeList();
                for (int i = 0; i < childScopeList.size(); i++) {
                    if (i < parentScopeList.size()) {
                        if (mutexList.contains(childScopeList.get(i) + parentScopeList.get(i))) {
                            flag = false;
                            break;
                        }
                    }
                }
                if (!node.getChildNodes().contains(newNode) && !newNode.getParents().contains(node)
                        && !node.equals(newNode) && flag) {
                    node.getChildNodes().add(newNode);
                    newNode.setParentNode(node);
                    node.getEdgeMap().put(newNode, edgeType);
                } else if (node.getChildNodes().contains(newNode) && newNode.getParents().contains(node)
                        && !node.equals(newNode) && flag) {
                    if (node.getEdgeMap().containsKey(newNode)) {
                        String value = node.getEdgeMap().get(newNode);
                        if (!edgeType.equals(value)) {
                            node.getEdgeMap().replace(newNode, "cd");
                        }
                    } else {
                        node.getEdgeMap().put(newNode, edgeType);
                    }
                }
            }
        }
        return true;
    }

    public void computeTotalNumberOfNodes(GraphNode node, List<GraphNode> list) {
        if (node != null && !list.contains(node)) {
            totalNumber++;
            list.add(node);
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                computeTotalNumberOfNodes(node.getChildNodes().get(i), list);
            }
        }
    }

    public int setScopeIndexToControlNodes(GraphNode node, List<GraphNode> list, int index, Map<Integer, GraphNode> map) {
        if (node != null && !list.contains(node)) {
            list.add(node);
            if (node.isControl()) {
                map.put(index, node);
                index = index + 1;
            }
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                index = setScopeIndexToControlNodes(node.getChildNodes().get(i), list, index, map);
            }
        }
        return index;
    }

    public void addAllDataDependencyNodeToControlNodes(GraphNode node, List<GraphNode> list, Map<Integer, GraphNode> map) {
        if (node != null && !list.contains(node)) {
            list.add(node);
            List<Integer> scopeList = new ArrayList<>();
            for (String index : node.getScopeList()) {
                scopeList.add(Integer.parseInt(index));
            }
            if (!node.isControl() && !isSpecialNode(node.getCompleteMethodDeclaration())) {
                List<GraphNode> dataDependencyList = node.getParentNodes();
                for (GraphNode graphNode : dataDependencyList) {
                    if ((graphNode != null) && !graphNode.isControl() && !isSpecialNode(graphNode.getCompleteMethodDeclaration())
                            && !graphNode.isCondition()) {
                        for (Integer scope : scopeList) {
                            if (map.containsKey(scope) && !map.get(scope).isContainRemoveNode(map.get(scope), graphNode, new ArrayList<>())) {
                                addDataDependencyToControlNode(graphNode, map.get(scope));
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                addAllDataDependencyNodeToControlNodes(node.getChildNodes().get(i), list, map);
            }
        }
    }

    public void printScopeList(GraphNode node, List<GraphNode> list) {
        if (node != null && !list.contains(node)) {
            list.add(node);
            System.out.println(node.getCompleteMethodDeclaration() + " " + node.getScopeList());
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                printScopeList(node.getChildNodes().get(i), list);
            }
        }
    }

    public GraphNode getGraphNode(int serialNumber, ArrayList<GraphNode> reList) {
        List<GraphNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GraphNode node = nodeList.get(index);
                if (!reList.contains(node)) {
                    reList.add(node);
                    if (node.getSerialNumber() == serialNumber) {
                        return node;
                    } else {
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            tempList.add(node.getChildNodes().get(j));
                        }
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return null;
    }

    public GraphNode getHoleNode(ArrayList<GraphNode> reList) {
        try {
            List<GraphNode> nodeList = new ArrayList<>();
            nodeList.add(this.getRoot());
            while (nodeList.size() > 0) {
                List<GraphNode> tempList = new ArrayList<GraphNode>();
                for (int index = 0; index < nodeList.size(); index++) {
                    GraphNode node = nodeList.get(index);
                    if (!reList.contains(node)) {
                        reList.add(node);
                        if (node.getCompleteMethodDeclaration().equals("hole")) {
                            return node;
                        } else {
                            for (int j = 0; j < node.getChildNodes().size(); j++) {
                                tempList.add(node.getChildNodes().get(j));
                            }
                        }
                    }

                }
                nodeList.removeAll(nodeList);
                nodeList = tempList;
            }
            return null;
        }catch(Exception e){
            return null;
        }catch(Error e){
            return null;
        }
    }

    public void dealHoleNode(ArrayList<GraphNode> reList, List<GraphNode> holeParentAndChildNode) {
        List<GraphNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GraphNode node = nodeList.get(index);
                if (!reList.contains(node)) {
                    reList.add(node);
                    if (node.getCompleteMethodDeclaration().equals("hole")) {
                        if (node.getParentNode() != null) {
                            holeParentAndChildNode.add(node.getParentNode());
                            int holeIndex = node.getParentNode().getChildNodes().indexOf(node);
                            if (node.getParentNode().getChildNodes().size() > (holeIndex + 1)) {
                                holeParentAndChildNode.add(node.getParentNode().getChildNodes().get(holeIndex + 1));
                            } else {
                                holeParentAndChildNode.add(null);
                            }
                            node.getParentNode().getChildNodes().remove(node);
                            node.getParentNode().getEdgeMap().remove(node);
                            GraphNode tnode = new GraphNode();
                            tnode.setCompleteMethodDeclaration("hole");
                            addNode(node.getParentNode(), tnode, new ArrayList<>(), "unknown");
                            node.setAbsoluteParentNode(null);
                            return;
                        } else {
                            holeParentAndChildNode.add(null);
                            holeParentAndChildNode.add(this.getRoot());
                            return;
                        }
                    } else {
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            tempList.add(node.getChildNodes().get(j));
                        }
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        if (holeParentAndChildNode.size() == 0) {
            holeParentAndChildNode.add(null);
            holeParentAndChildNode.add(null);
        }
    }

    public boolean removeNode(GraphNode removeNode, List<GraphNode> judgeList) {
        List<GraphNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        boolean flag = true;
        if (removeNode.isControl()) {
            flag = false;
        } else {
            while (nodeList.size() > 0) {
                List<GraphNode> tempList = new ArrayList<GraphNode>();
                for (int index = 0; index < nodeList.size(); index++) {
                    if (!judgeList.contains(nodeList.get(index))) {
                        judgeList.add(nodeList.get(index));
                        GraphNode node = nodeList.get(index);
                        if (node.equals(removeNode)) {
                            GraphNode tempParentNode = node.getParentNode();
                            if (tempParentNode != null) {
                                if (node.getChildNodes().size() == 0) {
                                    List<GraphNode> parentNodeList = node.getParents();
                                    for (int i = 0; i < parentNodeList.size(); i++) {
                                        parentNodeList.get(i).getChildNodes().remove(node);
                                        parentNodeList.get(i).getEdgeMap().remove(node);
                                    }
                                    node.getParents().removeAll(node.getParents());
                                } else {
                                    int removeIndex = tempParentNode.getChildNodes().indexOf(node);
                                    List<GraphNode> parentNodeList = node.getParents();
                                    for (int i = 0; i < parentNodeList.size(); i++) {
                                        if (parentNodeList.get(i).equals(tempParentNode)) {
                                            if (!parentNodeList.get(i).getChildNodes().contains(node.getChildNodes().get(0))) {
                                                parentNodeList.get(i).getChildNodes().set(removeIndex, node.getChildNodes().get(0));
                                                parentNodeList.get(i).getEdgeMap().put(node.getChildNodes().get(0), "c");
                                            } else {
                                                int ind = parentNodeList.get(i).getChildNodes().indexOf(node.getChildNodes().get(0));
                                                GraphNode tNode = new GraphNode();
                                                parentNodeList.get(i).getChildNodes().set(ind, tNode);
                                                parentNodeList.get(i).getChildNodes().set(removeIndex, node.getChildNodes().get(0));
                                                parentNodeList.get(i).getChildNodes().remove(tNode);
                                                //parentNodeList.get(i).getChildNodes().remove(node);
                                            }
                                        } else {
                                            parentNodeList.get(i).getChildNodes().remove(node);
                                        }
                                        parentNodeList.get(i).getEdgeMap().remove(node);
                                    }
                                    node.getParents().removeAll(node.getParents());
                                    List<GraphNode> childNodeList = node.getChildNodes();
                                    for (int i = 0; i < childNodeList.size(); i++) {
                                        if (i == 0) {
                                            childNodeList.get(i).getParents().remove(node);
                                            childNodeList.get(i).setParentNode(tempParentNode);
                                            childNodeList.get(i).setAbsoluteParentNode(tempParentNode);
                                            if (tempParentNode.getEdgeMap().containsKey(childNodeList.get(i)) && tempParentNode.getEdgeMap().get(childNodeList.get(i)) != null &&
                                                    tempParentNode.getEdgeMap().get(childNodeList.get(i)).contains("d")) {
                                                tempParentNode.getEdgeMap().put(childNodeList.get(i), "cd");
                                            } else {
                                                tempParentNode.getEdgeMap().put(childNodeList.get(i), "c");
                                            }
                                        } else {
                                            childNodeList.get(i).getParents().remove(node);
                                        }
                                        node.getEdgeMap().remove(childNodeList.get(i));
                                    }
                                }
                            } else {
                                if (node.getChildNodes().size() == 0) {
                                    flag = false;
                                } else {
                                    List<GraphNode> childNodeList = node.getChildNodes();
                                    for (int i = 0; i < childNodeList.size(); i++) {
                                        if (i == 0) {
                                            childNodeList.get(i).getParents().remove(node);
                                            childNodeList.get(i).setParentNode(tempParentNode);
                                            childNodeList.get(i).setAbsoluteParentNode(tempParentNode);
                                            //tempParentNode.getEdgeMap().put(childNodeList.get(i), "c");
                                        } else {
                                            childNodeList.get(i).getParents().remove(node);
                                        }
                                        node.getEdgeMap().remove(childNodeList.get(i));
                                    }
                                    root = node.getChildNodes().get(0);
                                }
                            }
                            node.getChildNodes().removeAll(node.getChildNodes());
                            node.getParents().removeAll(node.getParents());
                            node.setAbsoluteParentNode(null);
                            return flag;
                        } else {
                            for (int j = 0; j < node.getChildNodes().size(); j++) {
                                if (node.getChildNodes().get(j) != null) {
                                    tempList.add(node.getChildNodes().get(j));
                                }
                            }
                        }
                    }

                }
                nodeList.removeAll(nodeList);
                nodeList = tempList;
            }
        }
        return flag;//原本是return false,现在设置为true是为了过滤那些在if,while中已经被过扔掉了的结点
    }

    public void initAllVariables(GraphNode graphNode, List<GraphNode> reList) {
        List<GraphNode> list = new ArrayList<GraphNode>();
        list.add(graphNode);
        for (String str : this.getUsedClassFieldAndMethodArgumentVariableList()) {
            if (!allVariableNamesList.contains(str)) {
                allVariableNamesList.add(str);
            }
        }
        if (graphNode != null) {
            while (list.size() > 0) {
                List<GraphNode> tempList = new ArrayList<GraphNode>();
                for (int i = 0; i < list.size(); i++) {
                    if (!reList.contains(list.get(i))) {
                        GraphNode node = list.get(i);
                        reList.add(node);
                        List<String> nodeVariableNames = node.getPreviousVariableNames();
                        for (int j = 0; j < nodeVariableNames.size(); j++) {
                            if (!allVariableNamesList.contains(nodeVariableNames.get(j))) {
                                allVariableNamesList.add(nodeVariableNames.get(j));
                            }
                        }
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            tempList.add(node.getChildNodes().get(j));
                        }
                    }
                }
                list.removeAll(list);
                list = tempList;
            }
        }
    }

    public void dealNodeVariables(GraphNode node, List<String> gloveVocabList, List<String> stopWordsList, List<GraphNode> reList) {
        if (!reList.contains(node)) {
            reList.add(node);
            List<String> currentList = node.getPreviousVariableNames();
            if (node.isVariableDeclaration() && !node.isAssign()) {
                String variableName = node.getVariableName();
                if (variableName != null) {
                    variableName = variableName.replaceAll("\r", "");
                    variableName = variableName.replaceAll("\n", "");
                    List<String> tempList = processVariables(variableName, gloveVocabList, stopWordsList);
                    for (int j = 0; j < tempList.size(); j++) {
                        if (!currentList.contains(tempList.get(j))) {
                            currentList.add(tempList.get(j));
                        }
                    }
                }
            }
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                GraphNode currentNode = node.getChildNodes().get(i);
                dealNodeVariables(currentNode, gloveVocabList, stopWordsList, reList);
            }
        }
    }

    public void initPreviousVariables(GraphNode node, List<String> duplicatedVariableList, List<String> gloveVocabList, List<String> stopWordsList, List<GraphNode> reList) {
        if (!reList.contains(node)) {
            reList.add(node);
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                GraphNode currentNode = node.getChildNodes().get(i);
                GraphNode parentNode = currentNode.getParentNode();
                reList.add(node);
                if (parentNode != null) {
                    List<String> parentList = parentNode.getPreviousVariableNames();
                    List<String> currentList = currentNode.getPreviousVariableNames();
                    for (int j = 0; j < parentList.size(); j++) {
                        if (!currentList.contains(parentList.get(j))) {
                            currentList.add(parentList.get(j));
                        }
                    }
                    String variableName = parentNode.getVariableName();
                    if (variableName != null && !duplicatedVariableList.contains(variableName)) {
                        variableName = variableName.replaceAll("\r", "");
                        variableName = variableName.replaceAll("\n", "");
                        List<String> tempList = processVariables(variableName, gloveVocabList, stopWordsList);
                        for (int j = 0; j < tempList.size(); j++) {
                            if (!currentList.contains(tempList.get(j))) {
                                currentList.add(tempList.get(j));
                            }
                        }
                        duplicatedVariableList.add(variableName);
                    }
                }
                initPreviousVariables(currentNode, duplicatedVariableList, gloveVocabList, stopWordsList, reList);
            }
        }
    }


    public List<String> processVariables(String variableName, List<String> gloveVocabList, List<String> stopWordsList) {
        Tokenize tokenize = new Tokenize();
        Stemming stemming = new Stemming();
        List<String> finalList = new ArrayList<>();
        //第一步:替换掉数字及[]
        String variableWithoutNumberAndParentheses = tokenize.removeNumberAndParentheses(variableName);
        //第二步:按"_"和"$"进行split
        List<String> originalList = tokenize.splitSpecialCharacter(variableWithoutNumberAndParentheses);
        //第三步:按照Camel case进行分词(得到的词用"_"连接，还要再按照"_"进行一次split)
        List<String> camelCaseSplitList = new ArrayList<>();
        for (int j = 0; j < originalList.size(); j++) {
            String camelCaseSplitString = tokenize.splitCamelCase(originalList.get(j));
            camelCaseSplitList.add(camelCaseSplitString);
        }
        //第四步:对进行了camel case split后的单词按照"_"进行split
        List<String> camelCaseFinalList = new ArrayList<>();
        for (int j = 0; j < camelCaseSplitList.size(); j++) {
            List<String> tempList = tokenize.splitSpecialCharacter(camelCaseSplitList.get(j));
            for (int k = 0; k < tempList.size(); k++) {
                camelCaseFinalList.add(tempList.get(k));
            }
        }
        //第五步:stemming
        List<String> stemmingList = new ArrayList<>();
        for (int j = 0; j < camelCaseFinalList.size(); j++) {
            stemmingList.add(stemming.getLemma(camelCaseFinalList.get(j)));
        }
        //第六步:stopwords
        List<String> filterStopWordsList = new ArrayList<>();
        for (int j = 0; j < stemmingList.size(); j++) {
            if (!stopWordsList.contains(stemmingList.get(j)) && stemmingList.get(j).length() > 1) {
                filterStopWordsList.add(stemmingList.get(j));
            }
        }
        //第七步:Glove检查
        for (int j = 0; j < filterStopWordsList.size(); j++) {
            if (gloveVocabList.contains(filterStopWordsList.get(j))) {
                finalList.add(filterStopWordsList.get(j));
            }
        }
        return finalList;
    }

    public void processRootVariables(GraphNode node, List<String> gloveVocabList, List<String> stopWordsList) {
        List<String> variables = node.getPreviousVariableNames();
        Tokenize tokenize = new Tokenize();
        Stemming stemming = new Stemming();
        List<String> finalList = new ArrayList<>();
        for (int i = 0; i < variables.size(); i++) {
            //第一步:替换掉数字及[]
            String variableWithoutNumberAndParentheses = tokenize.removeNumberAndParentheses(variables.get(i));
            //第二步:按"_"和"$"进行split
            List<String> originalList = tokenize.splitSpecialCharacter(variableWithoutNumberAndParentheses);
            //第三步:按照Camel case进行分词(得到的词用"_"连接，还要再按照"_"进行一次split)
            List<String> camelCaseSplitList = new ArrayList<>();
            for (int j = 0; j < originalList.size(); j++) {
                String camelCaseSplitString = tokenize.splitCamelCase(originalList.get(j));
                camelCaseSplitList.add(camelCaseSplitString);
            }
            //第四步:对进行了camel case split后的单词按照"_"进行split
            List<String> camelCaseFinalList = new ArrayList<>();
            for (int j = 0; j < camelCaseSplitList.size(); j++) {
                List<String> tempList = tokenize.splitSpecialCharacter(camelCaseSplitList.get(j));
                for (int k = 0; k < tempList.size(); k++) {
                    camelCaseFinalList.add(tempList.get(k));
                }
            }
            //第五步:stemming
            List<String> stemmingList = new ArrayList<>();
            for (int j = 0; j < camelCaseFinalList.size(); j++) {
                stemmingList.add(stemming.getLemma(camelCaseFinalList.get(j)));
            }
            //第六步:stopwords
            List<String> filterStopWordsList = new ArrayList<>();
            for (int j = 0; j < stemmingList.size(); j++) {
                if (!stopWordsList.contains(stemmingList.get(j)) && stemmingList.get(j).length() > 1) {
                    filterStopWordsList.add(stemmingList.get(j));
                }
            }
            //第七步:Glove检查
            for (int j = 0; j < filterStopWordsList.size(); j++) {
                if (gloveVocabList.contains(filterStopWordsList.get(j)) && !finalList.contains(filterStopWordsList.get(j))) {
                    finalList.add(filterStopWordsList.get(j));
                }
            }
        }
        //第八步:替换Node中的previousVariableNames
        node.setPreviousVariableNames(finalList);
    }

    public void initCommentList(GraphNode node, List<String> commentList) {
        node.setCommentList(commentList);
        for (int i = 0; i < node.getChildNodes().size(); i++) {
            initCommentList(node.getChildNodes().get(i), commentList);
        }
    }

    public void filterSpecialCharacterInOriginalStatement(GraphNode node, ArrayList<GraphNode> list) {
        if (!list.contains(node)) {
            list.add(node);
            String statement = node.getStatement();
            if (statement != null) {
                statement = statement.replaceAll("\r", "");
                statement = statement.replaceAll("\n", "");
                node.setStatement(statement);
            }
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                GraphNode currentNode = node.getChildNodes().get(i);
                filterSpecialCharacterInOriginalStatement(currentNode, list);
            }
        }
    }

    public int getLines(GraphNode node, List<GraphNode> reList) {
        if (!reList.contains(node)) {
            reList.add(node);
            int count = 0;
            List<GraphNode> list = node.getChildNodes();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    if ("condition".equals(list.get(i).getCompleteMethodDeclaration()) ||
                            "end".equals(list.get(i).getCompleteMethodDeclaration())
                            || "break".equals(list.get(i).getCompleteMethodDeclaration())
                            || "continue".equals(list.get(i).getCompleteMethodDeclaration())
                            || "return".equals(list.get(i).getCompleteMethodDeclaration())
                            || "conditionEnd".equals(list.get(i).getCompleteMethodDeclaration())
                            || "hole".equals(list.get(i).getCompleteMethodDeclaration())
                    ) {
                        //todo nothing
                    } else {
                        count++;
                    }
                    count += getLines(list.get(i), list);
                }
                return count;
            } else {
                return count;
            }
        } else {
            return 0;
        }
    }

    public void setSerialNumberOfNode(GraphNode graphNode, List<GraphNode> reList) {
        int index = 0;
        maxSerialNumber = 0;
        List<GraphNode> list = new ArrayList<GraphNode>();
        list.add(graphNode);
        while (list.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int i = 0; i < list.size(); i++) {
                if (!reList.contains(list.get(i))) {
                    GraphNode node = list.get(i);
                    reList.add(node);
                    node.setSerialNumber(++index);
                    maxSerialNumber += 1;
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        //node.getChildNodes().get(j).setSerialNumber(++index);
                        //if (!"d".equals(node.getEdgeMap().get(node.getChildNodes().get(j)))) {
                        tempList.add(node.getChildNodes().get(j));
                        //}
                    }
                }
            }
            list.removeAll(list);
            list = tempList;
        }
    }

    public boolean isContainHoleNode(List<GraphNode> reList) {
        List<GraphNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GraphNode node = nodeList.get(index);
                if (!reList.contains(node)) {
                    reList.add(node);
                    if (node.getCompleteMethodDeclaration().equals("//hole")) {
                        return true;
                    } else {
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            tempList.add(node.getChildNodes().get(j));
                        }
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return false;
    }

    public Graph replaceHoleNode(GraphNode replaceNode, List<GraphNode> reList) {
        List<GraphNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GraphNode node = nodeList.get(index);
                if (!reList.contains(node)) {
                    reList.add(node);
                    if (node.getCompleteMethodDeclaration().equals("//hole")) {
                        if (node.getParentNode() != null) {
                            node.getParentNode().getChildNodes().remove(node);
                            addNode(node.getParentNode(), replaceNode, new ArrayList<>(), "unknown");
                            node.setParentNode(null);
                            return this;
                        } else {
                            return null;
                        }
                    } else {
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            tempList.add(node.getChildNodes().get(j));
                        }
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return null;
    }

    public int getTotalPosition(GraphNode root, List<GraphNode> repeatList) {
        int position = 0;
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            if (!root.getCompleteMethodDeclaration().equals("conditionEnd") && !root.getCompleteMethodDeclaration().equals("end")
                    && !root.getCompleteMethodDeclaration().equals("//hole")) {
                position += 1;
            }
            for (GraphNode node : root.getChildNodes()) {
                if (!node.isCondition()) {
                    position += getTotalPosition(node, repeatList);
                }
            }
        }
        return position;
    }

    public void print(GraphNode root, List<GraphNode> repeatList) {
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            for (GraphNode node : root.getChildNodes()) {
                print(node, repeatList);
            }
        }
    }

    public int getPositionBeforeHole(GraphNode root, List<GraphNode> repeatList) {
        int position = 0;
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            if (root.getCompleteMethodDeclaration().equals("//hole")) {
                return position;
            } else {
                if (!root.getCompleteMethodDeclaration().equals("conditionEnd") && !root.getCompleteMethodDeclaration().equals("end")
                ) {
                    position += 1;
                }
                for (GraphNode node : root.getChildNodes()) {
                    if (!node.isCondition()) {
                        position += getPositionBeforeHole(node, repeatList);
                    }
                }
            }
        }
        return position;
    }

    public void getParameterNodes(GraphNode node, List<GraphNode> list, List<String> repeatName) {
        if (!list.contains(node) && node.getVariableName() != null && !repeatName.contains(node.getVariableName())) {
            list.add(node);
            repeatName.add(node.getVariableName());
        }
        for (GraphNode childNode : node.getChildNodes()) {
            if (!list.contains(childNode)) {
                getParameterNodes(childNode, list, repeatName);
            }
        }
    }

    public boolean isSpecialNode(String str) {
        List<String> list = new ArrayList<>();
        list.add("end");
        list.add("conditionEnd");
        list.add("break");
        list.add("continue");
        list.add("return");
        if (list.contains(str)) {
            return true;
        }
        return false;
    }

    public void processConditionHole(GraphNode root, List<GraphNode> repeatList) {
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            if (root.isControl()) {
                if (root.getChildNodes().size() >= 2) {
                    if (root.getChildNodes().get(0).getCompleteMethodDeclaration().equals("conditionEnd")
                            && root.getChildNodes().get(1).getCompleteMethodDeclaration().equals("java.lang.System.out.println(java.lang.String)")) {
                        root.getChildNodes().remove(0);
                        GraphNode tempNode = root.getChildNodes().get(0);
                        addNode(root, tempNode.getChildNodes().get(0), new ArrayList<>(), "c");
                        root.getChildNodes().remove(tempNode);
                        GraphNode hole = new GraphNode();
                        hole.setCompleteMethodDeclaration("//hole");
                        addNode(root, hole, new ArrayList<>(), "unknown");
                        return;
                    }
                }
            }
            for (GraphNode node : root.getChildNodes()) {
                processConditionHole(node, repeatList);
            }
        }
    }

    public void printEdgeType(GraphNode graphNode, List<GraphNode> reList) {
        List<GraphNode> list = new ArrayList<GraphNode>();
        list.add(graphNode);
        while (list.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int i = 0; i < list.size(); i++) {
                if (!reList.contains(list.get(i))) {
                    GraphNode node = list.get(i);
                    reList.add(node);
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        System.out.println(node.getCompleteMethodDeclaration() + " " + node.getChildNodes().get(j).getCompleteMethodDeclaration() + " " + node.getEdgeMap().get(node.getChildNodes().get(j)));
                        tempList.add(node.getChildNodes().get(j));
                    }
                }
            }
            list.removeAll(list);
            list = tempList;
        }
    }

    public void setHoleNodeEdgeToUnknown(GraphNode node, List<GraphNode> reList) {
        if (!reList.contains(node)) {
            reList.add(node);
            if (node.getCompleteMethodDeclaration().equals("hole")) {
                for (GraphNode n : node.getEdgeMap().keySet()) {
                    node.getEdgeMap().put(n, "unknown");
                }
            } else {
                for (GraphNode n : node.getChildNodes()) {
                    setHoleNodeEdgeToUnknown(n,reList);
                }
            }
        }
    }

    public void setSerialNumberofNode(GraphNode graphNode, List<GraphNode> reList) {
        int index = 0;
        maxSerialNumber = 0;
        List<GraphNode> list = new ArrayList<GraphNode>();
        list.add(graphNode);
        while (list.size() > 0) {
            List<GraphNode> tempList = new ArrayList<GraphNode>();
            for (int i = 0; i < list.size(); i++) {
                if (!reList.contains(list.get(i))) {
                    GraphNode node = list.get(i);
                    reList.add(node);
                    node.setSerialNumber(++index);
                    maxSerialNumber += 1;
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        //node.getChildNodes().get(j).setSerialNumber(++index);
                        //if (!"d".equals(node.getEdgeMap().get(node.getChildNodes().get(j)))) {
                        tempList.add(node.getChildNodes().get(j));
                        //}
                    }
                }
            }
            list.removeAll(list);
            list = tempList;
        }
    }

}
