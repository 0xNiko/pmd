/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang;

import java.util.Collections;
import java.util.Set;

import net.sourceforge.pmd.util.CollectionUtil;

import net.sf.saxon.lib.ExtensionFunctionDefinition;


/**
 * Interface for performing Language specific XPath handling, such as
 * initialization and navigation.
 */
public interface XPathHandler {

    /**
     * Returns the set of extension functions for this language module.
     * These are the additional functions available in XPath queries.
     */
    Set<ExtensionFunctionDefinition> getRegisteredExtensionFunctions();


    static XPathHandler noFunctionDefinitions() {
        return Collections::emptySet;
    }


    /**
     * Returns a default XPath handler.
     */
    static XPathHandler getHandlerForFunctionDefs(ExtensionFunctionDefinition first, ExtensionFunctionDefinition... defs) {
        Set<ExtensionFunctionDefinition> set = CollectionUtil.setOf(first, defs);
        return () -> set;
    }
}
