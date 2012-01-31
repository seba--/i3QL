package sae.bytecode.model


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.01.12
 * Time: 16:43
 *
 */
trait FieldIdentifier
{
    def declaringClass: de.tud.cs.st.bat.ObjectType

    def name: String

    def fieldType: de.tud.cs.st.bat.FieldType
}