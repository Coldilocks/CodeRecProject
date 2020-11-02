package parameterModel.model;

import parameterModel.GroumNode;

import java.util.HashMap;
import java.util.LinkedList;

public class Path extends LinkedList<GroumNode> {
    public static String API_SPLIT = "/";
    public static String DD_SPLIT = ";";
    @Override
    public String toString(){
//        StringBuilder sb = new StringBuilder();
//        for(GroumNode node : this){
//            sb.append(node.getCompleteMethodDeclaration()+ "->");
//        }
//        return sb.toString();
        return this.getLabel();
    }

    public boolean equals(Path path){
        if(this.size() == path.size()){
            for(int i = 0; i < path.size(); i ++){
                if(!path.get(i).equals(this.get(i))){
                    return false;
                }
            }
        }else{
            return false;
        }

        return true;
    }

    public String getLabel(){
        HashMap<String, Integer> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for(GroumNode node : this){
            String key = node.getCompleteMethodDeclaration();
            if (map.containsKey(key)){ // same label
                int index = map.get(key) + 1;
                sb.append(key + "_" + index + API_SPLIT);
                map.put(key, index);
            }else {
                sb.append(key + API_SPLIT);
                map.put(key, 0);
            }
        }
        String s = sb.toString();
        if (this.size()>0){
            s = s.substring(0, s.length()-API_SPLIT.length());
        }
        return s;
    }

    public HashMap<GroumNode, String> getLabelMap(){
        HashMap<String, Integer> map = new HashMap<>();
        HashMap<GroumNode, String> labelMap = new HashMap<>();
        for(GroumNode node : this){
            String key = node.getCompleteMethodDeclaration();
            if (map.containsKey(key)){ // same label
                int index = map.get(key) + 1;
                map.put(key, index);
                labelMap.put(node, key + "_" + index);
            }else {
                map.put(key, 0);
                labelMap.put(node, key);
            }
        }
        return labelMap;
    }

}