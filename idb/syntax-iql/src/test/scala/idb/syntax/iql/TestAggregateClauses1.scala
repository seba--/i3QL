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
    @Ignore
    @Test
    def testAggregateCountStar () {
        val query = plan (
            SELECT (COUNT (*)) FROM students
        )


        /*   assertEqualStructure (
               aggregationSelfMaintainedWithoutGrouping (
                   students,
                   start,
                   COUNT.start,
                   COUNT.added _,
                   COUNT.removed _,
                   COUNT.updated _
               ),
               query
           )*/

    }


    @Test
    def testAggregateSumCreditPoints () {
        val query = plan (
            SELECT (SUM ((c: Rep[Course]) => c.creditPoints)) FROM courses
        )

        assertEqualStructure (
            aggregationSelfMaintainedWithoutGrouping (
                extent (courses),
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
        val query = plan (
            SELECT (COUNT ((s: Rep[Student]) => s.lastName)) FROM students
        )

        assertEqualStructure (
            aggregationSelfMaintainedWithoutGrouping (
                extent (students),
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

        val query = plan (
            SELECT ((s: Rep[String]) => s) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )

        assertEqualStructure (
            grouping (
                extent (students),
                fun ((s: Rep[Student]) => s.lastName)
            ),
            query
        )

    }

    @Test
    def testAggregateGroup2 () {

        val query = plan (
            SELECT (
                //(firstName : Rep[String], lastName : Rep[String]) => firstName + " " + lastName
                //TODO Re-enable
                // in later version
                (pair: Rep[(String, String)]) => pair._1 + " " + pair._2
            ) FROM students GROUP BY ((s: Rep[Student]) => (s.firstName, s.lastName)) // returns Rep[(String,String)]
        )

    }

    @Ignore
    @Test
    def testAggregateGroupCountStar () {
        val query = plan (
            SELECT (COUNT (*)) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )
    }

    @Ignore
    @Test
    def testAggregateGroupCountStarWithGroup () {
        val query = plan (
            SELECT ((s: Rep[String]) => s, COUNT (*)) FROM students GROUP BY ((s: Rep[Student]) => s.lastName)
        )
    }

    @Test
    def testAggregateGroupWithWhere () {

        val query = plan (
            SELECT ((s: Rep[String]) => s) FROM students WHERE (_.matriculationNumber > 10000) GROUP BY (
                (s: Rep[Student]) => s.lastName)
        )

    }

    @Ignore
    @Test
    def testAggregateGroupCountStarWithWhere () {
        val query = plan (
            SELECT (COUNT (*)) FROM students WHERE (_.matriculationNumber > 10000) GROUP BY (
                (s: Rep[Student]) => s.lastName)
        )
    }

    @Ignore
    @Test
    def testAggregateGroupCountStarWithGroupWithWhere () {
        val query = plan (
            SELECT ((s: Rep[String]) => s, COUNT (*)) FROM students WHERE (_.matriculationNumber > 10000) GROUP BY (
                (s: Rep[Student]) => s.lastName)
        )
    }

}
