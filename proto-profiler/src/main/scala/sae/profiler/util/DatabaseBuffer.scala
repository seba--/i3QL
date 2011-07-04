package sae.profiler.util
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model._
import sae.DefaultLazyView
import sae.bytecode.model._
import dependencies._
import sae.collections._
import sae.collections.Conversions._
import sae.bytecode.transform._
import de.tud.cs.st.bat._
import de.tud.cs.st.lyrebird.replayframework.EventSet
import sae.bytecode.{Database, BytecodeDatabase}

class DatabaseBuffer(val db : BytecodeDatabase) extends Database {

    val classfiles : ObserverBuffer[ObjectType] = new ObserverBuffer(db.classfiles)
    val classfile_methods : ObserverBuffer[Method] = new ObserverBuffer(db.classfile_methods)
    val classfile_fields : ObserverBuffer[Field] = new ObserverBuffer(db.classfile_fields)
    val classes : ObserverBuffer[ObjectType] = new ObserverBuffer(db.classes)
    val methods : ObserverBuffer[Method] = new ObserverBuffer(db.methods)
    val fields : ObserverBuffer[Field] = new ObserverBuffer(db.fields)
    val instructions : ObserverBuffer[Instr[_]] = new ObserverBuffer(db.instructions)
    val `extends` : ObserverBuffer[`extends`] = new ObserverBuffer(db.`extends`)
    val implements : ObserverBuffer[implements] = new ObserverBuffer(db.implements)
    val field_type : ObserverBuffer[field_type] = new ObserverBuffer(db.field_type)
    val parameter : ObserverBuffer[parameter] = new ObserverBuffer(db.parameter)
    val return_type : ObserverBuffer[return_type] = new ObserverBuffer(db.return_type)
    val write_field : ObserverBuffer[write_field] = new ObserverBuffer(db.write_field)
    val read_field : ObserverBuffer[read_field] = new ObserverBuffer(db.read_field)
    val calls : ObserverBuffer[calls] = new ObserverBuffer(db.calls)
    val class_cast : ObserverBuffer[class_cast] = new ObserverBuffer(db.class_cast)
    val handled_exceptions : ObserverBuffer[ExceptionHandler] = new ObserverBuffer(db.handled_exceptions)
    val exception_handlers : ObserverBuffer[ExceptionHandler] = new ObserverBuffer(db.handled_exceptions)


  val subtypes: ObserverBuffer[(ObjectType, ObjectType)] = new ObserverBuffer[(ObjectType, ObjectType)](db.subtypes)


    def replay() : Unit = {
        classfiles.replay
        classfile_methods.replay
        classfile_fields.replay
        classes.replay
        methods.replay
        fields.replay
        instructions.replay
        `extends`.replay
        implements.replay
        field_type.replay
        parameter.replay
        return_type.replay
        write_field.replay
        read_field.replay
        calls.replay
        class_cast.replay
        handled_exceptions.replay
        subtypes.replay
        exception_handlers.replay
    }

    def reset() {
         classfiles.reset
        classfile_methods.reset
        classfile_fields.reset
        classes.reset
        methods.reset
        fields.reset
        instructions.reset
        `extends`.reset
        implements.reset
        field_type.reset
        parameter.reset
        return_type.reset
        write_field.reset
        read_field.reset
        calls.reset
        class_cast.reset
        handled_exceptions.reset
        subtypes.reset
        exception_handlers.reset
    }

  def getRemoveClassFileFunction = throw new Error()

  def getAddClassFileFunction = throw new Error()
}