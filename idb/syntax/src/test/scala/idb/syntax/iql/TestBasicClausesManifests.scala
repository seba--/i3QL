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
class TestBasicClausesManifests
{

    @Test
    def testSelectStarFromStudents () {
        val query = plan (
            SELECT (*) FROM students
        )

        assertEquals(
            scala.collection.immutable.List(
                manifest[Student]
            ),
            query.tp.typeArguments
        )
    }

    @Test
    def testSelectFirstNameFromStudents () {
        val query = plan (
            SELECT (firstName) FROM students
        )

        assertEquals(
            scala.collection.immutable.List(
                manifest[String]
            ),
            query.tp.typeArguments
        )


        assertEquals(
            scala.collection.immutable.List(
                manifest[Student],
                manifest[String]
            ),
            (query match {
                case Def(Projection(_, fun)) => fun
            }).tp.typeArguments
        )

        assertEquals(
            (
                manifest[Student],
                manifest[String]
            ),
            (query match {
                case Def(Projection(_, fun)) => compilationTypes(fun)
            })
        )
    }

    @Test
    def testSelectFirstNameFromStudentsCompileTypes () {
        val query = plan (
            SELECT (firstName) FROM students
        )

        matching(query)
    }

    def matching[Domain: Manifest] (query: Rep[Query[Domain]]) {
        query match {
            case Def (Selection (r, f)) => compilationTypes(f)
            case Def (Projection (r, f)) => compilationTypes(f)
        }
    }

    def compilationTypes[A: Manifest, B: Manifest] (f: Rep[A => B]) : (Manifest[A], Manifest[B]) = {
        (manifest[A], manifest[B])
    }
}
