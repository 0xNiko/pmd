/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl.ast;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.sourceforge.pmd.lang.java.ast.ASTAnyTypeDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTMethodOrConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId;
import net.sourceforge.pmd.lang.java.ast.InternalApiBridge;
import net.sourceforge.pmd.lang.java.ast.JavaParserVisitorAdapter;
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JTypeParameterOwnerSymbol;


/**
 * Populates symbols on declaration nodes. Cannot be reused.
 */
final class AstSymbolMakerVisitor extends JavaParserVisitorAdapter {

    // these map simple name to count of local classes with that name in the given class
    private final Deque<Map<String, Integer>> currentLocalIndices = new ArrayDeque<>();
    // these are counts of anon classes in the enclosing class
    private final Deque<MutableInt> anonymousCounters = new ArrayDeque<>();
    // these are binary names, eg may contain pack.Foo, pack.Foo$Nested, pack.Foo$Nested$1Local
    private final Deque<String> enclosingBinaryNames = new ArrayDeque<>();
    // these are canonical names. Contains null values if the enclosing decl has no canonical name
    private final Deque<@Nullable String> enclosingCanonicalNames = new ArrayDeque<>();
    // these are symbols, NOT 1-to-1 with the type name stack because may contain method/ctor symbols
    private final Deque<JTypeParameterOwnerSymbol> enclosingSymbols = new ArrayDeque<>();

    /** Package name of the current file. */
    private final String packageName;

    AstSymbolMakerVisitor(ASTCompilationUnit node) {
        // update the package list
        packageName = node.getPackageName();
    }

    @Override
    public Object visit(ASTVariableDeclaratorId node, Object data) {

        if (isTrueLocalVar(node)) {
            ((AstSymFactory) data).setLocalVarSymbol(node);
        } else {
            // in the other cases, building the method/ctor/class symbols already set the symbols
            assert node.getSymbol() != null : "Symbol was null for " + node;
        }

        return super.visit(node, data);
    }

    private boolean isTrueLocalVar(ASTVariableDeclaratorId node) {
        return !(node.isField() || node.isEnumConstant() || node.getParent() instanceof ASTFormalParameter);
    }


    @Override
    public Object visit(ASTAnyTypeDeclaration node, Object data) {
        String binaryName = makeBinaryName(node);
        @Nullable String canonicalName = makeCanonicalName(node, binaryName);
        InternalApiBridge.setQname(node, binaryName, canonicalName);
        JClassSymbol sym = ((AstSymFactory) data).setClassSymbol(enclosingSymbols.peek(), node);

        enclosingBinaryNames.push(binaryName);
        enclosingCanonicalNames.push(canonicalName);
        enclosingSymbols.push(sym);
        anonymousCounters.push(new MutableInt(0));
        currentLocalIndices.push(new HashMap<>());

        super.visit(node, data);

        currentLocalIndices.pop();
        anonymousCounters.pop();
        enclosingSymbols.pop();
        enclosingBinaryNames.pop();
        enclosingCanonicalNames.pop();

        return null;
    }

    @NonNull
    private String makeBinaryName(ASTAnyTypeDeclaration node) {
        String simpleName = node.getSimpleName();
        if (node.isLocal()) {
            simpleName = getNextIndexFromHistogram(currentLocalIndices.getFirst(), node.getSimpleName(), 1)
                + simpleName;
        } else if (node.isAnonymous()) {
            simpleName = "" + anonymousCounters.getFirst().incrementAndGet();
        }

        String enclosing = enclosingBinaryNames.peek();
        return enclosing != null ? enclosing + "$" + simpleName
                                 : packageName.isEmpty() ? simpleName
                                                         : packageName + "." + simpleName;
    }

    @Nullable
    private String makeCanonicalName(ASTAnyTypeDeclaration node, String binaryName) {
        if (node.isAnonymous() || node.isLocal()) {
            return null;
        }

        if (enclosingCanonicalNames.isEmpty()) {
            // toplevel
            return binaryName;
        }

        @Nullable String enclCanon = enclosingCanonicalNames.getFirst();
        return enclCanon == null
               ? null  // enclosing has no canonical name, so this one doesn't either
               : enclCanon + '.' + node.getSimpleName();

    }

    @Override
    public Object visit(ASTMethodOrConstructorDeclaration node, Object data) {
        enclosingSymbols.push(node.getSymbol());
        super.visit(node, data);
        enclosingSymbols.pop();
        return null;
    }


    /**
     * Gets the next available index based on a key and a histogram (map of keys to int counters).
     * If the key doesn't exist, we add a new entry with the startIndex.
     *
     * <p>Used for lambda and anonymous class counters
     *
     * @param histogram  The histogram map
     * @param key        The key to access
     * @param startIndex First index given out when the key doesn't exist
     *
     * @return The next free index
     */
    private static <T> int getNextIndexFromHistogram(Map<T, Integer> histogram, T key, int startIndex) {
        Integer count = histogram.get(key);
        if (count == null) {
            histogram.put(key, startIndex);
            return startIndex;
        } else {
            histogram.put(key, count + 1);
            return count + 1;
        }
    }

}
