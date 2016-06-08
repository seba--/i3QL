package idb.query

import idb.query.colors.Color
import org.junit.Test
import org.junit.Ignore
import org.junit.Assert._

/**
 * @author Mirko Köhler
 */
class QueryEnvironmentTest {

	@Test
	def testPermission(): Unit = {
		val hostA = NamedHost("A")
		val hostB = NamedHost("B")
		val hostC = NamedHost("C")

	/*	val qe = QueryEnvironment.create(
			hosts = List(hostA, hostB, hostC),
		    permissions = Map("green" -> List(0, 1), "red" -> List(1), "yellow" -> List(1, 2), "blue" -> List(0, 1, 2), "orange" -> List(2))
		) */

	/*	val d1 = Color("green")
		assertEquals(List(hostA, hostB), qe permission d1)

		val d2 = SetDescription(Set(NameDescription("green"), NameDescription("red"), NameDescription("yellow")))
		assertEquals(List(hostB), qe permission d2)

		val d3 = SetDescription(Set(NameDescription("yellow"), NameDescription("blue")))
		assertEquals(List(hostB, hostC), qe permission d3)

		val d4 = SetDescription(Set(NameDescription("orange"), NameDescription("red")))
		assertEquals(List(), qe permission d4)

		val d5 =  SetDescription(Set(NameDescription("green"),
									SetDescription(Set(NameDescription("red"), NameDescription("yellow"))),
									NameDescription("blue")
		))
		assertEquals(List(hostB), qe permission d5)

		*/
	}

}
