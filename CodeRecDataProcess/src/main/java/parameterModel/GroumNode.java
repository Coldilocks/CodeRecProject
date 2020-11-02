package parameterModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroumNode implements Cloneable, Serializable {

    private GroumNode parentNode;
    private List<GroumNode> childNodes;
    private List<GroumNode> parents;
    private String className;// API simple name or control name(if, while)
    private String completeClassName; // API complete name or control name(if, while)
    private String methodName;// simple method call or control expression(if, while)
    private String completeMethodName;// complete method call or control expression(if, while)
    private List<String> argumentList;// method argument
    private List<GroumNode> dataDependency;// store data dependency
    private boolean isControl;// this flag is used to judge whether a node is a structure node(if while for)
    //	private boolean isTruePath;// used to set up the path to be true if it satisfy the if condition or loop condition
    private boolean isExit;//used to judge whether a node is the exit
    private int depth;
    private int serialNumber;
    private boolean isCondition;
    private String completeMethodDeclaration;
    private boolean isVariablePreserved = false;
    private boolean isPrimitive = false;
    private boolean isUsed = false;
    private boolean isVariableDeclaration = false;
    private boolean isAssign = false;
    private boolean isModified = true;//用来过滤同名变量的节点
    private String statement = null;//用来保存原始语句（没有进行抽象过的）
    private String variableName = null;//用来保存声明的变量名（如果当前结点是变量声明结点的话）
    private List<String> previousVariableNames = new ArrayList();//用来保存出现在当前结点前的结点
    private List<String> commentList = new ArrayList();//用来保存注释信息
    private GroumNode holeParentNode = null;
    private List<String> scopeList = new ArrayList<>();
    private String type = null;
    private int variableCount = 1;

    public int getVariableCount() {
        return variableCount;
    }

    public void setVariableCount(int variableCount) {
        this.variableCount = variableCount;
    }

    public String getType() {
        return type;
    }

    public List<GroumNode> getParents() {
        return parents;
    }

    public List<String> getScopeList() {
        return scopeList;
    }

    public void setAbsoluteParentNode(GroumNode parentNode) {
        this.parentNode = parentNode;
    }

    public void setParents(List<GroumNode> parents) {
        this.parents = parents;
    }

    public GroumNode getHoleParentNode() {
        return holeParentNode;
    }

    public void setHoleParentNode(GroumNode holeParentNode) {
        this.holeParentNode = holeParentNode;
    }

    public List<String> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<String> commentList) {
        this.commentList = commentList;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName, String type, int variableCount) {
        if(variableName != null && type != null) {
            this.variableName = variableName;
            this.type = type;
            this.variableCount = variableCount;
        }
    }

    public boolean isAssign() {
        return isAssign;
    }

    public void setAssign(boolean assign) {
        isAssign = assign;
    }

    public List<String> getPreviousVariableNames() {
        return previousVariableNames;
    }

    public void setPreviousVariableNames(List<String> previousVariableNames) {
        this.previousVariableNames = previousVariableNames;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public boolean isVariablePreserved() {
        return isVariablePreserved;
    }

    public void setVariablePreserved(boolean preserved) {
        isVariablePreserved = preserved;
    }

    public boolean isVariableDeclaration() {
        return isVariableDeclaration;
    }

    public void setVariableDeclaration(boolean variableDeclaration) {
        isVariableDeclaration = variableDeclaration;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public void setPrimitive(boolean primitive) {
        isPrimitive = primitive;
    }

    public void setCompleteMethodDeclaration(String completeMethodDeclaration) {
        this.completeMethodDeclaration = completeMethodDeclaration;
    }

    public boolean isCondition() {
        return isCondition;
    }

    public void setCondition(boolean condition) {
        isCondition = condition;
    }

    public boolean isAddMethodName() {
        return isAddMethodName;
    }

    public void setAddMethodName(boolean addMethodName) {
        isAddMethodName = addMethodName;
    }

    private boolean isAddMethodName;

    public GroumNode() {
        completeMethodDeclaration = null;
        parentNode = null;
        parents = new ArrayList<GroumNode>();
        childNodes = new ArrayList<GroumNode>();
        className = null;
        methodName = null;
        argumentList = new ArrayList<String>();
        dataDependency = new ArrayList<GroumNode>();
        isControl = false;
        //isTruePath = true;
        isExit = false;
        isAddMethodName = true;
        isCondition = false;
        scopeList.add("1");
        variableCount = 1;
    }

    public List<GroumNode> getDataDependency() {
        return dataDependency;
    }

    public void setDataDependency(List<GroumNode> dataDependency) {
        this.dataDependency = dataDependency;
    }


    public GroumNode getParentNode() {
        return parentNode;
    }

    public List<GroumNode> getParentNodes() {
        return parents;
    }

    public void setParentNode(GroumNode parentNode) {
        if (!parents.contains(parentNode)) {
            parents.add(parentNode);
        }
        this.parentNode = parents.get(0);
    }

    public List<GroumNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<GroumNode> childNodes) {
        this.childNodes = childNodes;
    }

//	public boolean isTruePath() {
//		return isTruePath;
//	}
//
//	public void setTruePath(boolean isTruePath) {
//		this.isTruePath = isTruePath;
//	}

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(List<String> argumentList) {
        this.argumentList = argumentList;
    }

    public boolean isControl() {
        return isControl;
    }

    public void setControl(boolean isControl) {
        this.isControl = isControl;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setExit(boolean isExit) {
        this.isExit = isExit;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public String getCompleteMethodName() {
        return completeMethodName;
    }

    public void setCompleteMethodName(String completeMethodName) {
        this.completeMethodName = completeMethodName;
    }

    public String getCompleteClassName() {
        return completeClassName;
    }

    public void setCompleteClassName(String completeClassName) {
        this.completeClassName = completeClassName;
    }

    public String toString() {
        String str;
        if (isControl) {
            str = new String(className);
        } else if (!isAddMethodName) {
            str = new String(className);
        } else {
            str = new String(className + "." + methodName);
        }
        return str;

    }

    public String getCompleteMethodDeclaration() {
        if (completeMethodDeclaration == null) {
            if (isControl) {
                return completeClassName;
            } else if (!isAddMethodName) {
                return completeClassName;
            } else {
                return completeClassName + "." + completeMethodName;
            }
        } else {
            return completeMethodDeclaration;
        }
    }

    public Object clone() {
        Object o = null;
        try {
            o = (GroumNode) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println(e.toString());
        }
        return o;
    }

    //
    public void copyNode(GroumNode node) {
        completeMethodDeclaration = node.getCompleteMethodDeclaration();
        parentNode = null;
        childNodes = new ArrayList<GroumNode>();
        className = node.getClassName();
        methodName = node.getMethodName();
        argumentList = node.getArgumentList();
        dataDependency = node.getDataDependency();
        isControl = node.isControl();
//		isTruePath = node.isTruePath();
        isExit = node.isExit();
        int depth = node.getDepth();
        serialNumber = node.getSerialNumber();
        completeMethodName = node.getCompleteMethodName();
        completeClassName = node.getCompleteClassName();
        isAddMethodName = node.isAddMethodName();
        isCondition = node.isCondition;
        isVariablePreserved = node.isVariablePreserved();
        isPrimitive = node.isPrimitive();
        isUsed = node.isUsed();
        isVariableDeclaration = node.isVariableDeclaration();
        isAssign = node.isAssign();
        isModified = node.isModified();
        statement = node.getStatement();
        variableName = node.getVariableName();
        previousVariableNames = node.getPreviousVariableNames();
        parents = node.getParents();
    }

    public int countNodeChildrenExcludeConditionAndEnd(GroumNode node, List<GroumNode> tempList) {
        int count = 0;
        List<GroumNode> list = node.getChildNodes();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (!tempList.contains(list.get(i))) {
                    tempList.add(list.get(i));
                    if ("condition".equals(list.get(i).getCompleteMethodDeclaration()) ||
                            "end".equals(list.get(i).getCompleteMethodDeclaration())
                            || "break".equals(list.get(i).getCompleteMethodDeclaration())
                            || "continue".equals(list.get(i).getCompleteMethodDeclaration())
                            || "return".equals(list.get(i).getCompleteMethodDeclaration())
                            || "conditionEnd".equals(list.get(i).getCompleteMethodDeclaration())) {
                        //todo nothing
                    } else {
                        count++;
                    }
                    count += countNodeChildrenExcludeConditionAndEnd(list.get(i), tempList);
                }
            }
            return count;
        } else {
            return count;
        }
    }

//    public boolean isContainRemoveNode(GroumNode node, GroumNode removeNode, List<GroumNode> tempList) {
//        boolean result = false;
//        if (node != null) {
//           // System.out.println(node.toString());
//            if(!tempList.contains(node)) {
//                tempList.add(node);
//                if (node.equals(removeNode)) {
//                    result = true;
//                    return result;
//                } else {
//                    List<GroumNode> list = node.getChildNodes();
//                    if (list != null) {
//                        for (int i = 0; i < list.size(); i++) {
//                            if (list.get(i).equals(removeNode)) {
//                                result = true;
//                                return result;
//                            }
//                            result = isContainRemoveNode(list.get(i), removeNode,tempList);
//                            if (result) {
//                                return result;
//                            }
//                        }
//                        return result;
//                    } else {
//                        return result;
//                    }
//                }
//            }
//            else{
//                //System.out.println(node.toString());
//                return result;
//            }
//        } else {
//            return result;
//        }
//    }
//
    public boolean isContainRemoveNode(GroumNode node, GroumNode removeNode, List<GroumNode> tempList) {
        boolean result = false;
        List<GroumNode> list = new ArrayList<>();
        list.add(node);
        while(list.size() > 0){
            List<GroumNode> list2 = new ArrayList<>();
            for(int i = 0; i < list.size(); i ++){
                if(!tempList.contains(list.get(i))) {
                    tempList.add(list.get(i));
                    if (removeNode.equals(list.get(i))) {
                        result = true;
                        return result;
                    } else {
                        List<GroumNode> children = list.get(i).getChildNodes();
                        for (int j = 0; j < children.size(); j++) {
                            list2.add(children.get(j));
                        }
                    }
                }
            }
            list.removeAll(list);
            list = list2;
        }
        return result;
    }

}

