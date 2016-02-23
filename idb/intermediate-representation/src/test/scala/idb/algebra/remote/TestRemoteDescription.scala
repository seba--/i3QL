package idb.algebra.remote

import idb.query.{CompoundColor, SingleColor, NoColor, Color$}
import org.junit.Assert._
import org.junit.Test

/**
 * @author Mirko Köhler
 */
class TestRemoteDescription {

	val d1 = SingleColor("red")
	val d2 = SingleColor("green")
	val d3 = CompoundColor(Set(SingleColor("red"), SingleColor("blue"), SingleColor("yellow")))
	val d4 = CompoundColor(Set(SingleColor("red"), SingleColor("blue"), SingleColor("gray")))
	val d5 = CompoundColor(Set(SingleColor("green")))
	val d6 = CompoundColor(Set(d3, d5))
	val d7 = CompoundColor(Set(SingleColor("red"), NoColor, SingleColor("gray")))


	@Test
	def testCompareTo1(): Unit = {
		assertTrue(Color.compareTo(d1, NoColor) > 0)
		assertTrue(Color.compareTo(NoColor, NoColor) == 0)
		assertTrue(Color.compareTo(NoColor, d4) < 0)
	}

	@Test
	def testCompareTo2(): Unit = {
		assertTrue(Color.compareTo(d1, d2) > 0)
		assertTrue(Color.compareTo(d1, d1) == 0)
		assertTrue(Color.compareTo(d1, d3) < 0)
		assertTrue(Color.compareTo(d5, d1) > 0)
	}

	@Test
	def testCompareTo3(): Unit = {
		assertTrue(Color.compareTo(d3, d3) == 0)

		assertTrue(Color.compareTo(d3, d4) > 0)
		assertTrue(Color.compareTo(d5, d3) < 0)
		assertTrue(Color.compareTo(d7, d4) < 0)
		assertTrue(Color.compareTo(d7, d3) < 0)

		assertTrue(Color.compareTo(d6, d3) < 0)
		assertTrue(Color.compareTo(d6, d5) > 0)
	}


}
