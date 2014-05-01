package sae.interpreter.regexps.Function

import sae.interpreter.Interpreter
import scala.collection.mutable
import idb.SetTable
import sae.interpreter.utils.MaterializedMap

object FunctionInterpreter {

	var printUpdates = true

	trait RegExp
	case class Terminal(s: String) extends RegExp
	case class Alt(r1: RegExp, e2: RegExp) extends RegExp
	case class Asterisk(r1: RegExp) extends RegExp
	case class Sequence(r1: RegExp, r2: RegExp) extends RegExp



	def interp(e : RegExp, s : String): Set[String] = e match {
		case Terminal(s2) => if (s.startsWith(s2)) Set(s.substring(s2.length)) else Set()
		case Alt(r1, r2) => interp(r1, s) ++ interp(r2, s)
		case Asterisk(r) => Set(s) ++ interp(Sequence(r, Asterisk(r)), s)
		case Sequence(r1, r2) => interp(r1, s) flatMap (s2 => interp(r2, s2))
	}


	type Exp = RegExp
	type Value = (String => Set[String])



	trait RegExpKind
	case object TerminalKind extends RegExpKind
	case object AltKind extends RegExpKind
	case object AsteriskKind extends RegExpKind
	case object SequenceKind extends RegExpKind

	type ExpKind = RegExpKind

	//1. create interpreter that has a function as value
	private def interpPrivate(e : Exp) : Value = e match {
		case Terminal(s2) =>
			(s : String) => if (s.startsWith(s2)) Set(s.substring(s2.length)) else Set()
		case Alt(r1, r2) =>
			(s : String) => interpPrivate(r1)(s) ++ interpPrivate(r2)(s)
		case Asterisk(r) =>
			(s : String) => Set(s) ++ interpPrivate(Sequence(r, Asterisk(r)))(s)
		case Sequence(r1, r2) =>
			(s : String) => interpPrivate(r1)(s) flatMap (s2 => interpPrivate(r2)(s2))
	}

	private val interpMap : mutable.Map[ExpKind, Exp => Value] = mutable.HashMap.empty[ExpKind, Exp => Value]

	interpMap.put(TerminalKind,	(e : Exp) => (s : String) => e match {case Terminal(s2) => if (s.startsWith(s2)) Set(s.substring(s2.length)) else Set()})

	import idb.syntax.iql.IR.{Table, Relation}
	type Key = Int
	type IExpTable = Table[(Key, Either[(ExpKind,Seq[Key]),Value])]
	type IExpMap = mutable.Map[Key, Either[(ExpKind,Seq[Key]),Value]]
	type IExp = (IExpTable, IExpMap)
	type IValue = Relation[(Key, Value)]

	private var freshID = 0

	private def fresh() : Int = {
		freshID = freshID + 1
		freshID
	}

	//1. Build insterExp according to interp
	def insertExp(e: Exp, tab: IExp): Key = e match {
		case l@Terminal(s2) => insertValue(interpPrivate(l), tab) //No recursive call => insertValue, convert literal to value using interp
		case Alt(r1, r2) => insertNode(AltKind, Seq(insertExp(r1, tab), insertExp(r2, tab)), tab) //Recursive call => insertNode, (r1, s) emerges from the definition of interp
		case Asterisk(r) => insertNode(AsteriskKind, Seq(insertExp(Sequence(r, Asterisk(r)), tab)), tab) //Use same parameter as the recursive call in interp
		case Sequence(r1,r2) => insertNode(SequenceKind, Seq(insertExp(r1, tab), insertExp(r2, tab)), tab) //Ignore other logic
	}

	def insertValue(v: Value, tab: IExp): Key = {
		val exp = Right(v)
		val id = fresh()
		tab._1 add (id, exp)
		tab._2.put(id, exp)
		id
	}

	def insertNode(k: ExpKind, kids: Seq[Key], tab: IExp): Key = {
		val exp = Left(k, kids)
		val id = fresh()
		tab._1 add (id, exp)
		tab._2.put(id, exp)
		id
	}

