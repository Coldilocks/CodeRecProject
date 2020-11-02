package parameterModel;

//import jnr.ffi.Struct;
import parameterModel.model.PathModel;
import parameterModel.model.db.Record;
import parameterModel.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenchi on 18/2/4.
 */
public class NaturalnessPrediction {
    public  List<List<String>> sizes = new ArrayList<>();
    public  List<String> counts = new ArrayList<>();
    public  Map<String,List<Pair<GroumNode, Double>>> parameterMap = new HashMap<>();

    public  List<String> getNaturalnessPrediction_1(Groum groum, List<String> treeLSTMResult,Map<String, Record> map){
        List<String> naturalnessResult = new ArrayList<>();
        try {
            List<String> tempTreeLSTMResult = new ArrayList<>();
            for(int i = 0; i < treeLSTMResult.size(); i ++){
                tempTreeLSTMResult.add(treeLSTMResult.get(i).split(" +")[0]);
            }
            List<Groum> codes = new ArrayList<>();
            List<GroumNode> appenedNodes = new ArrayList<>();
            for(int i = 0; i < tempTreeLSTMResult.size(); i ++) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(groum);
                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bis);
                Groum newGroum = (Groum) ois.readObject();
                GroumNode appendedNode = new GroumNode();
                newGroum.replaceHoleNode(appendedNode,new ArrayList<>());
                appendedNode.setCompleteMethodDeclaration(tempTreeLSTMResult.get(i));
                appenedNodes.add(appendedNode);
                codes.add(newGroum);
                oos.close();
                ois.close();
            }
            PathModel pathModel = new PathModel();
            List<Double> probs = pathModel.APISuggestion_1(codes,appenedNodes,map,sizes,counts);
            for(int i = 0; i < codes.size(); i ++){
                naturalnessResult.add(tempTreeLSTMResult.get(i) + " " + probs.get(i).doubleValue());
            }
            parameterMap = pathModel.parameterMap;
            bubbleSort(naturalnessResult);
            return naturalnessResult;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return naturalnessResult;
    }

    private void bubbleSort(List<String> sortNum){
        String temp = null;
        String tempCount = null;
        for (int i = 0; i < sortNum.size() - 1; i++) {
            for (int j = 0; j < sortNum.size()-1-i; j++) {
                if(Double.parseDouble(sortNum.get(j + 1).split(" +")[1]) > Double.parseDouble(sortNum.get(j).split(" +")[1])){
                    temp = sortNum.get(j);
                    sortNum.set(j,sortNum.get(j + 1));
                    sortNum.set(j + 1,temp);
                    tempCount = counts.get(j);
                    counts.set(j,counts.get(j + 1));
                    counts.set(j + 1,tempCount);

                }
            }
        }
    }
}
