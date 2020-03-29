/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal.impl

import io.kotlintest.matchers.haveSize
import io.kotlintest.should
import io.kotlintest.specs.FunSpec
import net.sourceforge.pmd.lang.ast.test.shouldBe

/**
 * @author Clément Fournier
 */
class UnresolvedClassTest : FunSpec({

    test("Test simple unresolved class") {

        val sym = UnresolvedSymFactory().makeUnresolvedReference("some.pack.Class", 0)

        sym::isUnresolved shouldBe true
        sym::getSimpleName shouldBe "Class"
        sym::getPackageName shouldBe "some.pack"
        sym::getCanonicalName shouldBe "some.pack.Class"
        sym::getBinaryName shouldBe "some.pack.Class"

        sym::isClass shouldBe true
        sym::isArray shouldBe false
        sym::isAnonymousClass shouldBe false
        sym::isEnum shouldBe false
        sym::isInterface shouldBe false

        sym::getTypeParameterCount shouldBe 0
        sym::getTypeParameters shouldBe emptyList()
    }

    test("Test arity change") {

        val sym = UnresolvedSymFactory().makeUnresolvedReference("some.pack.Class", 0) as UnresolvedClassImpl

        sym::getTypeParameterCount shouldBe 0
        sym::getTypeParameters shouldBe emptyList()

        sym.typeParameterCount = 2

        sym::getTypeParameterCount shouldBe 2
        val tparams = sym.typeParameters
        tparams should haveSize(2)
        tparams.forEach { it::getDeclaringSymbol shouldBe sym }
        tparams.distinctBy { it.simpleName } should haveSize(2)

        sym.typeParameterCount = 3

        // no change
        sym::getTypeParameterCount shouldBe 2
        sym::getTypeParameters shouldBe tparams
    }

})
