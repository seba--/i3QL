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
package idb.syntax.iql

import TestUtil.assertEqualStructure
import UniversityDatabase._
import idb.query.QueryEnvironment
import idb.schema.university._
import idb.syntax.iql.IR._
import org.junit.{Ignore, Test}
import scala.language.implicitConversions



/**
 *
 * @author Ralf Mitschke
 */
class TestAggregateClauses1
{
	@Test
	def testAggregateCountStudents () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
			SELECT (COUNT ((s : Rep[Student]) => s.lastName)) FROM students
		)


		assertEqualStructure (
			aggregationSelfMaintainedWithoutGrouping (
				table (students),
				0,
				fun ((c: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((c: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((c1: Rep[Student], c2: Rep[Student], i: Rep[Int]) => i)
			),
			query
		)
	}

	@Test
    def testAggregateCountStar () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
            SELECT (COUNT (*)) FROM students
        )

		assertEqualStructure (
			aggregationSelfMaintainedWithoutGrouping (
				table (students),
				0,
				fun ((c: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((c: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((c1: Rep[Student], c2: Rep[Student], i: Rep[Int]) => i)
			),
			query
		)
	}


    @Test
    def testAggregateSumCreditPoints () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
            SELECT (SUM ((c: Rep[Course]) => c.creditPoints)) FROM courses
        )

        assertEqualStructure (
            aggregationSelfMaintainedWithoutGrouping (
                table (courses),
                0,
                fun ((c: Rep[Course], i: Rep[Int]) => i + c.creditPoints),
                fun ((c: Rep[Course], i: Rep[Int]) => i - c.creditPoints),
                fun ((c1: Rep[Course], c2: Rep[Course], i: Rep[Int]) => i - c1.creditPoints + c2.creditPoints)
            ),
            query
        )
    }

    @Test
    def testAggregateCountStudentNames () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
            SELECT (COUNT ((s: Rep[Student]) => s.lastName)) FROM students
        )

        assertEqualStructure (
            aggregationSelfMaintainedWithoutGrouping (
                table (students),
                0,
                fun ((s: Rep[Student], i: Rep[Int]) => i + 1),
                fun ((s: Rep[Student], i: Rep[Int]) => i - 1),
                fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i)
            ),
            query
        )
    }


    @Test
    def testAggregateGroup1 () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
            SELECT ((s: Rep[String]) => s) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )

        assertEqualStructure (
            grouping (
                table (students),
                fun ((s: Rep[Student]) => s.lastName)
            ),
            query
        )

    }

    @Test
    def testAggregateGroup2 () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
            SELECT (
                (firstName : Rep[String], lastName : Rep[String]) => firstName + " " + lastName
            ) FROM students GROUP BY ((s: Rep[Student]) => (s.firstName, s.lastName))
        )

		assertEqualStructure (
			projection (
				grouping (
					table (students),
					fun ((s: Rep[Student]) => (s.firstName, s.lastName))
				),
				fun ((firstName : Rep[String], lastName : Rep[String]) => firstName + " " + lastName)
			),
			query
		)

    }

    @Test
    def testAggregateGroupCountStar () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
            SELECT (COUNT (*)) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )

		assertEqualStructure (
			aggregationSelfMaintainedWithoutConvert (
				table (students),
				fun ((s: Rep[Student]) =>  s.lastName),
				0,
				fun ((s: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((s: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i)
			),
			query
		)

    }


	@Test
	def testAggregateGroupCountWithGroup () {
		implicit val env = QueryEnvironment.Local
		val query = plan (
			SELECT ((s: Rep[String]) => s, COUNT ((s : Rep[Student]) => s) ) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
		)

		assertEqualStructure (
			aggregationSelfMaintained (
				table (students),
				fun ((s: Rep[Student]) =>  s.lastName),
				0,
				fun ((s: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((s: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i),
				fun ((s: Rep[String]) => s),
				fun ( (x : Rep[(String, Int)]) => x )
			),
			query
		)
	}

    @Test
    def testAggregateGroupCountStarWithGroup () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT ((s: Rep[String]) => s, COUNT (*)) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )

		assertEqualStructure (
			aggregationSelfMaintained (
				table (students),
				fun ((s: Rep[Student]) =>  s.lastName),
				0,
				fun ((s: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((s: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i),
				fun ((s: Rep[String]) => s),
				fun ( (x : Rep[(String, Int)]) => x )
			),
			query
		)
    }


    @Test
    def testAggregateSumMatriulationNumberWithGroup () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT ((s: Rep[String]) => s, SUM ( (s:Rep[Student]) => s.matriculationNumber)) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )

		assertEqualStructure (
			aggregationSelfMaintained (
				table (students),
				fun ((s: Rep[Student]) =>  s.lastName),
				0,
				fun ((s: Rep[Student], i: Rep[Int]) => i + s.matriculationNumber),
				fun ((s: Rep[Student], i: Rep[Int]) => i - s.matriculationNumber),
				fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i - s1.matriculationNumber + s2.matriculationNumber),
				fun ((s: Rep[String]) => s),
				fun ( (x : Rep[(String, Int)]) => x )
			),
			query
		)


    }

    @Test
    def testAggregateGroupWithWhere () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT ((s: Rep[String]) => s) FROM students WHERE (_.matriculationNumber > 10000) GROUP BY (
                (s: Rep[Student]) => s.lastName)
        )

		assertEqualStructure (
			grouping (
				selection (
					table (students),
					fun ((s : Rep[Student]) => s.matriculationNumber > 10000)
				),
				fun ((s: Rep[Student]) => s.lastName)
			),
			query
		)

    }

    @Test
    def testAggregateGroupCountStarWithWhere () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT (COUNT (*)) FROM students WHERE (_.matriculationNumber > 10000) GROUP BY (
                (s: Rep[Student]) => s.lastName)
        )

		assertEqualStructure (
			aggregationSelfMaintainedWithoutConvert (
				selection (
					table (students),
					fun ((s : Rep[Student]) => s.matriculationNumber > 10000)
				),
				fun ((s: Rep[Student]) =>  s.lastName),
				0,
				fun ((s: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((s: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i)
			),
			query
		)
    }


    @Test
    def testAggregateGroupCountStarWithGroupWithWhere () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT ((s: Rep[String]) => s, COUNT (*)) FROM students WHERE (_.matriculationNumber > 10000) GROUP BY (
                (s: Rep[Student]) => s.lastName)
        )

		assertEqualStructure (
			aggregationSelfMaintained (
				selection (
					table (students),
					fun ((s : Rep[Student]) => s.matriculationNumber > 10000)
				),
				fun ((s: Rep[Student]) =>  s.lastName),
				0,
				fun ((s: Rep[Student], i: Rep[Int]) => i + 1),
				fun ((s: Rep[Student], i: Rep[Int]) => i - 1),
				fun ((s1: Rep[Student], s2: Rep[Student], i: Rep[Int]) => i),
				fun ((s: Rep[String]) => s),
				fun ( (x : Rep[(String, Int)]) => x )
			),
			query
		)
    }

}
