package sandbox.stackAnalysis.datastructure

import de.tud.cs.st.bat.resolved.Type


/**
 * An item holds all information that are stored on a single stack slot or in a local variable.

 *
 * @param declaredType The type of the item. The declared type always holds the most informative type information. If you want to check an item for the possibility of being null, use the corresponding flag instead.
 * @param pc The program counter where this item originates from. -1 if the item could originate from different sources or unknown.
 * @param flags This holds additional information about the item. Use the constants in the object Item to set the flags.
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

  def isReturnValue: Boolean = {
    isFlag(Item.FLAG_IS_RETURN_VALUE)
  }

  def isCreatedByNew: Boolean = {
    isFlag(Item.FLAG_IS_CREATED_BY_NEW)
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

  def apply(declaredType: Type, pc: Int, flags: Int): Item = {
    Item(ItemType.fromType(declaredType), pc, flags)
  }

  val FLAG_DEFAULT: Int = 0x00000000
  val FLAG_IS_PARAMETER: Int = 0x80000000
  val FLAG_COULD_BE_NULL: Int = 0x40000000
  val FLAG_IS_CONTINUE: Int = 0x20000000
  val FLAG_COULD_BE_ZERO: Int = 0x10000000
  val FLAG_IS_NOT_INITIALIZED: Int = 0x08000000
  val FLAG_IS_RETURN_VALUE: Int = 0x04000000
  val FLAG_IS_CREATED_BY_NEW: Int = 0x02000000


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
    if (itemTypeList == Nil)
      Item.createNoneItem
    else if (itemTypeList.size == 1)
      itemTypeList.head
    else
      combine(itemTypeList(0).combineWith(itemTypeList(1)) :: itemTypeList.drop(2))

  }
}
