package parameterModel.model.db;

import parameterModel.model.Path;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Record implements Serializable {
    String path = "";
    int length;
    String data_dependency = "";
    String endNodeLabel = "";
    int count = 0;

    public Record(){

    }

    public Record(String path, int length, String data_dependency, String endNodeLabel, int count){
        this.path = path;
        this.length = length;
        this.data_dependency = data_dependency;
        this.endNodeLabel = endNodeLabel;
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getData_dependency() {
        return data_dependency;
    }

    public void setData_dependency(String data_dependency) {
        this.data_dependency = data_dependency;
    }

    public String getEndNodeLabel() {
        return endNodeLabel;
    }

    public void setEndNodeLabel(String endNodeLable) {
        this.endNodeLabel = endNodeLable;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * database should be empty, otherwise throws exception
     * @param record
     */
    public void merge(Record record) {
        this.count += record.getCount();
        if (this.length == 1)
            return;
        String[] dds1 = data_dependency.split(Path.DD_SPLIT);
        String[] dds2 = record.getData_dependency().split(Path.DD_SPLIT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<dds1.length; i++){
            int index = dds1[i].lastIndexOf(Path.API_SPLIT);
            int count1 = Integer.parseInt(dds1[i].substring(index+1));
            int count2 = Integer.parseInt(dds2[i].substring(index+1));
            sb.append(dds1[i].substring(0, index+1) + (count1+count2) + Path.DD_SPLIT);
        }
        String content = sb.toString();
        if (dds1.length > 0){
            content = content.substring(0, content.length() - Path.DD_SPLIT.length());
        }
        this.data_dependency = content;
    }

    /**
     * no need to clean data base, but is slow
     * @param record
     */
    public void merge2(Record record) {
        if (!path.equals(record.getPath())){
            System.err.println("Error! Wrong merge!");
            return;
        }
        this.count += record.getCount();
        if (this.length == 1)
            return;
        HashMap<String, Integer> map = new HashMap<>();
        String[] dds1 = data_dependency.split(Path.DD_SPLIT);
        for (String dd : dds1){
            int index = dd.lastIndexOf(Path.API_SPLIT);
            String ddKey = dd.substring(0, index+1);
            int count = Integer.parseInt(dd.substring(index+1));
            map.put(ddKey, count);
        }
        String[] dds2 = record.getData_dependency().split(Path.DD_SPLIT);
        for (String dd : dds2){
            int index = dd.lastIndexOf(Path.API_SPLIT);
            String ddKey = dd.substring(0, index+1);
            int count = Integer.parseInt(dd.substring(index+1));
            if (map.containsKey(ddKey))
                count += map.get(ddKey);
            map.put(ddKey, count);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : map.entrySet()){
            sb.append(entry.getKey() + entry.getValue() + Path.DD_SPLIT);
        }
        String content = sb.toString();
        if (map.size() > 0){
            content = content.substring(0, content.length() - Path.DD_SPLIT.length());
        }
        this.data_dependency = content;
    }
}
