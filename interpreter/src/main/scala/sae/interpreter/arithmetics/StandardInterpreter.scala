
import idb.syntax.iql.IR.Table
import scala.collection.mutable

object StandardInterpreter {

  abstract class Exp
  case class Num(n: Int) extends Exp
  case class Add(e1: Exp, e2: Exp) extends Exp
  case class Sub(e1: Exp, e2: Exp) extends Exp

  type Value = Int

  def interp(e: Exp): Value = e match {
    case Num(n) => n
    case Add(e1, e2) => interp(e1) + interp(e2)
    case Sub(e1, e2) => interp(e1) - interp(e2)
  }

  abstract class ExpKind
  case object NumKind extends ExpKind
  case object AddKind extends ExpKind
  case object SubKind extends ExpKind

  type Key = Int
  type IExpTable = Table[(Key, Either[(ExpKind,Seq[Key]),Value])]
  type IExpMap = mutable.Map[Key, Either[(ExpKind,Seq[Key]),Value]]
  type IExp = (IExpTable, IExpMap)
  type IValue = Table[(Key, Value)]

  private var freshID = 0

  private def fresh() : Int = {
    freshID = freshID + 1
    freshID
  }

  def insertExp(e: Exp, tab: IExp): Key = e match {
    case Num(n) => insertValue(n, tab)
    case Add(e1, e2) => insertNode(AddKind, Seq(insertExp(e1, tab), insertExp(e2, tab)), tab)
    case Sub(e1, e2) => insertNode(SubKind, Seq(insertExp(e1, tab), insertExp(e2, tab)), tab)
  }

  def insertValue(v: Value, tab: IExp): Key = {
    val exp = Right(v)
    val id = fresh()
    tab._1 add (id, exp)
    tab._2 += ((id, exp))
    id
  }

  def insertNode(k: ExpKind, kids: Seq[Key], tab: IExp): Key = {
    val exp = Left(k, kids)
    val id = fresh()
    tab._1 add (id, exp)
    tab._2 += ((id, exp))
    id
  }


  def updateExp(old: Key, e: Exp, tab: IExp): Key = {
    tab._2(old) match {
      case Left((kind, kids)) => -1
      case oldV@Right(_) => {
        if (e.isInstanceOf[Num])
          tab._1.update((old, oldV), (old, Right(e.asInstanceOf[Num].n)))
        else
          null
        old
      }
    }
  }

}