	def updateExp(oldKey : Key, newExp : Exp, tab: IExp) : Key = {
		val oldValue = tab._2(oldKey)
		(newExp, oldValue) match {

			//I
			//TODO function == function does not work
			case (l@Terminal(s2), Right(v1)) if interpPrivate(l) == v1 => oldKey //Use interp to convert literal to value, use interp case distinction
			case (l@Terminal(s2), _ ) => updateValue(oldKey, interpPrivate(l), tab) //Use interp to convert literal to value

			//II
			case (Alt(r1, r2), Left((AltKind, seq))) => {
				updateExp(seq(0), r1, tab) //use expression in the recursive call
				updateExp(seq(1), r2, tab)
				oldKey
			}
			case (Alt(r1, r2), Left((_, seq))) if seq.size == 2 => {
				updateExp(seq(0), r1, tab)
				updateExp(seq(1), r2, tab)
				updateNode(oldKey, AltKind, seq, tab)
			}
			case (Alt(r1, r2),_) =>
				updateNode(oldKey, AltKind, Seq(insertExp(r1,tab), insertExp(r2,tab)), tab)

			//III
    		case (Asterisk(r), Left((AsteriskKind, seq))) => {
				updateExp(seq(0), Sequence(r, Asterisk(r)), tab)
				oldKey
			}
			case (Asterisk(r), Left((_, seq))) if seq.size == 1 => {
				updateExp(seq(0), Sequence(r, Asterisk(r)), tab)
				updateNode(oldKey, AsteriskKind, seq, tab)
			}
			case (Asterisk(r), _) => {
				updateNode(oldKey, AsteriskKind, Seq(insertExp(Sequence(r, Asterisk(r)),tab)), tab)
			}

			//IV
			case (Sequence(r1, r2), Left((SequenceKind, seq))) => {
				updateExp(seq(0), r1, tab) //use expression in the recursive call
				updateExp(seq(1), r2, tab)
				oldKey
			}
			case (Sequence(r1, r2), Left((_, seq))) if seq.size == 2 => {
				updateExp(seq(0), r1, tab)
				updateExp(seq(1), r2, tab)
				updateNode(oldKey, SequenceKind, seq, tab)
			}
			case (Sequence(r1, r2),_) =>
				updateNode(oldKey, SequenceKind, Seq(insertExp(r1,tab), insertExp(r2,tab)), tab)
		}
	}

	def updateValue(oldKey : Key, v : Value, tab : IExp): Key = {
		if (printUpdates) println("updateValue: oldKey = " + oldKey + ", v = " + v)
		val exp = Right(v)
		tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
		tab._2.put(oldKey, exp)
		oldKey
	}

	def updateNode(oldKey : Key, k : ExpKind, kids : Seq[Key], tab : IExp): Key = {
		if (printUpdates) println("updateNode: oldKey = " + oldKey + ", k = " + k + ", kids = " + kids)
		val exp = Left(k, kids)
		tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
		tab._2.put(oldKey, exp)
		oldKey
	}

	def getValues(tab : IExp) : PartialFunction[Key, Value] with Iterable[(Key,Value)] = {
		val interpreter = new IncrementalInterpreter(tab._1)
		val result = new MaterializedMap[Key, Value]
		interpreter.values(null).addObserver(result)
		result
	}

	def createIExp : IExp = (SetTable.empty[(Key, Either[(ExpKind,Seq[Key]),Value])], mutable.HashMap.empty)

	private class IncrementalInterpreter(override val expressions : IExpTable) extends Interpreter[Key, ExpKind, Null, Value] {
		override def interpret(e: ExpKind, c : Null, seq: Seq[Value]): Value = e match {
			case TerminalKind =>
				(s : String) => seq(0)(s) //Literal => just return the first value of the sequence
			case AltKind =>
				(s : String) => seq(0)(s) ++ seq(1)(s) //Non-literal => like interp but substitute calls to interp with the values of the sequence
			case AsteriskKind =>
				(s : String) => Set(s) ++ seq(0)(s)
			case SequenceKind =>
				(s : String) => seq(0)(s) flatMap (s2 => seq(1)(s))
		}
	}


	def main(args : Array[String]) {
		val tab : IExp = createIExp

		val exp1 = Alt(Terminal("a"), Terminal("b"))
		val exp2 = Sequence(Terminal("a"), Terminal("b"))

		val values = getValues(tab)

		var ref1 = insertExp(exp1, tab)

		val s = "a"

		println("Before update: " + values(ref1)(s) + "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)

		ref1 = updateExp(ref1, exp2, tab)

		println("After update: " + values(ref1)(s) + "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)
	}
}