package utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaParserUtil {

    private LinkedList<String> names;// 存取待匹配类名
    private LinkedList<String> packages;// 存取前缀包名
    private LinkedList<String> filternames;// 存取过滤的类名

    private String packageName = "";
    private boolean isFilter = false;
    private LinkedList<String> completenames;// 完整类名列表

    public JavaParserUtil() {
    }

    public JavaParserUtil(boolean isFilter) {
        this.isFilter = isFilter;
    }

    /**
     * 输入要静态解析的CompliationUnit,返回一个包含该文件中所有声明到的完整类名的list,以string方式存储
     */
    public LinkedList<String> parse(CompilationUnit cu) throws Exception {
        names = new LinkedList<>();
        packages = new LinkedList<>();
        filternames = new LinkedList<>();
        completenames = new LinkedList<>();

        add2List(packages, "java.lang");// java会自动引入java.lang包
        CompilationUnit result = cu;
        result.accept(new MyVisitor(), null);
        return  handleNames(names, packages);
    }

    /**
     * 合成完整类名所在位置
     */
    private LinkedList<String> handleNames(LinkedList<String> names, LinkedList<String> packages) {
        for (String clazzName : names) {
            if (!isIncluded(clazzName, names)) {
                try {
                    add2List(completenames, Thread.currentThread().getContextClassLoader().loadClass(clazzName).getName());
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException) {
                        match2Package(clazzName, completenames);
                    }
                } catch (Error e) {

                }
            }
        }
        if (isFilter) {// Filter the user define class names
            completenames = filter(completenames, packageName);
        }
        return completenames;
    }

    /**
     * 若在列表中,已有最大匹配的完整类名路径,则无需再次匹配添加包的路径前缀
     */
    private boolean isIncluded(String clazzName, LinkedList<String> list) {
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
     */
    private void match2Package(String clazzName, LinkedList<String> list) {
        for (String packageName : packages) {
            try {
                add2List(list, Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + clazzName).getName());
                return;
            } catch (Exception e) {
                // skip
            } catch (Error e) {
                //skip
            }
        }
    }


    /**
     * 将输入的className,不重复地插入到list中,并返回这个list
     */
    private LinkedList<String> add2List(LinkedList<String> list, String className) {
        if (!list.contains(className)) {
            list.addLast(className);
        }
        return list;
    }

    /**
     * 过滤出第三方类名,输入需要过滤的完整类名list,用户所在包名,返回过滤后的list
     */
    private LinkedList<String> filter(LinkedList<String> list, String packageName) {
        LinkedList<String> result = new LinkedList<>();
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
                    add2List(filternames, name);
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
        type = type.replaceAll("\\[\\]", "");
        if (type.contains("<")) {
            type = filterAngleBracket(type);
        }
        return type;
    }

    public LinkedList<String> getFilternames() {
        return filternames;
    }

    class MyVisitor extends VoidVisitorAdapter<Void> {
        private MyVisitor() {

        }

        // 抽取package name
        @Override
        public void visit(PackageDeclaration node, Void arg) {
            packageName = node.getName().toString();
            add2List(packages, node.getName().toString());
            super.visit(node, arg);
        }

        // 抽取import的包名, java会自动引入java.lang包
        @Override
        public void visit(ImportDeclaration node, Void arg) {
            // 按需导入的包,作为前缀等待补全
            if (node.isAsterisk()) {
                add2List(packages, node.getName().toString());
            } else {
                add2List(completenames, node.getName().toString());
            }
            super.visit(node, arg);
        }

        @Override
        public void visit(MethodDeclaration node, Void arg) {
            // 按需导入的包,作为前缀等待补全
            if(node.getParameters() != null){
                for(int i = 0; i < node.getParameters().size(); i ++){
                    add2List(names, filterSquareBracket(filterAngleBracket(node.getParameters().get(i).getType().toString())));
                }
            }
            super.visit(node, arg);
        }

        @Override
        public void visit(FieldDeclaration node, Void arg) {
            add2List(names, filterSquareBracket(filterAngleBracket(node.getCommonType().toString())));
            super.visit(node, arg);
        }

        @Override
        public void visit(NameExpr node, Void arg){
            add2List(names,filterSquareBracket(filterAngleBracket(node.getName().toString())));
            super.visit(node,arg);
        }

        // 抽取所有的类和接口声明
        @Override
        public void visit(ClassOrInterfaceType node, Void arg){
            add2List(names,filterSquareBracket(filterAngleBracket(node.getName().toString())));
            super.visit(node, arg);
        }
    }


}
