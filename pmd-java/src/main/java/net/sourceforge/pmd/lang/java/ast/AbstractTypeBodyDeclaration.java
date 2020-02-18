/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

/**
 * @author Clément Fournier
 * @since 6.2.0
 */
abstract class AbstractTypeBodyDeclaration extends AbstractJavaNode implements ASTAnyTypeBodyDeclaration {

    AbstractTypeBodyDeclaration(int id) {
        super(id);
    }


    @Override
    public JavaNode getDeclarationNode() {
        if (getNumChildren() == 0) {
            return null;
        }

        // skips the annotations
        AccessNode node = getFirstChildOfType(AccessNode.class);
        if (node == null) {
            return getFirstChildOfType(ASTInitializer.class);
        }

        return node;
    }


}
