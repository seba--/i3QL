package sandbox.stackAnalysis.datastructure

import sae.operators.Combinable
import de.tud.cs.st.bat.resolved.ObjectType
import sae.bytecode.structure.MethodDeclaration

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 04.11.12
 * Time: 20:02
 */
case class State(stacks: Stacks, variables: LocalVariables) extends Combinable[State] {

  def upperBound(other: State): State = {
    new State(stacks.upperBound(other.stacks), variables.upperBound(other.variables))
  }

  override def equals(other: Any): Boolean = {
    if (other == null)
      return false
    if (!other.isInstanceOf[State])
      return false

    val otherSR = other.asInstanceOf[State]

    return (stacks equals otherSR.stacks) && (variables equals otherSR.variables)
  }


}

case object State {
  def createEmptyState(maxStack: Int, maxLoc: Int): State =
    State(Stacks(maxStack, Nil), LocalVariables(Array.ofDim[Item](maxLoc)))

  def createStartState(maxStack: Int, maxLoc: Int, declaringMethod: MethodDeclaration): State = {
    var stacks = Stacks(maxStack, Nil).addStack()

    var lvs = if (declaringMethod.isStatic)
      LocalVariables(Array.fill[Item](maxLoc)(Item( -1, ItemType.None, Item.FLAG_IS_NOT_INITIALIZED)))
    else
      LocalVariables(Array.fill[Item](maxLoc)(Item( -1, ItemType.None, Item.FLAG_IS_NOT_INITIALIZED))).setVar(0, Item( -1, ItemType.SomeRef(ObjectType.Class), Item.FLAG_IS_PARAMETER))

    var i: Int = if (declaringMethod.isStatic) -1 else 0

    for (t <- declaringMethod.parameterTypes) {
      i = i + 1
      lvs = lvs.setVar(i, Item( -1, ItemType.fromType(t), Item.FLAG_IS_PARAMETER))
    }

    State(stacks, lvs)
  }

}
