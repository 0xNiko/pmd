/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */


package net.sourceforge.pmd.lang.java.symbols;

import net.sourceforge.pmd.annotation.Experimental;
import net.sourceforge.pmd.annotation.InternalApi;
import net.sourceforge.pmd.lang.java.symbols.internal.impl.SymbolFactory;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;


/**
 * Represents a named program element that can be referred to by simple name. Abstracts over
 * whether the declaration is in the analysed file or not, using reflection when it's not.
 *
 * <p>This type hierarchy is probably not directly relevant to users writing
 * rules. It's mostly intended to unify the representation of type resolution
 * and symbol analysis.
 *
 * <p>SymbolDeclarations have no reference to the scope they were found in, because
 * that would tie the code reference to the analysed file, preventing the garbage
 * collection of scopes and nodes. This is a major difference with {@link NameDeclaration}.
 * The declaring scope would also vary from file to file. E.g.
 *
 * <pre>
 * class Foo {
 *     public int foo;
 *     // here the declaring scope of Foo#foo would be the class scope of this file
 * }
 *
 * class Bar extends Foo {
 *     // here the declaring scope of Foo#foo would be the inherited scope from Foo
 * }
 * </pre>
 *
 * <p>By storing no reference, we ensure that code references can be shared across the
 * analysed project, allowing reflective resolution to be only done once.
 *
 *
 * @author Clément Fournier
 * @since 7.0.0
 */
@Experimental
@InternalApi
public interface JElementSymbol {


    /**
     * Gets the name with which this declaration may be referred to,
     * eg the name of the method, or the simple name of the class.
     *
     * @return the name
     */
    String getSimpleName();


    /**
     * Two symbols representing the same program element should be equal.
     * So eg two {@link JClassSymbol}, even if their implementation class
     * is different, should compare publicly observable properties (their
     * binary name is enough). {@link #hashCode()} must of course be consistent
     * with this contract.
     *
     * <p>Symbols should only be compared using this method, never with {@code ==},
     * because their unicity is not guaranteed (even for the static ones
     * declared in {@link SymbolFactory}).
     *
     * @param o Comparand
     *
     * @return True if the other is a symbol of the same type and
     */
    @Override
    boolean equals(Object o);

    // TODO access to annotations could be added to the API if we publish it

    // TODO tests

    // TODO add type information when TypeDefinitions are reviewed
    // We should be able to create a type definition from a java.lang.reflect.Type,
    // paying attention to type variables of enclosing methods and types.
    // We should also be able to do so from an ASTType, with support from a JSymbolTable.
}
