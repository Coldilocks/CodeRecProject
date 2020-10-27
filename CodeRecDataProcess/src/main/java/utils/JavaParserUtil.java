package utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaParserUtil {

    private HashSet<String> names;// 存取待匹配类名
    private HashSet<String> packageNames;// 存取前缀包名
    private HashSet<String> filterNames;// 存取过滤的类名
    private HashSet<String> qualifiedClassName;// 完整类名列表

    private String packageName = "";
    private boolean isFilter = false;


    public JavaParserUtil() { }

    public JavaParserUtil(boolean isFilter) {
        this.isFilter = isFilter;
    }

    /**
     * 输入要静态解析的CompliationUnit,返回一个包含该文件中所有声明到的完整类名的list,以string方式存储
     * @param cu ast
     * @return
     */
    public HashSet<String> parse(CompilationUnit cu){
        names = new HashSet<>();
        packageNames = new HashSet<>();
        filterNames = new HashSet<>();
        qualifiedClassName = new HashSet<>();

        // java会自动引入java.lang包
        packageNames.add("java.lang");

        cu.accept(new MyVisitor(), null);
        return this.handleNames(names, packageNames);
    }

    /**
     * 合成完整类名所在位置
     * @param names
     * @param packages
     * @return
     */
    private HashSet<String> handleNames(HashSet<String> names, HashSet<String> packages) {
        for (String clazzName : names) {
            if (!isIncluded(clazzName, names)) {
                try {
                    qualifiedClassName.add(Thread.currentThread().getContextClassLoader().loadClass(clazzName).getName());
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException) {
                        this.match2Package(clazzName, qualifiedClassName);
                    }
                } catch (Error e) {

                }
            }
        }
        if (isFilter) {
            // Filter the user define class names
            qualifiedClassName = this.filter(qualifiedClassName, packageName);
        }
        return qualifiedClassName;
    }

    /**
     * 若在列表中,已有最大匹配的完整类名路径,则无需再次匹配添加包的路径前缀
     * @param clazzName
     * @param list
     * @return
     */
    private boolean isIncluded(String clazzName, HashSet<String> list) {
        String rg = ".*\\." + clazzName + "$";
        Pattern pattern = Pattern.compile(rg);
        for (String name : list) {
            Matcher match = pattern.matcher(name);
            if (match.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当单个类名无法匹配时,运用排列组合方法从前缀packages中匹配包名
     * @param clazzName
     * @param list
     */
    private void match2Package(String clazzName, HashSet<String> list) {
        for (String packageName : packageNames) {
            try {
                list.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + clazzName).getName());
                return;
            } catch (Exception | Error e) {
                // skip
            }
        }
    }

    /**
     * 将输入的className,不重复地插入到list中,并返回这个list
     * @param list
     * @param className
     * @return
     */
    private LinkedList<String> add2List(LinkedList<String> list, String className) {
        if (!list.contains(className)) {
            list.addLast(className);
        }
        return list;
    }

    /**
     * 过滤出第三方类名,输入需要过滤的完整类名list,用户所在包名,返回过滤后的list
     * @param list
     * @param packageName
     * @return
     */
    private HashSet<String> filter(HashSet<String> list, String packageName) {
        HashSet<String> result = new HashSet<>();
        PackageElement packageElement = null;
        if (!packageName.equals("")) {
            packageElement = new PackageElement();
            packageElement.setPackageString("package " + packageName + "; \n");
        }
        for (String name : list) {
            PackageElement anotherPackageElement = new PackageElement();
            anotherPackageElement.setPackageString("import " + name + "; \n");
            if (packageElement == null || !packageElement.matchpackageNames(anotherPackageElement)) {
                result.add(name);
            } else {
                if (packageElement.matchpackageNames(anotherPackageElement)) {
                    filterNames.add(name);
                }
            }
        }
        return result;
    }

    private String filterAngleBracket(String type) {
        if (type.contains("<")) {
            int index = type.indexOf("<");
            type = type.substring(0, index);
        }
        return type;
    }

    private String filterSquareBracket(String type) {
        type = type.replaceAll("\\[]", "");
        if (type.contains("<")) {
            type = filterAngleBracket(type);
        }
        return type;
    }

    public HashSet<String> getFilterNames() {
        return filterNames;
    }

    class MyVisitor extends VoidVisitorAdapter<Void> {
        private MyVisitor() {}

        /**
         * 抽取package name
         * @param node
         * @param arg
         */
        @Override
        public void visit(PackageDeclaration node, Void arg) {
            packageName = node.getNameAsString();
            packageNames.add(node.getNameAsString());
            super.visit(node, arg);
        }

        /**
         * 抽取import的包名, java会自动引入java.lang包
         * @param node
         * @param arg
         */
        @Override
        public void visit(ImportDeclaration node, Void arg) {
            // 按需导入的包,作为前缀等待补全
            if (node.isAsterisk()) {
                packageNames.add(node.getNameAsString());
            } else {
                qualifiedClassName.add(node.getNameAsString());
            }
            super.visit(node, arg);
        }

        /**
         *
         * @param node
         * @param arg
         */
        @Override
        public void visit(MethodDeclaration node, Void arg) {
            // 按需导入的包,作为前缀等待补全
            if(node.getParameters() != null){
                for(int i = 0; i < node.getParameters().size(); i ++){
                    names.add(filterSquareBracket(filterAngleBracket(node.getParameters().get(i).getType().toString())));
                }
            }
            super.visit(node, arg);
        }

        @Override
        public void visit(FieldDeclaration node, Void arg) {
            names.add(filterSquareBracket(filterAngleBracket(node.getCommonType().toString())));
            super.visit(node, arg);
        }

        @Override
        public void visit(NameExpr node, Void arg){
            names.add(filterSquareBracket(filterAngleBracket(node.getName().toString())));
            super.visit(node,arg);
        }

        // 抽取所有的类和接口声明
        @Override
        public void visit(ClassOrInterfaceType node, Void arg){
            names.add(filterSquareBracket(filterAngleBracket(node.getName().toString())));
            super.visit(node, arg);
        }
    }


}
