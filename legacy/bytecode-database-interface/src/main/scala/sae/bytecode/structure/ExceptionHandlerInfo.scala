package sae.bytecode.structure

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.09.12
 * Time: 14:30
 *
 */
case class ExceptionHandlerInfo(declaringMethod: MethodDeclaration, handler: de.tud.cs.st.bat.resolved.ExceptionHandler)
{
    def startPc = handler.startPC

    def endPc = handler.endPC

    def handlerPc = handler.handlerPC

    def catchType = handler.catchType
}