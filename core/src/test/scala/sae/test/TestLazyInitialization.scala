package sae.test

import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import sae.collections.Table

/**
 *
 * Author: Ralf Mitschke
 * Date: 17.01.12
 * Time: 17:27
 *
 */
class TestLazyInitialization
        extends ShouldMatchers
{

    import sae.syntax.RelationalAlgebraSyntax._

    @Test
    def testDifferenceLazyInitOnLeftAddEvent() {

        val a = new Table[String]
        val b = new Table[String]

        val difference = a ∖ b

        a += "Hello"

        difference.size should be(1)
    }

    @Test
    def testDifferenceLazyInitOnRightAddEvent() {
        val a = new Table[String]
        val b = new Table[String]
        a += "Hello"

        val difference = a ∖ b

        b += "Hello"

        difference.size should be(0)
    }

    @Test
    def testDifferenceInJoin() {
        val a = new Table[String]
        val b = new Table[String]
        val c = new Table[String]
        val difference = a ∖ b


        val join = ((difference, identity(_: String)) ⋈(identity(_: String), c)) {(s1: String, s2: String) => (s1, s2)}

        a += "Hello"
        c += "Hello"

        difference.asList should be(
            List("Hello")
        )

        join.asList should be(
            List(("Hello", "Hello"))
        )
    }

}