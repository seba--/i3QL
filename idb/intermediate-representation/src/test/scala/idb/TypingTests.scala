package idb

import org.junit.Test
import org.junit.Assert._

/**
 *
 * @author Ralf Mitschke
 */
class TypingTests
{

    @Test
    def testApp () {
        // this should work the same in the lms compiler for selection operators
        val s = List ("Hello", "World")

        val s1 = myFilter (s, foo)
        assertEquals (s, s1)

        val s2 = myFilter (s, bar)
        assertEquals (Nil, s2)
    }

    @Test
    def testImplicit () {
        val x: Int = 1
        val y: Int = 2
        val z = funWithEvidence (x, y)
    }


    def funWithEvidence[T: Numeric] (t1: T, t2: T) = {
        implicitly[Numeric[T]].plus (t1, t2)
    }

    def myFilter[A] (s: Seq[A], fun: A => Boolean) = {
        s.filter (fun)
    }

    def foo (o: Any): Boolean = true

    def bar (o: String): Boolean = o.toString == ""
}
