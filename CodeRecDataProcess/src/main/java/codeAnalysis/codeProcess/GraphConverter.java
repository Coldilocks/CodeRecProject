package codeAnalysis.codeProcess;


import codeAnalysis.codeRepresentation.Graph;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public abstract class GraphConverter extends VoidVisitorAdapter<Object> {

    protected abstract Graph convert(MethodDeclaration n);

    protected abstract Graph convert(AssertStmt n);

    protected abstract Graph convert(BlockStmt n);

    protected abstract Graph convert(BreakStmt n);

    protected abstract Graph convert(ContinueStmt n);

    protected abstract Graph convert(DoStmt n);

    protected abstract Graph convert(EmptyStmt n);

    protected abstract Graph convert(ExpressionStmt n);

    protected abstract Graph convert(ForEachStmt n);

    protected abstract Graph convert(ForStmt n);

    protected abstract Graph convert(IfStmt n);

    protected abstract Graph convert(LabeledStmt n);

    protected abstract Graph convert(ReturnStmt n);

    protected abstract Graph convert(SynchronizedStmt n);

    protected abstract Graph convert(TryStmt n);

    //protected abstract Graph convert(TypeDeclarationStmt n);

    protected abstract Graph convert(WhileStmt n);

    protected abstract Graph convert(ExplicitConstructorInvocationStmt n);

    protected abstract Graph convert(SwitchStmt n);

    protected abstract Graph convert(ThrowStmt n);

    protected abstract Graph convert(LineComment n);

    protected abstract Graph convert(BlockComment n);

    protected abstract Graph convert(JavadocComment n);

    protected abstract Graph newInstance(Node n);

    @Override
    public void visit(MethodDeclaration n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(AssertStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(BlockStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(BreakStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ContinueStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(DoStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(EmptyStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ExpressionStmt n, Object arg) {
        convert(n);
    }

//    @Override
//    public void visit(ForeachStmt n, Object arg) {
//        convert(n);
//    }

    @Override
    public void visit(ForStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(IfStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(LabeledStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ReturnStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(SynchronizedStmt n, Object arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, Object arg) {
        convert(n);
    }

//    @Override
//    public void visit(TypeDeclarationStmt n, Object arg) {
//        convert(n);
//    }

    @Override
    public void visit(WhileStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(SwitchStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(ThrowStmt n, Object arg) {
        convert(n);
    }

    @Override
    public void visit(LineComment n, Object arg){convert(n);}

    @Override
    public void visit(BlockComment n, Object arg){convert(n);}

    @Override
    public void visit(JavadocComment n, Object arg){convert(n);}

}
