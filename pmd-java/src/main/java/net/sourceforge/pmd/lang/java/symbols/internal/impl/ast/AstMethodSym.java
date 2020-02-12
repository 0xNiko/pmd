/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl.ast;

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JMethodSymbol;

/**
 * @author Clément Fournier
 */
final class AstMethodSym
    extends AbstractAstExecSymbol<ASTMethodDeclaration>
    implements JMethodSymbol {


    AstMethodSym(ASTMethodDeclaration node, AstSymFactory factory, JClassSymbol owner) {
        super(node, factory, owner);
    }

    @Override
    public String getSimpleName() {
        return node.getName();
    }

}
