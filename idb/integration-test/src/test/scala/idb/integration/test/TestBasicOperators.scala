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

import UniversityDatabase._
import idb.syntax.iql._
import org.junit.Assert._
import org.junit.{Ignore, Test}
import idb.schema.university.{Registration, Student}
import idb.syntax.iql.IR._



/**
 *
 * @author Ralf Mitschke
 */
class TestBasicOperators
{

	@Test
	def testStudentNames() {

		val query = compile (
			SELECT (
				(s : Rep[String] ) => "Student: " + s
			) FROM
				students
			GROUP BY (
				(s: Rep[Student]) => s.firstName
			)
		).asMaterialized

		/*val query = compile (
			SELECT.apply[(String,String),String]( (pair : Rep[(String, String)] ) => pair._1 + " " + pair._2).FROM(students).GROUP((s: Rep[Student]) => (s.firstName, s.lastName))
		).asMaterialized */

		val john = Student(11111, "John", "Doe")
		val john2 = Student(11111, "John", "Carter")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")

		students += john += judy += jane += john2
		students.endTransaction()

		assertTrue(query.contains("Student: John"))
		assertTrue(query.contains("Student: Judy"))
		assertTrue(query.contains("Student: Jane"))
	}


	//TODO fix manifest bug
	@Ignore
	@Test
	def testStudentNames2() {

		val query = compile (
			SELECT (
				(pair : Rep[(String, String)] ) => pair._1 + " " + pair._2
			) FROM
				students
			GROUP BY (
				(s: Rep[Student]) => (s.firstName, s.lastName)
			)
		).asMaterialized

		/*val query = compile (
			SELECT.apply[(String,String),String]( (pair : Rep[(String, String)] ) => pair._1 + " " + pair._2).FROM(students).GROUP((s: Rep[Student]) => (s.firstName, s.lastName))
		).asMaterialized */

		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")

		students += john += judy += jane

		assertTrue(query.contains("John Doe"))
		assertTrue(query.contains("Judy Carter"))
		assertTrue(query.contains("Jane Doe"))
	}

	@Ignore
	@Test
	def testGroup2() {

		val query = compile (
			SELECT (
				(s : Rep[String] ) => "Student: " + s
			) FROM (
				students, registrations
			) GROUP BY (
				(p : Rep[(Student,Registration)]) => p._1.firstName
			)
		).asMaterialized

		val john = Student(11111, "John", "Doe")
		val john2 = Student(11111, "John", "Carter")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")

		students += john += judy += jane += john2
		students.endTransaction()

		val reg1 = Registration(123,11111,"I'm John.")
		val reg2 = Registration(123,22222,"")
		val reg3 = Registration(234,11111,"")

		registrations += reg1 += reg2 += reg3
		registrations.endTransaction()

		assertTrue(query.contains("Student: John"))
		assertTrue(query.contains("Student: Judy"))
		assertTrue(query.contains("Student: Jane"))
	}


	@Test
    def testSelectFirstNameFromStudents () {
        val query = compile (
            SELECT ((_:Rep[Student]).firstName) FROM students
        ).asMaterialized

        val john = new Student(11111, "John", "Doe")

		students += john
		students.endTransaction()

        assertTrue(query.contains("John"))

		students -= john
		students.endTransaction()

		assertFalse(query.contains("John"))

    }

	@Test
	def testSelectFirstNameLastNameFromStudents () {
		val query = compile (
			SELECT ((s:Rep[Student]) => (s.firstName, s.lastName)) FROM students
		).asMaterialized

		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")

		students += john += judy
		students.endTransaction()

		assertTrue(query.contains(("John", "Doe")))
		assertTrue(query.contains(("Judy", "Carter")))

		students -= john
		students.endTransaction()

		assertFalse(query.contains(("John", "Doe")))
		assertTrue(query.contains(("Judy", "Carter")))
	}

	@Test
	def testGetStudentIDFromStudents () {
		val query = compile (
			SELECT ((s : Rep[Student]) => s.lastName + "@" + s.matriculationNumber) FROM students
		).asMaterialized

		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")

		students += john += judy
		students.endTransaction()

		assertTrue(query.contains("Doe@11111"))
		assertTrue(query.contains("Carter@22222"))

		students -= john
		students.endTransaction()

		assertFalse(query.contains("Doe@11111"))
		assertTrue(query.contains("Carter@22222"))
	}

	@Test
	def testRegistrationsForCourse () {

		val query = compile(
			SELECT (*) FROM registrations WHERE ((r : Rep[Registration]) => {
				r.courseNumber == 123
			})).asMaterialized

		val reg1 = Registration(123,11111,"I'm John.")
		val reg2 = Registration(123,22222,"")
		val reg3 = Registration(234,11111,"")

		registrations += reg1 += reg2 += reg3
		registrations.endTransaction()

		assertTrue(query.contains(reg1))
		assertTrue(query.contains(reg2))
		assertFalse(query.contains(reg3))

		registrations -= reg1
		registrations.endTransaction()

		assertFalse(query.contains(reg1))
		assertTrue(query.contains(reg2))
		assertFalse(query.contains(reg3))
	}

