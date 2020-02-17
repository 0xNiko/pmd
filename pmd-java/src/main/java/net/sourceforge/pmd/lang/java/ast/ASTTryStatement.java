/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * Try statement node.
 *
 *
 * <pre class="grammar">
 *
 * TryStatement ::= "try" {@link ASTResourceList ResourceList}?
 *                  {@link ASTBlock Block}
 *                  {@link ASTCatchClause CatchClause}*
 *                  {@link ASTFinallyClause FinallyClause}?
 *
 * </pre>
 */
public final class ASTTryStatement extends AbstractStatement {

    ASTTryStatement(int id) {
        super(id);
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }


    /**
     * Returns true if this node is a try-with-resources, in which case it
     * has a ResourceSpecification child node.
     */
    public boolean isTryWithResources() {
        return getChild(0) instanceof ASTResourceList;
    }

    /**
     * Returns the node for the resource list. This is null if this is
     * not a try-with-resources.
     */
    @Nullable
    public ASTResourceList getResources() {
        return AstImplUtil.getChildAs(this, 0, ASTResourceList.class);
    }


    /**
     * Returns the body of this try statement.
     */
    public ASTBlock getBody() {
        return children(ASTBlock.class).first();
    }

    /**
     * Returns the catch statement nodes of this try statement.
     * If there are none, returns an empty list.
     */
    public List<ASTCatchClause> getCatchClauses() {
        return findChildrenOfType(ASTCatchClause.class);
    }


    /**
     * Returns the {@code finally} clause of this try statement, if any.
     *
     * @return The finally statement, or null if there is none
     */
    @Nullable
    public ASTFinallyClause getFinallyClause() {
        return getFirstChildOfType(ASTFinallyClause.class);
    }

}
