package unisson.query.compiler

import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import unisson.query.code_model.SourceElement
import de.tud.cs.st.bat.resolved.{VoidType, ArrayType, ObjectType, ByteType}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.MethodDeclaration
import sae.bytecode.structure.FieldDeclaration

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

    import SourceElement.ordering


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
                SourceElement (a)
            )
        )


    }

    @Test
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
                SourceElement (f1)
            )
        )
    }


    @Test
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
                SourceElement (hello1)
            )
        )


    }

    @Test
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
                SourceElement (hello1),
                SourceElement (hello2)
            )
        )
    }

    @Test
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
                SourceElement (hello2)
            )
        )
    }

    @Test
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
                SourceElement (hello3)
            )
        )
    }

    @Test
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
                SourceElement (hello1)
            )
        )
    }


    @Test
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
                SourceElement (hello2)
            )
        )
    }
}