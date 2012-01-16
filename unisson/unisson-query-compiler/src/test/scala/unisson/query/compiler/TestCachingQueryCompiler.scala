package unisson.query.compiler

import org.junit.Test
import sae.bytecode.{BytecodeDatabase, Database}
import sae.bytecode.model.dependencies.`extends`
import unisson.query.code_model.SourceElement
import de.tud.cs.st.bat.ObjectType
import unisson.query.parser.QueryParser
import sae.collections.QueryResult
import org.scalatest.matchers.ShouldMatchers
import junit.framework.Assert._
import sae.Observer
import unisson.query.ast.{PackageQuery, OrQuery}

/**
 *
 * Author: Ralf Mitschke
 * Date: 16.01.12
 * Time: 15:34
 *
 */
class TestCachingQueryCompiler
        extends ShouldMatchers
{


    import SourceElement.ordering
    import sae.collections.Conversions._
    import sae.ObserverAccessor._

    @Test
    def testReuseCachedPackageQuery() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)


        val viewA = compiler.compile(parser.parse("package('test')").get)

        val viewB = compiler.compile(parser.parse("package('test')").get)

        assertSame(viewA, viewB)
    }

    @Test
    def testDisposePackageQuery() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)

        compiler.compile(parser.parse("package('test')").get)

        compiler.dispose(parser.parse("package('test')").get)

        bc.classfiles.hasObservers should be(false)
        bc.classfile_fields.hasObservers should be(false)
        bc.classfile_methods.hasObservers should be(false)

        compiler.cachedQueries should have size (0)
    }

    @Test
    def testKeepReusedSubQuery() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)

        compiler.compile(parser.parse("package('test')").get)

        val resultB: QueryResult[SourceElement[AnyRef]] = compiler
                .compile(parser.parse("package('test') or package('other')").get)

        compiler.dispose(parser.parse("package('test')").get)

        val a = ObjectType("test/A")
        bc.classfiles.element_added(a)

        resultB.asList.sorted should be(
            List(
                SourceElement(a)
            )
        )

    }

    @Test
    def testKeepUsedQuery() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)

        val result: QueryResult[SourceElement[AnyRef]] = compiler
                .compile(parser.parse("package('test') or package('other')").get)

        compiler.dispose(parser.parse("package('test') or package('other')").get)

        val testA = ObjectType("test/A")
        val otherB = ObjectType("other/B")
        bc.classfiles.element_added(testA)
        bc.classfiles.element_added(otherB)

        result.asList.sorted should be(
            List(
                SourceElement(otherB),
                SourceElement(testA)
            )
        )

    }

    @Test
    def testReuseOrQueryWithDifferentOrder() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)


        val viewA = compiler.compile(parser.parse("package('test') or package('other')").get)

        val viewB = compiler.compile(parser.parse("package('other') or package('test')").get)

        assertSame(viewA, viewB)
    }

    @Test
    def testReuseOfSubExpressionsInOrQueries() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)

        // we use the parse but rely on the order here so the order is checked
        // prerequisite is that no two 'or' sub-trees are syntactically equal
        // so even though a and b are just permutated no or query expression is reused
        val queryA = parser.parse("package('a') or package('b') or package('c')").get
        queryA should be(
            OrQuery(
                PackageQuery("a"),
                OrQuery(
                    PackageQuery("b"),
                    PackageQuery("c")
                )
            )
        )

        val queryB = parser.parse("package('b') or package('a') or package('d')").get
        queryB should be(
            OrQuery(
                PackageQuery("b"),
                OrQuery(
                    PackageQuery("a"),
                    PackageQuery("d")
                )
            )
        )

        compiler.compile(queryA)

        compiler.cachedQueries should have size (5)
        observerCount(bc.classfiles) should be (3)
        observerCount(bc.classfile_fields) should be (3)
        observerCount(bc.classfile_methods) should be (3)

        compiler.compile(queryB)

        compiler.cachedQueries should have size (8) // added package('d') && 2 or queries
        observerCount(bc.classfiles) should be (4)
        observerCount(bc.classfile_fields) should be (4)
        observerCount(bc.classfile_methods) should be (4)

        compiler.dispose(queryA)

        compiler.cachedQueries should have size (5) // removed package('c') && 2 or queries
        observerCount(bc.classfiles) should be (3)
        observerCount(bc.classfile_fields) should be (3)
        observerCount(bc.classfile_methods) should be (3)

        compiler.dispose(queryB)

        compiler.cachedQueries should have size (0)
        observerCount(bc.classfiles) should be (0)
        observerCount(bc.classfile_fields) should be (0)
        observerCount(bc.classfile_methods) should be (0)

    }


    @Test
    def testDisposeOrQuery() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)

        compiler.compile(parser.parse("package('test') or package('other')").get)

        compiler.dispose(parser.parse("package('test') or package('other')").get)

        bc.classfiles.hasObservers should be(false)
        bc.classfile_fields.hasObservers should be(false)
        bc.classfile_methods.hasObservers should be(false)

        compiler.cachedQueries should have size (0)
    }

    @Test
    def testDistinctTransitiveSuperTypeQueries() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)



        val viewA = compiler.compile(parser.parse("transitive(supertype(class('java.lang','Object')))").get)
        val viewB = compiler.compile(parser.parse("transitive(supertype(class('test','A')))").get)

        assertNotSame(viewA, viewB)
    }

    @Test
    def testDisposeTransitiveSuperTypeQuery() {
        val bc: Database = new BytecodeDatabase()
        val parser = new QueryParser()
        val compiler = new CachingQueryCompiler(bc)



        val viewExtendsObject =
            compiler.compile(parser.parse("transitive(supertype(class('java.lang','Object')))").get)
        val extendsObject: QueryResult[SourceElement[AnyRef]] = viewExtendsObject

        val extendsA: QueryResult[SourceElement[AnyRef]] =
            compiler.compile(parser.parse("transitive(supertype(class('test','A')))").get)

        val obj = ObjectType("java/lang/Object")
        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        val d = ObjectType("test/D")
        bc.classfiles.element_added(obj)
        bc.classfiles.element_added(a)
        bc.`extends`.element_added(`extends`(a, obj))
        bc.classfiles.element_added(b)
        bc.`extends`.element_added(`extends`(b, obj))
        bc.`extends`.element_added(`extends`(b, a))
        bc.classfiles.element_added(c)
        bc.`extends`.element_added(`extends`(c, obj))
        bc.`extends`.element_added(`extends`(c, a))
        bc.classfiles.element_added(d)
        bc.`extends`.element_added(`extends`(d, obj))
        bc.`extends`.element_added(`extends`(d, b))

        extendsObject.asList.sorted should be(
            List(
                SourceElement(a),
                SourceElement(b),
                SourceElement(c),
                SourceElement(d)
            )
        )

        extendsA.asList.sorted should be(
            List(
                SourceElement(b),
                SourceElement(c),
                SourceElement(d)
            )
        )

        // remove the result from observers
        viewExtendsObject.removeObserver(extendsObject.asInstanceOf[Observer[Any]])
        compiler.dispose(parser.parse("transitive(supertype(class('java.lang','Object')))").get)

        compiler
                .cachedQueries should have size (2) // one query with subexpressions: class + transitive-supertype (is one query)


        val e = ObjectType("test/E")
        bc.classfiles.element_added(e)
        bc.`extends`.element_added(`extends`(e, obj))
        bc.`extends`.element_added(`extends`(e, d))

        extendsA.asList.sorted should be(
            List(
                SourceElement(b),
                SourceElement(c),
                SourceElement(d),
                SourceElement(e)
            )
        )
    }

}