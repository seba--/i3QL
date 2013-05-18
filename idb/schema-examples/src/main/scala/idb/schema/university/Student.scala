package idb.schema.university


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
            Map ("matriculationNumber" -> matriculationNumber, "firstName" -> firstName, "lastName" -> lastName)
        )

    def infix_matriculationNumber (s: Rep[Student]): Rep[Int] = field[Int](s, "matriculationNumber")

    def matriculationNumber = infix_matriculationNumber _


}