package sae.bytecode

import model.dependencies._
import model.instructions.{invokevirtual, invokespecial, invokeinterface}
import model.{ExceptionHandler, Instr, Field, Method}
import sae.LazyView
import de.tud.cs.st.bat.ObjectType
import java.io.File
import sae.syntax.RelationalAlgebraSyntax.Π._


/**
 *
 * Author: Ralf Mitschke
 * Author: Malte V
 * Created: 22.06.11 15:22
 *
 */

trait Database
{

    def classfiles: LazyView[ObjectType]

    def classfile_methods: LazyView[Method]

    def classfile_fields: LazyView[Field]

    def classes: LazyView[ObjectType]

    def create: LazyView[create]

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

    def handled_exceptions : LazyView[ExceptionHandler]

    def exception_handlers : LazyView[ExceptionHandler]

    def inner_classes: LazyView[inner_class]

    def invoke_interface: LazyView[invoke_interface]

    def invoke_special: LazyView[invoke_special]

    def invoke_virtual: LazyView[invoke_virtual]

    def invoke_static: LazyView[invoke_static]

    def thrown_exceptions: LazyView[throws]

    def getAddClassFileFunction: (File) => Unit

    def getRemoveClassFileFunction: (File) => Unit

    def addClassFile (stream: java.io.InputStream)

    def removeClassFile(stream: java.io.InputStream)

}