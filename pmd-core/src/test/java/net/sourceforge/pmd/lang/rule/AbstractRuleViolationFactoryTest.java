/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.lang.ast.Node;

public class AbstractRuleViolationFactoryTest {
    private RuleContext ruleContext;
    private RuleViolationFactory factory;

    private static class TestRule extends AbstractRule {
        @Override
        public void apply(List<? extends Node> nodes, RuleContext ctx) {
            throw new UnsupportedOperationException("not implemented");
        }
    }

    @Before
    public void setup() {
        ruleContext = new RuleContext();
        factory = new AbstractRuleViolationFactory() {};
    }
    
    @Test
    public void testMessage() {
        factory.addViolation(ruleContext, new TestRule(), null, "message with \"'{'\"", null);

        RuleViolation violation = ruleContext.getReport().iterator().next();
        Assert.assertEquals("message with \"{\"", violation.getDescription());
    }

    @Test
    public void testMessageArgs() {
        factory.addViolation(ruleContext, new TestRule(), null, "message with 1 argument: \"{0}\"", new Object[] {"testarg1"});

        RuleViolation violation = ruleContext.getReport().iterator().next();
        Assert.assertEquals("message with 1 argument: \"testarg1\"", violation.getDescription());
    }
}
