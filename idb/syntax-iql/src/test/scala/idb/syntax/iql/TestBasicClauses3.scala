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
import TestUtil.assertEqualStructure
import idb.query.QueryEnvironment
import idb.schema.university._
import idb.syntax.iql.IR._
import org.junit.{Ignore, Test}

/**
 * Test clauses with three relations.
 * By convention the algebra representation will currently contain binary nodes, where the the first two relations
 * are always constructed as leaves of one node and tail relations are added as single leaves of parent nodes.
 *
 * @author Ralf Mitschke
 */
class TestBasicClauses3
{

    @Test
    def testCrossProduct3 () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query = plan (
            SELECT ((s : Rep[Student], r : Rep[Registration], c : Rep[Course]) => (s,r,c)) FROM (students, registrations, courses)
        )

        assertEqualStructure (
            projection (
                crossProduct (
                    crossProduct (table (students), table (registrations)),
                    table (courses)
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }

    @Test
    def testCrossProduct3Selection1st () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query = plan (
            SELECT (*) FROM(students, registrations, courses) WHERE (
                (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                    s.firstName == "Sally"
                }
                )
        )

        assertEqualStructure (
            projection (
                crossProduct (
                    crossProduct (
                        selection (table (students), (s: Rep[Student]) => s.firstName == "Sally"),
                        table (registrations)
                    ),
                    table (courses)
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }

    @Test
    def testCrossProduct3Selection1stAnd2nd () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query = plan (
            SELECT (*) FROM(students, registrations, courses) WHERE (
                (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                    s.firstName == "Sally" &&
                        r.comment == "This is an introductory Course"
                }
                )
        )

        assertEqualStructure (
            projection (
                crossProduct (
                    crossProduct (
                        selection (
                            table (students),
                            (s: Rep[Student]) => s.firstName == "Sally"
                        ),
                        selection (
                            table (registrations),
                            (r: Rep[Registration]) => r.comment == "This is an introductory Course"
                        )
                    ),
                    table (courses)
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }

    @Ignore
    @Test
    def testCrossProduct3Selection1stAnd2ndAnd3rd () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query = plan (
            SELECT (*) FROM(students, registrations, courses) WHERE (
                (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                    s.firstName == "Sally" &&
                        r.comment == "This is an introductory Course" &&
                        c.title.startsWith ("Introduction")
                }
                )
        )

        assertEqualStructure (
            projection (
                crossProduct (
                    crossProduct (
                        selection (
                            table (students),
                            (s: Rep[Student]) => s.firstName == "Sally"
                        ),
                        selection (
                            table (registrations),
                            (r: Rep[Registration]) => r.comment == "This is an introductory Course"
                        )
                    ),
                    selection (
                        table (courses),
                        (c: Rep[Course]) => c.title.startsWith ("Introduction")
                    )
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }

    @Ignore
    @Test
    def testCrossProduct3Selection1stAnd2ndAnd3rdInterleaved () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query =
            plan (
                SELECT (*) FROM(students, registrations, courses) WHERE (
                    (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                        c.title.startsWith ("Introduction") &&
                            r.comment == "This is an introductory Course" &&
                            s.firstName == "Sally" &&
                            c.creditPoints > 4 &&
                            s.lastName == "Fields" &&
                            r.studentMatriculationNumber > 100000
                    }
                    )
            )


        assertEqualStructure (
            projection (
                crossProduct (
                    crossProduct (
                        selection (
                            table (students),
                            (s: Rep[Student]) =>
                                s.firstName == "Sally" &&
                                    s.lastName == "Fields"
                        ),
                        selection (
                            table (registrations),
                            (r: Rep[Registration]) =>
                                r.comment == "This is an introductory Course" &&
                                    r.studentMatriculationNumber > 100000
                        )
                    ),
                    selection (
                        table (courses),
                        (c: Rep[Course]) =>
                            c.title.startsWith ("Introduction") &&
                                c.creditPoints > 4
                    )
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }


    @Ignore
    @Test
    def testCrossProduct3Selection1stAnd2ndCombined () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query =
            plan (
                SELECT (*) FROM(students, registrations, courses) WHERE (
                    (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                        r.comment == "This is an introductory Course" ||
                            s.firstName == "Sally"
                    }
                    )
            )


        assertEqualStructure (
            projection (
                crossProduct (
                    selection (
                        crossProduct (
                            table (students),
                            table (registrations)
                        ),
                        (s: Rep[Student], r: Rep[Registration]) => {
                            r.comment == "This is an introductory Course" ||
                                s.firstName == "Sally"
                        }
                    ),
                    table (courses)
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }

    @Ignore
    @Test
    def testCrossProduct3Selection1stAnd2ndAnd3ndCombined () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query =
            plan (
                SELECT (*) FROM(students, registrations, courses) WHERE (
                    (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                        r.comment == "This is an introductory Course" ||
                            c.creditPoints > 4 ||
                            s.firstName == "Sally"
                    }
                    )
            )


        assertEqualStructure (
            selection (
                crossProduct (
                    table (students),
                    table (registrations),
                    table (courses)
                ),
                (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                    r.comment == "This is an introductory Course" ||
                        c.creditPoints > 4 ||
                        s.firstName == "Sally"
                }
            ),
            query
        )
	}


    @Ignore
    @Test
    def testJoin3On1stAnd2nd () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query =
            plan (
                SELECT (*) FROM(students, registrations, courses) WHERE (
                    (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                        s.matriculationNumber == r.studentMatriculationNumber
                    }
                    )
            )

		//TODO Fix automatic test
        assertEqualStructure (
            projection (
                crossProduct (
                    equiJoin (
                        table (students),
                        table (registrations),
                        scala.List (
                            scala.Tuple2 (
                                fun((s: Rep[Student]) => s.matriculationNumber),
                                fun((r: Rep[Registration]) => r.studentMatriculationNumber)
                            )
                        )
                    ),
                    table (courses)
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }


    @Ignore
    @Test
    def testJoin3On1stAnd2ndPlusOn2ndAnd3rd () {
		implicit val queryEnvironment = QueryEnvironment.Local
        val query =
            plan (
                SELECT (*) FROM(students, registrations, courses) WHERE (
                    (s: Rep[Student], r: Rep[Registration], c: Rep[Course]) => {
                        s.matriculationNumber == r.studentMatriculationNumber &&
                            r.courseNumber == c.number
                    }
                    )
            )

		//TODO fix automatic test
        assertEqualStructure (
            projection (
                equiJoin (
                    equiJoin (
                        table (students),
                        table (registrations),
                        scala.List (
                            scala.Tuple2 (
                                fun((s: Rep[Student]) => s.matriculationNumber),
                                fun((r: Rep[Registration]) => r.studentMatriculationNumber)
                            )
                        )
                    ),
                    table (courses),
                    scala.List (
                        scala.Tuple2 (
                            fun((s: Rep[Student], r: Rep[Registration]) => r.courseNumber),
                            fun((c: Rep[Course]) => c.number)
                        )
                    )
                ),
                fun (
                    (sr_c: Rep[((Student, Registration), Course)]) => (sr_c._1._1, sr_c._1._2, sr_c._2)
                )
            ),
            query
        )
    }
}
