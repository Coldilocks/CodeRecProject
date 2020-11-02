package parameterModel;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * Created by chenchi on 18/1/17.
 */
public abstract class GroumConverter extends VoidVisitorAdapter<Object> {

    protected abstract Groum convert(MethodDeclaration n);

    protected abstract Groum convert(AssertStmt n);

    protected abstract Groum convert(BlockStmt n);

    protected abstract Groum convert(BreakStmt n);

    protected abstract Groum convert(ContinueStmt n);

    protected abstract Groum convert(DoStmt n);

    protected abstract Groum convert(EmptyStmt n);

    protected abstract Groum convert(ExpressionStmt n);

    protected abstract Groum convert(ForEachStmt n);

    protected abstract Groum convert(ForStmt n);

    protected abstract Groum convert(IfStmt n);

    protected abstract Groum convert(LabeledStmt n);

    protected abstract Groum convert(ReturnStmt n);

    protected abstract Groum convert(SynchronizedStmt n);

    protected abstract Groum convert(TryStmt n);

    protected abstract Groum convert(TypeDeclaration n);

    protected abstract Groum convert(WhileStmt n);

    protected abstract Groum convert(ExplicitConstructorInvocationStmt n);

    protected abstract Groum convert(SwitchStmt n);

    protected abstract Groum convert(ThrowStmt n);

    protected abstract Groum convert(LineComment n);

    protected abstract Groum convert(BlockComment n);

    protected abstract Groum convert(JavadocComment n);

    protected abstract Groum newInstance(Node n);

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

    @Override
    public void visit(ForEachStmt n, Object arg) {
        convert(n);
    }

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
//	public Groum getGroum() {
//		return result;
//	}

}
