/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.util.Iterator;

/**
 * Base class for some expressions that are parsed left-recursively.
 *
 * @author Clément Fournier
 */
abstract class AbstractLrBinaryExpr extends AbstractJavaExpr
    implements ASTExpression, LeftRecursiveNode, Iterable<ASTExpression> {

    private BinaryOp operator;

    AbstractLrBinaryExpr(int i) {
        super(i);
    }

    AbstractLrBinaryExpr(JavaParser p, int i) {
        super(p, i);
    }


    @Override
    public void jjtClose() {
        super.jjtClose();

        // At this point the expression is fully left-recursive
        // If any of its left children are also AdditiveExpressions with the same operator,
        // we adopt their children to flatten the node

        JavaNode first = (JavaNode) jjtGetChild(0);
        // they could be of different types, but the getOp check ensures
        // they are of the same type
        if (first instanceof AbstractLrBinaryExpr
            && ((AbstractLrBinaryExpr) first).getOperator() == getOperator()
            && !((AbstractLrBinaryExpr) first).isParenthesized()) {
            flatten(0);
        }
    }

    @Override
    public Iterator<ASTExpression> iterator() {
        return new NodeChildrenIterator<>(this, ASTExpression.class);
    }

    @Override
    public void setImage(String image) {
        super.setImage(image);
        this.operator = BinaryOp.fromImage(image);
    }

    /**
     * Returns the operator.
     */
    public BinaryOp getOperator() {
        return operator;
    }
}
