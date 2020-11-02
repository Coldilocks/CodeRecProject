package parameterModel.model;

import parameterModel.Groum;
import parameterModel.GroumNode;
import parameterModel.util.Pair;
import java.io.*;
import java.util.*;

public class PathExplorer2 {

    // true : parent->child
    // false : child->parent
    private boolean pathType = false;
    public static final boolean CHILD_2_PARENT = false;
    public static final boolean PARENT_2_CHILD = true;
    public boolean globalFlag = true;
    //  <key, value> : <node in conditional branch, control node>
    HashMap<GroumNode, GroumNode> conditionRelations = new HashMap<>();

    private void init() {
        conditionRelations = new HashMap<>();
    }

    public static Groum getGroumFromFile(String path) {
        ObjectInputStream in = null;
        Groum groum = null;
        try {
            in = new ObjectInputStream(new FileInputStream(path));
            Object object = in.readObject();
            groum = (Groum) object;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return groum;
    }

    /**
     * @param startNode
     * @param pathType
     * @return
     */
    public List<Path> getPaths(GroumNode root, GroumNode startNode, boolean pathType) {
        init();
        // <key, value> : <node, paths reach/start node/leaf node>
        HashMap<GroumNode, List<Path>> map = new HashMap<>();
        this.pathType = pathType;
        List<Path> paths = new ArrayList<>();
        if (pathType == PARENT_2_CHILD) { // when use PARENT_2_CHILD, we need to extract all sub-paths
            getAllCompletePaths(startNode, paths, new ArrayList<>());
            paths = filterWrongPaths(paths);
            paths = extractAllSubPaths(paths);
            paths = dealPaths(root,paths);
        }
        else {  // otherwise we only consider whole path
            getCompletePathsOfReachNode(root,startNode, paths, new HashMap<>());
            paths = filterWrongPaths(paths);
            //paths = extractAllSubPaths(paths);
            paths = extractAllSubPathsFilterLengthTwo(paths);
            paths = dealPaths(root,paths);
        }
        return paths;
    }


    public void getAllCompletePaths(GroumNode root, List<Path> pathList, List<GroumNode> reList) {
        List<GroumNode> nodeList = new ArrayList<>();
        nodeList.add(root);
        while (nodeList.size() > 0) {
            if(globalFlag) {
                List<GroumNode> tempList = new ArrayList<GroumNode>();
                for (int index = 0; index < nodeList.size(); index++) {
                    GroumNode node = nodeList.get(index);
                    if (!reList.contains(node)) {
                        reList.add(node);
                        getCompletePathsOfReachNode(root,node, pathList, new HashMap<>());
                        for (int j = 0; j < node.getChildNodes().size(); j++) {
                            tempList.add(node.getChildNodes().get(j));
                        }
                    }
                }
                nodeList.removeAll(nodeList);
                nodeList = tempList;
            }
            else{
                pathList.removeAll(pathList);
                return;
            }
        }
        if(!globalFlag){
            pathList.removeAll(pathList);
        }

    }

    public List<Path> dealPaths(GroumNode root, List<Path> list){
        Map<Integer,GroumNode> map = new HashMap<>();
        List<Path> result = new ArrayList<>();
        setScopeIndexToControlNodes(root,new ArrayList<>(),2,map);
        for(Path path: list){
            Path newPath = new Path();
            for(int i = 0; i < path.size() - 1; i ++){
                if((!isControl(path.get(i).getCompleteMethodDeclaration()) && !isControl(path.get(i+1).getCompleteMethodDeclaration())) &&
                        !(path.get(i).getScopeList().toString().equals(path.get(i + 1).getScopeList().toString()))){
                    newPath.add(path.get(i));
                    for(int j = path.get(i).getScopeList().size(); j < path.get(i + 1).getScopeList().size(); j ++){
                        int index = Integer.parseInt(path.get(i + 1).getScopeList().get(j));
                        if(map.containsKey(index)){
                            newPath.add(map.get(index));
                        }
                    }
                }else{
                    newPath.add(path.get(i));
                }
            }
            newPath.add(path.getLast());
            result.add(newPath);
        }
        return result;
    }

    public List<Path> dealPathsWithCondition(GroumNode root, List<Path> list){
        Map<Integer,GroumNode> map = new HashMap<>();
        List<Path> result = new ArrayList<>();
        setScopeIndexToControlNodes(root,new ArrayList<>(),2,map);
        for(Path path: list){
            Path newPath = new Path();
            for(int i = 0; i < path.size() - 1; i ++){
                if((!isControl(path.get(i).getCompleteMethodDeclaration()) && !isControl(path.get(i+1).getCompleteMethodDeclaration())) &&
                        !(path.get(i).getScopeList().toString().equals(path.get(i + 1).getScopeList().toString()))){
                    if(!newPath.contains(path.get(i))) {
                        newPath.add(path.get(i));
                    }
                    for(int j = path.get(i).getScopeList().size(); j < path.get(i + 1).getScopeList().size(); j ++){
                        int index = Integer.parseInt(path.get(i + 1).getScopeList().get(j));
                        if(map.containsKey(index)){
                            if(!newPath.contains(map.get(index))) {
                                newPath.add(map.get(index));
                            }
                            addConditionInPath(map.get(index),path.get(i + 1),newPath);
                        }
                    }
                }else{
                    if(isControl(path.get(i).getCompleteMethodDeclaration())) {
                        if(!newPath.contains(path.get(i))) {
                            newPath.add(path.get(i));
                        }
                        addConditionInPath(path.get(i),path.get(i + 1),newPath);
                    }else{
                        if(!newPath.contains(path.get(i))) {
                            newPath.add(path.get(i));
                        }
                    }
                }
            }
            if(!newPath.contains(path.getLast())) {
                newPath.add(path.getLast());
            }
            result.add(newPath);
        }
        return result;
    }

    public void addConditionInPath(GroumNode node, GroumNode nextNode, Path path){
        GroumNode conditionNode = new GroumNode();
        if(node.getChildNodes().size() > 0){
            conditionNode = node.getChildNodes().get(0);
        }
        if(!conditionNode.equals(nextNode) && conditionNode.isCondition() && !conditionNode.getCompleteMethodDeclaration().equals("conditionEnd")){
            path.add(conditionNode);
            //addConditionInPath(conditionNode,new GroumNode(),path);
        }
    }

    public int setScopeIndexToControlNodes(GroumNode node, List<GroumNode> list, int index,Map<Integer,GroumNode> map) {
        if (node != null && !list.contains(node)) {
            list.add(node);
            if(node.isControl()){
                map.put(index,node);
                index = index + 1;
            }
            for (int i = 0; i < node.getChildNodes().size(); i++) {
                index = setScopeIndexToControlNodes(node.getChildNodes().get(i), list,index, map);
            }
        }
        return index;
    }


    public void getCompletePathsOfReachNode(GroumNode root,GroumNode reachNode, List<Path> pathList, Map<GroumNode, List<Path>> map) {
        List<GroumNode> list = new ArrayList<>();
        list.add(reachNode);
        while (list.size() > 0) {
            List<GroumNode> tempList = new ArrayList<>();
            Map<GroumNode, List<Path>> newMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                GroumNode curNode = list.get(i);
                List<GroumNode> parents = curNode.getParentNodes();
                if(parents != null){
                    for(GroumNode addNode: parents){
                        if(addNode != null && !tempList.contains(addNode)){
                            tempList.add(addNode);
                        }
                    }
                }
                boolean flag = true;
                if (map.containsKey(curNode)) {
                    List<Path> judgePathList = map.get(curNode);
                    List<Path> tempPathList = new ArrayList<>();
                    for (Path p : judgePathList) {
                        if (!p.contains(curNode)) {
                            tempPathList.add(p);
                        }
                    }
                    if (flag) {
                        List<Path> copyPathList = new ArrayList<>();
                        for (Path p : tempPathList) {
                            Path path = new Path();
                            for (GroumNode n : p) {
                                path.add(n);
                            }
                            if(curNode.getChildNodes()!= null  && curNode.getChildNodes().contains(path.get(0)) && !curNode.equals(path.get(0)) && !path.contains(curNode)) {
                                path.add(0, curNode);
                            }
                            copyPathList.add(path);
                        }
                        if (parents != null) {
                            if (parents.size() > 0) {
                                for (GroumNode node : parents) {
                                    if (node != null) {
                                        if(!newMap.containsKey(node)){
                                            newMap.put(node, copyPathList);
                                        }else{
                                            List<Path> mapPathList = newMap.get(node);
                                            for(Path pp:copyPathList){
                                                boolean containFlag = false;
                                                for(Path pp1: mapPathList){
                                                    if(pp1.equals(pp)){
                                                        containFlag = true;
                                                        break;
                                                    }
                                                }
                                                if(!containFlag && node.getChildNodes() != null && node.getChildNodes().contains(pp.get(0)) && !node.equals(pp.get(0)) && !pp.contains(node)){
                                                    mapPathList.add(pp);
                                                }
                                            }
                                        }
                                    } else {
                                        for (Path p : copyPathList) {
                                            if (!isContains(pathList,p) && p.get(0).equals(root) && p.getLast().equals(reachNode)) {
                                                pathList.add(p);
                                            }
//                                            if(pathList.size() > 10000){
//                                                globalFlag =false;
//                                                return;
//                                            }
                                        }
                                    }
                                }
                            } else {
                                for (Path p : copyPathList) {
                                    if (!isContains(pathList,p) && p.get(0).equals(root) && p.getLast().equals(reachNode)) {
                                        pathList.add(p);
                                    }
//                                    if(pathList.size() > 10000){
//                                        globalFlag =false;
//                                        return;
//                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (parents != null) {
                        if (parents.size() > 0) {
                            for (GroumNode node : parents) {
                                Path path = new Path();
                                path.add(0, curNode);
                                List<Path> tempPathList = new ArrayList<>();
                                tempPathList.add(path);
                                if (node != null) {
                                    if(!newMap.containsKey(node)){
                                        newMap.put(node, tempPathList);
                                    }else{
                                        List<Path> mapPathList = newMap.get(node);
                                        for(Path pp:tempPathList){
                                            boolean containFlag = false;
                                            for(Path pp1: mapPathList){
                                                if(pp1.equals(pp)){
                                                    containFlag = true;
                                                    break;
                                                }
                                            }
                                            if(!containFlag && node.getChildNodes() != null && node.getChildNodes().contains(pp.get(0)) && !node.equals(pp.get(0)) && !pp.contains(node)){
                                                mapPathList.add(pp);
                                            }

                                        }
                                    }

                                } else {
                                    for (Path p : tempPathList) {
                                        if (!isContains(pathList,p) && p.get(0).equals(root) && p.getLast().equals(reachNode)) {
                                            pathList.add(p);
                                        }
//                                        if(pathList.size() > 10000){
//                                           globalFlag =false;
//                                            return;
//                                        }
                                    }
                                }
                            }
                        } else {
                            Path path = new Path();
                            path.add(0, curNode);
                            List<Path> tempPathList = new ArrayList<>();
                            tempPathList.add(path);
                            for (Path p : tempPathList) {
                                if (!isContains(pathList,p) && p.get(0).equals(root) && p.getLast().equals(reachNode)) {
                                    pathList.add(p);
                                }
//                                if(pathList.size() > 10000){
//                                    globalFlag =false;
//                                     return;
//                                }
                            }
                        }
                    }
                }
            }
            list.removeAll(list);
            list = tempList;
            map = newMap;
        }

    }

    public List<Path> filterWrongPaths(List<Path> paths){
        List<Path> result = new ArrayList<>();
        for(Path path: paths){
            boolean flag = true;
            List<GroumNode> repeatList = new ArrayList<>();
            for(int i = 0; i < path.size() - 1; i ++){
                if(!repeatList.contains(path.get(i))){
                    repeatList.add(path.get(i));
                }else{
                    flag = false;
                    break;
                }
                if(path.get(i).getChildNodes() != null && !path.get(i).getChildNodes().contains(path.get(i + 1))){
                   flag = false;
                   break;
                }else if(path.get(i).getChildNodes() == null){
                    flag = false;
                    break;
                }
            }
            if(repeatList.contains(path.getLast())){
                flag = false;
            }
            if(flag){
                result.add(path);
            }
        }
        return result;
    }

    public List<Path> extractAllSubPaths(List<Path> paths) {
        List<Path> list = new ArrayList<>();
        for (Path p : paths) {
            for (int i = 0; i < p.size() - 1; i++) {
                Path path = new Path();
                for (int j = i; j < p.size(); j++) {
                    path.add(p.get(j));
                }
                if (path.size() > 1 && !isContains(list, path)) {
                    list.add(path);
                }
            }
        }
        return list;
    }

    public List<Path> extractAllSubPathsFilterLengthTwo(List<Path> paths) {
        List<Path> list = new ArrayList<>();
        for (Path p : paths) {
            if(p.size() > 2) {
                for (int i = 0; i < p.size() - 2; i++) {
                    Path path = new Path();
                    for (int j = i; j < p.size(); j++) {
                        path.add(p.get(j));
                    }
                    if (path.size() > 2 && !isContains(list, path)) {
                        list.add(path);
                    }
                }
            }else{
                if (!isContains(list, p)) {
                    list.add(p);
                }
            }
        }
        return list;
    }




    public boolean isContains(List<Path> list, Path p) {
        for (Path path : list) {
            if (path.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param ddMap < pathLabel, <node-node, countNum> >
     */
    public void updateDataDependency(List<Path> paths,
                                     HashMap<String, HashMap<String, Integer>> ddMap) {

        for (Path p : paths) {
            String label = p.getLabel();
            //System.out.println(label);
            HashMap<String, Integer> countMap;
            if ((countMap = ddMap.get(label)) == null) {
                countMap = new HashMap<>();
                ddMap.put(label, countMap);
            }
            updateDDCount(countMap, p);
        }
    }


    public void updatePathCount(List<Path> paths,
                                HashMap<String, Pair<Integer, Integer>> countMap) {
        for (Path p : paths) {
            String label = p.getLabel();
            if (countMap.containsKey(label)) {
                Pair<Integer, Integer> pair = countMap.get(label);
                pair.b = pair.b + 1;
            } else {
                Pair<Integer, Integer> pair = new Pair<>(p.size(), 1);
                countMap.put(label, pair);
            }
        }
    }

    /**
     * for Path p, countMap record the times of data dependency
     *
     * @param countMap
     * @param p
     */
    private void updateDDCount(HashMap<String, Integer> countMap, Path p) {
        if (p.size() <= 1)
            return;
        HashMap<GroumNode, String> labelMap = p.getLabelMap();
        GroumNode end = p.get(p.size() - 1);
        for (int i = 0; i < p.size() - 1; i++) {
            GroumNode start = p.get(i);
            String lable = labelMap.get(start) + Path.API_SPLIT + labelMap.get(end);
            Integer count = hasDataDependency(start, end) ? 1 : 0;
            if (countMap.containsKey(lable)) {
                count += countMap.get(lable);
            }
            countMap.put(lable, count);
        }
    }

    private boolean hasDataDependency(GroumNode start, GroumNode end) {
        for (GroumNode node : start.getChildNodes()) {
            if (node == end)
                return true;
        }
        return false;
    }

    public static boolean isControl(String prediction){
        List<String> list = new ArrayList<>();
        list.add("if");
        list.add("elseif");
        list.add("else");
        list.add("for");
        list.add("while");
        list.add("doWhile");
        list.add("foreach");
        list.add("try");
        list.add("catch");
        list.add("finally");
        list.add("switch");
        list.add("case");
        list.add("deafult");
        if(list.contains(prediction)){
            return true;
        }
        return false;
    }

}
