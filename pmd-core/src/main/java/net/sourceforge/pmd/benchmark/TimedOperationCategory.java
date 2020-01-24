/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.benchmark;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * A category for a {@link TimedOperation}, rendered either as a section
 * if several operations are registered on the same category but with
 * distinct labels, or put into the "remaining categories" section.
 *
 * @author Juan Martín Sotuyo Dodero
 */
public enum TimedOperationCategory {
    RULE,
    RULECHAIN_RULE,
    COLLECT_FILES,
    LOAD_RULES,
    PARSER,
    /** Subdivided into one label for each stage. */
    LANGUAGE_SPECIFIC_PROCESSING,
    RULECHAIN_AST_INDEXATION,
    REPORTING,
    FILE_PROCESSING,
    UNACCOUNTED;

    public String displayName() {
        final String[] parts = name().toLowerCase(Locale.getDefault()).split("_");
        final StringBuilder sb = new StringBuilder();
        for (final String part : parts) {
            sb.append(StringUtils.capitalize(part)).append(' ');
        }
        sb.setLength(sb.length() - 1); // remove the final space
        return sb.toString();
    }
}
