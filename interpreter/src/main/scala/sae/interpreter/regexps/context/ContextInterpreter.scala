package sae.interpreter.regexps.context

import scala.collection.mutable
import idb.SetTable
import sae.interpreter.utils.MaterializedMap



object ContextInterpreter {

	var printUpdates = true

	trait RegExp
	case class Terminal(s: String) extends RegExp
	case class Alt(r1: RegExp, e2: RegExp) extends RegExp
	case class Asterisk(r1: RegExp) extends RegExp
	case class Sequence(r1: RegExp, r2: RegExp) extends RegExp

	type Exp = RegExp
	type Context = String
	type Value = Set[String]

	def interp(e : Exp, c : Context): Value = e match {
		case Terminal(s2) => if (c.startsWith(s2)) Set(c.substring(s2.length)) else Set()
		case Alt(r1, r2) => interp(r1, c) ++ interp(r2, c) //Could be written interp(r1, s) ++ interp(r2, s)
		case Asterisk(r) => Set(c) ++ interp(Sequence(r, Asterisk(r)), c)
		//case Asterisk(r) => interp(r, c) flatMap (c2 => interp(Asterisk(r), c2))
		case Sequence(r1, r2) => interp(r1, c) flatMap (s2 => interp(r2, s2))
	}

	trait RegExpKind
	case object TerminalKind extends RegExpKind
	case object AltKind extends RegExpKind
	case object AsteriskKind extends RegExpKind
	case object SequenceKind extends RegExpKind


	import idb.syntax.iql.IR.{Table, Relation}
	type ExpKind = RegExpKind
	type Key = Int
	type IExpTable = Table[(Key, Either[(ExpKind,Seq[Key]),Value])]
	type IExpKindMap = mutable.Map[Key, Either[(ExpKind,Seq[Key]),Value]]
	type IExpMap = mutable.Map[Key, Exp]
	type IExp = (IExpTable, IExpKindMap, IExpMap)
	type IValue = Relation[(Key, Value)]

	private var freshID = 0

	private def fresh() : Int = {
		freshID = freshID + 1
		freshID
	}

	def insertExp(e: Exp, c : Context, tab: IExp): Key = e match {
		//1. Build case distinction according to interp
		case l@Terminal(s2) => insertValue(e, interp(l,c), tab) //No recursive call => insertValue, convert literal to value using interp
		case Alt(r1, r2) => insertNode(e, AltKind, Seq(insertExp(r1, c, tab), insertExp(r2, c, tab)), tab) //Recursive call => insertNode, (r1, s) emerges from the definition of interp
		case Asterisk(r) => insertNode(e, AsteriskKind, Seq(insertExp(Sequence(r, Asterisk(r)), c, tab)), tab) //Use same parameter as the recursive call in interp
		case Sequence(r1,r2) => insertNode(e, SequenceKind, Seq(insertExp(r1, c, tab), insertExp(r2, c, tab)), tab) //Ignore other logic
	}

	def insertValue(e : Exp, v: Value, tab: IExp): Key = {
		val exp = Right(v)
		val id = fresh()
		tab._1 add (id, exp)
		tab._2.put(id, exp)
		tab._3.put(id, e)
		id
	}

	def insertNode(e : Exp, k: ExpKind, kids: Seq[Key], tab: IExp): Key = {
		val exp = Left(k, kids)
		val id = fresh()
		tab._1 add (id, exp)
		tab._2.put(id, exp)
		tab._3.put(id, e)
		id
	}

