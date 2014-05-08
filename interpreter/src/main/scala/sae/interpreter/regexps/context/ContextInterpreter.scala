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

  Sequence(Terminal("aa"), Terminal("bb"))
  // 4 matches "aaa", "aa", "a", ""

	def interp(e : Exp, c : Context): Value = e match {
		case Terminal(s2) => if (c.startsWith(s2)) Set(c.substring(s2.length)) else Set()
		case Alt(r1, r2) => interp(r1, c) ++ interp(r2, c)
		case Asterisk(r) => Set(c) ++ interp(Sequence(r, Asterisk(r)), c)
		case Sequence(r1, r2) => interp(r1, c) flatMap (s2 => interp(r2, s2))
	}

  def interpk[T](e : Exp, c : Context, k: Value => T): T = e match {
    case Terminal(s2) => if (c.startsWith(s2)) k(Set(c.substring(s2.length))) else k(Set())
    case Alt(r1, r2) => interpk(r1, c, res1 => interpk(r2, c, res2 => k(res1 ++ res2)))
    case Asterisk(r) => interpk(Sequence(r, Asterisk(r)), c, res => k(Set(c) ++ res))
    case Sequence(r1, r2) => interpk(r1, c, res =>
                              mapK[String, Value, T]
                                   (res.toList)
                                   (s2 => k => interpk(r2, s2, k))
                                   ((l: List[Value]) => k(l.toSet.flatten)))
  }

  def mapK[T,U,W](l: List[T])(f: T => (U => W) => W)(k: List[U] => W): W = l match {
    case Nil => k(Nil)
    case x::xs => f(x)(u => mapK(xs)(f)(us => k(u::us)))
  }


  // Asterisk(Terminal("a"))

	trait RegExpKind
	case object TerminalKind extends RegExpKind
	case object AltKind extends RegExpKind
	case object AsteriskKind extends RegExpKind
	case object SequenceKind extends RegExpKind



	import idb.syntax.iql.IR.{Table, Relation}
	type ExpKind = RegExpKind
	type Key = Int
	type Node = (ExpKind, Option[Context], Seq[Key])
	type Lit = (ExpKind, Option[Context], Seq[Any])

	type IExpTable = Table[(Key, Either[Node, Lit])]
	type IExpKindMap = mutable.Map[Key, Either[Node, Lit]]
	type IExpMap = mutable.Map[Key, Exp]
	type IExp = (IExpTable, IExpKindMap, IExpMap)
	type IValue = Relation[(Key, Value)]

	private var freshID = 0

	private def fresh() : Int = {
		freshID = freshID + 1
		freshID
	}

	def insertExp(e : Exp, c : Context, tab : IExp) : Key = insertExp(e, Some(c), tab)

	def insertExp(e: Exp, c : Option[Context], tab: IExp): Key = e match {
		//1. Build case distinction according to interp
		case l@Terminal(s2) => insertLiteral(e, TerminalKind, c, Seq(s2), tab) //No recursive call => insertLiteral, param is the sequence of parameters of the literal
		case Alt(r1, r2) => insertNode(e, AltKind, c, Seq(insertExp(r1, c, tab), insertExp(r2, c, tab)), tab) //Recursive call => insertNode, (r1, s) emerges from the definition of interp
		case Asterisk(r) => insertNode(e, AsteriskKind, c, Seq(insertExp(Sequence(r, Asterisk(r)), c, tab)), tab) //Use same parameter as the recursive call in interp
		case Sequence(r1,r2) => insertNode(e, SequenceKind, c, Seq(insertExp(r1, c, tab), insertExp(r2, None, tab)), tab) //Use none if the context differs from the context in the parameter
	}

	def insertLiteral(e : Exp, k: ExpKind, c : Option[Context], param: Seq[Any], tab: IExp): Key = {
		val exp = Right(k, c, param)
		val id = fresh()
		tab._1 add (id, exp)
		tab._2.put(id, exp)
		tab._3.put(id, e)
		id
	}

	def insertNode(e : Exp, k: ExpKind, c : Option[Context], kids: Seq[Key], tab: IExp): Key = {
		val exp = Left(k, c, kids)
		val id = fresh()
		tab._1.add(id, exp)
		tab._2.put(id, exp)
		tab._3.put(id, e)
		id
	}

	def updateExp(oldKey : Key, newExp : Exp, newC : Context, tab : IExp) : Key = updateExp(oldKey, newExp, Some(newC), tab)

	def updateExp(oldKey : Key, newExp : Exp, newC : Option[Context], tab: IExp) : Key = {
		val oldValue = tab._2(oldKey)
		(newExp, oldValue) match {

			//I
			case (l@Terminal(s2), Right(v1)) if v1._1 == TerminalKind && v1._2 == newC && v1._3 == Seq(s2) => oldKey
			case (l@Terminal(s2), _ ) => updateLiteral(oldKey, newExp, TerminalKind, newC, Seq(s2), tab)

			//II
			case (Alt(r1, r2), Left((AltKind, `newC`, seq))) => {
				updateExp(seq(0), r1, newC, tab) //use expression in the recursive call
				updateExp(seq(1), r2, newC, tab)
				oldKey
			}
			case (Alt(r1, r2), Left((_, c, seq))) if seq.size == 2 => {
				updateExp(seq(0), r1, newC, tab)
				updateExp(seq(1), r2, newC, tab)
				updateNode(oldKey, newExp, AltKind, newC, seq, tab)
			}
			case (Alt(r1, r2),_) =>
				updateNode(oldKey, newExp, AltKind, newC, Seq(insertExp(r1, newC, tab), insertExp(r2, newC, tab)), tab)

			//III
    		case (Asterisk(r), Left((AsteriskKind, `newC`, seq))) => {
				updateExp(seq(0), Sequence(r, Asterisk(r)), newC, tab)
				oldKey
			}
			case (Asterisk(r), Left((_, c, seq))) if seq.size == 1 => {
				updateExp(seq(0), Sequence(r, Asterisk(r)), newC, tab)
				updateNode(oldKey, newExp, AsteriskKind, newC, seq, tab)
			}
			case (Asterisk(r), _) => {
				updateNode(oldKey, newExp, AsteriskKind, newC, Seq(insertExp(Sequence(r, Asterisk(r)), newC, tab)), tab)
			}

			//IV
			case (Sequence(r1, r2), Left((SequenceKind, `newC`, seq))) => {
				updateExp(seq(0), r1, newC, tab) //use expression in the recursive call
				updateExp(seq(1), r2, newC, tab)
				oldKey
			}
			case (Sequence(r1, r2), Left((_, c, seq))) if seq.size == 2 => {
				updateExp(seq(0), r1, newC, tab)
				updateExp(seq(1), r2, newC, tab)
				updateNode(oldKey, newExp, SequenceKind, newC, seq, tab)
			}
			case (Sequence(r1, r2),_) =>
				updateNode(oldKey, newExp, SequenceKind, newC, Seq(insertExp(r1, newC, tab), insertExp(r2, newC, tab)), tab)
		}
	}

	def updateLiteral(oldKey : Key, e : Exp, k : ExpKind, c : Option[Context], param : Seq[Any], tab : IExp): Key = {
		if (printUpdates) println("updateLiteral: oldKey = " + oldKey + ", k = " + k + ", param = " + param)
		val exp = Right(k, c, param)
		tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
		tab._2.put(oldKey, exp)
		tab._3.put(oldKey, e)
		oldKey
	}

	def updateNode(oldKey : Key, e : Exp, k : ExpKind, c : Option[Context], kids : Seq[Key], tab : IExp): Key = {
		if (printUpdates) println("updateNode: oldKey = " + oldKey + ", k = " + k + ", kids = " + kids)
		val exp = Left(k, c, kids)
		tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
		tab._2.put(oldKey, exp)
		tab._3.put(oldKey, e)
		oldKey
	}

	def getValues(tab : IExp) : PartialFunction[Key, Value] with Iterable[(Key,Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def createIExp : IExp = (SetTable.empty, mutable.HashMap.empty, mutable.HashMap.empty)

	private class IncrementalInterpreter(val tab : IExp) { //extends Interpreter[Key, ExpKind, Context, Value] {

		val expressions : Relation[IDef] = tab._1

		def interpretLiteral(ref : Key, k : ExpKind, c : Context, s : Seq[Any]): Value = k match {
			case TerminalKind =>  {
				val s0 = s(0).asInstanceOf[String] //Cast parameters of the terminal accordingly
				if (c.startsWith(s0)) Set(c.substring(s0.length)) else Set()
			}
		}

		def interpretNode(ref : Key, k : ExpKind, c : Context, s: Seq[Value]): Value = k match {
			case AltKind => s(0) ++ s(1) //Non-literal => like interp but substitute calls to interp with the values of the sequence
			case AsteriskKind => Set(c) ++ s(0)
			case SequenceKind => {
				s(0) flatMap (s2 => result(insertExp(tab._3(ref), Some(s2), tab)))
			}
		}

		import idb.syntax.iql.IR._
		import idb.syntax.iql._

		type Node = (ExpKind, Option[Context], Seq[Key])
		type Lit =  (ExpKind, Option[Context], Seq[Any])

		type IDef = (Key, Either[Node,Lit])
		type INode = (Key, Node)
		type ILit = (Key, Lit)

		type IValue = (Key, Value)


		//def interpret(ref : Key, k : ExpKind, c : Context, values : Seq[Value]) : Value

		protected val iDefAsILit : Rep[IDef => ILit] = staticData (
			(d : IDef) => {
				val e = d._2.right.get
				(d._1, (e._1, e._2, e._3))
			}
		)

		protected val iDefAsINode : Rep[IDef => INode] = staticData (
			(d : IDef) => {
				val e = d._2.left.get
				(d._1, (e._1, e._2, e._3))
			}
		)

		protected val interpretILit : Rep[ILit => Value] = staticData (
			(t : ILit) => interpretLiteral(t._1, t._2._1, t._2._2.get, t._2._3)
		)

		protected val interpretPriv : Rep[((Key, ExpKind, Context, Seq[Value])) => Value] = staticData (
			(t : (Key, ExpKind, Context, Seq[Value])) => interpretNode(t._1, t._2, t._3, t._4)
		)

		protected val definitionIsLiteral : Rep[IDef => Boolean] = staticData (
			(s : IDef) => s._2.isRight && s._2.right.get._2.isDefined
		)

		protected val definitionIsNode : Rep[IDef => Boolean] = staticData (
			(s : IDef) => s._2.isLeft
		)

		protected val children : Rep[INode => Seq[Key]] = staticData (
			(t : INode) => t._2._3
		)


		//val expressions : Table[IDef]

		protected val literals : Relation[IValue] =
			SELECT ((d : Rep[IDef]) => (d._1, interpretILit(iDefAsILit(d)))) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsLiteral(d))

		protected val nonLiterals : Relation[INode] =
			SELECT (iDefAsINode(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsNode(d))

		protected val nonLiteralsOneArgument : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 1)

		protected val nonLiteralsTwoArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 2)

		protected val nonLiteralsThreeArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 3)

		val values : Relation[IValue] = {
			WITH RECURSIVE (
				(vQuery : Rep[Query[IValue]]) => {
					literals UNION ALL (
						queryToInfixOps (
							SELECT (
								(d  : Rep[INode], v1 : Rep[IValue], v2 : Rep[IValue]) =>
									(d._1, interpretPriv (d._1, d._2._1, d._2._2.get, Seq(v1._2, v2._2)))
							) FROM (
								nonLiteralsTwoArguments, vQuery, vQuery
								) WHERE (
								(d  : Rep[INode], v1 : Rep[IValue], v2 : Rep[IValue]) =>
									(children(d)(0) == v1._1) AND
									(children(d)(1) == v2._1) AND
									d._2._2.isDefined
								)
						) UNION ALL (
							queryToInfixOps (
								SELECT (
									(d  : Rep[INode], v1 : Rep[IValue], v2 : Rep[IValue], v3 : Rep[IValue]) =>
										(d._1, interpretPriv (d._1, d._2._1, d._2._2.get, Seq(v1._2, v2._2, v3._2)))
								) FROM (
									nonLiteralsThreeArguments, vQuery, vQuery, vQuery
									) WHERE (
									(d  : Rep[INode], v1 : Rep[IValue], v2 : Rep[IValue], v3 : Rep[IValue]) =>
										(children(d)(0) == v1._1) AND
										(children(d)(1) == v2._1) AND
										(children(d)(2) == v3._1) AND
										d._2._2.isDefined
									)
							) UNION ALL (
								SELECT (
									(d  : Rep[INode], v1 : Rep[IValue]) =>
										(d._1, interpretPriv (d._1, d._2._1, d._2._2.get, Seq(v1._2)))
								) FROM (
									nonLiteralsOneArgument, vQuery
									) WHERE (
									(d  : Rep[INode], v1 : Rep[IValue]) =>
										children(d)(0) == v1._1 AND
										d._2._2.isDefined
									)
							)
						)
					)
				}
			)
		}

		val result = new MaterializedMap[Key, Value]
		values.addObserver(result)
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
				Terminal("a")
			)

		val s = "babac"

      	val values = getValues(tab)

		var ref1 = insertExp(exp1, s, tab)

		println("Before update: "
		//	+ values(ref1)
			+ "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)

		println("##########################################################")

		ref1 = updateExp(ref1, exp4, s, tab)

		println("After update: "
		//	+ values(ref1)
			+ "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)
	}
}