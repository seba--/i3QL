package idb.schema.university

import scala.virtualization.lms.common.BaseExp

/**
 *
 * @author Ralf Mitschke
 */
trait UniversityNoStructs
    extends BaseExp
{

    case class StudentExp (firstName: Rep[String], lastName: Rep[String]) extends Def[Student]

    def Student (firstName: Rep[String], lastName: Rep[String]): Rep[Student] =
        StudentExp (firstName, lastName)

    def infix_firstName (s: Rep[Student]): Rep[String] = s match {
        case Def (StudentExp (firstName, _)) => firstName
        case _ => throw new UnsupportedOperationException (s.toString)
    }

    def infix_lastName (s: Rep[Student]): Rep[String] = s match {
        case Def (StudentExp (_, lastName)) => lastName
        case _ => throw new UnsupportedOperationException (s.toString)
    }

}