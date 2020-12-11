/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.metrics.impl;

import net.sourceforge.pmd.lang.java.metrics.api.JavaMetrics;
import net.sourceforge.pmd.test.AbstractMetricTestRule;

/**
 * @author Clément Fournier
 */
public class WmcTestRule extends JavaIntMetricTestRule {

    public WmcTestRule() {
        super(JavaMetrics.WEIGHED_METHOD_COUNT);
    }
}
