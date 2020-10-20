package codeAnalysis.codeProcess;

import java.util.ArrayList;
import java.util.List;

public class UserClassProcessing {

    private List<String> userClassList = new ArrayList<>();

    private List<String> jdkList;

    public List<String> getUserClassList() {
        return userClassList;
    }

    public void addUserClass(String clazz){
        userClassList.add(clazz);
    }

    public void setJdkList(List<String> jdkList) {
        this.jdkList = jdkList;
    }

    public void setUserClassList(List<String> userClassList) {
        this.userClassList = userClassList;
    }

    public boolean isUserClassProcessing(String type) {
        if(type != null) {
            type = type.replaceAll("\\[\\]", "");
            if(type.equals("null")){
                return true;
            }
            for (int i = 0; i < userClassList.size(); i++) {
                String str = userClassList.get(i);
                String[] strs = str.split("\\.");
                str = strs[strs.length - 1];
                if (userClassList.get(i).equals(type) || str.equals(type)) {
                    return true;
                }
            }
            String[] simpleTypes = type.split("\\.");
            String simpleType = simpleTypes[simpleTypes.length - 1];
            if(!jdkList.contains(type) && !jdkList.contains(simpleType)){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }
        //return false;
    }
}
