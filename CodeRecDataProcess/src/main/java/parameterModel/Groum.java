package parameterModel;

import codeAnalysis.textTokenProcess.Stemming;
import codeAnalysis.textTokenProcess.Tokenize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchi on 18/1/17.
 */
public class Groum implements Serializable {
    private static final long serialVersionUID = -2205872838993746704l;
    private GroumNode root;
    private int totalNumber = 0; // used to store the total number of nodes
    private Map<String, String> class_variable = new HashMap<String, String>();
    private List<String> class_variable_list = new ArrayList<>();
    private Map<String, String> class_name_map = new HashMap<>();
    private Map<String, Integer> variable_line_map = new HashMap<>();
    private Map<String, Integer> variable_use_map = new HashMap<>();
    private String functionTrace;
    private int maxSerialNumber = 0;

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

    public GroumNode getRoot() {
        return root;
    }

    public int getTotalNumber(List<GroumNode> list) {
        totalNumber = 0;
        computeTotalNumberOfNodes(root, list);
        return totalNumber;
    }

    public void setRoot(GroumNode root) {
        this.root = root;
    }

    public Groum() {
        root = null;
    }

    public void addDataDependencyToControlNode(GroumNode node, GroumNode newNode) {
        if (!node.getChildNodes().contains(newNode) && !newNode.getParents().contains(node)
                && !node.equals(newNode)) {
            node.getChildNodes().add(newNode);
            newNode.setParentNode(node);
        }
    }

