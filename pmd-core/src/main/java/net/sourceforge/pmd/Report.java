/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.dfa.report.ReportTree;
import net.sourceforge.pmd.lang.rule.stat.StatisticalRule;
import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.stat.Metric;
import net.sourceforge.pmd.util.DateTimeUtil;
import net.sourceforge.pmd.util.NumericConstants;

/**
 * A {@link Report} is the output of a PMD execution. This
 * includes violations, suppressed violations, metrics, error
 * during processing and configuration errors. PMD's entry point creates
 * a report (see {@link PMD#processFiles(PMDConfiguration, List, Collection, List)}).
 * The mutation methods on this class are deprecated, as they will be
 * internalized in PMD 7.
 */
public class Report implements Iterable<RuleViolation> {

    /*
     * The idea is to store the violations in a tree instead of a list, to do
     * better and faster sort and filter mechanism and to visualize the result
     * as tree. (ide plugins).
     */
    private final ReportTree violationTree = new ReportTree();

    // Note that this and the above data structure are both being maintained for
    // a bit
    private final List<RuleViolation> violations = new ArrayList<>();
    private final Set<Metric> metrics = new HashSet<>();
    private final List<ThreadSafeReportListener> listeners = new ArrayList<>();
    private final List<ProcessingError> errors = new ArrayList<>();
    private final List<ConfigurationError> configErrors = new ArrayList<>();
    private final Object lock = new Object();
    private Map<Integer, String> linesToSuppress = new HashMap<>();
    private long start;
    private long end;
    private final List<SuppressedViolation> suppressedRuleViolations = new ArrayList<>();

    /**
     * @deprecated {@link Report} instances are created by PMD. There is no need
     * to create a own report. This constructor will be hidden
     * in PMD7.
     */
    @Deprecated
    @InternalApi
    public Report() { // NOPMD - UnnecessaryConstructor
        // TODO: should be package-private, you have to use a listener to build a report.
    }

    /**
     * Creates a new, initialized, empty report for the given file name.
     *
     * @param ctx      The context to use to connect to the report
     * @param fileName the filename used to report any violations
     *
     * @return the new report
     *
     * @deprecated Is internal API
     */
    @Deprecated
    @InternalApi
    public static Report createReport(RuleContext ctx, String fileName) {
        Report report = new Report();

        // overtake the listener
        report.addListeners(ctx.getReport().getListeners());

        ctx.setReport(report);
        ctx.setSourceCodeFile(new File(fileName));
        return report;
    }

    /**
     * Represents a duration. Useful for reporting processing time.
     *
     * @deprecated Not used within PMD. Rendering durations is format-specific.
     */
    @Deprecated
    public static class ReadableDuration {
        private final long duration;

        /**
         * Creates a new duration.
         *
         * @param duration
         *            the duration in milliseconds.
         */
        public ReadableDuration(long duration) {
            this.duration = duration;
        }

        /**
         * Gets a human readable representation of the duration, such as "1h 3m
         * 5s".
         *
         * @return human readable representation of the duration
         */
        public String getTime() {
            return DateTimeUtil.asHoursMinutesSeconds(duration);
        }
    }

    /**
     * Represents a configuration error.
     */
    public static class ConfigurationError {
        private final Rule rule;
        private final String issue;

        /**
         * Creates a new configuration error for a specific rule.
         *
         * @param theRule
         *            the rule which is configured wrongly
         * @param theIssue
         *            the reason, why the configuration is wrong
         */
        public ConfigurationError(Rule theRule, String theIssue) {
            rule = theRule;
            issue = theIssue;
        }

        /**
         * Gets the wrongly configured rule
         *
         * @return the wrongly configured rule
         */
        public Rule rule() {
            return rule;
        }

        /**
         * Gets the reason for the configuration error.
         *
         * @return the issue
         */
        public String issue() {
            return issue;
        }
    }

    /**
     * Represents a processing error, such as a parse error.
     */
    public static class ProcessingError {
        private final Throwable error;
        private final String file;

        /**
         * Creates a new processing error
         *
         * @param error
         *            the error
         * @param file
         *            the file during which the error occurred
         */
        public ProcessingError(Throwable error, String file) {
            this.error = error;
            this.file = file;
        }

        public String getMsg() {
            return error.getClass().getSimpleName() + ": " + error.getMessage();
        }

