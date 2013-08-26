package sae.java

import members.Method
import types.ObjectType

/**
 * 
 * Author: Ralf Mitschke
 * Created: 20.06.11 13:43
 *
 */
case class ExceptionHandler(declaringMethod : Method, catchType : Option[ObjectType], startPC : Int, endPC : Int, handlerPC : Int)
{

}