/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.apex.multifile;

import net.sourceforge.pmd.lang.apex.ast.ApexNode;

/**
 * @author Clément Fournier
 */
public class ApexMultifileVisitorFacade {

    public void initializeWith(ApexNode<?> rootNode) {
        ApexMultifileVisitor visitor = new ApexMultifileVisitor(ApexProjectMirror.INSTANCE);
        rootNode.acceptVisitor(visitor, null);
    }

}
