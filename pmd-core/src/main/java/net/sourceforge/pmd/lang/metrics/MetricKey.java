/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.metrics;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.internal.util.AssertionUtil;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.util.DataMap.DataKey;

/**
 * Key identifying a metric. Such keys <i>must</i> implement the hashCode method. Enums are well fitted to serve as
 * metric keys.
 *
 * @param <N> Type of nodes the metric can be computed on
 *
 * @author Clément Fournier
 * @since 5.8.0
 */
public interface MetricKey<N extends Node> extends DataKey<MetricKey<N>, Double> {

    /**
     * Returns the name of the metric.
     *
     * @return The name of the metric
     */
    String name();


    /**
     * Returns the object used to calculate the metric.
     *
     * @return The calculator
     */
    Metric<N> getCalculator();


    /**
     * Returns true if the metric held by this key can be computed on this node.
     * TODO this should be turned into supports(Node)
     *
     * @param node The node to test
     *
     * @return Whether or not the metric can be computed on this node
     */
    boolean supports(N node);

    // TODO the metric key should know about supported options


    /**
     * Creates a new metric key from its metric and name.
     *
     * @param name   The name of the metric
     * @param metric The metric to use
     * @param <T>    Type of node the metric can be computed on
     *
     * @return The metric key
     *
     * @throws NullPointerException If either parameter is null
     */
    static <T extends Node> MetricKey<T> of(@NonNull String name, @NonNull Metric<T> metric) {
        AssertionUtil.requireParamNotNull("name", name);
        AssertionUtil.requireParamNotNull("metric", metric);

        return new MetricKey<T>() {
            @Override
            public String name() {
                return name;
            }


            @Override
            public Metric<T> getCalculator() {
                return metric;
            }


            @Override
            public boolean supports(T node) {
                return metric.supports(node);
            }


            @Override
            public boolean equals(Object obj) {
                return obj != null && getClass() == obj.getClass()
                    && Objects.equals(name(), ((MetricKey) obj).name())
                    && Objects.equals(getCalculator(), ((MetricKey) obj).getCalculator());
            }


            @Override
            public int hashCode() {
                return metric.hashCode() * 31 + name.hashCode();
            }
        };
    }
}
