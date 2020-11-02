package utils;

import java.util.LinkedList;

public class VariableItem {

    LinkedList<String> variables;

    public VariableItem() {
        this.variables = new LinkedList<>();
    }

    public void addVariable(String v){
        this.variables.addLast(v.trim());
    }

    public LinkedList<String> getVariables() {
        return variables;
    }

    public void setVariables(LinkedList<String> variables) {
        this.variables = variables;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < variables.size(); i++) {
            if(i != (variables.size() - 1)){
                sb.append(variables.get(i)).append("woshifengefu");
            }else{
                sb.append(variables.get(i));
            }
        }
        return sb.toString().trim();
    }

    public void addVariable(LinkedList<String> strings) {
        for (int i = 0; i < strings.size(); i++) {
            this.addVariable(strings.get(i).trim());
        }
    }
}
