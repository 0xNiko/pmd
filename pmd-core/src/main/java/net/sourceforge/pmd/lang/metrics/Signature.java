/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.metrics;

import net.sourceforge.pmd.annotation.DeprecatedUntil700;
import net.sourceforge.pmd.lang.ast.SignedNode;

/**
 * Signature of a node.
 *
 * @param <N> The type of node this signature signs
 *
 * @author Clément Fournier
 * @since 6.0.0
 */
@Deprecated
@DeprecatedUntil700
public interface Signature<N extends SignedNode<N>> {
}
