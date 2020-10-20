package codeAnalysis.codeProcess;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MethodReflection {
    private Map<String, List<String>> methodAndParameterTypeMap = new HashMap<>(); //this field is used to store the parameter type with its corresponding method declaration
    private Map<String, List<String>> methodAndParameterCompleteTypeMap = new HashMap<>();
    private List<Method> methodList = new ArrayList<>();
    private List<Field> fieldList = new ArrayList<>();
    private Map<String, String> map = new HashMap<>();
    private List<String> combinedCompleteMethodList = new ArrayList<>();
    private List<String> combinedSimpleMethodList = new ArrayList<>();
    private Map<String, String> returnTypeMap = new HashMap<>();

    public List<Method> getMethodList() {
        return methodList;
    }

    public List<String> getCombinedCompleteMethodList() {
        return combinedCompleteMethodList;
    }

    public List<String> getCombinedSimpleMethodList() {
        return combinedSimpleMethodList;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public Map<String, String> getAllMethodsReturnTypeMap(String className) {
        getAllMethodDeclaration(className);
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
            combinedMethods(objectList.get(i), 1, str, str2, className);
        }
        return returnTypeMap;
    }

    public MethodReflection() {

    }

    public MethodReflection(String className) {
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Method[] methods = clazz.getMethods();
            Field[] fields = clazz.getFields();
            methods = filterRepeatMethod(methods);
            fields = filterRepeatedFields(fields);
            for (Method method : methods) {
                methodList.add(method);
            }
            for (Field field : fields) {
                fieldList.add(field);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {

        }
    }

    public Method[] filterRepeatMethod(Method[] methods) {
        Map<String, Method> methodStringMap = new HashMap<>();
        List<String> methodStringList = new ArrayList<>();
        for (Method method : methods) {
            String methodCompleteName = getMethodCompleteDeclaration(method, new ArrayList<>());
            if (methodStringMap.get(methodCompleteName) == null) {
                methodStringList.add(methodCompleteName);
                methodStringMap.put(methodCompleteName, method);
            } else {
                if (!method.getReturnType().isInterface() && methodStringMap.get(methodCompleteName).getReturnType().isInterface()) {
                    methodStringMap.replace(methodCompleteName, method);
                } else if (!method.getReturnType().isInterface() && !methodStringMap.get(methodCompleteName).getReturnType().isInterface()) {
                    //judge whether there is extend relationship exit
                    Class clazz = method.getReturnType();
                    Class clazz2 = methodStringMap.get(methodCompleteName).getReturnType();
                    try {
                        if (clazz2.isAssignableFrom(clazz)) {
                            methodStringMap.replace(methodCompleteName, method);
                        }
                    } catch (ClassCastException e) {
                        //means that no extend relationship exit
                    }
                }
            }
        }
        Method[] result = new Method[methodStringList.size()];
        for (int i = 0; i < methodStringList.size(); i++) {
            result[i] = methodStringMap.get(methodStringList.get(i));
        }
        return result;
    }

    public Field[] filterRepeatedFields(Field[] fields) {
        List<String> fieldStringList = new ArrayList<>();
        Map<String, Field> filedStringMap = new HashMap<>();
        for (Field filed : fields) {
            if (!fieldStringList.contains(filed.getName())) {
                fieldStringList.add(filed.getName());
                filedStringMap.put(filed.getName(), filed);
            } else {
                if (!filed.getType().isInterface() && filedStringMap.get(filed.getName()).getType().isInterface()) {
                    filedStringMap.replace(filed.getName(), filed);
                } else if (!filed.getType().isInterface() && !filedStringMap.get(filed.getName()).getType().isInterface()) {
                    //judge whether there is extend relationship exit
                    Class clazz = filed.getType();
                    Class clazz2 = filedStringMap.get(filed.getName()).getType();
                    try {
                        if (clazz2.isAssignableFrom(clazz)) {
                            filedStringMap.replace(filed.getName(), filed);
                        }
                    } catch (ClassCastException e) {
                        //means that no extend relationship exit
                    }
                }
            }
        }
        Field[] result = new Field[fieldStringList.size()];
        for (int i = 0; i < fieldStringList.size(); i++) {
            result[i] = filedStringMap.get(fieldStringList.get(i));
        }
        return result;
    }

    public Map<String, List<String>> getMethodAndParameterTypeMap() {
        return methodAndParameterTypeMap;
    }


    public Map<String, List<String>> getMethodAndParameterCompleteTypeMap() {
        return methodAndParameterCompleteTypeMap;
    }

    public Map<String, String> getSimpleToCompleteName(String className) {
        if (className != null) {
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                Constructor[] constructors = clazz.getConstructors();
                Method[] methods = clazz.getMethods();
                Field[] fields = clazz.getFields();
                methods = filterRepeatMethod(methods);
                fields = filterRepeatedFields(fields);
                for (Constructor constructor : constructors) {
                    String constructorDeclaration = new String(clazz.getSimpleName() + ".new(");
                    String completeConstructorDeclaration = new String("new(");//do not store the complete class name ,just store corresponding complete method name
                    constructorDeclaration += getMethodSimpleDeclaration(constructor, new ArrayList<>());
                    completeConstructorDeclaration += getMethodCompleteDeclaration(constructor, new ArrayList<>());
                    map.put(constructorDeclaration, completeConstructorDeclaration);
                }
                for (Method method : methods) {
                    String methodDeclaration = new String(clazz.getSimpleName());
                    String completeMethodDeclaration = new String("");//do not store the complete class name ,just store corresponding complete method name
                    methodDeclaration += getMethodSimpleDeclaration(method, new ArrayList<>());
                    completeMethodDeclaration += getMethodCompleteDeclaration(method, new ArrayList<>());
                    completeMethodDeclaration = completeMethodDeclaration.substring(1, completeMethodDeclaration.length());//filter out "."
                    map.put(methodDeclaration, completeMethodDeclaration);
                }
                for (int i = 0; i < fields.length; i++) {
                    String fieldString = new String("");
                    fieldString = clazz.getSimpleName() + "." + fields[i].getName();
                    map.put(fieldString, fields[i].getName());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            } catch (Error e) {

            }
        }
        return map;
    }

    public List<String> getAllMethodDeclaration(String className) {
        List<String> methodDeclarationsString = new ArrayList<>();
        if (className != null) {
            try {
                List<String> constructors = getConstructors(className);
                List<String> otherMethods = getMethods(className);
                List<String> fields = getAllStaticFields(className);
                getCompleteConstructors(className);
                getCompleteMethods(className);
                getAllCompleteStaticFields(className);
                appendList(methodDeclarationsString, constructors);
                appendList(methodDeclarationsString, otherMethods);
                appendList(methodDeclarationsString, fields);
            } catch (Exception e) {
                //System.out.println(className + " class not found");
                //e.printStackTrace();
            } catch (Error e) {
                //System.out.println(className + " class not found");
            }
        }
        return methodDeclarationsString;
    }

    // get all complete member of class
    public List<String> getAllMemberOfObjectClass(String className) {
        List<String> allMemberList = new ArrayList<>();
        try {
            Thread.currentThread().getContextClassLoader().loadClass(className);
            List<String> constructors = getCompleteConstructors(className);
            List<String> otherMethods = getCompleteMethods(className);
            List<String> fields = getAllCompleteStaticFields(className);
            appendList(allMemberList, constructors);
            appendList(allMemberList, otherMethods);
            appendList(allMemberList, fields);
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //System.out.println(className + " class not found");
        }
        return allMemberList;
    }

    public void saveAllMemberInFile(String path, List<String> allMemberList) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
                FileWriter writer = new FileWriter(path, true);
                for (int i = 0; i < allMemberList.size(); i++) {
                    writer.write(allMemberList.get(i) + "\r\n");
                }
                writer.close();
            } else {
                System.out.println("重复：" + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<String> getAllStaticFields(String className) {
        List<String> fieldString = new ArrayList<>();
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Field[] fields = clazz.getFields();
            fields = filterRepeatedFields(fields);
            for (int i = 0; i < fields.length; i++) {
                fieldString.add(clazz.getSimpleName() + "." + fields[i].getName());
                returnTypeMap.put(clazz.getSimpleName() + "." + fields[i].getName(), fields[i].getType().getTypeName());
                methodAndParameterTypeMap.put(clazz.getSimpleName() + "." + fields[i].getName(), new ArrayList<>());
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //System.out.println(className + " class not found");
        }
        return fieldString;
    }

    public List<String> getAllCompleteStaticFields(String className) {
        List<String> fieldString = new ArrayList<>();
        if (className != null) {
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                Field[] fields = clazz.getFields();
                fields = filterRepeatedFields(fields);
                for (int i = 0; i < fields.length; i++) {
                    //System.out.println(fields[i].getType().getTypeName());
                    fieldString.add(clazz.getName() + "." + fields[i].getName());
                    methodAndParameterCompleteTypeMap.put(clazz.getName() + "." + fields[i].getName(), new ArrayList<>());
                }
            } catch (Exception e) {
                //e.printStackTrace();
            } catch (Error e) {
                //System.out.println(className + " class not found");
            }
        }
        return fieldString;
    }

    // get simple name of class
    public List<String> getConstructors(String className) {
        List<String> constructorsString = new ArrayList<>();
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Constructor[] constructors = clazz.getConstructors();
            for (Constructor constructor : constructors) {
                String constructorDeclaration = new String(clazz.getSimpleName() + ".new(");
                List<String> parameterTypeStringList = new ArrayList<>();
                constructorDeclaration += getMethodSimpleDeclaration(constructor, parameterTypeStringList);
                constructorsString.add(constructorDeclaration);
                methodAndParameterTypeMap.put(constructorDeclaration, parameterTypeStringList);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //System.out.println(className + " class not found");
        }

        return constructorsString;
    }

    //get complete name of class
    public List<String> getCompleteConstructors(String className) {
        List<String> constructorsString = new ArrayList<>();
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Constructor[] constructors = clazz.getConstructors();
            for (Constructor constructor : constructors) {
                String constructorDeclaration = new String(clazz.getName() + ".new(");
                List<String> parameterTypeStringList = new ArrayList<>();
                constructorDeclaration += getMethodCompleteDeclaration(constructor, parameterTypeStringList);
                constructorsString.add(constructorDeclaration);
                // the following statement is used to map simple method declaration to complete parameter type list
                methodAndParameterCompleteTypeMap.put(clazz.getSimpleName() + ".new(" + getMethodSimpleDeclaration(constructor, new ArrayList<>()), parameterTypeStringList);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //System.out.println(className + " class not found");
        }

        return constructorsString;
    }

    // get the simple name of method and parameter
    public List<String> getMethods(String className) {
        List<String> methodsString = new ArrayList<>();
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Method[] methods = clazz.getMethods();
            methods = filterRepeatMethod(methods);
            for (Method method : methods) {
                //methodList.add(method);
                String methodDeclaration = new String(clazz.getSimpleName());
                List<String> parameterTypeStringList = new ArrayList<>();
                methodDeclaration += getMethodSimpleDeclaration(method, parameterTypeStringList);
                methodsString.add(methodDeclaration);
                // if (method.getReturnType().toString().contains(".")) {
                returnTypeMap.put(methodDeclaration, method.getReturnType().getTypeName());
                // }
                methodAndParameterTypeMap.put(methodDeclaration, parameterTypeStringList);

            }
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //System.out.println(className + " class not found");
        }

        return methodsString;
    }

    // get the complete name of method and parameter
    public List<String> getCompleteMethods(String className) {
        List<String> methodsString = new ArrayList<>();
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            Method[] methods = clazz.getMethods();
            methods = filterRepeatMethod(methods);
            for (Method method : methods) {
                String methodDeclaration = new String(clazz.getName());
                List<String> parameterTypeStringList = new ArrayList<>();
                methodDeclaration += getMethodCompleteDeclaration(method, parameterTypeStringList);
                methodsString.add(methodDeclaration);
                // the following statement is used to map simple method declaration to complete parameter type list
                methodAndParameterCompleteTypeMap.put(clazz.getSimpleName() + getMethodSimpleDeclaration(method, new ArrayList<>()), parameterTypeStringList);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } catch (Error e) {
            //System.out.println(className + " class not found");
        }

        return methodsString;
    }

    public void combinedMethods(Object object, int count, String simpleDeclaration, String completeDeclaration, String className) {
        String returnType = null;
        List<String> parameterTypeList = new ArrayList<>();
        List<String> parameterTypeList2 = new ArrayList<>();
        if (object instanceof Method) {
            Method method = (Method) object;
            returnType = method.getReturnType().getTypeName();
            if (returnType.contains(".") && !returnType.contains("[") && !returnType.contains("<")) {
                if (simpleDeclaration.equals("")) {
                    simpleDeclaration += getMethodSimpleDeclaration(method, parameterTypeList);
                    //simpleDeclaration = simpleDeclaration.substring(1, simpleDeclaration.length());//filter out the "."
                    parameterTypeList.removeAll(parameterTypeList);
                }
                if (completeDeclaration.equals("")) {
                    completeDeclaration += getMethodCompleteDeclaration(method, parameterTypeList2);
                    //completeDeclaration = completeDeclaration.substring(1, completeDeclaration.length());//filter out the "."
                    parameterTypeList2.removeAll(parameterTypeList2);
                }

            }
        } else if (object instanceof Field) {
            Field field = (Field) object;
            returnType = field.getType().getTypeName();
            if (returnType.contains(".") && !returnType.contains("[") && !returnType.contains("<")) {
                if (simpleDeclaration.equals("")) {
                    simpleDeclaration += "." + field.getName();
                }
                if (completeDeclaration.equals("")) {
                    completeDeclaration += "." + field.getName();
                }
            }
        }
        if (returnType != null && returnType.contains(".") && !returnType.contains("[") && !returnType.contains("<")) {
            if (count < 2) {
                try {
                    Class clazz = Thread.currentThread().getContextClassLoader().loadClass(returnType);
                    Class cla = Thread.currentThread().getContextClassLoader().loadClass(className);
                    Method[] methods = clazz.getMethods();
                    Field[] fields = clazz.getFields();
                    List<Object> list = new ArrayList<>();
                    methods = filterRepeatMethod(methods);
                    fields = filterRepeatedFields(fields);
                    for (Method m : methods) {
                        list.add(m);
                    }
                    for (Field field : fields) {
                        list.add(field);
                    }
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) instanceof Method) {
                            Method m = (Method) list.get(i);
                            String simpleStr = simpleDeclaration;
                            String completeStr = completeDeclaration;
                            simpleStr += getMethodSimpleDeclaration(m, parameterTypeList);
                            completeStr += getMethodCompleteDeclaration(m, parameterTypeList2);
                            returnTypeMap.put(cla.getSimpleName() + simpleStr, m.getReturnType().getTypeName());
                            methodAndParameterTypeMap.put(cla.getSimpleName() + simpleStr, parameterTypeList);
                            map.put(cla.getSimpleName() + simpleStr, completeStr.substring(1, completeStr.length()));
                            combinedCompleteMethodList.add(cla.getName() + completeStr);
                            combinedSimpleMethodList.add(cla.getSimpleName() + simpleStr);
                            //System.out.println(cla.getSimpleName() + simpleStr);
                            combinedMethods(list.get(i), count + 1, simpleStr, completeStr, className);
                        } else if (list.get(i) instanceof Field) {
                            Field field = (Field) list.get(i);
                            String simpleStr = simpleDeclaration;
                            String completeStr = completeDeclaration;
                            simpleStr += "." + field.getName();
                            completeStr += "." + field.getName();
                            returnTypeMap.put(cla.getSimpleName() + simpleStr, field.getType().getTypeName());
                            map.put(cla.getSimpleName() + simpleStr, completeStr.substring(1, completeStr.length()));
                            combinedCompleteMethodList.add(cla.getName() + completeStr);
                            combinedSimpleMethodList.add(cla.getSimpleName() + simpleStr);
                            combinedMethods(list.get(i), count + 1, simpleStr, completeStr, className);
                        } else {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    //System.out.println(returnType + " class not found");
                } catch (Error e) {
                    //System.out.println(className + " class not found");
                }
            }
        }

    }

    public String getMethodSimpleDeclaration(Object method, List<String> parameterTypeStringList) {
        String result = new String("");
        String methodName = new String("");
        Class<?>[] parameterTypeClass = null;
        if (method instanceof Method) {
            methodName = ((Method) method).getName();
            result += "." + methodName + "(";
            parameterTypeClass = ((Method) method).getParameterTypes();
        } else if (method instanceof Constructor) {
            parameterTypeClass = ((Constructor) method).getParameterTypes();
        }
        for (int i = 0; i < parameterTypeClass.length; i++) {
            try {
                String parameterTypeString = parameterTypeClass[i].getSimpleName();
                if (i == 0) {
                    result += parameterTypeString;
                } else {
                    result += "," + parameterTypeString;
                }
                parameterTypeStringList.add(parameterTypeString);
            } catch (Exception e) {
                result = "null";
                return result;
            } catch (Error e) {
                result = "null";
                return result;
            }
        }
        result += ")";
        return result;
    }

    public String getMethodCompleteDeclaration(Object method, List<String> parameterTypeStringList) {
        String result = new String("");
        String methodName = new String("");
        Class<?>[] parameterTypeClass = null;
        if (method instanceof Method) {
            methodName = ((Method) method).getName();
            result += "." + methodName + "(";
            parameterTypeClass = ((Method) method).getParameterTypes();
        } else if (method instanceof Constructor) {
            parameterTypeClass = ((Constructor) method).getParameterTypes();
        }
        for (int i = 0; i < parameterTypeClass.length; i++) {
            String parameterTypeString = parameterTypeClass[i].getTypeName();
            if (i == 0) {
                result += parameterTypeString;
            } else {
                result += "," + parameterTypeString;
            }
            parameterTypeStringList.add(parameterTypeString);
        }
        result += ")";
        return result;
    }

    public void appendList(List<String> masterList, List<String> slaveList) {
        for (int i = 0; i < slaveList.size(); i++) {
            masterList.add(slaveList.get(i));
        }
    }


}
