package idb.schema.lms

import scala.virtualization.lms.common.StructExp

/**
 *
 * @author Ralf Mitschke
 */
trait University
    extends StructExp
{

    def Student (firstName: Rep[String], lastName: Rep[String]): Rep[Student] =
        struct[Student](
            ClassTag[Student] ("Student"),
            Map ("firstName" -> firstName, "lastName" -> lastName)
        )

    def infix_firstName (s: Rep[Student]): Rep[String] = field[String](s, "firstName")

    def infix_lastName (s: Rep[Student]): Rep[String] = field[String](s, "lastName")

}