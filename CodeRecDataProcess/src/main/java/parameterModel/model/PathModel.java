package parameterModel.model;

import parameterModel.Groum;
import parameterModel.GroumNode;
import parameterModel.model.db.DBManager;
import parameterModel.model.db.Record;
import parameterModel.util.Pair;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class PathModel {
    public Map<String,List<Pair<GroumNode, Double>>> parameterMap = new HashMap<>();
    public List<Double> APISuggestion_1(List<Groum> codes, List<GroumNode> appenedNodes, Map<String, Record> map, List<List<String>> sizes, List<String> counts) {
        List<Double> suggestion = new ArrayList<>(appenedNodes.size());
        List<Integer> levels = new ArrayList<>(suggestion.size());
        double sum = 0;
        for (int i = 0; i < appenedNodes.size(); i++) {
            int degree = getAPISuggestionDegree_1(codes.get(i), appenedNodes.get(i), map, sizes);
            counts.add(Integer.toString(degree));
            levels.add(degree);
            sum += degree;
        }
        if (sum == 0) {
            sum = 1;
        }
        for (int i = 0; i < levels.size(); i++) {
            suggestion.add(levels.get(i) / sum);
        }
        return suggestion;
    }


    public int getAPISuggestionDegree_1(Groum code, GroumNode appenedNode, Map<String, Record> map, List<List<String>> sizes) {
        DBManager dbManager = null;
        PathExplorer2 explorer = new PathExplorer2();
        List<Path> paths = explorer.getPaths(code.getRoot(), appenedNode, PathExplorer2.CHILD_2_PARENT); // get all direct paths reach appenedNode
        if (!explorer.globalFlag) {
            return 0;
        }
        getNodeProbability(paths,appenedNode, map);
        List<String> size = new ArrayList<>();
        for (Path p : paths) {
            size.add(Integer.toString(p.size()));
        }
        sizes.add(size);
        int directDDCount = 0;
        int maxDiffDistance = 2;
        long start = System.currentTimeMillis();
        //System.out.println();
        for (Path p : paths) {
            directDDCount += fetchFromSimilarPath_1(p, maxDiffDistance, dbManager, appenedNode.getCompleteMethodDeclaration(), map);
        }
        /**
         * --------------------------------------------------------------------------------------
         * get all paths from root to nodes in condition branch ,
         * and assume that there are paths reaching appenedNode by appending it to the end of path
         */
        return directDDCount;
    }



    private int fetchFromSimilarPath_1(Path p, int maxDiffDistance, DBManager dbManager, String endNodeLabel, Map<String, Record> map) {
        int directDDCount = 0;
        List<Record> rs = new ArrayList<>();
        if (map.get(p.getLabel()) != null) {
            rs.add(map.get(p.getLabel()));
        } else {
            rs.add(null);
        }
        for (Record r : rs) {
            try {
               // String target = p.getFirst().getCompleteMethodDeclaration() + Path.API_SPLIT + p.getLabelMap().get(p.getLast())+Path.API_SPLIT+"0";
                String[] dds = r.getData_dependency().split(Path.DD_SPLIT); // else/end/0;int.Declaration/end/0;for/end/0;if/end/0;java.lang.Math.max(int,int)/end/3;if_1/end/0
//                for (String dd : dds) {
//                    if (dd.equals(target))
//                        return 0;
//                }
                for (String dd : dds) {
                    String[] apis = dd.split(Path.API_SPLIT);
                    int count = apis[apis.length - 1].length() == 0 ? 1 : Integer.parseInt(apis[apis.length - 1]);
                    directDDCount += count;
                }

            } catch (Exception e) {
                directDDCount += 0;
            }
        }
        return directDDCount;
    }


    public void storeGroumMap2(Map<String, Record> groumMap) {
        try {
            ObjectOutputStream groumMapWriter = new ObjectOutputStream(new FileOutputStream("/home/x/mydisk/groumMapNew7_1.txt"));
            groumMapWriter.writeObject(groumMap);
            groumMapWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    public boolean dealGroum(Groum code, Map<String, Record> groumMap) {
        PathExplorer2 explorer = new PathExplorer2();
        HashMap<String, Pair<Integer, Integer>> pathCount = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> dataDepends = new HashMap<>();
        List<Path> paths = explorer.getPaths(code.getRoot(), code.getRoot(), PathExplorer2.PARENT_2_CHILD);
        if (!explorer.globalFlag) {
            return false;
        }
        explorer.updatePathCount(paths, pathCount);
        explorer.updateDataDependency(paths, dataDepends);
        for (Map.Entry<String, HashMap<String, Integer>> entry : dataDepends.entrySet()) {
            String pathLabel = entry.getKey();
            Record record = new Record();
            record.setPath(pathLabel);
            record.setLength(pathCount.get(pathLabel).a);
            record.setCount(pathCount.get(pathLabel).b);
            String[] apis = pathLabel.split(Path.API_SPLIT);
            record.setEndNodeLabel(apis.length <= 1 ? pathLabel : apis[apis.length - 1]);
            HashMap<String, Integer> dataD = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> e : dataD.entrySet()) {
                sb.append(e.getKey() + Path.API_SPLIT + e.getValue() + Path.DD_SPLIT);
            }
            record.setData_dependency(sb.toString());
            if (!groumMap.containsKey(pathLabel)) {
                groumMap.put(pathLabel, record);
            } else {
                Record oldRecord = groumMap.get(pathLabel);
                record.merge2(oldRecord);
                groumMap.replace(pathLabel, record);
            }
        }
        return true;
    }

    /**
     * insert and update paths of code in database
     *
     * @param code
     */
    public void importDB(Groum code) {
        PathExplorer2 explorer = new PathExplorer2();
        HashMap<String, Pair<Integer, Integer>> pathCount = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> dataDepends = new HashMap<>();
        List<Path> paths = explorer.getPaths(code.getRoot(), code.getRoot(), PathExplorer2.PARENT_2_CHILD);
        explorer.updatePathCount(paths, pathCount);
        explorer.updateDataDependency(paths, dataDepends);
        List<Record> records = new LinkedList<>();
        for (Map.Entry<String, HashMap<String, Integer>> entry : dataDepends.entrySet()) {
            String pathLabel = entry.getKey();
            Record record = new Record();
            record.setPath(pathLabel);
            record.setLength(pathCount.get(pathLabel).a);
            record.setCount(pathCount.get(pathLabel).b);
            String[] apis = pathLabel.split(Path.API_SPLIT);
            record.setEndNodeLabel(apis.length <= 1 ? pathLabel : apis[apis.length - 1]);
            HashMap<String, Integer> dataD = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> e : dataD.entrySet()) {
                sb.append(e.getKey() + Path.API_SPLIT + e.getValue() + Path.DD_SPLIT);
            }
            record.setData_dependency(sb.toString());
            records.add(record);
        }

        String url = "jdbc:mysql://127.0.0.1:3306/path";
        String user = "root";
        String password = "fdse";
        DBManager manager = new DBManager(url, user, password);
        splitAndInsertUpdate(records, manager);
        manager.close();
    }

    public void importDBBatch(List<Groum> codes) {
        int MAX_BATCH_SIZE = 20000;
        String url = "jdbc:mysql://127.0.0.1:3306/path";
        String user = "root";
        String password = "fdse";
        DBManager manager = new DBManager(url, user, password);
        HashMap<String, Pair<Integer, Integer>> pathCount = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> dataDepends = new HashMap<>();
        PathExplorer2 explorer = new PathExplorer2();
        for (Groum code : codes) {
            try {
                List<Path> paths = explorer.getPaths(code.getRoot(), code.getRoot(), PathExplorer2.PARENT_2_CHILD);
                explorer.updatePathCount(paths, pathCount);
                explorer.updateDataDependency(paths, dataDepends);
                if (dataDepends.size() > MAX_BATCH_SIZE) {
                    List<Record> records = extractRecords(dataDepends, pathCount);
                    splitAndInsertUpdate(records, manager);
                    pathCount.clear();
                    dataDepends.clear();
                }
            } catch (Exception e) {
                // exception is supposed to be thrown from getPaths() method
                // for safety, pathCount and dataDepends should be restored if throws exception,
                // but ignore it now.
                e.printStackTrace();
            }
        }
        if (dataDepends.size() > 0) {
            List<Record> records = extractRecords(dataDepends, pathCount);
            splitAndInsertUpdate(records, manager);
        }
        manager.close();
    }

    public void importDBAll(List<Groum> codes) {
        String url = "jdbc:mysql://127.0.0.1:3306/path";
        String user = "root";
        String password = "fdse";
        DBManager manager = new DBManager(url, user, password);
        HashMap<String, Pair<Integer, Integer>> pathCount = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> dataDepends = new HashMap<>();
        PathExplorer2 explorer = new PathExplorer2();
        for (Groum code : codes) {
            try {
                List<Path> paths = explorer.getPaths(code.getRoot(), code.getRoot(), PathExplorer2.PARENT_2_CHILD);
                explorer.updatePathCount(paths, pathCount);
                explorer.updateDataDependency(paths, dataDepends);
            } catch (Exception e) {
                // exception is supposed to be thrown from getPaths() method
                // for safety, pathCount and dataDepends should be restored if throws exception,
                // but ignore it now.
                e.printStackTrace();
            }
        }
        if (dataDepends.size() > 0) {
            List<Record> records = extractRecords(dataDepends, pathCount);
            splitAndInsert(records, manager);
        }
        manager.close();
    }

    /**
     * change dataDependency to Record list
     *
     * @param dataDepends
     * @param pathCount
     * @return
     */
    private List<Record> extractRecords(HashMap<String, HashMap<String, Integer>> dataDepends,
                                        HashMap<String, Pair<Integer, Integer>> pathCount) {
        List<Record> records = new LinkedList<>();
        for (Map.Entry<String, HashMap<String, Integer>> entry : dataDepends.entrySet()) {
            String pathLabel = entry.getKey();
            Record record = new Record();
            record.setPath(pathLabel);
            record.setLength(pathCount.get(pathLabel).a);
            record.setCount(pathCount.get(pathLabel).b);
            String[] apis = pathLabel.split(Path.API_SPLIT);
            record.setEndNodeLabel(apis.length <= 1 ? pathLabel : apis[apis.length - 1]);
            HashMap<String, Integer> dataD = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> e : dataD.entrySet()) {
                sb.append(e.getKey() + Path.API_SPLIT + e.getValue() + Path.DD_SPLIT);
            }
            record.setData_dependency(sb.toString());
            records.add(record);
        }
        return records;
    }

    /**
     * split Records into different table and insert
     *
     * @param records
     */
    public void splitAndInsertUpdate(List<Record> records, DBManager manager) {
        HashMap<String, List<Record>> recordQ = new HashMap<>(); // <table, tuples>
        for (Record record : records) {
            String table = getTableName(record);
            List<Record> list;
            if ((list = recordQ.get(table)) == null) {
                list = new LinkedList<>();
                recordQ.put(table, list);
            }
            list.add(record);
        }
        for (Map.Entry<String, List<Record>> entry : recordQ.entrySet()) {
            manager.insertAndUpdate(entry.getValue(), entry.getKey());
        }
    }

    public void splitAndInsert(List<Record> records, DBManager manager) {
        HashMap<String, List<Record>> recordQ = new HashMap<>(); // <table, tuples>
        for (Record record : records) {
            String table = getTableName(record);
            List<Record> list;
            if ((list = recordQ.get(table)) == null) {
                list = new LinkedList<>();
                recordQ.put(table, list);
            }
            list.add(record);
        }
        for (Map.Entry<String, List<Record>> entry : recordQ.entrySet()) {
            manager.insert(entry.getValue(), entry.getKey());
        }
    }

    private String getTableName(Record record) {
        String table = "patht";
        table = table + record.getLength();
        return table;
    }

    private String getTableName(int length) {
        String table = "patht";
        table = table + length;
        return table;
    }

    public static boolean isControl(String prediction) {
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
        //list.add("break");
        //list.add("continue");
        //list.add("return");
        if (list.contains(prediction)) {
            return true;
        }
        return false;
    }


    /**
     * given a groum and its end, extract all paths and calculate the dd count of one node
     * @param map
     * @return
     */
    public void getNodeProbability(List<Path> paths, GroumNode appendNode, Map<String, Record> map) {
        List<Pair<GroumNode, Double>> parameterList = new ArrayList<>();
        HashMap<GroumNode, Integer> nodeCount = new HashMap<>();
        int sum = 0;
        for (Path p : paths) {
            for(GroumNode node: p){
                if(!nodeCount.containsKey(node) && node.getVariableName() != null){
                    nodeCount.put(node,0);
                }
                if(node.isControl() && node.getVariableName() !=null && (node.getCompleteMethodDeclaration().equals("for")||node.getCompleteMethodDeclaration().equals("foreach"))){
                    GroumNode child = node.getChildNodes().get(0);
                    if(!child.getCompleteMethodDeclaration().equals("conditionEnd") && !nodeCount.containsKey(child) && child.getVariableName() !=null){
                        nodeCount.put(child,0);
                    }
                }
            }
            sum +=updateNobeCount(p, map.get(p.getLabel()), nodeCount);
        }
        if(sum == 0){
           sum = 1;
        }
        if (sum > 0) {
            for (Map.Entry<GroumNode, Integer> entry : nodeCount.entrySet()) {
                parameterList.add(new Pair<>(entry.getKey(), 1.0 * entry.getValue() / sum));
            }
        }
        parameterMap.put(appendNode.getCompleteMethodDeclaration(),parameterList);

    }

    private int updateNobeCount(Path p, Record r, HashMap<GroumNode, Integer> nodeCount) {
        int sum = 0;
        HashMap<String, GroumNode> nodeMap = new HashMap<>();
        for (Map.Entry<GroumNode, String> entry : p.getLabelMap().entrySet()){
            nodeMap.put(entry.getValue(), entry.getKey());
        }
        try {
            //String target = p.getFirst().getCompleteMethodDeclaration() + Path.API_SPLIT + p.getLabelMap().get(p.getLast())+Path.API_SPLIT+"0";
            String[] dds = r.getData_dependency().split(Path.DD_SPLIT); // else/end/0;int.Declaration/end/0;for/end/0;if/end/0;java.lang.Math.max(int,int)/end/3;if_1/end/0
//            for (String dd : dds) {
//                if (dd.equals(target))
//                    return 0;
//            }
            for (String dd : dds) {
                String[] apis = dd.split(Path.API_SPLIT);
                int count = apis[apis.length - 1].length() == 0 ? 1 : Integer.parseInt(apis[apis.length - 1]);
                String nodeLabel = apis[0];
                if (nodeMap.containsKey(nodeLabel)){
                    GroumNode node = nodeMap.get(nodeLabel);
                    if (nodeCount.containsKey(node)){
                        nodeCount.put(node, count+nodeCount.get(node));
                    }else {
                        if(node.getVariableName() != null){
                            nodeCount.put(node, count);
                        }
                    }
                }
                sum += count;
            }

        } catch (Exception e) {
        }
        return sum;
    }
}

