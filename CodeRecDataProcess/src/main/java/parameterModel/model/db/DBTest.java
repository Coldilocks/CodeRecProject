package parameterModel.model.db;

import parameterModel.model.Path;

import java.util.LinkedList;
import java.util.List;

public class DBTest {
    public static void main(String[] args){
        String table = "patht1";
        List<Record> records = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            String path = i + Path.API_SPLIT + (i+1) + Path.API_SPLIT + (i+2);
            String dd = i + Path.API_SPLIT + (i+1) + Path.API_SPLIT + "1" + Path.DD_SPLIT
                    + (i+1) + Path.API_SPLIT + (i+2) + Path.API_SPLIT + "1" + Path.DD_SPLIT;
            dd = dd.substring(0, dd.length()-Path.DD_SPLIT.length());
            Record r = new Record(path, 3, dd, ""+i, 1);
            records.add(r);
        }
        String url = "jdbc:mysql://127.0.0.1:3306/coderecommendation";
        String user = "root";
        String password = "root";
        DBManager manager = new DBManager(url, user, password);
        manager.insertAndUpdate(records, table);
        String sql = "select * from " + table;
        List<Record> list = manager.select(sql);
        for (Record r : list){
            System.out.println(r.getPath() + "\t" + r.getData_dependency() + "\t" + r.getCount());
        }
        manager.close();
    }
}
