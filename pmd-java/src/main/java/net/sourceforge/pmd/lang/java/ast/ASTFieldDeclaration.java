/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.ast.SignedNode;
import net.sourceforge.pmd.lang.java.multifile.signature.JavaFieldSignature;


/**
 * Represents a field declaration in the body of a type declaration.
 *
 * <p>This declaration may define several variables, possibly of different
 * types (see {@link ASTVariableDeclaratorId#getType()}). The nodes
 * corresponding to the declared variables are accessible through {@link #iterator()}.
 *
 * <pre class="grammar">
 *
 * FieldDeclaration ::= {@link ASTModifierList ModifierList} {@linkplain ASTType Type} {@linkplain ASTVariableDeclarator VariableDeclarator} ( "," {@linkplain ASTVariableDeclarator VariableDeclarator} )* ";"
 *
 * </pre>
 */
public final class ASTFieldDeclaration extends AbstractJavaNode
    implements SignedNode<ASTFieldDeclaration>,
               Iterable<ASTVariableDeclaratorId>,
               LeftRecursiveNode,
               AccessNode,
               ASTBodyDeclaration,
               InternalInterfaces.MultiVariableIdOwner {

    private JavaFieldSignature signature;

    ASTFieldDeclaration(int id) {
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
     * Gets the variable name of this field. This method searches the first
     * VariableDeclartorId node and returns its image or <code>null</code> if
     * the child node is not found.
     *
     * @return a String representing the name of the variable
     *
     * @deprecated FieldDeclaration may declare several variables, so this is not exhaustive
     *     Iterate on the {@linkplain ASTVariableDeclaratorId VariableDeclaratorIds} instead
     */
    @Deprecated
    public String getVariableName() {
        ASTVariableDeclaratorId decl = getFirstDescendantOfType(ASTVariableDeclaratorId.class);
        if (decl != null) {
            return decl.getImage();
        }
        return null;
    }


    @Override
    public JavaFieldSignature getSignature() {
        if (signature == null) {
            signature = JavaFieldSignature.buildFor(this);
        }

        return signature;
    }

    /**
     * Returns the type node at the beginning of this field declaration.
     * The type of this node is not necessarily the type of the variables,
     * see {@link ASTVariableDeclaratorId#getType()}.
     */
    @Override
    public ASTType getTypeNode() {
        return getFirstChildOfType(ASTType.class);
    }

}
