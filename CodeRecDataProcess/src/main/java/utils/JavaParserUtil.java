package utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaParserUtil {

    /** 存取待匹配类名 */
    private HashSet<String> classNames;
    /** 存取前缀包名 */
    private HashSet<String> packageNames;
    /** 存取过滤的类名 */
    private HashSet<String> userDefinedClassNames;
    /** 完整类名列表 */
    private HashSet<String> qualifiedClassName;

    private String packageName = "";
    private boolean isFilter = false;


    public JavaParserUtil() {}

    public JavaParserUtil(boolean isFilter) {
        this.isFilter = isFilter;
    }

    /**
     * 输入要静态解析的CompliationUnit,返回一个包含该文件中所有声明到的完整类名的list,以string方式存储
     * @param cu ast
     * @return
     */
    public HashSet<String> parse(CompilationUnit cu){
        this.classNames = new HashSet<>();
        this.packageNames = new HashSet<>();
        this.userDefinedClassNames = new HashSet<>();
        this.qualifiedClassName = new HashSet<>();

        // java会自动引入java.lang包
        this.packageNames.add("java.lang");

        cu.accept(new MyVisitor(), null);
        return this.handleNames();
    }

    /**
     * 合成完整类名所在位置
     * @return
     */
    private HashSet<String> handleNames() {
        for (String clazzName : this.classNames) {
            if (!this.isIncluded(clazzName)) {
                try {
                    this.qualifiedClassName.add(Thread.currentThread().getContextClassLoader().loadClass(clazzName).getName());
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException) {
                        this.match2Package(clazzName);
                    }
                } catch (Error e) {

                }
            }
        }
        if (this.isFilter) {
            // Filter the user define class names
            this.qualifiedClassName = this.filterUserDefinedClass();
        }
        return this.qualifiedClassName;
    }

    /**
     * 若在列表中,已有最大匹配的完整类名路径,则无需再次匹配添加包的路径前缀
     * todo: what's the meaning?
     * @param clazzName
     * @return
     */
    private boolean isIncluded(String clazzName) {
        String rg = ".*\\." + clazzName + "$";
        Pattern pattern = Pattern.compile(rg);
        for (String name : this.classNames) {
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
     */
    private void match2Package(String clazzName) {
        for (String packageName : this.packageNames) {
            try {
                this.qualifiedClassName.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + clazzName).getName());
                return;
            } catch (Exception | Error e) {
                // skip
            }
        }
    }

    /**
     * 过滤出第三方类名,输入需要过滤的完整类名list,用户所在包名,返回过滤后的list
     * todo：有JDK的类被识别成第三方类了，需要修改；还有第三方的类没有被过滤，也需要修改
     * @return
     */
    private HashSet<String> filterUserDefinedClass() {
        HashSet<String> result = new HashSet<>();
        PackageElement packageElement = null;
        if (!this.packageName.isEmpty()) {
            packageElement = new PackageElement();
            packageElement.setPackageString("package " + packageName + "; \n");
        }
        for (String name : this.qualifiedClassName) {
            PackageElement anotherPackageElement = new PackageElement();
            anotherPackageElement.setPackageString("import " + name + "; \n");
            if (packageElement == null || !packageElement.matchpackageNames(anotherPackageElement)) {
                result.add(name);
            } else {
                if (packageElement.matchpackageNames(anotherPackageElement)) {
                    this.userDefinedClassNames.add(name);
                }
            }
        }
        return result;
    }

    /**
     * 过滤"<>"尖括号
     */
    private String filterAngleBracket(String type) {
        if (type.contains("<")) {
            int index = type.indexOf("<");
            type = type.substring(0, index);
        }
        return type;
    }

    /**
     * 过滤"[]"中括号
     */
    private String filterSquareBracket(String type) {
        type = type.replaceAll("\\[]", "");
        if (type.contains("<")) {
            type = filterAngleBracket(type);
        }
        return type;
    }

    public HashSet<String> getUserDefinedClassNames() {
        return userDefinedClassNames;
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
         * 抽取方法参数的类型
         * @param node method declaration
         * @param arg
         */
        @Override
        public void visit(MethodDeclaration node, Void arg) {
            // 按需导入的包,作为前缀等待补全
            if(node.getParameters() != null){
//                for(int i = 0; i < node.getParameters().size(); i ++){
//                    names.add(filterSquareBracket(filterAngleBracket(node.getParameters().get(i).getType().toString())));
//                }

                for(Parameter param : node.getParameters()){
                    classNames.add(filterSquareBracket(filterAngleBracket(param.getTypeAsString())));
                }
            }

            super.visit(node, arg);
        }

        /**
         * 抽取类字段的类型
         * @param node field declaration
         * @param arg
         */
        @Override
        public void visit(FieldDeclaration node, Void arg) {
            classNames.add(filterSquareBracket(filterAngleBracket(node.getCommonType().toString())));
            super.visit(node, arg);
        }

        /**
         * 收集变量名
         * todo: delete it
         * @param node
         * @param arg
         */
        @Override
        public void visit(NameExpr node, Void arg){
            classNames.add(filterSquareBracket(filterAngleBracket(node.getNameAsString())));
            super.visit(node,arg);
        }

        /**
         * 抽取所有的类和接口声明
         * @param node
         * @param arg
         */
        @Override
        public void visit(ClassOrInterfaceType node, Void arg){
            classNames.add(filterSquareBracket(filterAngleBracket(node.getName().toString())));
            super.visit(node, arg);
        }
    }


}
