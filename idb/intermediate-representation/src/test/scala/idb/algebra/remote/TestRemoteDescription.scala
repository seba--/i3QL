package idb.algebra.remote

import idb.query.{SetDescription, NameDescription, DefaultDescription, RemoteDescription}
import org.junit.Assert._
import org.junit.Test

/**
 * @author Mirko Köhler
 */
class TestRemoteDescription {

	val d1 = NameDescription("red")
	val d2 = NameDescription("green")
	val d3 = SetDescription(Set(NameDescription("red"), NameDescription("blue"), NameDescription("yellow")))
	val d4 = SetDescription(Set(NameDescription("red"), NameDescription("blue"), NameDescription("gray")))
	val d5 = SetDescription(Set(NameDescription("green")))
	val d6 = SetDescription(Set(d3, d5))
	val d7 = SetDescription(Set(NameDescription("red"), DefaultDescription, NameDescription("gray")))


	@Test
	def testCompareTo1(): Unit = {
		assertTrue(RemoteDescription.compareTo(d1, DefaultDescription) > 0)
		assertTrue(RemoteDescription.compareTo(DefaultDescription, DefaultDescription) == 0)
		assertTrue(RemoteDescription.compareTo(DefaultDescription, d4) < 0)
	}

	@Test
	def testCompareTo2(): Unit = {
		assertTrue(RemoteDescription.compareTo(d1, d2) > 0)
		assertTrue(RemoteDescription.compareTo(d1, d1) == 0)
		assertTrue(RemoteDescription.compareTo(d1, d3) < 0)
		assertTrue(RemoteDescription.compareTo(d5, d1) > 0)
	}

	@Test
	def testCompareTo3(): Unit = {
		assertTrue(RemoteDescription.compareTo(d3, d3) == 0)

		assertTrue(RemoteDescription.compareTo(d3, d4) > 0)
		assertTrue(RemoteDescription.compareTo(d5, d3) < 0)
		assertTrue(RemoteDescription.compareTo(d7, d4) < 0)
		assertTrue(RemoteDescription.compareTo(d7, d3) < 0)

		assertTrue(RemoteDescription.compareTo(d6, d3) < 0)
		assertTrue(RemoteDescription.compareTo(d6, d5) > 0)
	}


}
