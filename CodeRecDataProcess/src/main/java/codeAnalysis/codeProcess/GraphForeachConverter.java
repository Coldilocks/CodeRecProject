package codeAnalysis.codeProcess;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.CloneVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AstUtil;
import utils.CollectionUtils;

public class GraphForeachConverter {

    private static Logger log = LoggerFactory.getLogger(GraphForeachConverter.class);

    public static ForStmt toForStmt(ForEachStmt n) {
        ForStmt forStmt = new ForStmt();
        /* init */
        VariableDeclarationExpr varDecl = n.getVariable();
        NodeList<Expression> fakeIteratorInit = newIteratorInitExpr(n, varDecl);
        if(fakeIteratorInit != null && !fakeIteratorInit.contains(null)) {
            forStmt.setInitialization(fakeIteratorInit);
            /* compare */
            Expression fakeCompare = newFakeCompare(varDecl);
            forStmt.setCompare(fakeCompare);
            /* body */
            BlockStmt fakeBodyStmt = new BlockStmt();
            NodeList<Statement> stmts = new NodeList<>();
            // variable initialization
            VariableDeclarationExpr fakeVarDeclExpr = newVarInitExpr(varDecl);
            ExpressionStmt fakeVarDecl = new ExpressionStmt(fakeVarDeclExpr);
            stmts.add(fakeVarDecl);
            if (n.getBody() instanceof BlockStmt) {
                stmts.addAll(CollectionUtils.nullToEmpty(((BlockStmt) n.getBody()).getStatements()));
            }
            fakeBodyStmt.setStatements(stmts);
            forStmt.setBody(fakeBodyStmt);

            /* mark node as fake */
            AstUtil.markNodeAsFake(forStmt, n);
            AstUtil.markNodesAsFake(fakeIteratorInit, n.getIterable());
            AstUtil.markNodeAsFake(fakeCompare, varDecl);
            AstUtil.markNodeAsFake(fakeBodyStmt, n.getBody());
            AstUtil.markNodeAsFake(fakeVarDeclExpr, varDecl);
            AstUtil.markNodeAsFake(fakeVarDecl, varDecl);
            return forStmt;
        }else{
            return null;
        }
    }

    private static VariableDeclarationExpr newVarInitExpr(
            VariableDeclarationExpr varDecl) {
        CloneVisitor cloner = new CloneVisitor();
        VariableDeclarationExpr newExpr = (VariableDeclarationExpr) varDecl.accept(cloner, null);
        newExpr.getVariables().get(0).setInitializer(newExpression("tempIt.next()", varDecl));
        return newExpr;
    }

    private static Expression newFakeCompare(VariableDeclarationExpr varDecl) {
        return newExpression("tempIt.hasNext()", varDecl);
    }


    private static NodeList<Expression> newIteratorInitExpr(ForEachStmt n,
                                                        VariableDeclarationExpr varDecl) {
        Type t = varDecl.getElementType();
        String st = "";
        if (t instanceof PrimitiveType) {
            PrimitiveType pt = (PrimitiveType) t;
            if (pt.getType() == PrimitiveType.Primitive.BOOLEAN) {
                st = "Boolean";
            } else if (pt.getType() == PrimitiveType.Primitive.BYTE) {
                st = "Byte";
            } else if (pt.getType() == PrimitiveType.Primitive.CHAR) {
                st = "Character";
            } else if (pt.getType() == PrimitiveType.Primitive.DOUBLE) {
                st = "Double";
            } else if (pt.getType() == PrimitiveType.Primitive.FLOAT) {
                st = "Float";
            } else if (pt.getType() == PrimitiveType.Primitive.INT) {
                st = "Integer";
            } else if (pt.getType() == PrimitiveType.Primitive.LONG) {
                st = "Long";
            } else if (pt.getType() == PrimitiveType.Primitive.SHORT) {
                st = "Short";
            }
        } else {
            st = t.toString();
        }

        st = varDecl.getVariables().get(0).toString();
        String initExpr = String.format("%s = %s",
                st, n.getIterable());
        NodeList<Expression> fakeInit = (NodeList<Expression>) CollectionUtils.listOf(newExpression(initExpr, varDecl));
        return fakeInit;
    }

    private static Expression newExpression(String exprStr, Node n) {
        JavaParser javaParser = new JavaParser();
        return javaParser.parseExpression(exprStr).getResult().get();
    }
}