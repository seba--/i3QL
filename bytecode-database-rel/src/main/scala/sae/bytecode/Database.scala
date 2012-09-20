package sae.bytecode

import model._
import model.dependencies._
import sae.Relation
import de.tud.cs.st.bat.ObjectType
import java.io.File


/**
 *
 * Author: Ralf Mitschke
 * Author: Malte V
 * Created: 22.06.11 15:22
 *
 */

trait Database
{

    def declared_classes: Relation[ClassDeclaration]

    /**
     * convenience accessor for classDeclaration.objectType
     */
    def declared_types: Relation[ObjectType]

    def declared_methods: Relation[MethodDeclaration]

    def declared_fields: Relation[FieldDeclaration]

    def classes: Relation[ObjectType]

    def create: Relation[create]

    def methods: Relation[MethodReference]

    def fields: Relation[FieldReference]

    def instructions: Relation[Instr[_]]

    def `extends`: Relation[`extends`]

    def implements: Relation[implements]

    def subtypes: Relation[(ObjectType, ObjectType)]

    def field_type: Relation[field_type]

    def parameter: Relation[parameter]

    def return_type: Relation[return_type]

    def write_field: Relation[write_field]

    def read_field: Relation[read_field]

    def calls: Relation[calls]

    def class_cast: Relation[class_cast]

    def handled_exceptions: Relation[ExceptionHandler]

    def exception_handlers: Relation[ExceptionHandler]

    def inner_classes: Relation[inner_class]

    def invoke_interface: Relation[invoke_interface]

    def invoke_special: Relation[invoke_special]

    def invoke_virtual: Relation[invoke_virtual]

    def invoke_static: Relation[invoke_static]

    def thrown_exceptions: Relation[throws]

    def getAddClassFileFunction: (File) => Unit

    def getRemoveClassFileFunction: (File) => Unit

    def addClassFile(stream: java.io.InputStream)

    def removeClassFile(stream: java.io.InputStream)

    def addArchive(stream: java.io.InputStream)

    def removeArchive(stream: java.io.InputStream)
}