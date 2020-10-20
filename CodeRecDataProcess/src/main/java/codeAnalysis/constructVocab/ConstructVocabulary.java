package codeAnalysis.constructVocab;

import codeAnalysis.codeProcess.MethodReflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConstructVocabulary {
    //this method is used to get all public fields, public methods and constructors in object class
    public List<String> getObjectClassVocabulary(String className) {
        List result = new ArrayList();
        MethodReflection methodReflection = new MethodReflection();
        result = methodReflection.getAllMemberOfObjectClass(className);
        addSpecialVocabulary(className, result);
        return result;
    }

    //this method is used to add extra vocabulary such as int.null, int.Constant, int.cast etc.
    public List<String> getBasicTypeVocabulary(String className) {
        List result = new ArrayList();
        addSpecialVocabulary(className, result);
        return result;
    }

    public List<String> getObjectClassCombinedVocabulary(String className) {
        List<String> result = new ArrayList<>();
        MethodReflection methodReflection = new MethodReflection(className);
        List<Method> methodList = methodReflection.getMethodList();
        List<Field> fieldList = methodReflection.getFieldList();
        List<Object> objectList = new ArrayList<>();
        for (int i = 0; i < fieldList.size(); i++) {
            objectList.add(fieldList.get(i));
        }
        for (int i = 0; i < methodList.size(); i++) {
            objectList.add(methodList.get(i));
        }
        for (int i = 0; i < objectList.size(); i++) {
            String str = new String("");
            String str2 = new String("");
            methodReflection.combinedMethods(objectList.get(i), 1, str, str2, className);
            result = methodReflection.getCombinedCompleteMethodList();
        }
        return result;
    }

    public void saveVocabularyInFile(String path, List<String> list) {
        MethodReflection methodReflection = new MethodReflection();
        methodReflection.saveAllMemberInFile(path, list);
    }



    public void addSpecialVocabulary(String className, List<String> result) {
        String str = className;
        result.add(str + ".ArrayNull[]");
        result.add(str + ".ArrayNull[][]");
        result.add(str + ".ArrayConstant[]");
        result.add(str + ".ArrayConstant[][]");
        result.add(str + ".ArrayInit[]{}");
        result.add(str + ".ArrayInit[][]{}");
        result.add(str + ".ArrayDeclaration[]");
        result.add(str + ".ArrayDeclaration[][]");
        //result.add(str);
        result.add(str + ".Null");
        result.add(str + ".Constant");
        result.add(str + ".Declaration");
        result.add(str + ".UnaryExprOperator");
        result.add(str + ".Cast");
        result.add(str + ".new[]");
        result.add(str + ".new[][]");
        result.add(str + ".[index]");
        result.add(str + ".[index][]");
        result.add(str + ".[][index]");
        result.add(str + ".[index][index]");
        result.add(str + "[]" + ".length");
        result.add(str + "[][]" + ".length");
        result.add(str + "[]");
        result.add(str + "[][]");
        result.add(str + ".++");
        result.add(str + ".++p");
        result.add(str + ".--");
        result.add(str + ".--p");
        if(className.contains("[]")){
            str = className.replaceAll("\\[\\]","");
            result.add(str + ".ArrayNull[]");
            result.add(str + ".ArrayNull[][]");
            result.add(str + ".ArrayConstant[]");
            result.add(str + ".ArrayConstant[][]");
            result.add(str + ".ArrayInit[]{}");
            result.add(str + ".ArrayInit[][]{}");
            result.add(str + ".ArrayDeclaration[]");
            result.add(str + ".ArrayDeclaration[][]");
            result.add(str + ".Null");
            result.add(str + ".Constant");
            result.add(str + ".Declaration");
            result.add(str + ".UnaryExprOperator");
            result.add(str + ".Cast");
            result.add(str + ".new[]");
            result.add(str + ".new[][]");
            result.add(str + ".[index]");
            result.add(str + ".[index][index]");
            result.add(str + ".[index][]");
            result.add(str + ".[][index]");
            result.add(str + "[]" + ".length");
            result.add(str + "[][]" + ".length");
            result.add(str + "[]");
            result.add(str + "[][]");
            result.add(str + ".++");
            result.add(str + ".++p");
            result.add(str + ".--");
            result.add(str + ".--p");
        }
    }

}
