package sandbox.stackAnalysis

import de.tud.cs.st.bat.resolved._
import sandbox.stackAnalysis.TypeOption.ContinueType

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */

trait TypeOption {

  def max(other : TypeOption) : TypeOption
  def size : Int
  def getPC : Int

  def convertPC(newPC : Int) : TypeOption
  def isReference : Boolean
  def isType(t : Type) : Boolean
  def toType : Type

  def isType(tl : List[Type]) : Boolean =
    if(tl == Nil)
      false
    else if(isType(tl.head))
      true
    else
      isType(tl.tail)

}
object TypeOption {
  case class NullType(pc : Int) extends TypeOption {
    def max(other : TypeOption) : TypeOption = {
      if(other == NoneType)
        this
      else if (other.isInstanceOf[ContinueType])
        other.asInstanceOf[ContinueType].t
      else
        other
    }

    def getPC : Int = pc

    def size : Int = 1

    def convertPC(newPC : Int) : NullType = NullType(newPC)

    override def toString : String = "(null,"+pc+")"

    def isReference : Boolean = true

    def isType(t : Type) : Boolean = true

    def toType : Type = null
  }

  case object NoneType extends TypeOption {
    def max(other : TypeOption) : TypeOption = {
      other
    }

    def getPC : Int = -1

    def size : Int = 1

    def convertPC(newPC : Int) : NoneType.type = NoneType

    override def toString : String = "(none)"

    def isReference : Boolean = false

    def isType(t : Type) : Boolean = false

    def toType : Type = null
  }

  case class SomeType(t : Type, pc : Int) extends TypeOption {

    def max(other : TypeOption) : TypeOption = {
      this
    }

    def getPC : Int = pc

    def size : Int = t.computationalType.operandSize

    def convertPC(newPC : Int) : SomeType = SomeType(t,newPC)

    override def toString : String = "("+t.toJava+","+pc+")"

    def isReference : Boolean = t.isReferenceType

    def isType(t : Type) : Boolean = t.equals(this.t)

    def toType : Type = t
  }

  case class ContinueType(t : SomeType) extends TypeOption {
    def max(other : TypeOption) : TypeOption = {
      if(other == NoneType)
        this
      else if (other.isInstanceOf[NullType])
        t
      else
        other
    }

    def getPC : Int = t.getPC

    def size : Int = 1

    def convertPC(newPC : Int) : ContinueType = ContinueType(t.convertPC(newPC))

    def isReference : Boolean = false

    override def toString : String = "(Continue of " + t + ")"

    def isType(t : Type) : Boolean = this.t.isType(t)

    def toType : Type = t.toType
  }
}
