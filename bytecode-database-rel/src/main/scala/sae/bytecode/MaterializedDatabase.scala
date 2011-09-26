package sae
package bytecode

import sae.bytecode.model._
import dependencies._
import sae.collections._
import sae.collections.Conversions._
import de.tud.cs.st.bat._

/**
 * Convenience database that materializes all queries as a result.
 */
class MaterializedDatabase(private val db : BytecodeDatabase)
extends Database
{

    def this()
    {
        this(new BytecodeDatabase())
    }

    val classfiles : QueryResult[ObjectType] = db.classfiles

    val classfile_methods : QueryResult[Method] = db.classfile_methods

    val classfile_fields : QueryResult[Field] = db.classfile_fields

    val classes : QueryResult[ObjectType] = db.classes

    val methods : QueryResult[Method] = db.methods

    val fields : QueryResult[Field] = db.fields

    val instructions : QueryResult[Instr[_]] = db.instructions

    val `extends` : QueryResult[`extends`] = db.`extends`

    val implements : QueryResult[implements] = db.implements

    val subtypes : QueryResult[(ObjectType, ObjectType)] = db.subtypes

    val field_type : QueryResult[field_type] = db.field_type

    val parameter : QueryResult[parameter] = db.parameter

    val return_type : QueryResult[return_type] = db.return_type

    val write_field : QueryResult[write_field] = db.write_field

    val read_field : QueryResult[read_field] = db.read_field

    val calls : QueryResult[calls] = db.calls

    val class_cast : QueryResult[class_cast] = db.class_cast

    val inner_classes: QueryResult[inner_class] = db.inner_classes


    lazy val handled_exceptions : QueryResult[ExceptionHandler] = db.handled_exceptions

     val exception_handlers : QueryResult[ExceptionHandler] = db.exception_handlers

    /**
     * Convenience method that opens a stream from a resource in the class path
     */
    def addArchiveAsResource(name : String)
    {
        db.addArchiveAsResource(name)
    }

    /**
     * Convenience method that opens a stream from a file in the file system
     */
    def addArchiveAsFile(name : String)
    {
        db.addArchiveAsFile(name)
    }

    /**
     * Read a jar archive from the stream.
     * The underlying data is assumed to be in zip (jar) format
     */
    def addArchiveStream(stream : java.io.InputStream)
    {
        db.addArchiveStream(stream)
    }

    def getAddClassFileFunction = {
        db.getAddClassFileFunction
    }

    def getRemoveClassFileFunction = {
        db.getRemoveClassFileFunction
    }

}