package parameterModel.model;

import parameterModel.Groum;
import parameterModel.GroumNode;
import parameterModel.model.db.Record;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StorePathMain {
    public static void main(String[] args) {
        try {
            PathModel pathModel = new PathModel();
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("/home/x/mydisk/groum.txt"));
            Object object = null;
            int count = 0;
            //List<Groum> list = new ArrayList<>();
            Map<String, Record> map = new HashMap<>();
            while ((object = in.readObject()) != null) {
                Groum groum = (Groum) object;
                count += 1;
                // System.out.println("deal " + count + " groum";
                if (groum.getTotalNumber(new ArrayList<>()) <= 50 && count != 10389 && count != 32982
                        && count != 32984 && count != 33320 && count != 40060 && count != 45737
                        && count != 53771 && count != 56321 && count != 72694 && count != 72824 && count != 73080
                        && count != 112371 && count != 117067 && count != 121218 && count != 124999
                        && count != 129880 && count != 129882 && count != 129894 && count != 129924 && count != 146333
                        && count != 168264 && count != 169166 && count != 171665 && count != 198376 && count != 202293
                        && count != 234741 && count != 256022 && count != 256063 && count != 256064 && count != 267813
                        && count != 286157 && count != 325800) {
                    // Map<Integer, GroumNode> scopeMap = new HashMap<>();
                    // groum.setScopeIndexToControlNodes(groum.getRoot(),new ArrayList<>(),2,scopeMap);
                    // groum.addAllDataDependencyNodeToControlNodes(groum.getRoot(),new ArrayList<>(),scopeMap);
                    pathModel.dealGroum(groum, map);
                    //System.out.println(count + " done!");
                }

                if (count % 1000 == 0) {
                    System.out.println("deal " + count + " groum");
                }
            }

            // handle rest
            //if (list.size() > 0){
            //    pathModel.importDBBatch(list);
            //    System.out.println(count + " done!");
            //    list.removeAll(list);
            //}

            in.close();
            System.out.println("start store");
            //pathModel.storeGroumMap2(map);
            //System.err.println("total data amount " + map.size());
            // pathModel.insertToDB(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            PathModel pathModel = new PathModel();
//            ObjectInputStream in = new ObjectInputStream(new FileInputStream("/home/x/mydisk/groum.txt"));
//            Object object = null;
//            int count = 0;
//            List<Groum> list = new ArrayList<>();
//            while((object =in.readObject())!=null) {
//                Groum groum = (Groum)object;
//                count += 1;
//                if(groum.getTotalNumber(new ArrayList<>()) <= 50){
//                    //pathModel.importDB(groum);
//                    //System.out.println(count + " done!");
//                    //count += 1;
//                    list.add(groum);
//                }
////                else{
////                    System.out.println(count + " done!");
////                }
//                if(count % 100 == 0 && count != 0){
//                    pathModel.importDBBatch(list);
//                    System.out.println(count + " done!");
//                    list.removeAll(list);
//                }
//            }
//            in.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
}
