package sae.findbugs.analyses

import de.tud.cs.st.bat.ObjectType
import sae.bytecode.{BytecodeDatabase, Database}
import sae.Relation
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.ExceptionHandler

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 19:29
 *
 */
object IMSE_DONT_CATCH_IMSE
{

    val IllegalMonitorStateExceptionType = Some(ObjectType("java/lang/IllegalMonitorStateException"))

    def apply(database: BytecodeDatabase): Relation[ExceptionHandler] =
        σ((_: ExceptionHandler).catchType == IllegalMonitorStateExceptionType)(database.exception_handlers)

}