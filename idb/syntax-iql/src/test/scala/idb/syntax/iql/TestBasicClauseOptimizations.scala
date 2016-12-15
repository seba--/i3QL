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

import UniversityDatabase._
import idb.query.QueryEnvironment
import idb.schema.university._
import idb.syntax.iql.IR._
import org.junit.Assert._
import org.junit.{Ignore, Test}
import idb.syntax.iql.TestUtil._


/**
 *
 * @author Ralf Mitschke
 */
class TestBasicClauseOptimizations
{

    @Test
    def testPropagateFilterToRightViaJoin () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT (*) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber &&
                s.matriculationNumber == 12345
            })
        )

      assertEqualStructure (
            equiJoin (
                selection (table (students), (s: Rep[Student]) => s.matriculationNumber == 12345),
               	table (registrations),
                scala.List ((
                    fun ((s: Rep[Student]) => s.matriculationNumber),
                    fun ((r: Rep[Registration]) => r.studentMatriculationNumber)
                    ))
            ),
            query
        )
    }

    @Test
    def testPropagateFilterToLeftViaJoin () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT (*) FROM(students, registrations) WHERE ((s: Rep[Student], r: Rep[Registration]) => {
                s.matriculationNumber == r.studentMatriculationNumber &&
                    r.studentMatriculationNumber == 12345
            })
        )

      assertEqualStructure (
            equiJoin (
                table (students),
                selection (table (registrations), (r: Rep[Registration]) => r.studentMatriculationNumber == 12345),
                scala.List ((
                    fun ((s: Rep[Student]) => s.matriculationNumber),
                    fun ((r: Rep[Registration]) => r.studentMatriculationNumber)
                    ))
            ),
            query
        )
    }

    @Test
    def testLocalIncrementJoin () {
		implicit val env = QueryEnvironment.Local
        val query = plan (
            SELECT (*) FROM(lectures, bookRecommendations) WHERE ((l: Rep[Lecture], b: Rep[BookRecommendation]) => {
                l.course == b.course
            })
        )

      assertEqualStructure (
            equiJoin (
                table (lectures),
                table (bookRecommendations),
                scala.List ((
                    fun ((l: Rep[Lecture]) => l.course),
                    fun ((b: Rep[BookRecommendation]) => b.course)
                    ))
            ),
            query
        )
    }
}
