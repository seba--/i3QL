package sandbox.findbugs


import de.tud.cs.st.bat.resolved._


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 30.11.12
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
object TypeChecker extends Enumeration {

  val SEEMS_OK, UNCHECKED, ARRAY_AND_OBJECT, NOT_OK = Value

  def checkCompatibility(expected : Type, actual : Type) : TypeChecker.Value = {
    NOT_OK
  }

  private def checkCompatibilityWithArray(t : Type) : TypeChecker.Value = {
    if(t.equals(ObjectType.Object))
      return ARRAY_AND_OBJECT;
    return NOT_OK
  }
}
