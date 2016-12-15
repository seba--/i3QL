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

/**
 *
 * @author Ralf Mitschke
 */
class TestAggregateClauses2
{
    @Test
    def testGrouping () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT ((s: Rep[String]) => s) FROM(students, registrations) GROUP BY (
                (s: Rep[Person], r: Rep[Registration]) => s.lastName + r.comment
            )
        )


        assertEqualStructure (
            grouping (
                crossProduct (
                    table (students),
                    table (registrations)
                ),
                fun ((s: Rep[Person], r: Rep[Registration]) => s.lastName + r.comment)
            ),
            query
        )

    }


    @Test
    def testJoin2CountStar () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT (COUNT (*)) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) =>
                s.matriculationNumber == r.studentMatriculationNumber
            )
        )

		assertEqualStructure (
			aggregationSelfMaintainedWithoutGrouping (
				selection (
					crossProduct (
						table (students),
						table (registrations)
					),
					fun ((s: Rep[Student], r: Rep[Registration]) => s.matriculationNumber == r.studentMatriculationNumber)
				),
				0,
				fun ((s: Rep[(Student, Registration)], i: Rep[Int]) => i + 1),
				fun ((s: Rep[(Student, Registration)], i: Rep[Int]) => i - 1),
				fun ((s1: Rep[(Student, Registration)], s2: Rep[(Student, Registration)], i: Rep[Int]) => i)
			),
			query
		)
    }

    @Test
    def testJoin2AggregateGroup1 () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT ((s: Rep[String]) => s) FROM (students, registrations) WHERE ((
            s: Rep[Student],
            r: Rep[Registration]
            ) => s.matriculationNumber == r.studentMatriculationNumber
            ) GROUP BY ((s: Rep[Student], r: Rep[Registration]) => s.lastName)
        )

		assertEqualStructure (
			grouping (
				selection (
					crossProduct (
						table (students),
						table (registrations)
					),
					fun ((s: Rep[Student], r: Rep[Registration]) => s.matriculationNumber == r.studentMatriculationNumber)
				),
				fun ((s: Rep[Student], r: Rep[Registration]) => s.lastName)
			),
			query
		)

    }

    @Test
    def testJoin2SumCreditPoints () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT (SUM ((r: Rep[Registration], c: Rep[Course]) => c.creditPoints)) FROM(registrations, courses) WHERE (
                (r: Rep[Registration], c: Rep[Course]) => r.courseNumber == c.number
                )
        )

		assertEqualStructure (
			aggregationSelfMaintainedWithoutGrouping (
				selection (
					crossProduct (
						table (registrations),
						table (courses)
					),
					fun ((r: Rep[Registration], c: Rep[Course]) => r.courseNumber == c.number)
				),
				0,
				fun ((s: Rep[(Registration, Course)], i: Rep[Int]) => i + s._2.creditPoints),
				fun ((s: Rep[(Registration, Course)], i: Rep[Int]) => i - s._2.creditPoints),
				fun ((s1: Rep[(Registration, Course)], s2: Rep[(Registration, Course)], i: Rep[Int]) => i - s1._2.creditPoints + s2._2.creditPoints)
			),
			query
		)
    }


}