        public String getDetail() {
            try (StringWriter stringWriter = new StringWriter();
                    PrintWriter writer = new PrintWriter(stringWriter)) {
                error.printStackTrace(writer);
                return stringWriter.toString();
            } catch (IOException e) {
                // IOException on close - should never happen when using StringWriter
                throw new RuntimeException(e);
            }
        }

        public String getFile() {
            return file;
        }

        public Throwable getError() {
            return error;
        }
    }

    /**
     * Represents a violation, that has been suppressed.
     */
    public static class SuppressedViolation {
        private final RuleViolation rv;
        private final boolean isNOPMD;
        private final String userMessage;

        /**
         * Creates a suppressed violation.
         *
         * @param rv
         *            the actual violation, that has been suppressed
         * @param isNOPMD
         *            the suppression mode: <code>true</code> if it is
         *            suppressed via a NOPMD comment, <code>false</code> if
         *            suppressed via annotations.
         * @param userMessage
         *            contains the suppressed code line or <code>null</code>
         */
        public SuppressedViolation(RuleViolation rv, boolean isNOPMD, String userMessage) {
            this.isNOPMD = isNOPMD;
            this.rv = rv;
            this.userMessage = userMessage;
        }

        /**
         * Returns <code>true</code> if the violation has been suppressed via a
         * NOPMD comment.
         *
         * @return <code>true</code> if the violation has been suppressed via a
         *         NOPMD comment.
         */
        public boolean suppressedByNOPMD() {
            return this.isNOPMD;
        }

        /**
         * Returns <code>true</code> if the violation has been suppressed via a
         * annotation.
         *
         * @return <code>true</code> if the violation has been suppressed via a
         *         annotation.
         */
        public boolean suppressedByAnnotation() {
            return !this.isNOPMD;
        }

        public RuleViolation getRuleViolation() {
            return this.rv;
        }

        public String getUserMessage() {
            return userMessage;
        }
    }

    /**
     * Configure the lines, that are suppressed via a NOPMD comment.
     *
     * @param lines
     *            the suppressed lines
     */
    public void suppress(Map<Integer, String> lines) {
        linesToSuppress = lines;
    }

    private static String keyFor(RuleViolation rv) {

        return StringUtils.isNotBlank(rv.getPackageName()) ? rv.getPackageName() + '.' + rv.getClassName() : "";
    }

    /**
     * Calculate a summary of violation counts per fully classified class name.
     *
     * @return violations per class name
     *
     * @deprecated This is too specific. Not every violation has a qualified name.
     */
    @Deprecated
    public Map<String, Integer> getCountSummary() {
        Map<String, Integer> summary = new HashMap<>();
        for (RuleViolation rv : violationTree) {
            String key = keyFor(rv);
            Integer o = summary.get(key);
            summary.put(key, o == null ? NumericConstants.ONE : o + 1);
        }
        return summary;
    }

    /**
     * @deprecated The {@link ReportTree} is deprecated
     */
    @Deprecated
    public ReportTree getViolationTree() {
        return this.violationTree;
    }

    /**
     * Calculate a summary of violations per rule.
     *
     * @return a Map summarizing the Report: String (rule name) -&gt; Integer (count
     *         of violations)
     *
     * @deprecated This is too specific, only used by one renderer.
     */
    @Deprecated
    public Map<String, Integer> getSummary() {
        Map<String, Integer> summary = new HashMap<>();
        for (RuleViolation rv : violations) {
            String name = rv.getRule().getName();
            if (!summary.containsKey(name)) {
                summary.put(name, NumericConstants.ZERO);
            }
            Integer count = summary.get(name);
            summary.put(name, count + 1);
        }
        return summary;
    }

    /**
     * Registers a report listener
     *
     * @param listener
     *            the listener
     */
    @Deprecated
    public void addListener(ThreadSafeReportListener listener) {
        listeners.add(listener);
    }

    /**
     * Returns the suppressed violations.
     *
     * @deprecated Use {@link #getSuppressedViolations()} (be aware, that that method returns an unmodifiable list)
     */
    @Deprecated
    public List<SuppressedViolation> getSuppressedRuleViolations() {
        return suppressedRuleViolations;
    }

