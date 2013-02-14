package sandbox.stackAnalysis.datastructure

import de.tud.cs.st.bat.resolved._


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
//TODO: implement one function for push and pop
case class Stack(maxStack: Int, itemStack: List[Item]) {
  require(itemStack.size <= maxStack)

  def size: Int = {
    itemStack.size
  }

  def apply(index: Int): Item = {
    itemStack(index)
  }

  def get(index: Int): Item = {
    var listIndex = 0
    var currentIndex = 0

    while (currentIndex < index) {
      currentIndex = currentIndex + 1
      listIndex = listIndex + itemStack(listIndex).size
    }

    return itemStack(listIndex)
  }

  def getSlot(index: Int): Item = {
    itemStack(index)
  }

  def push(t: Item): Stack = {
    if (t == null)
      return this
    if (size + t.size > maxStack)
      return this

    var res = itemStack

    for (i <- 1 until t.size) {
      res = Item.createContinue(t) :: res
    }

    res = t :: res
    return Stack(maxStack, res)
  }


  def push(t: Type, pc: Int): Stack = {
    push(Item.createItem(pc,ItemType.fromType(t)))
  }

  def pop(amount: Int): Stack = {
    if (itemStack.size < amount)
      return this

    var res = itemStack
    for (i <- 1 to amount) {
      res = res.tail
    }

    return Stack(maxStack, res)
  }

  def pop(): Stack = {
    if (itemStack == Nil)
      return this

    pop(itemStack.head.size)
  }


  def swap(): Stack = {
    if (size < 2)
      return this

    return Stack(maxStack, itemStack(1) :: itemStack(0) :: itemStack.drop(2))

  }

  def dup(amount: Int, offset: Int): Stack = {
    if (size + amount + offset > maxStack)
      return this

    val duplicate = itemStack.take(amount)

    return Stack(maxStack, duplicate ++ itemStack.slice(amount, amount + offset) ++ duplicate ++ itemStack.drop(amount + offset))
  }

  override def toString(): String = {
    return itemStack.mkString("[", "/", "]")
  }


}
