package ru.slie.luna.plugins.gravity.script.groovy;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

public class NodeFinder extends CodeVisitorSupport {
    private final int targetLine;
    private final int targetColumn;
    private ASTNode foundNode;

    public NodeFinder(int line, int column) {
        this.targetLine = line;
        this.targetColumn = column;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        check(call);
        super.visitMethodCallExpression(call);
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        check(expression);
        super.visitVariableExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        check(expression);
        super.visitPropertyExpression(expression);
    }

    private void check(ASTNode node) {
        if (node == null || node.getLineNumber() == -1) return;

        if (targetLine >= node.getLineNumber() && targetLine <= node.getLastLineNumber()) {
            if (targetLine == node.getLineNumber() && targetColumn < node.getColumnNumber()) return;
            if (targetLine == node.getLastLineNumber() && targetColumn > node.getLastColumnNumber()) return;

            foundNode = node;
        }
    }

    public ASTNode getFoundNode() { return foundNode; }
}
