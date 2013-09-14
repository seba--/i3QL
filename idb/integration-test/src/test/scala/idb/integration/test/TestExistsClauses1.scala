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
import idb.schema.university._
import idb.syntax.iql.IR._
import idb.syntax.iql._
import org.junit.Assert._
import org.junit.Test
import scala.language.implicitConversions


/**
 *
 * @author Ralf Mitschke
 */
class TestExistsClauses1
    extends UniversityTestData
{


    @Test
    def testExists () {
        val result = compile (
            SELECT (*) FROM students WHERE ((s: Rep[Student]) =>
                EXISTS (
                    SELECT (*) FROM registrations WHERE ((r: Rep[Registration]) =>
                        s.matriculationNumber == r.studentMatriculationNumber
                        )
                )
                )
        ).asMaterialized

        assertEquals (result.asList, Nil)

        students += sallyFields

        assertEquals (result.asList, Nil)

        registrations += sallyTakesIcs1

        assertEquals (result.asList, scala.List (sallyFields))

        students -= sallyFields

        assertEquals (result.asList, Nil)

        students += sallyFields

        assertEquals (result.asList, scala.List (sallyFields))

        registrations -= sallyTakesIcs1

        assertEquals (result.asList, Nil)
    }

    @Test
    def testNotExists () {
        val result = compile (
            SELECT (*) FROM students WHERE ((s: Rep[Student]) =>
                NOT (
                    EXISTS (
                        SELECT (*) FROM registrations WHERE ((r: Rep[Registration]) =>
                            s.matriculationNumber == r.studentMatriculationNumber
                            )
                    )
                )
                )
        ).asMaterialized

        assertEquals (result.asList, Nil)

        students += sallyFields

        assertEquals (result.asList, scala.List (sallyFields))

        registrations += sallyTakesIcs1

        assertEquals (result.asList, Nil)

        students -= sallyFields

        assertEquals (result.asList, Nil)

        students += sallyFields

        assertEquals (result.asList, Nil)

        registrations -= sallyTakesIcs1

        assertEquals (result.asList, scala.List (sallyFields))
    }
}