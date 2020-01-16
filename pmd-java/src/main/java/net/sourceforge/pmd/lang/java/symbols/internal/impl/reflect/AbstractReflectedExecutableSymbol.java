/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl.reflect;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.sourceforge.pmd.lang.java.symbols.JClassSymbol;
import net.sourceforge.pmd.lang.java.symbols.JExecutableSymbol;
import net.sourceforge.pmd.lang.java.symbols.JFormalParamSymbol;

abstract class AbstractReflectedExecutableSymbol<T extends Executable> extends AbstractTypeParamOwnerSymbol<T> implements JExecutableSymbol {

    private final @NonNull ReflectedClassImpl owner;
    private List<JFormalParamSymbol> params;


    AbstractReflectedExecutableSymbol(@NonNull ReflectedClassImpl owner, T executable) {
        super(owner.symFactory, executable);
        this.owner = owner;
    }


    @NonNull
    @Override
    public final JClassSymbol getEnclosingClass() {
        return owner;
    }

    @Override
    public boolean isVarargs() {
        return reflected.isVarArgs();
    }

    @Override
    public int getArity() {
        return reflected.getParameterCount();
    }

    @Override
    public int getModifiers() {
        return reflected.getModifiers();
    }

    @Override
    public final List<JFormalParamSymbol> getFormalParameters() {
        if (params == null) {
            this.params = Arrays.stream(reflected.getParameters())
                                .map(p -> new ReflectedMethodParamImpl(this, p))
                                .collect(Collectors.toList());
        }

        return params;
    }
}
