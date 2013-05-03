package idb.schema.lms

import scala.virtualization.lms.common.StructExpOptCommon

/**
 *
 * @author Ralf Mitschke
 */
trait University
    extends StructExpOptCommon
{

    type Student = Record {
        val firstName: String
        val lastName: String
    }

    def Student (_firstName: Rep[String], _lastName: Rep[String]): Rep[Student] =
        new Record
        {
            val firstName = _firstName
            val lastName = _lastName
        }
}