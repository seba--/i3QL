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
import idb.schema.university._
import idb.syntax.iql.IR._
import org.junit.Assert._
import org.junit.Test

/**
 *
 * @author Ralf Mitschke
 */
class TestBasicClauses1
{
    @Test
    def testExtent () {
        val query = plan (
            SELECT (*) FROM students
        )

        assertEquals (extent (students), query)

        assertEquals (
            scala.collection.immutable.List (
                manifest[Student]
            ),
            query.tp.typeArguments
        )
    }

    @Test
    def testProject1 () {
        val query = plan (
            SELECT (firstName) FROM students
        )

        assertEquals (
            projection (extent (students), firstName),
            query
        )

        assertEquals (
            scala.collection.immutable.List (
                manifest[String]
            ),
            query.tp.typeArguments
        )


    }

    @Test
    def testProject1TupleFun () {
        val query = plan (
            SELECT (firstName, lastName) FROM students
        )

        assertEquals (
            projection (extent (students), fun ((s: Rep[Student]) => (s.firstName, s.lastName))),
            query
        )
    }


    @Test
    def testSelection1 () {
        val query = plan (
            SELECT (*) FROM students WHERE ((s: Rep[Student]) => s.firstName == "Sally")
        )

        assertEquals (
            selection (extent (students), (s: Rep[Student]) => s.firstName == "Sally"),
            query
        )
    }

    @Test
    def testSelection1FunCall () {
        val query = plan (
            SELECT (*) FROM courses WHERE ((c: Rep[Course]) => c.title.startsWith ("Introduction"))
        )

        assertEquals (
            selection (extent (courses), (c: Rep[Course]) => c.title.startsWith ("Introduction")),
            query
        )
    }

    @Test
    def testSelection1ProjectTupleFun () {
        val query = plan (
            SELECT (firstName, lastName) FROM students WHERE ((s: Rep[Student]) => s.firstName == "Sally")
        )

        assertEquals (
            projection (
                selection (
                    extent (students),
                    fun ((s: Rep[Student]) => s.firstName == "Sally")
                ),
                fun ((s: Rep[Student]) => (s.firstName, s.lastName))
            ),
            query
        )
    }


    @Test
    def testSelection1ProjectTupleDirect () {
        val query = plan (
            SELECT ((s: Rep[Student]) => (s.firstName, s.lastName)) FROM students WHERE ((s: Rep[Student]) => s.firstName == "Sally")
        )

        assertEquals (
            projection (
                selection (
                    extent (students),
                    fun ((s: Rep[Student]) => s.firstName == "Sally")
                ),
                fun ((s: Rep[Student]) => (s.firstName, s.lastName))
            ),
            query
        )
    }


    @Test
    def testDistinctExtent () {
        val query = plan (
            SELECT DISTINCT (*) FROM students
        )

        assertEquals (
            duplicateElimination (
                extent (students)
            ),
            query
        )
    }

}
