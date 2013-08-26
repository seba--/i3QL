package unisson.query.compiler

import org.scalatest.matchers.ShouldMatchers
import org.junit.{Ignore, Test}
import org.junit.Assert._
import unisson.query.code_model.SourceElementFactory
import de.tud.cs.st.bat.resolved.{VoidType, ArrayType, ObjectType, ByteType}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.{InheritanceRelation, MethodDeclaration, FieldDeclaration}
import unisson.query.parser.QueryParser

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.01.12
 * Time: 10:58
 *
 */
class TestQueryDefinitions
    extends ShouldMatchers
{

    import SourceElementFactory.ordering


    @Test
    def testClassQuery() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.`class` ("test", "A")
        )

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)


        result.asList.sorted should be (
            List (
                SourceElementFactory (a)
            )
        )


    }

    @Test
    @Ignore
    def testFieldQuery() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.field (
                queries.`class` ("test", "A"),
                "hello",
                queries.typeQuery ("java.lang.String")
            )
        )

        val a = ObjectType ("test/A")
        bc.typeDeclarations.element_added (a)

        val f1 = FieldDeclaration (
            a,
            "hello",
            ObjectType ("java/lang/String")
        )
        val f2 = FieldDeclaration (
            a,
            "hello",
            ObjectType ("java/lang/Object")
        )

        bc.fieldDeclarations.element_added (f1)
        bc.fieldDeclarations.element_added (f2)

        result.asList.sorted should be (
            List (
                SourceElementFactory (f1)
            )
        )
    }


    @Test
    @Ignore
    def testMethodQuery() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.method (
                queries.`class` ("test", "A"),
                "hello",
                queries.typeQuery ("void"),
                queries.`class` ("test", "B"),
                queries.`class` ("test", "C")
            )
        )
        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)

        val hello1 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (b, c)
        )
        val hello2 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (c, b)
        )
        val hello3 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq ()
        )
        bc.methodDeclarations.element_added (hello1)
        bc.methodDeclarations.element_added (hello2)
        bc.methodDeclarations.element_added (hello3)

        result.asList.sorted should be (
            List (
                SourceElementFactory (hello1)
            )
        )


    }

    @Test
    @Ignore
    def testMethodQueryWithReturnTypeSubquery() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.method (
                queries.`class` ("test", "A"),
                "hello",
                queries.`package` ("test")
            )
        )

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("other/C")
        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.typeDeclarations.element_added (c)

        val hello1 = MethodDeclaration (
            a,
            "hello",
            a,
            Seq ()
        )
        val hello2 = MethodDeclaration (
            a,
            "hello",
            b,
            Seq ()
        )
        val hello3 = MethodDeclaration (
            a,
            "hello",
            c,
            Seq ()
        )
        bc.methodDeclarations.element_added (hello1)
        bc.methodDeclarations.element_added (hello2)
        bc.methodDeclarations.element_added (hello3)

        result.asList.sorted should be (
            List (
                SourceElementFactory (hello1),
                SourceElementFactory (hello2)
            )
        )
    }

    @Test
    @Ignore
    def testMethodQueryWithPrimitiveArrayParams() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.method (
                queries.`class` ("test", "A"),
                "hello",
                queries.typeQuery ("void"),
                queries.typeQuery ("byte[]")
            )
        )

        val a = ObjectType ("test/A")
        bc.typeDeclarations.element_added (a)

        val hello1 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ByteType)
        )
        val hello2 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (1, ByteType))
        )
        val hello3 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (2, ByteType))
        )
        bc.methodDeclarations.element_added (hello1)
        bc.methodDeclarations.element_added (hello2)
        bc.methodDeclarations.element_added (hello3)

        result.asList.sorted should be (
            List (
                SourceElementFactory (hello2)
            )
        )
    }

    @Test
    @Ignore
    def testMethodQueryWithPrimitiveMultiDimArrayParams() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.method (
                queries.`class` ("test", "A"),
                "hello",
                queries.typeQuery ("void"),
                queries.typeQuery ("byte[][][]")
            )
        )

        val a = ObjectType ("test/A")
        bc.typeDeclarations.element_added (a)

        val hello1 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (1, ByteType))
        )
        val hello3 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (3, ByteType))
        )
        bc.methodDeclarations.element_added (hello1)
        bc.methodDeclarations.element_added (hello3)

        result.asList.sorted should be (
            List (
                SourceElementFactory (hello3)
            )
        )
    }

    @Test
    @Ignore
    def testMethodQueryWithObjectTypeParam() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.method (
                queries.`class` ("test", "A"),
                "hello",
                queries.typeQuery ("void"),
                queries.typeQuery ("test.B")
            )
        )
        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)

        val hello1 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (b)
        )
        val hello2 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (1, b))
        )
        val hello3 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (2, b))
        )
        bc.methodDeclarations.element_added (hello1)
        bc.methodDeclarations.element_added (hello2)
        bc.methodDeclarations.element_added (hello3)

        result.asList.sorted should be (
            List (
                SourceElementFactory (hello1)
            )
        )
    }


    @Test
    @Ignore
    def testMethodQueryWithObjectTypeArrayParams() {
        val bc = BATDatabaseFactory.create ()
        val queries = new QueryDefinitions (bc)

        val result = sae.relationToResult (
            queries.method (
                queries.`class` ("test", "A"),
                "hello",
                queries.typeQuery ("void"),
                queries.typeQuery ("test.B[]")
            )
        )

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        bc.typeDeclarations.element_added (a)

        val hello1 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (b)
        )
        val hello2 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (1, b))
        )
        val hello3 = MethodDeclaration (
            a,
            "hello",
            VoidType,
            Seq (ArrayType (2, b))
        )
        bc.methodDeclarations.element_added (hello1)
        bc.methodDeclarations.element_added (hello2)
        bc.methodDeclarations.element_added (hello3)

        result.asList.sorted should be (
            List (
                SourceElementFactory (hello2)
            )
        )
    }


    @Test
    def testSuperTypeQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val compiler = new BaseQueryCompiler (bc)

        val extendsObject = sae.relationToResult (compiler.compile (parser.parse ("supertype(class('java.lang','Object'))").get))

        val extendsA = sae.relationToResult (
            compiler.compile (parser.parse ("supertype(class('test','A'))").get)
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

        assertEquals (
            List (
                SourceElementFactory (a),
                SourceElementFactory (b),
                SourceElementFactory (c),
                SourceElementFactory (d)
            ),
            extendsObject.asList.sorted
        )

        assertEquals (
            List (
                SourceElementFactory (b),
                SourceElementFactory (c)
            ),
            extendsA.asList.sorted
        )
    }

    @Test
    def testTransitiveSuperTypeQuery() {
        val bc = BATDatabaseFactory.create ()
        val parser = new QueryParser ()
        val compiler = new BaseQueryCompiler (bc)


        val extendsObject = sae.relationToResult (
            compiler.compile (parser.parse ("transitive(supertype(class('java.lang','Object')))").get)
        )

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

        assertEquals (
            List (
                SourceElementFactory (a),
                SourceElementFactory (b),
                SourceElementFactory (c),
                SourceElementFactory (d)
            ),
            extendsObject.asList.sorted
        )

        assertEquals (
            List (
                SourceElementFactory (b),
                SourceElementFactory (c),
                SourceElementFactory (d)
            ),
            extendsA.asList.sorted
        )

    }
}