/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.annotation.InternalApi;

public class ASTPrimaryExpression extends AbstractJavaTypeNode {

    @InternalApi
    @Deprecated
    public ASTPrimaryExpression(int id) {
        super(id);
    }


    @Override
    protected <P, R> R acceptVisitor(JavaVisitor<P, R> visitor, P data) {
        return visitor.visit(this, data);
    }

}
