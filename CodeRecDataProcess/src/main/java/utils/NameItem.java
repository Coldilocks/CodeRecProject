package utils;

import java.util.LinkedList;

/**
 * 存储解析信息的Entity
 * */
public class NameItem {
    String receiver;
    String method_field;
    LinkedList<String> parameters;
    String type;

    public NameItem() {
        receiver = "";
        method_field = "";
        parameters = new LinkedList<>();
    }

    public void addParameter(String p){
        this.parameters.addLast(p);
    }
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMethod_field() {
        return method_field;
    }

    public void setMethod_field(String method_field) {
        this.method_field = method_field;
    }

    public LinkedList<String> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedList<String> parameters) {
        this.parameters = parameters;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        if(receiver!=null && !method_field.equals("new")) {
            sb.append(receiver);
        }
        //sb.append(" ").append(method_field);
        for (int i = 0; i < parameters.size(); i++) {
            String p = parameters.get(i);
            sb.append(" ").append(p);
        }
        return sb.toString().trim();
    }

    public int getVariableSize() {
        int size = 0;
        if(receiver.length() > 0){
            size++;
        }
        size+=parameters.size();
        return size;
    }
}
