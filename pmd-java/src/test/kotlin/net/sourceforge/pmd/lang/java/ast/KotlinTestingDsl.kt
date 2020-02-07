package net.sourceforge.pmd.lang.java.ast

import com.github.oowekyala.treeutils.matchers.baseShouldMatchSubtree
import com.github.oowekyala.treeutils.printers.KotlintestBeanTreePrinter
import io.kotlintest.Matcher
import io.kotlintest.Result
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldThrow
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.ast.ParseException
import net.sourceforge.pmd.lang.ast.TokenMgrError
import net.sourceforge.pmd.lang.ast.test.*
import net.sourceforge.pmd.lang.java.JavaParsingHelper
import java.beans.PropertyDescriptor

/**
 * Represents the different Java language versions.
 */
enum class JavaVersion : Comparable<JavaVersion> {
    J1_3, J1_4, J1_5, J1_6, J1_7, J1_8, J9, J10, J11, J12, J12__PREVIEW, J13, J13__PREVIEW;

    /** Name suitable for use with e.g. [JavaParsingHelper.parse] */
    val pmdName: String = name.removePrefix("J").replaceFirst("__", "-").replace('_', '.').toLowerCase()

    val parser: JavaParsingHelper = JavaParsingHelper.WITH_PROCESSING.withDefaultVersion(pmdName)

    operator fun not(): List<JavaVersion> = values().toList() - this

    /**
     * Overloads the range operator, e.g. (`J9..J11`).
     * If both operands are the same, a singleton list is returned.
     */
    operator fun rangeTo(last: JavaVersion): List<JavaVersion> =
            when {
                last == this -> listOf(this)
                last.ordinal > this.ordinal -> values().filter { ver -> ver >= this && ver <= last }
                else -> values().filter { ver -> ver <= this && ver >= last }
            }

    companion object {
        val Latest = values().last()
        val Earliest = values().first()
    }
}


object CustomTreePrinter : KotlintestBeanTreePrinter<Node>(NodeTreeLikeAdapter) {

    override fun takePropertyDescriptorIf(node: Node, prop: PropertyDescriptor): Boolean =
            when {
                prop.readMethod?.declaringClass !== node.javaClass -> false
                // avoid outputting too much, it's bad for readability
                node is ASTNumericLiteral -> when {
                    node.isIntLiteral || node.isLongLiteral -> prop.name == "valueAsInt"
                    else -> prop.name == "valueAsDouble"
                }
                else -> true
            }

    // dump the 'it::getName' instead of 'it.name' syntax

    override fun formatPropertyAssertion(expected: Any?, actualPropertyAccess: String): String? {
        val javaGetterName = convertKtPropAccessToGetterAccess(actualPropertyAccess)
        return super.formatPropertyAssertion(expected, "it::$javaGetterName")
    }

    override fun getContextAroundChildAssertion(node: Node, childIndex: Int, actualPropertyAccess: String): Pair<String, String> {
        val javaGetterName = convertKtPropAccessToGetterAccess(actualPropertyAccess)
        return super.getContextAroundChildAssertion(node, childIndex, "it::$javaGetterName")
    }

    private fun convertKtPropAccessToGetterAccess(ktPropAccess: String): String {
        val ktPropName = ktPropAccess.split('.')[1]

        return when {
            // boolean getter
            ktPropName matches Regex("is[A-Z].*") -> ktPropName
            else -> "get" + ktPropName.capitalize()
        }
    }

}

// invariants that should be preserved always
private val javaImplicitAssertions: Assertions<Node> = {
    DefaultMatchingConfig.implicitAssertions(it)

    if (it is ASTLiteral) {
        it::isNumericLiteral shouldBe (it is ASTNumericLiteral)
        it::isCharLiteral shouldBe (it is ASTCharLiteral)
        it::isStringLiteral shouldBe (it is ASTStringLiteral)
        it::isBooleanLiteral shouldBe (it is ASTBooleanLiteral)
        it::isNullLiteral shouldBe (it is ASTNullLiteral)
    }

    if (it is ASTExpression) run {
        it::isParenthesized shouldBe (it.parenthesisDepth > 0)
    }

    if (it is InternalInterfaces.AtLeastOneChild) {
        assert(it.numChildren > 0) {
            "Expected at least one child for $it"
        }
    }

    if (it is AccessNode) run {
        it.modifiers.effectiveModifiers.shouldContainAll(it.modifiers.explicitModifiers)
        it.modifiers.effectiveModifiers.shouldContainAtMostOneOf(JModifier.PUBLIC, JModifier.PRIVATE, JModifier.PROTECTED)
        it.modifiers.effectiveModifiers.shouldContainAtMostOneOf(JModifier.FINAL, JModifier.ABSTRACT)
        it.modifiers.effectiveModifiers.shouldContainAtMostOneOf(JModifier.DEFAULT, JModifier.ABSTRACT)
    }

}


