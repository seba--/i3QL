package sae.bytecode.model

import de.tud.cs.st.bat.ObjectType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 20.06.11 13:43
 *
 */

case class ExceptionHandler(declaringMethod : MethodReference, catchType : Option[ObjectType], startPC : Int, endPC : Int, handlerPC : Int)
{

}