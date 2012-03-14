package unisson.query.compiler

import org.scalatest.matchers.ShouldMatchers
import sae.bytecode.{BytecodeDatabase, Database}
import org.junit.Test
import sae.collections.QueryResult
import unisson.query.code_model.SourceElement
import de.tud.cs.st.bat.{ArrayType, ByteType, VoidType, ObjectType}
import sae.bytecode.model.{FieldDeclaration, MethodDeclaration}

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

    import sae.collections.Conversions._
    import SourceElement.ordering


    @Test
    def testClassQuery() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.`class`("test", "A")

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)


        result.asList.sorted should be(
            List(
                SourceElement(a)
            )
        )


    }

    @Test
    def testFieldQuery() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.field(
            queries.`class`("test", "A"),
            "hello",
            queries.typeQuery("java.lang.String")
        )

        val a = ObjectType("test/A")
        bc.declared_types.element_added(a)

        val f1 = FieldDeclaration(
            a,
            "hello",
            ObjectType("java/lang/String"),
            0,
            false,
            false
        )
        val f2 = FieldDeclaration(
            a,
            "hello",
            ObjectType("java/lang/Object"),
            0,
            false,
            false
        )

        bc.declared_fields.element_added(f1)
        bc.declared_fields.element_added(f2)

        result.asList.sorted should be(
            List(
                SourceElement(f1)
            )
        )
    }


    @Test
    def testMethodQuery() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.method(
            queries.`class`("test", "A"),
            "hello",
            queries.typeQuery("void"),
            queries.`class`("test", "B"),
            queries.`class`("test", "C")
        )

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("test/C")
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_types.element_added(c)

        val hello1 = new MethodDeclaration(
            a,
            "hello",
            Seq(b, c),
            VoidType(),
            0,
            false,
            false
        )
        val hello2 = new MethodDeclaration(
            a,
            "hello",
            Seq(c, b),
            VoidType(),
            0,
            false,
            false
        )
        val hello3 = new MethodDeclaration(
            a,
            "hello",
            Seq(),
            VoidType(),
            0,
            false,
            false
        )
        bc.declared_methods.element_added(hello1)
        bc.declared_methods.element_added(hello2)
        bc.declared_methods.element_added(hello3)

        result.asList.sorted should be(
            List(
                SourceElement(hello1)
            )
        )


    }

    @Test
    def testMethodQueryWithReturnTypeSubquery() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.method(
            queries.`class`("test", "A"),
            "hello",
            queries.`package`("test")
        )

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        val c = ObjectType("other/C")
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_types.element_added(c)

        val hello1 = new MethodDeclaration(
            a,
            "hello",
            Seq(),
            a,
            0,
            false,
            false
        )
        val hello2 = new MethodDeclaration(
            a,
            "hello",
            Seq(),
            b,
            0,
            false,
            false
        )
        val hello3 = new MethodDeclaration(
            a,
            "hello",
            Seq(),
            c,
            0,
            false,
            false
        )
        bc.declared_methods.element_added(hello1)
        bc.declared_methods.element_added(hello2)
        bc.declared_methods.element_added(hello3)

        result.asList.sorted should be(
            List(
                SourceElement(hello1),
                SourceElement(hello2)
            )
        )
    }

    @Test
    def testMethodQueryWithPrimitiveArrayParams() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.method(
            queries.`class`("test", "A"),
            "hello",
            queries.typeQuery("void"),
            queries.typeQuery("byte[]")
        )

        val a = ObjectType("test/A")
        bc.declared_types.element_added(a)

        val hello1 = new MethodDeclaration(
            a,
            "hello",
            Seq(ByteType()),
            VoidType(),
            0,
            false,
            false
        )
        val hello2 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(1, ByteType())),
            VoidType(),
            0,
            false,
            false
        )
        val hello3 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(2, ByteType())),
            VoidType(),
            0,
            false,
            false
        )
        bc.declared_methods.element_added(hello1)
        bc.declared_methods.element_added(hello2)
        bc.declared_methods.element_added(hello3)

        result.asList.sorted should be(
            List(
                SourceElement(hello2)
            )
        )
    }

    @Test
    def testMethodQueryWithPrimitiveMultiDimArrayParams() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.method(
            queries.`class`("test", "A"),
            "hello",
            queries.typeQuery("void"),
            queries.typeQuery("byte[][][]")
        )

        val a = ObjectType("test/A")
        bc.declared_types.element_added(a)

        val hello1 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(1, ByteType())),
            VoidType(),
            0,
            false,
            false
        )
        val hello3 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(3, ByteType())),
            VoidType(),
            0,
            false,
            false
        )
        bc.declared_methods.element_added(hello1)
        bc.declared_methods.element_added(hello3)

        result.asList.sorted should be(
            List(
                SourceElement(hello3)
            )
        )
    }

    @Test
    def testMethodQueryWithObjectTypeParam() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.method(
            queries.`class`("test", "A"),
            "hello",
            queries.typeQuery("void"),
            queries.typeQuery("test.B")
        )

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.declared_types.element_added(a)

        val hello1 = new MethodDeclaration(
            a,
            "hello",
            Seq(b),
            VoidType(),
            0,
            false,
            false
        )
        val hello2 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(1, b)),
            VoidType(),
            0,
            false,
            false
        )
        val hello3 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(2, b)),
            VoidType(),
            0,
            false,
            false
        )
        bc.declared_methods.element_added(hello1)
        bc.declared_methods.element_added(hello2)
        bc.declared_methods.element_added(hello3)

        result.asList.sorted should be(
            List(
                SourceElement(hello1)
            )
        )
    }


    @Test
    def testMethodQueryWithObjectTypeArrayParams() {
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result: QueryResult[SourceElement[AnyRef]] = queries.method(
            queries.`class`("test", "A"),
            "hello",
            queries.typeQuery("void"),
            queries.typeQuery("test.B[]")
        )

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.declared_types.element_added(a)

        val hello1 = new MethodDeclaration(
            a,
            "hello",
            Seq(b),
            VoidType(),
            0,
            false,
            false
        )
        val hello2 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(1, b)),
            VoidType(),
            0,
            false,
            false
        )
        val hello3 = new MethodDeclaration(
            a,
            "hello",
            Seq(ArrayType(2, b)),
            VoidType(),
            0,
            false,
            false
        )
        bc.declared_methods.element_added(hello1)
        bc.declared_methods.element_added(hello2)
        bc.declared_methods.element_added(hello3)

        result.asList.sorted should be(
            List(
                SourceElement(hello2)
            )
        )
    }
}