    /**
     * Adds a new rule violation to the report and notify the listeners.
     *
     * @param violation the violation to add
     *
     * @deprecated PMD's way of creating a report is internal and may be changed in pmd 7.
     */
    @Deprecated
    @InternalApi
    public void addRuleViolation(RuleViolation violation) {

        // NOPMD suppress
        int line = violation.getBeginLine();
        if (linesToSuppress.containsKey(line)) {
            suppressedRuleViolations.add(new SuppressedViolation(violation, true, linesToSuppress.get(line)));
            return;
        }

        if (violation.isSuppressed()) {
            suppressedRuleViolations.add(new SuppressedViolation(violation, false, null));
            return;
        }

        int index = Collections.binarySearch(violations, violation, RuleViolation.DEFAULT_COMPARATOR);
        violations.add(index < 0 ? -index - 1 : index, violation);
        violationTree.addRuleViolation(violation);
        for (ThreadSafeReportListener listener : listeners) {
            listener.ruleViolationAdded(violation);
        }
    }

    /**
     * Adds a new metric to the report and notify the listeners
     *
     * @param metric
     *            the metric to add
     *
     * @deprecated see {@link StatisticalRule}
     */
    @Deprecated
    public void addMetric(Metric metric) {
        metrics.add(metric);
        for (ThreadSafeReportListener listener : listeners) {
            listener.metricAdded(metric);
        }
    }

    /**
     * Adds a new configuration error to the report.
     *
     * @param error
     *            the error to add
     *
     * @deprecated PMD's way of creating a report is internal and may be changed in pmd 7.
     */
    @Deprecated
    @InternalApi
    public void addConfigError(ConfigurationError error) {
        configErrors.add(error);
    }

    /**
     * Adds a new processing error to the report.
     *
     * @param error
     *            the error to add
     * @deprecated PMD's way of creating a report is internal and may be changed in pmd 7.
     */
    @Deprecated
    @InternalApi
    public void addError(ProcessingError error) {
        errors.add(error);
    }

    /**
     * Merges the given report into this report. This might be necessary, if a
     * summary over all violations is needed as PMD creates one report per file
     * by default.
     *
     * <p>This is synchronized on an internal lock (note that other mutation
     * operations are not synchronized, todo for pmd 7).
     *
     * @param r the report to be merged into this.
     *
     * @see AbstractAccumulatingRenderer
     *
     * @deprecated Internal API
     */
    @Deprecated
    @InternalApi
    public void merge(Report r) {
        synchronized (lock) {
            errors.addAll(r.errors);
            configErrors.addAll(r.configErrors);
            metrics.addAll(r.metrics);
            suppressedRuleViolations.addAll(r.suppressedRuleViolations);

            for (RuleViolation violation : r.getViolations()) {
                int index = Collections.binarySearch(violations, violation, RuleViolation.DEFAULT_COMPARATOR);
                violations.add(index < 0 ? -index - 1 : index, violation);
                violationTree.addRuleViolation(violation);
            }
        }
    }

    /**
     * Check whether any metrics have been reported
     *
     * @return <code>true</code> if there are metrics, <code>false</code>
     *         otherwise
     *
     * @deprecated see {@link StatisticalRule}
     */
    @Deprecated
    public boolean hasMetrics() {
        return !metrics.isEmpty();
    }

    /**
     * Iterate over the metrics.
     *
     * @return an iterator over the metrics
     *
     * @deprecated see {@link StatisticalRule}
     */
    @Deprecated
    public Iterator<Metric> metrics() {
        return metrics.iterator();
    }

    /**
     * Checks whether there are no violations and no processing errors.
     * That means, that PMD analysis yielded nothing to worry about.
     *
     * @deprecated Use {@link #getViolations()} or {@link #getProcessingErrors()}
     */
    @Deprecated
    public boolean isEmpty() {
        return !violations.iterator().hasNext() && !hasErrors();
    }

    /**
     * Checks whether any processing errors have been reported.
     *
     * @return <code>true</code> if there were any processing errors,
     *         <code>false</code> otherwise
     *
     * @deprecated Use {@link #getProcessingErrors()}.isEmpty()
     */
    @Deprecated
    public boolean hasErrors() {
        return !getProcessingErrors().isEmpty();
    }

