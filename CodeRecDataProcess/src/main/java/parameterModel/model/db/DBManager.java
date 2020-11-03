package parameterModel.model.db;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * todo: replcace constant file path
 */
public class DBManager {

    // 驱动程序名
    String driver = "com.mysql.jdbc.Driver";
    // URL指向要访问的数据库名scutcs
    String url = "jdbc:mysql://127.0.0.1:3306/path";
    // MySQL配置时的用户名
    String user = "root";
    // MySQL配置时的密码
    String password = "root";
    Connection conn;

    int batchSize = 1000;

    public DBManager() {
        try {
            Class.forName(driver);
            // 连续数据库
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            if (!conn.isClosed())
                System.out.println("Succeeded connecting to the Database!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DBManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        try {
            Class.forName(driver);
            // 连续数据库
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            if (!conn.isClosed())
                System.out.println("Succeeded connecting to the Database!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Record> select(String sql) {
        List<Record> records = new LinkedList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE); // 通过流方式读取
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String path = rs.getString("path");
                int length = rs.getInt("length");
                String data_dependency = rs.getString("data_dependency");
                String endNodeLabel = rs.getString("endNodeLabel");
                int count = rs.getInt("count");
                Record record = new Record(path, length, data_dependency, endNodeLabel, count);
                records.add(record);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }

    public void insert(List<Record> list, String table) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into " + table
                    + " (`path`, `length`, `data_dependency`, `endNodeLabel`, `count`)  values (?,?,?,?,?)");

            for (int i = 0; i < list.size(); i++) {
                ps.setString(1, list.get(i).getPath());
                ps.setInt(2, list.get(i).getLength());
                ps.setString(3, list.get(i).getData_dependency());
                ps.setString(4, list.get(i).getEndNodeLabel());
                ps.setInt(5, list.get(i).getCount());
                ps.addBatch();
                if ((i + 1) % batchSize == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            if (list.size() % batchSize != 0) {
                ps.executeBatch();
                conn.commit();
                ps.clearBatch();
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insert(Record record, String table) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into " + table
                    + " (`path`, `length`, `data_dependency`, `endNodeLabel`, `count`)  values (?,?,?,?,?)");
            ps.setString(1, record.getPath());
            ps.setInt(2, record.getLength());
            ps.setString(3, record.getData_dependency());
            ps.setString(4, record.getEndNodeLabel());
            ps.setInt(5, record.getCount());
            ps.addBatch();
            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update records in table
     * note : all length of Record in list should be same
     *
     * @param list
     * @param table
     */
    public void update(List<Record> list, String table) {
        try {
            if (list.isEmpty())
                return;
            PreparedStatement ps = conn.prepareStatement("update " + table
                    + " set data_dependency=?, count=? WHERE path=?");

            for (int i = 0; i < list.size(); i++) {
                ps.setString(1, list.get(i).getData_dependency());
                ps.setInt(2, list.get(i).getCount());
                ps.setString(3, list.get(i).getPath());
                ps.addBatch();
                if ((i + 1) % batchSize == 0) {
                    ps.executeBatch();
                }
            }
            if (list.size() % batchSize != 0) {
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * all Records in list should have different path
     * insert new record and update old record
     *
     * @param list
     * @param table
     */
    public void insertAndUpdate(List<Record> list, String table) {
        try {
            PreparedStatement ips = conn.prepareStatement("insert into " + table
                    + " (`path`, `length`, `data_dependency`, `endNodeLabel`, `count`)  values (?,?,?,?,?)");

            PreparedStatement ups = conn.prepareStatement("update " + table
                    + " set data_dependency=?, count=? WHERE path=?");

            for (int i = 0; i < list.size(); i++) {
                Record record = list.get(i);
                String label = record.getPath();
                String sql = "select * from " + table + " where path=" + "'" + label + "'";
                List<Record> records = select(sql);
                if (records.isEmpty()) {
                    ips.setString(1, label);
                    ips.setInt(2, record.getLength());
                    ips.setString(3, record.getData_dependency());
                    ips.setString(4, record.getEndNodeLabel());
                    ips.setInt(5, record.getCount());
                    ips.addBatch();
                } else {
                    Record oldRecord = records.get(0);
                    record.merge2(oldRecord);
                    ups.setString(1, record.getData_dependency());
                    ups.setInt(2, record.getCount());
                    ups.setString(3, record.getPath());
                    ups.addBatch();
                }
                if ((i + 1) % batchSize == 0) {
                    ips.executeBatch();
                    ups.executeBatch();
                }
            }
            if (list.size() % batchSize != 0) {
                ips.executeBatch();
                ups.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}