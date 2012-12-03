package sandbox.stackAnalysis

import de.tud.cs.st.bat.resolved._
import sandbox.dataflowAnalysis.Combinable
import sandbox.stackAnalysis.TypeOption.NoneType

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.11.12
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
case class Stacks(maxSize: Int, collection : List[Stack]) extends Combinable[Stacks] {
  //TODO: remove for better performance
  //require(collection.forall((s) => s.maxSize == maxSize))

  def dup(amount: Int, offset: Int): Stacks = {
    Stacks(maxSize,collection.map(s => s.dup(amount,offset)))
  }

  def pop(amount: Int): Stacks = {
    Stacks(maxSize,collection.map(s => s.pop(amount)))
  }

  def pop(): Stacks = {
    Stacks(maxSize,collection.map(s => s.pop()))
  }

  def swap(): Stacks = {
    Stacks(maxSize,collection.map(s => s.swap()))
  }

  def push(t : TypeOption): Stacks = {
    Stacks(maxSize,collection.map(s => s.push(t)))
  }

  def push(t : Type, pc : Int): Stacks = {
    Stacks(maxSize,collection.map(s => s.push(t,pc)))
  }

  //TODO: Reactivate exceptions
  def combineWith(other: Stacks): Stacks = {
    if (other.maxSize != maxSize) {
      //System.err.println("The attribute maxSize needs to be the same.")
      return this
      // throw new IllegalArgumentException("The attribute maxSize needs to be the same.")
    }

    Stacks(maxSize, (collection ++ other.collection).distinct)
  }

  def addStack() : Stacks = {
    Stacks(maxSize,Stack(maxSize,Nil) :: collection)
  }

  def declaredTypeOf(index : Int) : TypeOption = {
    var res : TypeOption = NoneType
    for(stack <- collection) {
        if (index < stack.size)
          res = res.max(stack.values(index))
     }
    return res
  }

  override def toString() : String = {
    return collection.mkString("Stacks<"+maxSize+">: {"," || ","}")
  }

  /**
   * Checks if all possible stack entries are an instance of the given type option.
   * @param index The index of the stack value.
   * @tparam A The type option to be checked.
   * @return True if all stacks have the given type at the given index.
   */
  def checkAll[A](index : Int) : Boolean = {

    for(stack <- collection) {
      if(!stack.values(index).isInstanceOf[A])
        return false
    }

    return true
  }
}
