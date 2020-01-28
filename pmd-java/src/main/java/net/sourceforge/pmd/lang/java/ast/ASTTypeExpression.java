/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.InternalInterfaces.AtLeastOneChild;
import net.sourceforge.pmd.lang.java.typeresolution.typedefinition.JavaTypeDefinition;

/**
 * Wraps a type node but presents the interface of {@link ASTExpression}.
 * This is only used in the following contexts:
 * <ul>
 * <li>As the right-hand side of {@link BinaryOp#INSTANCEOF instanceof expressions}.
 * <li>As the qualifier of {@linkplain ASTMethodCall method calls},
 * {@link ASTFieldAccess field accesses}, when they access a static method or field
 * <li>As the qualifier of {@linkplain ASTMethodReference method references},
 * if it references a static method, or is a constructor reference
 * </ul>
 *
 * <pre class="grammar">
 *
 * TypeExpression ::= {@link ASTType Type}
 *
 * </pre>
 */
public final class ASTTypeExpression extends AbstractJavaNode implements ASTPrimaryExpression, AtLeastOneChild {

    ASTTypeExpression(int id) {
        super(id);
    }

    ASTTypeExpression(ASTType wrapped) {
        this(JavaParserImplTreeConstants.JJTTYPEEXPRESSION);
        this.jjtAddChild(wrapped, 0);
        copyTextCoordinates((AbstractJavaNode) wrapped);
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }

    /** Gets the wrapped type node. */
    public ASTType getTypeNode() {
        return (ASTType) getChild(0);
    }


    /** Returns 0, type expressions can never be parenthesized. */
    @Override
    public int getParenthesisDepth() {
        return 0;
    }

    /** Returns false, type expressions can never be parenthesized. */
    @Override
    public boolean isParenthesized() {
        return false;
    }

    @Override
    public @Nullable JavaTypeDefinition getTypeDefinition() {
        return getTypeNode().getTypeDefinition();
    }
}
