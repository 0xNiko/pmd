/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.symbols.internal

import io.kotlintest.matchers.haveSize
import io.kotlintest.properties.Gen
import io.kotlintest.should
import net.sourceforge.pmd.lang.java.symbols.JClassSymbol
import net.sourceforge.pmd.lang.java.symbols.internal.impl.reflect.ClasspathSymbolResolver
import net.sourceforge.pmd.lang.java.symbols.internal.impl.reflect.ReflectSymInternals.*
import net.sourceforge.pmd.lang.java.symbols.internal.impl.reflect.ReflectionSymFactory
import net.sourceforge.pmd.lang.java.symbols.table.internal.HeaderScopesTest
import java.io.File
import java.io.IOException
import java.util.*
import java.util.stream.Stream

/** Testing utilities */


fun <T> Stream<T>.firstOrNull(): T? = findFirst().orElse(null)

val testSymFactory = ReflectionSymFactory()
val testSymResolver = ClasspathSymbolResolver(HeaderScopesTest::class.java.classLoader, testSymFactory)

fun classSym(klass: Class<*>?) = testSymFactory.getClassSymbol(klass)

fun <T, K> List<T>.groupByUnique(keySelector: (T) -> K): Map<K, T> =
        groupBy(keySelector).mapValues { (_, vs) ->
            vs should haveSize(1)
            vs.first()
        }


fun <T, R> Gen<T>.forAllEqual(test: (T) -> Pair<R, R>) {
    random().forEach {
        val (t, r) = test(it)
        if (t != r && r == t || t == r && r != t) {
            throw AssertionError("Asymmetry in equals relation $t <=> $r")
        } else if (t != r) {
            throw AssertionError("Expected property of $it to be $r, got $t")
        }
    }
}

/** Generator of test instances. */
object TestClassesGen : Gen<Class<*>> {
    override fun constants(): Iterable<Class<*>> = emptyList()

    override fun random(): Sequence<Class<*>> =
            sequenceOf(
                    java.lang.Object::class.java,
                    IntArray::class.java,
                    Cloneable::class.java,
                    Integer.TYPE,
                    Array<String>::class.java) +
                    getClassesInPackage(javaClass.`package`.name + ".internal.testdata").asSequence()

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    fun getClassesInPackage(packageName: String): List<Class<*>> {

        val result = ArrayList<Class<*>>()

        /** Recursive method side-effecting on the result list. */
        fun findClasses(directory: File, packageName: String) {
            if (!directory.exists()) {
                return
            }
            val files = directory.listFiles()
            for (file in files) {
                if (file.isDirectory) {
                    assert(!file.name.contains("."))
                    findClasses(file, packageName + "." + file.name)
                } else if (file.name.endsWith(".class")) {
                    result.add(Class.forName(packageName + '.' + file.name.substring(0, file.name.length - ".class".length)))
                }
            }
        }

        val classLoader = Thread.currentThread().contextClassLoader!!
        val path = packageName.replace('.', '/')
        val resources = classLoader.getResources(path)
        val dirs = generateSequence {
            if (resources.hasMoreElements())
                File(resources.nextElement().file)
            else null
        }

        for (directory in dirs) {
            findClasses(directory, packageName)
        }
        return result
    }
}

/** Generator of test instances. */
object PrimitiveSymGen : Gen<JClassSymbol> {
    override fun constants() = listOf(
            INT_SYM,
            DOUBLE_SYM,
            FLOAT_SYM,
            VOID_SYM,
            CHAR_SYM,
            BYTE_SYM,
            SHORT_SYM,
            LONG_SYM,
            BOOLEAN_SYM
    )

    override fun random() = emptySequence<JClassSymbol>()
}