	def updateExp(oldKey : Key, newExp : Exp, c : Context, tab: IExp) : Key = {
		val oldValue = tab._2(oldKey)
		(newExp, oldValue) match {

			//I
			case (l@Terminal(s2), Right(v1)) if interp(l, c) == v1 => oldKey //Use interp to convert literal to value, use interp case distinction
			case (l@Terminal(s2), _ ) => updateValue(oldKey, newExp, interp(l, c), tab) //Use interp to convert literal to value

			//II
			case (Alt(r1, r2), Left((AltKind, seq))) => {
				updateExp(seq(0), r1, c, tab) //use expression in the recursive call
				updateExp(seq(1), r2, c, tab)
				oldKey
			}
			case (Alt(r1, r2), Left((_, seq))) if seq.size == 2 => {
				updateExp(seq(0), r1, c, tab)
				updateExp(seq(1), r2, c, tab)
				updateNode(oldKey, newExp, AltKind, seq, tab)
			}
			case (Alt(r1, r2),_) =>
				updateNode(oldKey, newExp, AltKind, Seq(insertExp(r1, c,tab), insertExp(r2, c,tab)), tab)

			//III
    		case (Asterisk(r), Left((AsteriskKind, seq))) => {
				updateExp(seq(0), Sequence(r, Asterisk(r)), c, tab)
				oldKey
			}
			case (Asterisk(r), Left((_, seq))) if seq.size == 1 => {
				updateExp(seq(0), Sequence(r, Asterisk(r)), c, tab)
				updateNode(oldKey, newExp, AsteriskKind, seq, tab)
			}
			case (Asterisk(r), _) => {
				updateNode(oldKey, newExp, AsteriskKind, Seq(insertExp(Sequence(r, Asterisk(r)), c, tab)), tab)
			}

			//IV
			case (Sequence(r1, r2), Left((SequenceKind, seq))) => {
				updateExp(seq(0), r1, c, tab) //use expression in the recursive call
				updateExp(seq(1), r2, c, tab)
				oldKey
			}
			case (Sequence(r1, r2), Left((_, seq))) if seq.size == 2 => {
				updateExp(seq(0), r1, c, tab)
				updateExp(seq(1), r2, c, tab)
				updateNode(oldKey, newExp, SequenceKind, seq, tab)
			}
			case (Sequence(r1, r2),_) =>
				updateNode(oldKey, newExp, SequenceKind, Seq(insertExp(r1, c, tab), insertExp(r2, c, tab)), tab)
		}
	}

	def updateValue(oldKey : Key, e : Exp, v : Value, tab : IExp): Key = {
		if (printUpdates) println("updateValue: oldKey = " + oldKey + ", v = " + v)
		val exp = Right(v)
		tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
		tab._2.put(oldKey, exp)
		tab._3.put(oldKey, e)
		oldKey
	}

	def updateNode(oldKey : Key, e : Exp, k : ExpKind, kids : Seq[Key], tab : IExp): Key = {
		if (printUpdates) println("updateNode: oldKey = " + oldKey + ", k = " + k + ", kids = " + kids)
		val exp = Left(k, kids)
		tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
		tab._2.put(oldKey, exp)
		tab._3.put(oldKey, e)
		oldKey
	}

	def getValues(c : Context, tab : IExp) : PartialFunction[Key, Value] with Iterable[(Key,Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		val result = new MaterializedMap[Key, Value]
		interpreter.values(c).addObserver(result)
		result
	}

	def createIExp : IExp = (SetTable.empty[(Key, Either[(ExpKind,Seq[Key]),Value])], mutable.HashMap.empty, mutable.HashMap.empty)

	private class IncrementalInterpreter(val tab : IExp) { //extends Interpreter[Key, ExpKind, Context, Value] {

		val expressions = tab._1

		def interpret(ref : Key, k : ExpKind, c : Context, s: Seq[Value]): Value = k match {
			case TerminalKind => s(0) //Literal => just return the first value of the sequence
			case AltKind => s(0) ++ s(1) //Non-literal => like interp but substitute calls to interp with the values of the sequence
			case AsteriskKind => Set(c) ++ s(0)
			case SequenceKind => {
				s(0) flatMap (s2 => {
					val newTab = createIExp
					val newVals = getValues(s2, newTab)
					val ref2 = insertExp(tab._3(ref), s2, newTab)
					newVals(ref2)
				})
			}
		}

		import idb.syntax.iql.IR._
		import idb.syntax.iql._

		type IDef = (Key, Either[(ExpKind,Seq[Key]),Value])
		type IValue = (Key, Value)
		type IComp = (Key, ExpKind, Seq[Key])

		//def interpret(ref : Key, k : ExpKind, c : Context, values : Seq[Value]) : Value

		protected val definitionAsValue : Rep[IDef => IValue] = staticData (
			(d : IDef) => (d._1, d._2.right.get)
		)

		protected val definitionAsComposite : Rep[IDef => IComp] = staticData (
			(d : IDef) => {
				val e : (ExpKind, Seq[Key]) = d._2.left.get
				(d._1,e._1,e._2)
			}
		)

		protected val interpretPriv : Rep[((Key, ExpKind, Context, Seq[Value])) => Value] = staticData (
			(t : (Key, ExpKind, Context, Seq[Value])) => interpret(t._1, t._2, t._3, t._4)
		)

		protected val definitionIsLiteral : Rep[IDef => Boolean] = staticData (
			(s : IDef) => s._2.isRight
		)

		//val expressions : Table[IDef]

		protected val literals : Relation[(Key, Value)] =
			SELECT (definitionAsValue(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsLiteral(d))

		protected val nonLiterals : Relation[(Key, ExpKind, Seq[Key])] =
			SELECT (definitionAsComposite(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => NOT (definitionIsLiteral(d)))

		protected val nonLiteralsOneArgument : Relation[IComp] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[IComp]) => d._3.length == 1)

		protected val nonLiteralsTwoArguments : Relation[IComp] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[IComp]) => d._3.length == 2)

