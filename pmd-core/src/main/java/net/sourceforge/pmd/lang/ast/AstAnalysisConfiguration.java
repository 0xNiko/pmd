/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.lang.LanguageVersion;


/**
 * Configuration relevant to e.g. an {@link AstProcessingStage}.
 *
 * @author Clément Fournier
 * @since 6.10.0
 */
@Experimental
public interface AstAnalysisConfiguration {


    /**
     * Gets the classloader used for type resolution.
     *
     * @return The classloader.
     */
    ClassLoader getTypeResolutionClassLoader();


    /**
     * Returns the language version used for this analysis.
     */
    LanguageVersion getLanguageVersion();

}
