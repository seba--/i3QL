package unisson.query.compiler

import org.junit.{Ignore, Test}
import unisson.query.code_model.SourceElementFactory
import unisson.query.code_model.SourceElementFactory._
import de.tud.cs.st.bat.resolved.ObjectType
import unisson.query.parser.QueryParser
import org.scalatest.matchers.ShouldMatchers
import junit.framework.Assert._
import sae.Observer
import unisson.query.ast.{PackageQuery, OrQuery}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.InheritanceRelation

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


    import sae.ObserverAccessor._

    @Test
    def testReuseCachedPackageQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)


        val viewA = compiler.compile (parser.parse ("package('test')").get)

        val viewB = compiler.compile (parser.parse ("package('test')").get)

        assertSame (viewA, viewB)
    }

    @Test
    def testDisposePackageQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)


        compiler.compile (parser.parse ("package('test')").get)

        compiler.dispose (parser.parse ("package('test')").get)

        bc.typeDeclarations.hasObservers should be (false)
        bc.fieldDeclarations.hasObservers should be (false)
        bc.methodDeclarations.hasObservers should be (false)

        compiler.cachedQueries should have size (0)
    }

    @Test
    def testKeepReusedSubQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)


        compiler.compile (parser.parse ("package('test')").get)

        val resultB = sae.relationToResult (
            compiler.compile (parser.parse ("package('test') or package('other')").get)
        )

        compiler.dispose (parser.parse ("package('test')").get)

        val a = ObjectType ("test/A")
        bc.typeDeclarations.element_added (a)

        resultB.asList.sorted should be (
            List (
                SourceElementFactory (a)
            )
        )

    }

    @Test
    def testKeepUsedQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)


        val result = sae.relationToResult (
            compiler.compile (parser.parse ("package('test') or package('other')").get)
        )
        compiler.dispose (parser.parse ("package('test') or package('other')").get)

        val testA = ObjectType ("test/A")
        val otherB = ObjectType ("other/B")
        bc.typeDeclarations.element_added (testA)
        bc.typeDeclarations.element_added (otherB)

        result.asList.sorted should be (
            List (
                SourceElementFactory (otherB),
                SourceElementFactory (testA)
            )
        )

    }

    @Test
    def testReuseOrQueryWithDifferentOrder() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)



        val viewA = compiler.compile (parser.parse ("package('test') or package('other')").get)

        val viewB = compiler.compile (parser.parse ("package('other') or package('test')").get)

        assertSame (viewA, viewB)
    }

    @Test
    @Ignore
    def testReuseOfSubExpressionsInOrQueries() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)


        // we use the parse but rely on the order here so the order is checked
        // prerequisite is that no two 'or' sub-trees are syntactically equal
        // so even though a and b are just permutated no or query expression is reused
        val queryA = parser.parse ("package('a') or package('b') or package('c')").get
        queryA should be (
            OrQuery (
                PackageQuery ("a"),
                OrQuery (
                    PackageQuery ("b"),
                    PackageQuery ("c")
                )
            )
        )

        val queryB = parser.parse ("package('b') or package('a') or package('d')").get
        queryB should be (
            OrQuery (
                PackageQuery ("b"),
                OrQuery (
                    PackageQuery ("a"),
                    PackageQuery ("d")
                )
            )
        )

        compiler.compile (queryA)

        compiler.cachedQueries should have size (5)
        observerCount (bc.typeDeclarations) should be (3)
        observerCount (bc.fieldDeclarations) should be (3)
        observerCount (bc.methodDeclarations) should be (3)

        compiler.compile (queryB)

        compiler.cachedQueries should have size (8) // added package('d') && 2 or queries
        observerCount (bc.typeDeclarations) should be (4)
        observerCount (bc.fieldDeclarations) should be (4)
        observerCount (bc.methodDeclarations) should be (4)

        compiler.dispose (queryA)

        compiler.cachedQueries should have size (5) // removed package('c') && 2 or queries
        observerCount (bc.typeDeclarations) should be (3)
        observerCount (bc.fieldDeclarations) should be (3)
        observerCount (bc.methodDeclarations) should be (3)

        compiler.dispose (queryB)

        compiler.cachedQueries should have size (0)
        observerCount (bc.typeDeclarations) should be (0)
        observerCount (bc.fieldDeclarations) should be (0)
        observerCount (bc.methodDeclarations) should be (0)

    }


    @Test
    def testDisposeOrQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)


        compiler.compile (parser.parse ("package('test') or package('other')").get)

        compiler.dispose (parser.parse ("package('test') or package('other')").get)

        bc.typeDeclarations.hasObservers should be (false)
        bc.fieldDeclarations.hasObservers should be (false)
        bc.methodDeclarations.hasObservers should be (false)

        compiler.cachedQueries should have size (0)
    }

    @Test
    def testDistinctTransitiveSuperTypeQueries() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)




        val viewA = compiler.compile (parser.parse ("transitive(supertype(class('java.lang','Object')))").get)
        val viewB = compiler.compile (parser.parse ("transitive(supertype(class('test','A')))").get)

        assertNotSame (viewA, viewB)
    }

    @Test
    def testDisposeTransitiveSuperTypeQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val baseCompiler = new BaseQueryCompiler (bc)
        val compiler = new CachingQueryCompiler (baseCompiler)




        val viewExtendsObject =
            compiler.compile (parser.parse ("transitive(supertype(class('java.lang','Object')))").get)
        val extendsObject = sae.relationToResult (viewExtendsObject)

        val extendsA = sae.relationToResult (
            compiler.compile (parser.parse ("transitive(supertype(class('test','A')))").get)
        )

        val obj = ObjectType ("java/lang/Object")
        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")
        bc.typeDeclarations.element_added (obj)
        bc.typeDeclarations.element_added (a)
        bc.classInheritance.element_added (InheritanceRelation (a, obj))
        bc.typeDeclarations.element_added (b)
        bc.classInheritance.element_added (InheritanceRelation (b, obj))
        bc.classInheritance.element_added (InheritanceRelation (b, a))
        bc.typeDeclarations.element_added (c)
        bc.classInheritance.element_added (InheritanceRelation (c, obj))
        bc.classInheritance.element_added (InheritanceRelation (c, a))
        bc.typeDeclarations.element_added (d)
        bc.classInheritance.element_added (InheritanceRelation (d, obj))
        bc.classInheritance.element_added (InheritanceRelation (d, b))

        extendsObject.asList.sorted should be (
            List (
                SourceElementFactory (a),
                SourceElementFactory (b),
                SourceElementFactory (c),
                SourceElementFactory (d)
            )
        )

        extendsA.asList.sorted should be (
            List (
                SourceElementFactory (b),
                SourceElementFactory (c),
                SourceElementFactory (d)
            )
        )

        // remove the result from observers
        viewExtendsObject.removeObserver (extendsObject.asInstanceOf[Observer[Any]])
        compiler.dispose (parser.parse ("transitive(supertype(class('java.lang','Object')))").get)

        compiler
            .cachedQueries should have size (2) // one query with subexpressions: class + transitive-supertype (is one query)


        val e = ObjectType ("test/E")
        bc.typeDeclarations.element_added (e)
        bc.classInheritance.element_added (InheritanceRelation (e, obj))
        bc.classInheritance.element_added (InheritanceRelation (e, d))

        extendsA.asList.sorted should be (
            List (
                SourceElementFactory (b),
                SourceElementFactory (c),
                SourceElementFactory (d),
                SourceElementFactory (e)
            )
        )
    }

}