package idb.schema.university


import scala.language.implicitConversions

/**
 *
 * @author Ralf Mitschke
 */
case class Student (matriculationNumber: Int, firstName: String, lastName: String)
    extends Person
{

}

trait StudentSchema
    extends PersonSchema
{

    import IR._

    def Student (matriculationNumber: Rep[Int], firstName: Rep[String], lastName: Rep[String]): Rep[Student] =
        struct[Student](
            ClassTag[Student]("Student"),
            Seq ("matriculationNumber" -> matriculationNumber, "firstName" -> firstName, "lastName" -> lastName)
        )

    case class StudentInfixOp (s: Rep[Student])
    {

        def matriculationNumber: Rep[Int] = field[Int](s, "matriculationNumber")

    }

    implicit def studentToInfixOp (s: Rep[Student]) = StudentInfixOp (s)
}