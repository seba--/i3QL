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

import idb.schema.university._
import idb.syntax.iql.IR._

/**
 *
 * @author Ralf Mitschke
 *
 */

object UniversitySchema
{
    /* Definitions for Student  */
    def Student (matriculationNumber: Rep[Int], firstName: Rep[String], lastName: Rep[String]): Rep[Student] =
        struct[Student](
            ClassTag[Student]("Student"),
            Map ("matriculationNumber" -> matriculationNumber, "firstName" -> firstName, "lastName" -> lastName)
        )

    def infix_firstName (s: Rep[Student]): Rep[String] = field[String](s, "firstName")

    def infix_lastName (s: Rep[Student]): Rep[String] = field[String](s, "lastName")

    def infix_matriculationNumber (s: Rep[Student]): Rep[Int] = field[Int](s, "matriculationNumber")

    def firstName = infix_firstName _

    def lastName = infix_lastName _

    def matriculationNumber = infix_matriculationNumber _

    /* Definitions for Course */
    def Course (number: Rep[Int], title: Rep[String], creditPoints: Rep[Int]) =
        struct[Course](
            ClassTag[Course]("Course"),
            Map ("number" -> number, "title" -> title, "creditPoints" -> creditPoints)
        )

    def infix_number (c: Rep[Course]): Rep[Int] = field[Int](c, "number")

    def infix_title (c: Rep[Course]): Rep[String] = field[String](c, "title")

    def infix_creditPoints (c: Rep[Course]): Rep[Int] = field[Int](c, "creditPoints")

    def number = infix_number _

    def title = infix_title _

    def creditPoints = infix_creditPoints _

    def Registration (courseNumber: Rep[Int], studentMatriculationNumber: Rep[Int], comment: Rep[String]) =
        struct[Registration](
            ClassTag[Registration]("Registration"),
            Map ("courseNumber" -> courseNumber,
                "studentMatriculationNumber" -> studentMatriculationNumber,
                "comment" -> comment
            )
        )

    def infix_courseNumber (r: Rep[Registration]): Rep[Int] = field[Int](r, "courseNumber")

    def infix_studentMatriculationNumber (r: Rep[Registration]): Rep[Int] = field[Int](r, "studentMatriculationNumber")

    def infix_comment (r: Rep[Registration]): Rep[String] = field[String](r, "comment")

}
