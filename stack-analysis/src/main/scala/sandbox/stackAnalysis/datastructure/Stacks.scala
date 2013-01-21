package sandbox.stackAnalysis.datastructure

import de.tud.cs.st.bat.resolved._
import sae.operators.Combinable


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.11.12
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
case class Stacks(maxSize: Int, collection: List[Stack]) extends Combinable[Stacks] {

  def dup(amount: Int, offset: Int): Stacks = {
    Stacks(maxSize, collection.map(s => s.dup(amount, offset)))
  }

  def pop(amount: Int): Stacks = {
    Stacks(maxSize, collection.map(s => s.pop(amount)).distinct)
  }

  def pop(): Stacks = {
    Stacks(maxSize, collection.map(s => s.pop()).distinct)
  }

  def swap(): Stacks = {
    Stacks(maxSize, collection.map(s => s.swap()))
  }

  def push(t: Item): Stacks = {
    Stacks(maxSize, collection.map(s => s.push(t)))
  }

  def push(t: Type, pc: Int): Stacks = {
    Stacks(maxSize, collection.map(s => s.push(t, pc)))
  }

  def push(ts: List[Item]): Stacks = {
    if (ts == Nil)
      return this
    else {
      push(Item.combine(ts))
    }
  }


  def combineWith(other: Stacks): Stacks = {
    if (other.maxSize != maxSize) {
      throw new IllegalArgumentException("The attribute maxSize needs to be the same.")
    }

    Stacks(maxSize, (collection ++ other.collection).distinct)
  }

  def addStack(): Stacks = {
    Stacks(maxSize, Stack(maxSize, Nil) :: collection)
  }

  override def toString(): String = {
    return collection.mkString("Stacks<" + maxSize + ">: {", " || ", "}")
  }

  /**
   * Checks if all possible stack entries are an instance of the given type option.
   * @param index The index of the stack value.
   * @tparam A The type option to be checked.
   * @return True if all stacks have the given type at the given index.
   */
 /* def checkAll[A](index: Int): Boolean = {

    for (stack <- collection) {
      if (!stack.values(index).isInstanceOf[A])
        return false
    }

    return true
  }    */

  def head: List[Item] = {
    var res: List[Item] = Nil

    for (stack <- collection) {
      if (stack.size == 0)
        return Nil

      res = stack(0) :: Nil
    }

    return res
  }
}
