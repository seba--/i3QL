package idb.algebra.remote

import idb.query.colors.{ClassColor, Color}
import org.junit.Assert._
import org.junit.Test

/**
 * @author Mirko Köhler
 */
class TestColors {

	val c1 = Color("red")
	val c2 = Color("green")
	val c3 = Color("blue")


	val f1 = Color("name" -> c1, "pin" -> c3)
	val f2 = Color("name" -> c1, "pin" -> c2)


	@Test
	def testTupleColor(): Unit = {
		assertEquals(Color("_1" -> c1, "_2" -> c2), Color.tupled(c1, c2))
	}

	@Test
	def testColorUnion() : Unit = {
		assertEquals(Color("name" -> c1, "pin" -> Color.group("green", "blue")), Color.union(f1, f2))
		assertEquals(Color("name" -> c1, "pin" -> Color.group("red", "blue")), Color.union(f1, c1))
	}

/*	val d1 = SingleColor("red")
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
	}    */


}