		protected val nonLiteralsThreeArguments : Relation[IComp] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[IComp]) => d._3.length == 3)

		def values(c : Context) : Relation[IValue] = {
			val context : Rep[Context] = staticData(c)
			WITH RECURSIVE (
				(vQuery : Rep[Query[IValue]]) => {
					literals UNION ALL (
						queryToInfixOps (
							SELECT (
								(d  : Rep[IComp], v1 : Rep[IValue], v2 : Rep[IValue]) =>
									(d._1, interpretPriv (d._1, d._2, context, Seq(v1._2, v2._2)))
							) FROM (
								nonLiteralsTwoArguments, vQuery, vQuery
								) WHERE (
								(d  : Rep[IComp], v1 : Rep[IValue], v2 : Rep[IValue]) =>
									(d._3(0) == v1._1) AND
										(d._3(1) == v2._1)
								)
						) UNION ALL (
							queryToInfixOps (
								SELECT (
									(d  : Rep[IComp], v1 : Rep[IValue], v2 : Rep[IValue], v3 : Rep[IValue]) =>
										(d._1, interpretPriv (d._1, d._2, context, Seq(v1._2, v2._2, v3._2)))
								) FROM (
									nonLiteralsThreeArguments, vQuery, vQuery, vQuery
									) WHERE (
									(d  : Rep[IComp], v1 : Rep[IValue], v2 : Rep[IValue], v3 : Rep[IValue]) =>
										(d._3(0) == v1._1) AND
											(d._3(1) == v2._1) AND
											(d._3(2) == v3._1)
									)
							) UNION ALL (
								SELECT (
									(d  : Rep[IComp], v1 : Rep[IValue]) =>
										(d._1, interpretPriv (d._1, d._2, context, Seq(v1._2)))
								) FROM (
									nonLiteralsOneArgument, vQuery
									) WHERE (
									(d  : Rep[IComp], v1 : Rep[IValue]) =>
										d._3(0) == v1._1
									)
							)
						)
					)
				}
				)
		}
	}


	def main(args : Array[String]) {
		val tab : IExp = createIExp

		val exp1 = Alt(Terminal("a"), Terminal("b"))
		val exp2 =
			Sequence(
				Sequence(
					Terminal("a"),
					Terminal("b")
				),
				Alt(
					Terminal("c"),
					Terminal("a")
				)
			)

		val exp3 =
				Alt(
					Terminal("b"),
					Terminal("a")
				)

		val exp4 =
			Sequence(
				Terminal("b"),
				Sequence(
					Terminal("a"),
					Terminal("b")
				)
			)

		val s = "babac"

      	val values = getValues(s, tab)

		var ref1 = insertExp(exp1, s, tab)

		println("Before update: " + values(ref1) + "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)

		ref1 = updateExp(ref1, exp4, s, tab)

		println("After update: " + values(ref1) + "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)
	}
}