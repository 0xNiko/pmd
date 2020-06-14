/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.annotation.InternalApi;

public class ASTAnnotationTypeMemberDeclaration extends AbstractTypeBodyDeclaration {

    @InternalApi
    @Deprecated
    public ASTAnnotationTypeMemberDeclaration(int id) {
        super(id);
    }

    @Override
    public <P, R> R acceptVisitor(JavaVisitor<P, R> visitor, P data) {
        return visitor.visit(this, data);
    }
}
