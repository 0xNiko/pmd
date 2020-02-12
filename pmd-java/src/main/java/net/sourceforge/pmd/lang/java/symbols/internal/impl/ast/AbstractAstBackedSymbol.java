/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl.ast;

import net.sourceforge.pmd.lang.java.ast.InternalApiBridge;
import net.sourceforge.pmd.lang.java.ast.SymbolDeclaratorNode;
import net.sourceforge.pmd.lang.java.symbols.JElementSymbol;
import net.sourceforge.pmd.lang.java.symbols.internal.impl.SymbolEquality;
import net.sourceforge.pmd.lang.java.symbols.internal.impl.SymbolToStrings;

/**
 * @author Clément Fournier
 */
abstract class AbstractAstBackedSymbol<T extends SymbolDeclaratorNode> implements JElementSymbol {

    protected final T node;
    protected final AstSymFactory factory;

    protected AbstractAstBackedSymbol(T node, AstSymFactory factory) {
        this.node = node;
        this.factory = factory;
        InternalApiBridge.setSymbol(node, this);
    }

    @Override
    public String toString() {
        return SymbolToStrings.AST.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        return SymbolEquality.equals(this, o);
    }

    @Override
    public int hashCode() {
        return SymbolEquality.hash(this);
    }
}
