/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.ast;

import net.sourceforge.pmd.lang.java.qname.JavaOperationQualifiedName;


/**
 * Groups method, constructor and lambda declarations under a common type.
 *
 * @author Clément Fournier
 * @since 6.1.0
 * @deprecated Lambda expressions should not be grouped with other kinds
 *     of method declarations, they have nothing in common. Giving them a
 *     qualified name is hacky and compiler-implementation-dependent.
 *     Ultimately this supertype is not useful and can go away.
 */
@Deprecated
public interface MethodLikeNode extends JavaQualifiableNode, JavaNode {


    /**
     * @deprecated Qualified names are not very useful objects. Use them
     *     to get a nice string for a method, but this is not going
     */
    @Override
    @Deprecated
    JavaOperationQualifiedName getQualifiedName();


}
