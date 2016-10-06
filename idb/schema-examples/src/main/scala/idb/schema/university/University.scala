package idb.schema.university

import scala.virtualization.lms.common.StructExp

/**
 *
 * @author Ralf Mitschke
 */
trait University
    extends StructExp
{

    def Student (matriculationNumber: Rep[Int], firstName: Rep[String], lastName: Rep[String]): Rep[Student] =
        struct[Student](
            ClassTag[Student]("Student"),
            Seq ("matriculationNumber" -> matriculationNumber, "firstName" -> firstName, "lastName" -> lastName)
        )

    def infix_firstName (s: Rep[Student]): Rep[String] = field[String](s, "firstName")

    def infix_lastName (s: Rep[Student]): Rep[String] = field[String](s, "lastName")

    def infix_matriculationNumber (s: Rep[Student]): Rep[Int] = field[Int](s, "matriculationNumber")
}