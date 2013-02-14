package sandbox.stackAnalysis.datastructure

import de.tud.cs.st.bat.resolved.Type
import sae.operators.Combinable


/**
 * An item holds all information that are stored on a single stack slot or in a local variable.

 *
 * @param itemType The type of the item. The declared type always holds the most informative type information. If you want to check an item for the possibility of being null, use the corresponding flag instead.
 * @param pc The program counter where this item originates from. -1 if the item could originate from different sources or unknown.
 * @param flags This holds additional information about the item. Use the constants in the object Item to set the flags.
 */
case class Item(pc: Int, itemType: ItemType, flags: Int) extends Combinable[Item] {

  private var fieldName: Option[String] = None

  def this(pc: Int, declaredType: ItemType) = {
    this( pc, declaredType, 0x00000000)
  }

  def this(pc: Int, declaredType: ItemType, flags: Int, fromField: String) = {
    this( pc, declaredType, flags)
    fieldName = Some(fromField)
  }

  def convertTo(newType: ItemType): Item = {
    return Item( pc, newType, flags)
  }

  def size: Int = {
    itemType.getSize
  }

  def getFieldName: String = {
    fieldName match {
      case Some(x) => x
      case None => ""
    }
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

  def isFromField: Boolean = {
    isFlag(Item.FLAG_ORIGINATES_FROM_FIELD)
  }

  private def isFlag(flag: Int): Boolean = {
    (flags & flag) != 0
  }

  def getItemType: ItemType = {
    itemType
  }

  def getPC: Int = {
    pc
  }

  def getFlags: Int = {
    flags
  }

  def upperBound(other: Item): Item = {
    if (other == null)
      return this

    val resItemType = ItemType.upperBound(itemType, other.itemType)
    val resPC = if (pc == other.pc) pc else -1
    val resFlags = flags | other.flags

    return Item(resPC, resItemType, resFlags)
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
    return "(" + itemType + ", " + pc + ", " + Integer.toHexString(flags) + ")"
  }


}

object Item {

  def apply(pc: Int, declaredType: Type, flags: Int): Item = {
    Item( pc, ItemType.fromType(declaredType), flags)
  }

  val FLAG_DEFAULT: Int = 0x00000000
  val FLAG_IS_PARAMETER: Int = 0x80000000
  val FLAG_COULD_BE_NULL: Int = 0x40000000
  val FLAG_IS_CONTINUE: Int = 0x20000000
  val FLAG_COULD_BE_ZERO: Int = 0x10000000
  val FLAG_IS_NOT_INITIALIZED: Int = 0x08000000
  val FLAG_IS_RETURN_VALUE: Int = 0x04000000
  val FLAG_IS_CREATED_BY_NEW: Int = 0x02000000
  val FLAG_ORIGINATES_FROM_FIELD: Int = 0x01000000


  def createNullItem(pc: Int): Item = {
    Item( pc, ItemType.Null, FLAG_COULD_BE_NULL)
  }

  def createParameterItem(declaredType: ItemType): Item = {
    if (declaredType.isUpperBoundOf(ItemType.Null))
      Item( -1, declaredType, FLAG_IS_PARAMETER | FLAG_COULD_BE_NULL)
    else
      Item( -1, declaredType, FLAG_IS_PARAMETER)
  }

  def createItem(pc: Int, declaredType: ItemType): Item = {
    new Item( pc, declaredType)
  }

  def createItem(pc: Int, declaredType: ItemType, value: Any): Item = {
    if (value == null) {
      return return Item( pc, declaredType, FLAG_COULD_BE_NULL)
    } else if (value == 0) {
      return Item( pc, declaredType, FLAG_COULD_BE_ZERO)
    } else {
      return new Item( pc , declaredType)
    }
  }

  def createContinue(continued: Item): Item = {
    Item( continued.getPC, continued.getItemType, continued.getFlags | FLAG_IS_CONTINUE)
  }

  def createNoneItem: Item = {
    new Item(-1, ItemType.None)
  }

  def upperBound(itemTypeList: List[Item]): Item = {
    if (itemTypeList == Nil)
      Item.createNoneItem
    else if (itemTypeList.size == 1)
      itemTypeList.head
    else
      upperBound(itemTypeList(0).upperBound(itemTypeList(1)) :: itemTypeList.drop(2))

  }
}