    /**
     * Checks whether any configuration errors have been reported.
     *
     * @return <code>true</code> if there were any configuration errors,
     *         <code>false</code> otherwise
     *
     * @deprecated Use {@link #getConfigurationErrors()}.isEmpty()
     */
    @Deprecated
    public boolean hasConfigErrors() {
        return !getConfigurationErrors().isEmpty();
    }

    /**
     * Checks whether no violations have been reported.
     *
     * @return <code>true</code> if no violations have been reported,
     *         <code>false</code> otherwise
     *
     * @deprecated The {@link ReportTree} is deprecated, use {@link #getViolations()}.isEmpty() instead.
     */
    @Deprecated
    public boolean treeIsEmpty() {
        return !violationTree.iterator().hasNext();
    }

    /**
     * Returns an iteration over the reported violations.
     *
     * @return an iterator
     *
     * @deprecated The {@link ReportTree} is deprecated
     */
    @Deprecated
    public Iterator<RuleViolation> treeIterator() {
        return violationTree.iterator();
    }

    /**
     * @deprecated Use {@link #getViolations()}
     */
    @Deprecated
    @Override
    public Iterator<RuleViolation> iterator() {
        return violations.iterator();
    }


    /**
     * Returns an unmodifiable list of violations that were suppressed.
     */
    public final List<SuppressedViolation> getSuppressedViolations() {
        return Collections.unmodifiableList(suppressedRuleViolations);
    }

    /**
     * Returns an unmodifiable list of violations that have been
     * recorded until now. None of those violations were suppressed.
     *
     * <p>The violations list is sorted with {@link RuleViolation#DEFAULT_COMPARATOR}.
     */
    public final List<RuleViolation> getViolations() {
        return Collections.unmodifiableList(violations);
    }


    /**
     * Returns an unmodifiable list of processing errors that have been
     * recorded until now.
     */
    public final List<ProcessingError> getProcessingErrors() {
        return Collections.unmodifiableList(errors);
    }


    /**
     * Returns an unmodifiable list of configuration errors that have
     * been recorded until now.
     */
    public final List<ConfigurationError> getConfigurationErrors() {
        return Collections.unmodifiableList(configErrors);
    }


    /**
     * Returns an iterator of the reported processing errors.
     *
     * @return the iterator
     *
     * @deprecated Use {@link #getProcessingErrors()}
     */
    @Deprecated
    public Iterator<ProcessingError> errors() {
        return getProcessingErrors().iterator();
    }

    /**
     * Returns an iterator of the reported configuration errors.
     *
     * @return the iterator
     * @deprecated Use {@link #getConfigurationErrors()}
     */
    @Deprecated
    public Iterator<ConfigurationError> configErrors() {
        return getConfigurationErrors().iterator();
    }

    /**
     * The number of violations.
     *
     * @return number of violations.
     *
     * @deprecated The {@link ReportTree} is deprecated
     */
    @Deprecated
    public int treeSize() {
        return violationTree.size();
    }

    /**
     * The number of violations.
     *
     * @return number of violations.
     *
     * @deprecated Use {@link #getViolations()}
     */
    @Deprecated
    public int size() {
        return violations.size();
    }

    /**
     * Mark the start time of the report. This is used to get the elapsed time
     * in the end.
     *
     * @see #getElapsedTimeInMillis()
     *
     * @deprecated Not used, {@link #getElapsedTimeInMillis()} will be removed
     */
    @Deprecated
    public void start() {
        start = System.currentTimeMillis();
    }

    /**
     * Mark the end time of the report. This is ued to get the elapsed time.
     *
     * @see #getElapsedTimeInMillis()
     * @deprecated Not used, {@link #getElapsedTimeInMillis()} will be removed
     */
    @Deprecated
    public void end() {
        end = System.currentTimeMillis();
    }

    /**
     * @deprecated Unused
     */
    @Deprecated
    public long getElapsedTimeInMillis() {
        return end - start;
    }

    /**
     * @deprecated {@link ThreadSafeReportListener} is deprecated
     */
    @Deprecated
    public List<ThreadSafeReportListener> getListeners() {
        return listeners;
    }

    /**
     * Adds all given listeners to this report
     *
     * @param allListeners
     *            the report listeners
     *
     * @deprecated {@link ThreadSafeReportListener} is deprecated
     */
    @Deprecated
    public void addListeners(List<ThreadSafeReportListener> allListeners) {
        listeners.addAll(allListeners);
    }
}
