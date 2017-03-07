/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.integration.test

import idb.integration.test.UniversityDatabase._
import idb.query.QueryEnvironment
import idb.schema.university.{Course, Student}
import idb.algebra.IR._
import idb.syntax.iql._
import org.junit.Assert._
import org.junit.Test


/**
 *
 * @author Ralf Mitschke, Mirko Köhler
 */
class TestAggregationOperators
{
    @Test
	def testCountStudents() {
		implicit val env = QueryEnvironment.Local

		val query = compile (
			SELECT (COUNT (*)) FROM students GROUP BY ((s : Rep[Student]) => s.lastName)
		).asMaterialized

		val john = Student(11111, "John", "Doe")
		val john2 = Student(11111, "John", "Carter")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")
		val moe = Student(33333, "Moe", "Doe")

		students += john += judy += jane += john2 += moe
		students.endTransaction()

		assertTrue(query.contains(2))
		assertTrue(query.contains(3))

	}

	@Test
	def testSumCreditPoints() {
		implicit val env = QueryEnvironment.Local

		val query = compile (
			SELECT (
				SUM (
					(c : Rep[Course]) => c.creditPoints
				)
			) FROM
				courses
		).asMaterialized

		val se = Course(1, "Software Engineering", 6)
		val math = Course(2, "Mathematics", 9)
		val ics = Course(1, "Introduction to Computer Science", 10)

		courses += se += math += ics
		courses.endTransaction()

		assertTrue(query.contains(25))
	}

	@Test
	def testAggregateGroupCountWithGroup () {
		implicit val env = QueryEnvironment.Local

		val query = compile (
			SELECT
				((s: Rep[String]) => s, COUNT ((s : Rep[Student]) => s) )
			FROM
				students
			GROUP BY
				((s: Rep[Student]) => s.lastName)
		).asMaterialized

		val john = Student(11111, "John", "Doe")
		val john2 = Student(11111, "John", "Carter")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")
		val moe = Student(33333, "Moe", "Doe")

		students += john += judy += jane += john2 += moe
		students.endTransaction()

		assertTrue (query.contains( ("Doe", 3) ))
		assertTrue (query.contains( ("Carter", 2) ))
	}









}