val JavaMatchingConfig = DefaultMatchingConfig.copy(
        errorPrinter = CustomTreePrinter,
        implicitAssertions = javaImplicitAssertions
)

/** Java-specific matching method. */
inline fun <reified N : Node> JavaNode?.shouldMatchNode(ignoreChildren: Boolean = false, noinline nodeSpec: NodeSpec<N>) {
    this.baseShouldMatchSubtree(JavaMatchingConfig, ignoreChildren, nodeSpec)
}

/**
 * Extensible environment to describe parse/match testing workflows in a concise way.
 * Can be used inside of a [ParserTestSpec] with [ParserTestSpec.parserTest].
 *
 * Import statements in the parsing contexts can be configured by adding types to [importedTypes],
 * or strings to [otherImports].
 *
 * Technically the utilities provided by this class may be used outside of [io.kotlintest.specs.FunSpec]s,
 * e.g. in regular JUnit tests, but I think we should strive to uniformize our testing style,
 * especially since KotlinTest defines so many.
 *
 * TODO allow to reference an existing type as the parsing context, for full type resolution
 *
 * @property javaVersion The java version that will be used for parsing.
 * @property importedTypes Types to import at the beginning of parsing contexts
 * @property otherImports Other imports, without the `import` and semicolon
 * @property genClassHeader Header of the enclosing class used in parsing contexts like parseExpression, etc. E.g. "class Foo"
 */
open class ParserTestCtx(val javaVersion: JavaVersion = JavaVersion.Latest,
                         val importedTypes: MutableList<Class<*>> = mutableListOf(),
                         val otherImports: MutableList<String> = mutableListOf(),
                         var packageName: String = "",
                         var genClassHeader: String = "class Foo") {

    val parser get() = javaVersion.parser

    var fullSource: String? = null

    /** Imports to add to the top of the parsing contexts. */
    internal val imports: List<String>
        get() {
            val types = importedTypes.mapNotNull { it.canonicalName }.map { "import $it;" }
            return types + otherImports.map { "import $it;" }
        }

    internal val packageDecl: String get() = if (packageName.isEmpty()) "" else "package $packageName;"

    /**
     * Places all node parsing contexts inside the declaration of the given class
     * of the given class.
     * It's like you were writing eg expressions inside the class, with the method
     * declarations around it and all.
     *
     * LIMITATIONS:
     * - does not work for [TopLevelTypeDeclarationParsingCtx]
     * - [klass] must be a toplevel class (not an enum, not an interface, not nested/local/anonymous)
     */
    fun asIfIn(klass: Class<*>) {
        assert(!klass.isArray && !klass.isPrimitive) {
            "$klass has no class name"
        }

        assert(!klass.isLocalClass
                && !klass.isAnonymousClass
                && klass.enclosingClass == null
                && !klass.isEnum
                && !klass.isInterface) {
            "Unsupported class $klass"
        }

        fullSource = javaVersion.parser.readClassSource(klass)
    }


    fun notParseIn(nodeParsingCtx: NodeParsingCtx<*>, expected: (ParseException) -> Unit = {}): Assertions<String> = {
        val e = shouldThrow<ParseException> {
            nodeParsingCtx.parseNode(it, this)
        }
        expected(e)
    }

    fun parseIn(nodeParsingCtx: NodeParsingCtx<*>) = object : Matcher<String> {

        override fun test(value: String): Result {
            val (pass, e) = try {
                nodeParsingCtx.parseNode(value, this@ParserTestCtx)
                Pair(true, null)
            } catch (e: ParseException) {
                Pair(false, e)
            } catch (e: TokenMgrError) {
                Pair(false, e)
            }

            return Result(pass,
                    "Expected '$value' to parse in $nodeParsingCtx, got $e",
                    "Expected '$value' not to parse in ${nodeParsingCtx.toString().addArticle()}"
            )

        }
    }

}