    public void addNode(GroumNode node, GroumNode newNode, List<String> mutexList) { // first parameter
        // "node" is the
        // node that will to
        // be the parent of
        // newNode
        if (node == null) {
            if (root == null) {
                root = newNode;
            }
        } else {
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
                }
            }
        }
    }

    public void computeTotalNumberOfNodes(GroumNode node, List<GroumNode> list) {
        if (node != null && !list.contains(node)) {
            totalNumber++;
            list.add(node);
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                computeTotalNumberOfNodes(node.getChildNodes().get(i), list);
            }
        }
    }

    public int setScopeIndexToControlNodes(GroumNode node, List<GroumNode> list, int index, Map<Integer, GroumNode> map) {
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

    public void addAllDataDependencyNodeToControlNodes(GroumNode node, List<GroumNode> list, Map<Integer, GroumNode> map) {
        if (node != null && !list.contains(node)) {
            list.add(node);
            List<Integer> scopeList = new ArrayList<>();
            for (String index : node.getScopeList()) {
                scopeList.add(Integer.parseInt(index));
            }
            if (!node.isControl() && !isSpecialNode(node.getCompleteMethodDeclaration())) {
                List<GroumNode> dataDependencyList = node.getParentNodes();
                for (GroumNode groumNode : dataDependencyList) {
                    if ((groumNode != null) && !groumNode.isControl() && !isSpecialNode(groumNode.getCompleteMethodDeclaration())
                            && !groumNode.isCondition()) {
                        for (Integer scope : scopeList) {
                            if (map.containsKey(scope) && !map.get(scope).isContainRemoveNode(map.get(scope), groumNode, new ArrayList<>())) {
                                addDataDependencyToControlNode(groumNode, map.get(scope));
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

    public void printScopeList(GroumNode node, List<GroumNode> list) {
        if (node != null && !list.contains(node)) {
            list.add(node);
            System.out.println(node.getCompleteMethodDeclaration() + " " + node.getScopeList());
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                printScopeList(node.getChildNodes().get(i), list);
            }
        }
    }

    public Groum copyCodeTree(Groum codeTree) {
        GroumNode root = new GroumNode();
        root.copyNode(codeTree.getRoot());
        List<GroumNode> nodeList = new ArrayList<GroumNode>();
        List<GroumNode> correspondingNodeList = new ArrayList<>();
        nodeList.add(root);
        correspondingNodeList.add(codeTree.getRoot());
        while (correspondingNodeList.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            List<GroumNode> correspondingTempList = new ArrayList<>();
            for (int i = 0; i < correspondingNodeList.size(); i++) {
                GroumNode correspondingNode = correspondingNodeList.get(i);
                GroumNode node = nodeList.get(i);
                for (int j = 0; j < correspondingNode.getChildNodes().size(); j++) {
                    GroumNode tempNode = new GroumNode();
                    tempNode.copyNode(correspondingNode.getChildNodes().get(j));
                    tempNode.setParentNode(node);
                    node.getChildNodes().add(tempNode);

                    tempList.add(tempNode);
                    correspondingTempList.add(correspondingNode.getChildNodes().get(j));
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
            correspondingNodeList.removeAll(correspondingNodeList);
            correspondingNodeList = correspondingTempList;
        }
        this.setRoot(root);
        return this;
    }

    public GroumNode getTreeNode(int serialNumber) {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GroumNode node = nodeList.get(index);
                if (node.getSerialNumber() == serialNumber) {
                    return node;
                } else {
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        tempList.add(node.getChildNodes().get(j));
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return null;
    }

    public GroumNode getHoleNode() {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GroumNode node = nodeList.get(index);
                if (node.getCompleteMethodDeclaration().equals("//hole")) {
                    return node;
                } else {
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        tempList.add(node.getChildNodes().get(j));
                    }
                }

            }
            nodeList.removeAll(nodeList);
            nodeList = tempList;
        }
        return null;
    }

    public boolean removeNode(GroumNode removeNode, List<GroumNode> judgeList) {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        boolean flag = true;
        if (removeNode.isControl()) {
            flag = false;
        } else {
            while (nodeList.size() > 0) {
                List<GroumNode> tempList = new ArrayList<GroumNode>();
                for (int index = 0; index < nodeList.size(); index++) {
                    if (!judgeList.contains(nodeList.get(index))) {
                        judgeList.add(nodeList.get(index));
                        GroumNode node = nodeList.get(index);
                        if (node.equals(removeNode)) {
                            GroumNode tempParentNode = node.getParentNode();
                            if (tempParentNode != null) {
                                if (node.getChildNodes().size() == 0) {
                                    List<GroumNode> parentNodeList = node.getParents();
                                    for (int i = 0; i < parentNodeList.size(); i++) {
                                        parentNodeList.get(i).getChildNodes().remove(node);
                                    }
                                    node.getParents().removeAll(node.getParents());
                                } else {
                                    int removeIndex = tempParentNode.getChildNodes().indexOf(node);
                                    List<GroumNode> parentNodeList = node.getParents();
                                    for (int i = 0; i < parentNodeList.size(); i++) {
                                        if (parentNodeList.get(i).equals(tempParentNode)) {
                                            if (!parentNodeList.get(i).getChildNodes().contains(node.getChildNodes().get(0))) {
                                                parentNodeList.get(i).getChildNodes().set(removeIndex, node.getChildNodes().get(0));
                                            } else {
                                                parentNodeList.get(i).getChildNodes().remove(node);
                                            }
                                        } else {
                                            parentNodeList.get(i).getChildNodes().remove(node);
                                        }
                                    }
                                    node.getParents().removeAll(node.getParents());
                                    List<GroumNode> childNodeList = node.getChildNodes();
                                    for (int i = 0; i < childNodeList.size(); i++) {
                                        if (i == 0) {
                                            childNodeList.get(i).getParents().remove(node);
                                            childNodeList.get(i).setParentNode(tempParentNode);
                                            childNodeList.get(i).setAbsoluteParentNode(tempParentNode);
                                        } else {
                                            childNodeList.get(i).getParents().remove(node);
                                        }
                                    }
                                }
                            } else {
                                if (node.getChildNodes().size() == 0) {
                                    flag = false;
                                } else {
                                    List<GroumNode> childNodeList = node.getChildNodes();
                                    for (int i = 0; i < childNodeList.size(); i++) {
                                        if (i == 0) {
                                            childNodeList.get(i).getParents().remove(node);
                                            childNodeList.get(i).setParentNode(tempParentNode);
                                            childNodeList.get(i).setAbsoluteParentNode(tempParentNode);
                                        } else {
                                            childNodeList.get(i).getParents().remove(node);
                                        }
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


    public void initPreviousVariables(GroumNode node, List<String> duplicatedVariableList, List<String> gloveVocabList, List<String> stopWordsList) {
        for (int i = 0; i < node.getChildNodes().size(); i++) {
            GroumNode currentNode = node.getChildNodes().get(i);
            GroumNode parentNode = currentNode.getParentNode();
            if (parentNode != null) {
                List<String> parentList = parentNode.getPreviousVariableNames();
                List<String> currentList = currentNode.getPreviousVariableNames();
                for (int j = 0; j < parentList.size(); j++) {
                    currentList.add(parentList.get(j));
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
            initPreviousVariables(currentNode, duplicatedVariableList, gloveVocabList, stopWordsList);
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

    public void processRootVariables(GroumNode node, List<String> gloveVocabList, List<String> stopWordsList) {
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

    public void initCommentList(GroumNode node, List<String> commentList) {
        node.setCommentList(commentList);
        for (int i = 0; i < node.getChildNodes().size(); i++) {
            initCommentList(node.getChildNodes().get(i), commentList);
        }
    }

    public void filterSpecialCharacterInOriginalStatement(GroumNode node) {
        String statement = node.getStatement();
        if (statement != null) {
            statement = statement.replaceAll("\r", "");
            statement = statement.replaceAll("\n", "");
            node.setStatement(statement);
        }
        for (int i = 0; i < node.getChildNodes().size(); i++) {
            GroumNode currentNode = node.getChildNodes().get(i);
            filterSpecialCharacterInOriginalStatement(currentNode);
        }
    }

    public int getLines(GroumNode node) {
        int count = 0;
        List<GroumNode> list = node.getChildNodes();
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
                count += getLines(list.get(i));
            }
            return count;
        } else {
            return count;
        }
    }

    public void setSerialNumberofNode(GroumNode groumNode, List<GroumNode> reList) {
        int index = 0;
        maxSerialNumber = 0;
        List<GroumNode> list = new ArrayList<GroumNode>();
        list.add(groumNode);
        while (list.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            for (int i = 0; i < list.size(); i++) {
                if (!reList.contains(list.get(i))) {
                    GroumNode node = list.get(i);
                    reList.add(node);
                    node.setSerialNumber(++index);
                    maxSerialNumber += 1;
                    for (int j = 0; j < node.getChildNodes().size(); j++) {
                        //node.getChildNodes().get(j).setSerialNumber(++index);
                        tempList.add(node.getChildNodes().get(j));
                    }
                }
            }
            list.removeAll(list);
            list = tempList;
        }
    }

    public boolean isContainHoleNode(List<GroumNode> reList) {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GroumNode node = nodeList.get(index);
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

    public void removeHoleNode(List<GroumNode> reList) {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GroumNode node = nodeList.get(index);
                if (!reList.contains(node)) {
                    reList.add(node);
                    if (node.getCompleteMethodDeclaration().equals("//hole")) {
                        if (node.getParentNode() != null) {
                            node.getParentNode().getChildNodes().remove(node);
                            if(node.getChildNodes() != null && node.getChildNodes().size() == 1){
                                addNode(node.getParentNode(),node.getChildNodes().get(0), new ArrayList<>());
                            }
                            node.setParentNode(null);
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
    }

    public Groum replaceHoleNode(GroumNode replaceNode, List<GroumNode> reList) {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(this.getRoot());
        while (nodeList.size() > 0) {
            List<GroumNode> tempList = new ArrayList<GroumNode>();
            for (int index = 0; index < nodeList.size(); index++) {
                GroumNode node = nodeList.get(index);
                if (!reList.contains(node)) {
                    reList.add(node);
                    if (node.getCompleteMethodDeclaration().equals("//hole")) {
                        if (node.getParentNode() != null) {
                            node.getParentNode().getChildNodes().remove(node);
                            addNode(node.getParentNode(), replaceNode, new ArrayList<>());
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

    public int getTotalPosition(GroumNode root, List<GroumNode> repeatList) {
        int position = 0;
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            if (!root.getCompleteMethodDeclaration().equals("conditionEnd") && !root.getCompleteMethodDeclaration().equals("end")
                    && !root.getCompleteMethodDeclaration().equals("//hole")) {
                position += 1;
            }
            for (GroumNode node : root.getChildNodes()) {
                if (!node.isCondition()) {
                    position += getTotalPosition(node, repeatList);
                }
            }
        }
        return position;
    }

    public void print(GroumNode root, List<GroumNode> repeatList) {
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            System.err.println(root.getCompleteMethodDeclaration() + " " + root.getVariableName());
            for (GroumNode node : root.getChildNodes()) {
                print(node, repeatList);
            }
        }
    }

    public int getPositionBeforeHole(GroumNode root, List<GroumNode> repeatList) {
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
                for (GroumNode node : root.getChildNodes()) {
                    if (!node.isCondition()) {
                        position += getPositionBeforeHole(node, repeatList);
                    }
                }
            }
        }
        return position;
    }

    public void getParameterNodes(GroumNode node, List<GroumNode> list, List<String> repeatName) {
        if (!list.contains(node) && node.getVariableName() != null && !repeatName.contains(node.getVariableName())) {
            list.add(node);
            repeatName.add(node.getVariableName());
        }
        for (GroumNode childNode : node.getChildNodes()) {
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

    public void processConditionHole(){
        GroumNode holeNode = this.getHoleNode();
        if(holeNode != null && holeNode.getChildNodes().size() > 0){
            GroumNode childNode = holeNode.getChildNodes().get(0);
            if("conditionEnd".equals(childNode.getCompleteMethodDeclaration())){
                holeNode.getChildNodes().remove(childNode);
                childNode.setParentNode(null);
            }
        }
    }
    public void processConditionHole(GroumNode root, List<GroumNode> repeatList) {
        if (!repeatList.contains(root)) {
            repeatList.add(root);
            if(root.isControl()){
                if(root.getChildNodes().size() >= 2){
                    if(root.getChildNodes().get(1).getCompleteMethodDeclaration().equals("double.Constant")){
                        if(root.getChildNodes().get(1).getChildNodes().size() == 1){
                            if(root.getChildNodes().get(1).getChildNodes().get(0).getCompleteMethodDeclaration().equals("java.lang.System.out.println(double)")){
                                GroumNode tempNode = root.getChildNodes().get(1).getChildNodes().get(0);
                                if(tempNode.getChildNodes().size() == 1){
                                    tempNode = tempNode.getChildNodes().get(0);
                                    root.getChildNodes().remove(1);
                                    root.getChildNodes().add(1,tempNode);
                                    tempNode.setParentNode(root);
                                }else{
                                    root.getChildNodes().remove(1);
                                }
                                GroumNode node = root.getChildNodes().get(0);
                                while(!node.getCompleteMethodDeclaration().equals("conditionEnd")){
                                    node = node.getChildNodes().get(0);
                                }
                                GroumNode hole = new GroumNode();
                                hole.setCompleteMethodDeclaration("//hole");
                                node.getParentNode().getChildNodes().add(hole);
                                hole.setParentNode(node.getParentNode());
                                node.getParentNode().getChildNodes().remove(node);
                                return;
                            }
                        }
                    }
                }
            }
        }
        for (GroumNode node : root.getChildNodes()) {
            processConditionHole(node, repeatList);
        }
    }
}
