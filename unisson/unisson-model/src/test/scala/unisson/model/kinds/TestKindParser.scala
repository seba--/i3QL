package unisson.model.kinds

import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import unisson.model.kinds.primitive._
import unisson.model.kinds.operator._
import unisson.model.kinds.group._


/**
 *
 * Author: Ralf Mitschke
 * Date: 10.12.11
 * Time: 15:32
 *
 */
class TestKindParser
        extends ShouldMatchers
{

    @Test
    def testAllKind() {
        val parser = new KindParser()
        (parser.parse("all").get) should be(AllKind)
    }

    @Test
    def testCallsKind() {
        val parser = new KindParser()
        (parser.parse("calls").get) should be(CallsKind)
    }

    @Test
    def testCreateKind() {
        val parser = new KindParser()
        (parser.parse("create").get) should be(CreateKind)
    }

    @Test
    def testInvokeVirtualKind() {
        val parser = new KindParser()
        (parser.parse("invoke_virtual").get) should be(InvokeVirtualKind)
    }


    @Test
    def testEmptyKind() {
        val parser = new KindParser()
        parser.parse("").successful should be(false)
    }

    @Test
    def testBogusKind() {
        val parser = new KindParser()
        parser.parse("bogus").successful should be(false)
    }

    @Test
    def testUnionOfTwoKinds() {
        val parser = new KindParser()
        (parser.parse("extends, implements").get) should be(Union(ExtendsKind, ImplementsKind))
    }

    @Test
    def testUnionOfManyKinds() {
        val parser = new KindParser()
        (parser.parse("extends, implements, create, calls").get) should be
        (Union(ExtendsKind, Union(ImplementsKind, Union(CreateKind, CallsKind))))
    }

    @Test
    def testNotKinds() {
        val parser = new KindParser()
        (parser.parse("!calls").get) should be(Not(CallsKind))
    }

    @Test
    def testNotInUnionOfManyKinds() {
        val parser = new KindParser()
        (parser.parse("!extends, implements, !create, calls").get) should be
        (Union(Not(ExtendsKind), Union(ImplementsKind, Union(Not(CreateKind), CallsKind))))
    }

    @Test
    def testDifferenceKinds() {
        val parser = new KindParser()
        (parser.parse("all \\ calls").get) should be(Difference(AllKind, CallsKind))
    }

    @Test
    def testDifferenceInUnionOfTwoKinds() {
        val parser = new KindParser()
        (parser.parse("calls \\ invoke_virtual, create").get) should be
        (Union(Difference(CallsKind, InvokeVirtualKind), CreateKind))
    }

    @Test
    def testDifferenceInUnionOfManyKinds() {
        val parser = new KindParser()
        (parser.parse("calls \\ invoke_virtual, create, subtype \\ implements").get) should be
        (Union(Difference(CallsKind, InvokeVirtualKind), Union(ImplementsKind, Difference(SubtypeKind, ImplementsKind))))
    }
}