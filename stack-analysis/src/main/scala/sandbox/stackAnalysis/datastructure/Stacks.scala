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
case class Stacks(maxStack: Int, stacks: List[Stack]) extends Combinable[Stacks] {

  def dup(amount: Int, offset: Int): Stacks = {
    Stacks(maxStack, stacks.map(s => s.dup(amount, offset)))
  }

  def pop(amount: Int): Stacks = {
    Stacks(maxStack, stacks.map(s => s.pop(amount)).distinct)
  }

  def pop(): Stacks = {
    Stacks(maxStack, stacks.map(s => s.pop()).distinct)
  }

  def swap(): Stacks = {
    Stacks(maxStack, stacks.map(s => s.swap()))
  }

  def push(t: Item): Stacks = {
    Stacks(maxStack, stacks.map(s => s.push(t)))
  }

  def push(t: Type, pc: Int): Stacks = {
    Stacks(maxStack, stacks.map(s => s.push(t, pc)))
  }

  def push(ts: List[Item]): Stacks = {
    if (ts == Nil)
      return this
    else {
      push(Item.upperBound(ts))
    }
  }


  def upperBound(other: Stacks): Stacks = {
    if (other.maxStack != maxStack) {
      throw new IllegalArgumentException("The attribute maxStack needs to be the same.")
    }

    Stacks(maxStack, (stacks ++ other.stacks).distinct)
  }

  def addStack(): Stacks = {
    Stacks(maxStack, Stack(maxStack, Nil) :: stacks)
  }

  override def toString(): String = {
    return stacks.mkString("Stacks<" + maxStack + ">: {", " || ", "}")
  }

  def head: List[Item] = {
    var res: List[Item] = Nil
    for (stack <- stacks) {
      if (stack.size == 0)
        return Nil

      res = stack(0) :: Nil
    }

    return res
  }
}
