package sandbox.stackAnalysis.datastructure


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */

/*
  flags: 0 = isParameter, 1 = couldBeNull, 2 = isContinue, 3 = couldBeZero , 4 = isNotInitialized
 */
case class Item(declaredType: ItemType, pc: Int, flags: Int) {

  def this(declaredType: ItemType, pc: Int) = {
    this(declaredType, pc, 0x00000000)
  }

  def convertTo(newType: ItemType): Item = {
    return Item(newType, pc, flags)
  }

  def size: Int = {
    declaredType.getSize
  }

  def isCouldBeNull: Boolean = {
    isFlag(Item.FLAG_COULD_BE_NULL)
  }

  def isParameter: Boolean = {
    isFlag(Item.FLAG_IS_PARAMETER)
  }

  def isCouldBeZero: Boolean = {
    isFlag(Item.FLAG_COULD_BE_ZERO)
  }

  private def isFlag(flag: Int): Boolean = {
    (flags & flag) != 0
  }

  def getDeclaredType: ItemType = {
    declaredType
  }

  def getPC: Int = {
    pc
  }

  def getFlags: Int = {
    flags
  }

  def combineWith(other: Item): Item = {
    if (other == null)
      return this

    val resItemType = ItemType.upperBound(declaredType, other.declaredType)
    val resPC = if (pc == other.pc) pc else -1
    val resFlags = flags | other.flags

    return Item(resItemType, resPC, resFlags)
  }


  /* override def equals(other: Any): Boolean = {
if (other == null)
  return false
else if (!other.isInstanceOf[Item])
  return false

val otherItem = other.asInstanceOf[Item]

return (getDeclaredType.equals(otherItem.getDeclaredType) && pc.equals(otherItem.pc))
}    */

  override def toString(): String = {
    return "(" + declaredType + ", " + pc + ", " + Integer.toHexString(flags) + ")"
  }


}

object Item {

  val FLAG_DEFAULT: Int = 0x00000000
  val FLAG_IS_PARAMETER: Int = 0x80000000
  val FLAG_COULD_BE_NULL: Int = 0x40000000
  val FLAG_IS_CONTINUE: Int = 0x20000000
  val FLAG_COULD_BE_ZERO: Int = 0x10000000
  val FLAG_IS_NOT_INITIALIZED: Int = 0x08000000


  def createNullItem(pc: Int): Item = {
    Item(ItemType.Null, pc, FLAG_COULD_BE_NULL)
  }

  def createParameterItem(declaredType: ItemType): Item = {
    if (declaredType.isUpperBoundOf(ItemType.Null))
      Item(declaredType, -1, FLAG_IS_PARAMETER | FLAG_COULD_BE_NULL)
    else
      Item(declaredType, -1, FLAG_IS_PARAMETER)
  }

  def createItem(declaredType: ItemType, pc: Int): Item = {
    new Item(declaredType, pc)
  }

  def createItem(declaredType: ItemType, pc: Int, value: Any): Item = {
    if (value == null) {
      return return Item(declaredType, pc, FLAG_COULD_BE_NULL)
    } else if (value == 0) {
      return Item(declaredType, pc, FLAG_COULD_BE_ZERO)
    } else {
      return new Item(declaredType, pc)
    }
  }

  def createContinue(continued: Item): Item = {
    Item(continued.getDeclaredType, continued.getPC, continued.getFlags | FLAG_IS_CONTINUE)
  }

  def createNoneItem: Item = {
    new Item(ItemType.None, -1)
  }

  def combine(itemTypeList: List[Item]): Item = {
    if (itemTypeList.size == 1)
      itemTypeList.head
    else
      itemTypeList.fold[Item](createItem(ItemType.None, -1))((a, b) => a.combineWith(b))
  }
}
