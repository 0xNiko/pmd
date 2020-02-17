/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Clément Fournier
 */
public interface TypeParamOwnerNode extends JavaNode {

    /**
     * Returns the type parameter declaration of this node, or null if
     * there is none.
     */
    @Nullable
    default ASTTypeParameters getTypeParameters() {
        return getFirstChildOfType(ASTTypeParameters.class);
    }


}