	@Ignore
	@Test
	def testGetStudentMatriculationNumber () {
		val query = compile(
			SELECT ((_:Rep[Student]).matriculationNumber) FROM students WHERE ((s: Rep[Student]) => {
				s.firstName == "John" && s.lastName == "Doe"
			})).asMaterialized

		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")
		val john2 = Student(33333, "John", "D'oh")

		students += john += judy += john2
		students.endTransaction()

		assertTrue(query.contains(11111))
		assertFalse(query.contains(22222))
		assertFalse(query.contains(33333))

		students -= john2
		students.endTransaction()

		assertTrue(query.contains(11111))
		assertFalse(query.contains(22222))
		assertFalse(query.contains(33333))

		students -= john
		students.endTransaction()

		assertFalse(query.contains(11111))
		assertFalse(query.contains(22222))
		assertFalse(query.contains(33333))
	}


	//TODO Re-enable this test as soon as materialized is re-enabled again
	@Ignore
	@Test
	def testGetStudentPairs () {
		val query = compile (
			SELECT (*) FROM(students, students)
		).asMaterialized

		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")

		students += john += judy
		students.endTransaction()

		Predef.println("---------------------------------")
		query.foreach(Predef.println(_))
		Predef.println("---------------------------------")

		assertTrue(query.contains((john,john)))
		assertTrue(query.contains((john,judy)))
		assertTrue(query.contains((judy,john)))
		assertTrue(query.contains((judy,judy)))

		students += jane -= judy
		students.endTransaction()

		Predef.println("---------------------------------")
		query.foreach(Predef.println(_))
		Predef.println("---------------------------------")

		assertTrue(query.contains((john,john)))
		assertTrue(query.contains((john,jane)))
		assertTrue(query.contains((jane,john)))
		assertTrue(query.contains((jane,jane)))

		assertFalse(query.contains((john,judy)))
		assertFalse(query.contains((judy,john)))
		assertFalse(query.contains((judy,judy)))

		assertFalse(query.contains((jane,judy)))
		assertFalse(query.contains((judy,jane)))
	}

	@Test
	@Ignore
	def testGetStudentsAndRegistrations () {
		val query = compile (
			SELECT (*) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
				s.matriculationNumber == r.studentMatriculationNumber
			})).asMaterialized


		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")

		val reg1 = Registration(123,11111,"I'm John.")
		val reg2 = Registration(123,22222,"")
		val reg3 = Registration(234,11111,"")
		val reg4 = Registration(234,33333,"")

		students += john += judy += jane
		students.endTransaction()
		registrations += reg1 += reg2 += reg3 += reg4
		registrations.endTransaction()

		assertTrue(query.contains((john,reg1)))
		assertTrue(query.contains((john,reg3)))
		assertTrue(query.contains((judy,reg2)))
		assertTrue(query.contains((jane,reg4)))

		students -= john
		students.endTransaction()

		assertFalse(query.contains((john,reg1)))
		assertFalse(query.contains((john,reg3)))
		assertTrue(query.contains((judy,reg2)))
		assertTrue(query.contains((jane,reg4)))

		registrations -= reg2
		registrations.endTransaction()

		assertFalse(query.contains((john,reg1)))
		assertFalse(query.contains((john,reg3)))
		assertFalse(query.contains((judy,reg2)))
		assertTrue(query.contains((jane,reg4)))
	}

	@Test
	@Ignore
	def testGetStudentsAndTheirRegistrations () {
		val query = compile (
			SELECT ((s: Rep[Student], r: Rep[Registration]) => (s.lastName, r.courseNumber)) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
				s.matriculationNumber == r.studentMatriculationNumber
			})).asMaterialized

		val john = Student(11111, "John", "Doe")
		val judy = Student(22222, "Judy", "Carter")
		val jane = Student(33333, "Jane", "Doe")

		val reg1 = Registration(123,11111,"I'm John.")
		val reg2 = Registration(123,22222,"")
		val reg3 = Registration(234,11111,"")
		val reg4 = Registration(234,33333,"")

		students += john += judy += jane
		students.endTransaction()
		registrations += reg1 += reg2 += reg3 += reg4
		registrations.endTransaction()

		assertTrue(query.contains(("Doe",123)))
		assertTrue(query.contains(("Doe",234)))
		assertTrue(query.contains(("Carter",123)))

		students -= john
		students.endTransaction()

		assertFalse(query.contains(("Doe",123)))
		assertTrue(query.contains(("Doe",234)))
		assertTrue(query.contains(("Carter",123)))

		registrations -= reg4
		registrations.endTransaction()

		assertFalse(query.contains(("Doe",123)))
		assertFalse(query.contains(("Doe",234)))
		assertTrue(query.contains(("Carter",123)))


	}









}
