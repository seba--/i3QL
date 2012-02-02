package unisson.model.kinds

import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import unisson.model.kinds.primitive._
import unisson.model.kinds.group._


/**
 *
 * Author: Ralf Mitschke
 * Date: 10.12.11
 * Time: 15:32
 *
 */
class TestKindResolver
        extends ShouldMatchers
{

    @Test
    def testAllKind {
        val parser = new KindParser()
        KindResolver(parser.parse("all").get) should be(
            Set(
                ClassCastKind,
                CreateKind,
                ExtendsKind,
                FieldTypeKind,
                ImplementsKind,
                InstanceOfKind,
                InvokeInterfaceKind,
                InvokeSpecialKind,
                InvokeStaticKind,
                InvokeVirtualKind,
                ParameterKind,
                ReadFieldKind,
                ReturnTypeKind,
                ThrowsKind,
                WriteFieldKind
            )
        )
    }

    @Test
    def testCallsKind {
        val parser = new KindParser()
        KindResolver(parser.parse("calls").get) should be(
            Set(
                InvokeInterfaceKind,
                InvokeSpecialKind,
                InvokeStaticKind,
                InvokeVirtualKind
            )
        )
    }

    @Test
    def testCreateKind {
        val parser = new KindParser()
        KindResolver(parser.parse("create").get) should be(
            Set(CreateKind)
        )
    }

    @Test
    def testUnionOfTwoKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("extends, implements").get) should be(
            Set(ExtendsKind, ImplementsKind)
        )
    }

    @Test
    def testDoubleUnionOfKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("extends, implements, subtype").get) should be(
            Set(ExtendsKind, ImplementsKind)
        )
    }

    @Test
    def testUnionOfManyKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("extends, implements, create, calls").get) should be(
            Set(
                ExtendsKind,
                ImplementsKind,
                CreateKind,
                InvokeInterfaceKind,
                InvokeSpecialKind,
                InvokeStaticKind,
                InvokeVirtualKind
            )
        )
    }

    @Test
    def testNotKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("!calls").get) should be(
            Set(
                ClassCastKind,
                CreateKind,
                ExtendsKind,
                FieldTypeKind,
                ImplementsKind,
                InstanceOfKind,
                ParameterKind,
                ReadFieldKind,
                ReturnTypeKind,
                ThrowsKind,
                WriteFieldKind
            )
        )
    }

    @Test
    def testTwoNotsInUnion {
        val parser = new KindParser()
        KindResolver(parser.parse("!calls, !subtype").get) should be
        (KindResolver(parser.parse("all").get))
    }

    @Test
    def testDifferenceKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("all \\ calls").get) should be(
            Set(
                ClassCastKind,
                CreateKind,
                ExtendsKind,
                FieldTypeKind,
                ImplementsKind,
                InstanceOfKind,
                ParameterKind,
                ReadFieldKind,
                ReturnTypeKind,
                ThrowsKind,
                WriteFieldKind
            )
        )
    }

    @Test
    def testDifferenceInUnionOfTwoKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("calls \\ invoke_virtual, subtype").get) should be(
            Set(
                InvokeInterfaceKind,
                InvokeSpecialKind,
                InvokeStaticKind,
                ExtendsKind,
                ImplementsKind
            )
        )
    }

    @Test
    def testDifferenceInUnionOfManyKinds {
        val parser = new KindParser()
        KindResolver(parser.parse("calls \\ invoke_virtual, create, subtype \\ implements").get) should be(
            Set(
                InvokeInterfaceKind,
                InvokeSpecialKind,
                InvokeStaticKind,
                ExtendsKind,
                CreateKind
            )
        )
    }
}