/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.ast.impl.antlr4;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import net.sourceforge.pmd.util.DataMap;
import net.sourceforge.pmd.util.DataMap.DataKey;

/**
 * @author Clément Fournier
 */
public class PmdAntlrTerminalNode extends TerminalNodeImpl implements AntlrNode {

    private final DataMap<DataKey<?, ?>> userData = DataMap.newDataMap();

    public PmdAntlrTerminalNode(Token t) {
        super(t);
    }

    @Override
    public AntlrNode getChild(int i) {
        return null;
    }

    @Override
    public int getNumChildren() {
        return 0;
    }

    @Override
    public String getXPathNodeName() {
        return "Token" + getSymbol().getType(); // TODO
    }

    @Override
    public AntlrNode getParent() {
        return (AntlrNode) super.getParent();
    }


    @Override
    public void setParent(RuleContext parent) {
        assert parent instanceof AntlrNode : "Parent should be a parent";
        super.setParent(parent);
    }


    // FIXME these coordinates are not accurate


    @Override
    public int getBeginLine() {
        return getSymbol().getLine();
    }

    @Override
    public int getBeginColumn() {
        return getSymbol().getCharPositionInLine() + 1;
    }

    @Override
    public int getEndLine() {
        return getBeginLine();
    }

    @Override
    public int getEndColumn() {
        return getBeginColumn() + getSymbol().getText().length();
    }

    @Override
    public DataMap<DataKey<?, ?>> getUserMap() {
        return userData;
    }
}
