import config.DataConfig;
import parameterModel.model.db.Record;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * todo: replace the constant file path
 */
public class LoadSource {
    public static Map<String, Record> map = new HashMap<>();
    public static Map<String,Boolean> castMap = new HashMap<>();
    public static Map<String,String> tipMap = new HashMap<>();
    public static Map<String,Boolean> staticMethodsMap = new HashMap<>();
    public static Map<String,String> apiReturnTypeMap = new HashMap<>();
    public static Map<String,String> singleAPIMap = new HashMap<>();

    public static void initMap(){
        if(map.size() > 0){
            return;
        }else{
            try {
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("/home/x/mydisk/HuaWeiProject/groumAndroidMap.txt"));
                //ObjectInputStream in = new ObjectInputStream(new FileInputStream("/home/x/mydisk/groumMapNew7_1.txt"));
                //ObjectInputStream in = new ObjectInputStream(new FileInputStream("/mydisk/fudan_se_dl/code_recommendation/groumMapNew7_1.txt"));
                Object object = null;
                object = in.readObject();
                map = (Map<String, Record>) object;
                in.close();
            }catch(Exception e){

            }
        }
    }


    public static void initCastMap(){
        if(castMap.size() > 0){
            return;
        }else{
            try {
                File fileTypeCast = new File(DataConfig.TYPE_CAST_CONFIG_FILE_PATH);
                FileInputStream fileInputStream = new FileInputStream(fileTypeCast);
                Scanner scanner = new Scanner(fileInputStream);
                while (scanner.hasNextLine()) {
                    castMap.put(scanner.nextLine(), true);
                }
                scanner.close();
                fileInputStream.close();
            }catch(Exception e){
                System.out.println(e.getMessage());
            }catch(Error e){
                System.out.println(e.getMessage());
            }
        }
    }

    public static void initTipMap(){
        if(tipMap.size() > 0){
            return;
        }else{
            try {
                File tipFile = new File("/home/x/mydisk/total_functionality_descriptions.csv");
                //File tipFile = new File("/mydisk/fudan_se_dl/code_recommendation/total_functionality_descriptions.csv");
                BufferedReader br  = new BufferedReader(new FileReader(tipFile));
                String line = null;
                while((line = br.readLine()) != null){
                    String[] strs = line.split(";");
                    int length = strs.length;
                    String tip = "API Description: ";
                    String api = strs[3];
                    api = api.replaceAll("T\\[\\]","java\\.lang\\.Object\\[\\]");
                    api = api.replaceAll("T\\.\\.\\.","java\\.lang\\.Object\\[\\]");
                    api = api.replaceAll("\\.\\.\\.","\\[\\]");
                    String tempAPI = api;
                    int count = 0;
                    while(tempAPI.contains(">")){
                        tempAPI = tempAPI.replaceFirst(">","");
                        count ++;
                    }
                    count -= 1;
                    for(int i = 0; i < count;i ++){
                        api = api.replaceFirst(">","");
                    }
                    String reg = "\\<[^\\>]*\\>";
                    api = api.replaceAll(reg,"");
                    api = api.replaceAll("\"","");
                    for(int i = 4; i < length - 1; i ++){
                        tip += strs[i];
                    }
                    tip = tip.replaceAll("\"","");
                    if(!tipMap.containsKey(api)){
                        tipMap.put(api,tip);
                    }
                }
                br.close();
            }catch(Exception e){

            }
        }
    }
}

