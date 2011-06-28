package sae.bytecode

import model.dependencies._
import model.{Instr, Field, Method}
import sae.LazyView
import de.tud.cs.st.bat.ObjectType

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.06.11 15:22
 *
 */

trait Database
{

    def classfiles: LazyView[ObjectType]

    def classfile_methods: LazyView[Method]

    def classfile_fields: LazyView[Field]

    def classes: LazyView[ObjectType]

    def methods: LazyView[Method]

    def fields: LazyView[Field]

    def instructions: LazyView[Instr[_]]

    def `extends` : LazyView[`extends`]

    def implements: LazyView[implements]

    def subtypes: LazyView[(ObjectType, ObjectType)]

    def field_type: LazyView[field_type]

    def parameter: LazyView[parameter]

    def return_type: LazyView[return_type]

    def write_field: LazyView[write_field]

    def read_field: LazyView[read_field]

    def calls: LazyView[calls]

    def class_cast: LazyView[class_cast]
}