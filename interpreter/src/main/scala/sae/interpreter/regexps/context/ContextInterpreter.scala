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
	type Context = Set[String]
	type Value = Set[String]

  Sequence(Terminal("aa"), Terminal("bb"))
  // 4 matches "aaa", "aa", "a", ""

  def matchRegexp(e: Exp, c: Context): Boolean = interp(e, c).contains("")

	def interp(e : Exp, c : Context): Value = e match {
		case Terminal(s2) => c flatMap (s => if (s.startsWith(s2)) Some(s.substring(s2.length)) else None)
		case Alt(r1, r2) => interp(r1, c) ++ interp(r2, c)
    case ths@Asterisk(r) => {
      if (!c.isEmpty) {
        val v1 = interp(r, c)
        val v2 = interp(ths, v1)
        c ++ v2
      }
      else
        c
    }
		case Sequence(r1, r2) => {
      val v1 = interp(r1, c)
      val v2 = interp(r2, v1)
      v2
    }
	}

//  def interpk[T](e : Exp, c : Context, k: Value => T): T = e match {
//    case Terminal(s2) => if (c.startsWith(s2)) k(Set(c.substring(s2.length))) else k(Set())
//    case Alt(r1, r2) => interpk(r1, c, res1 => interpk(r2, c, res2 => k(res1 ++ res2)))
//    case Asterisk(r) => interpk(Sequence(r, Asterisk(r)), c, res => k(Set(c) ++ res))
//    case Sequence(r1, r2) => interpk(r1, c, res =>
//                              mapK[String, Value, T]
//                                   (res.toList)
//                                   (s2 => k => interpk(r2, s2, k))
//                                   ((l: List[Value]) => k(l.toSet.flatten)))
//  }
//
//  def mapK[T,U,W](l: List[T])(f: T => (U => W) => W)(k: List[U] => W): W = l match {
//    case Nil => k(Nil)
//    case x::xs => f(x)(u => mapK(xs)(f)(us => k(u::us)))
//  }


  // Asterisk(Terminal("a"))

	trait RegExpKind
	case object TerminalKind extends RegExpKind
	case object AltKind extends RegExpKind
	case object AsteriskKind extends RegExpKind
	case object SequenceKind extends RegExpKind



	import idb.syntax.iql.IR.{Table, Relation}
	type ExpKind = RegExpKind
	type Key = Int
	type Node = (ExpKind, Seq[Key])
	type Leaf = (ExpKind, Seq[Any])

	type IExpKindTable = Table[(Key, Either[Node, Leaf])]
	type IContextTable = Table[(Key, Context)]
	type IExpKindMap = mutable.Map[Key, Either[Node, Leaf]]
	type IExpMap = mutable.Map[Key, Exp]
	type IExp = (IExpKindTable, IContextTable, IExpKindMap, IExpMap)
	type IValue = Relation[(Key, Value)]

	private var freshID = 0

	private def fresh() : Int = {
		freshID = freshID + 1
		freshID
	}

	private def expKindTable(tab : IExp) : IExpKindTable = tab._1
	private def contextTable(tab : IExp) : IContextTable = tab._2
	private def expKindMap(tab : IExp) : IExpKindMap = tab._3
	private def expMap(tab : IExp) : IExpMap = tab._4
	
	

	def insertExp(e : Exp, c : Context, tab : IExp) : Key = insertExp(e, Some(c), tab)

	def insertExp(e: Exp, c : Option[Context], tab: IExp): Key = e match {
		//1. Build case distinction according to interp
		case l@Terminal(s2) => insertLiteral(e, TerminalKind, c, Seq(s2), tab) //No recursive call => insertLiteral, param is the sequence of parameters of the literal
		case Alt(r1, r2) => insertNode(e, AltKind, c, Seq(insertExp(r1, c, tab), insertExp(r2, c, tab)), tab) //Recursive call => insertNode, (r1, s) emerges from the definition of interp
		case Asterisk(r) => insertNode(e, AsteriskKind, c, Seq(insertExp(Sequence(r, Asterisk(r)), c, tab)), tab) //Use same parameter as the recursive call in interp
		case Sequence(r1,r2) => insertNode(e, SequenceKind, c, Seq(insertExp(r1, c, tab), insertExp(r2, None, tab)), tab) //Use none if the context differs from the context in the parameter
	}

	def insertLiteral(e : Exp, k: ExpKind, c : Option[Context], param: Seq[Any], tab: IExp): Key = {
		val exp = Right(k, param)
		val id = fresh()
		expKindMap(tab).put(id, exp)
		expMap(tab).put(id, e)
		c match {case Some(x) => contextTable(tab).add((id,x)) ; case None => }
		expKindTable(tab).add((id, exp))


		id
	}

	def insertNode(e : Exp, k: ExpKind, c : Option[Context], kids: Seq[Key], tab: IExp): Key = {
		val exp = Left(k, kids)
		val id = fresh()
		expKindMap(tab).put(id, exp)
		expMap(tab).put(id, e)
		c match {case Some(x) => contextTable(tab).add((id,x)) ; case None => }
		expKindTable(tab).add((id, exp))


		id
	}

	def insertContext(key : Key, c : Context, tab : IExp) {
		contextTable(tab).add(key,c)
	}

/*	def updateExp(oldKey : Key, newExp : Exp, newC : Context, tab : IExp) : Key = updateExp(oldKey, newExp, Some(newC), tab)

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
	}  */

	def getValues(tab : IExp) : PartialFunction[(Key, Context), Value] with Iterable[((Key, Context),Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def createIExp : IExp = (SetTable.empty, SetTable.empty, mutable.HashMap.empty, mutable.HashMap.empty)

	private class IncrementalInterpreter(val tab : IExp) { //extends Interpreter[Key, ExpKind, Context, Value] {

		val expressions : Relation[IDef] = expKindTable(tab)
		val contexts : Relation[IContext] = contextTable(tab)

		def interpretLiteral(ref : Key, k : ExpKind, s : Seq[Any], c : Context): Value = k match {
			case TerminalKind =>  {
				val s0 = s(0).asInstanceOf[String] //Cast parameters of the terminal accordingly
				if (c.startsWith(s0)) Set(c.substring(s0.length)) else Set()
			}
		}

		def interpretNode(ref : Key, k : ExpKind, s: Seq[Value],  c : Context): Value = k match {
			case AltKind => s(0) ++ s(1) //Non-literal => like interp but substitute calls to interp with the values of the sequence
			case AsteriskKind => Set(c) ++ s(0)
			case SequenceKind => {
				s(0) flatMap (s2 => {
					println(expKindMap(tab))
					val key = expKindMap(tab)(ref).left.get._2(1)
					insertContext(key,s2, tab)
					result(key, s2)
				})
			}
		}

		import idb.syntax.iql.IR._
		import idb.syntax.iql._

		type Node = (ExpKind, Seq[Key])
		type Leaf =  (ExpKind, Seq[Any])

		type IDef = (Key, Either[Node,Leaf])
		type INode = (Key, Node)
		type ILeaf = (Key, Leaf)
		type IContext = (Key, Context)

		type IValue = (Key, Context, Value)


		//def interpret(ref : Key, k : ExpKind, c : Context, values : Seq[Value]) : Value

		protected val iDefAsILit : Rep[IDef => ILeaf] = staticData (
			(d : IDef) => {
				val e = d._2.right.get
				(d._1, (e._1, e._2))
			}
		)

		protected val iDefAsINode : Rep[IDef => INode] = staticData (
			(d : IDef) => {
				val e = d._2.left.get
				(d._1, (e._1, e._2))
			}
		)

		protected val interpretILit : Rep[((ILeaf, Context)) => Value] = staticData (
			(t : (ILeaf, Context)) => {
				val lit = t._1
				val c = t._2
				interpretLiteral(lit._1,lit._2._1, lit._2._2, c)
			}
		)

		protected val interpretPriv : Rep[((Key, ExpKind, Seq[Value], Context)) => Value] = staticData (
			(t : (Key, ExpKind, Seq[Value], Context)) => interpretNode(t._1, t._2, t._3, t._4)
		)

		protected val definitionIsLiteral : Rep[IDef => Boolean] = staticData (
			(s : IDef) => s._2.isRight
		)

		protected val definitionIsNode : Rep[IDef => Boolean] = staticData (
			(s : IDef) => s._2.isLeft
		)

		protected val children : Rep[INode => Seq[Key]] = staticData (
			(t : INode) => t._2._2
		)

		protected val isSequence : Rep[INode => Boolean] = staticData (
			(t : INode) => t._2._1 == SequenceKind
		)


		//val expressions : Table[IDef]

		protected val literals : Relation[ILeaf] =
			SELECT (iDefAsILit(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsLiteral(d))

		protected val nonLiterals : Relation[INode] =
			SELECT (iDefAsINode(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsNode(d))

		protected val nonLiteralsOneArgument : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 1)

		protected val nonLiteralsTwoArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 2)

		protected val nonLiteralsThreeArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 3)

		protected val literalsInterpreted : Relation[IValue] =
			SELECT (
				(d : Rep[ILeaf], c : Rep[IContext]) => (d._1, c._2, interpretILit(d, c._2))
			) FROM (
				literals, contexts
			) WHERE (
				(d : Rep[ILeaf], c : Rep[IContext]) => c._1 == d._1
			)

		val values : Relation[IValue] = {
			WITH RECURSIVE (
				(vQuery : Rep[Query[IValue]]) => {
					literalsInterpreted UNION ALL(
						queryToInfixOps(
							SELECT(
								(d: Rep[INode], c: Rep[IContext], v1: Rep[IValue], v2: Rep[IValue]) =>
									(d._1, c._2, interpretPriv(d._1, d._2._1, Seq(v1._3, v2._3), c._2))
							) FROM(
								nonLiteralsTwoArguments, contexts, vQuery, vQuery
								) WHERE (
								(d: Rep[INode], c: Rep[IContext], v1: Rep[IValue], v2: Rep[IValue]) =>
									(children(d)(0) == v1._1) AND
									(children(d)(1) == v2._1) AND
									c._1 == d._1
								)
						) UNION ALL(
							queryToInfixOps (
								SELECT(
									(d: Rep[INode], c: Rep[IContext], v1: Rep[IValue]) =>
										(d._1, c._2, interpretPriv(d._1, d._2._1, Seq(v1._3), c._2))
								) FROM(
									nonLiteralsOneArgument, contexts, vQuery
								) WHERE (
								(d: Rep[INode], c: Rep[IContext], v1: Rep[IValue]) =>
									(children(d)(0) == v1._1) AND
									c._1 == d._1
								)
							) UNION ALL (
								SELECT(
									(d: Rep[INode], c: Rep[IContext], v1: Rep[IValue]) =>
										(d._1, c._2, interpretPriv(d._1, d._2._1, Seq(v1._3), c._2))
								) FROM(
									nonLiteralsTwoArguments, contexts, vQuery
								) WHERE (
									(d: Rep[INode], c: Rep[IContext], v1: Rep[IValue]) =>
										isSequence(d) AND
										(children(d)(0) == v1._1) AND
										c._1 == d._1
								)
							)
						)
					)
				}
			)
		}

		val result = new MaterializedMap[Key, Context, Value]

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

		var ref1 = insertExp(exp4, s, tab)

		println("Before update: "
			+ values(ref1,s)
			+ "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println)

		println("##########################################################")

		/*ref1 = updateExp(ref1, exp4, s, tab)

		println("After update: "
		//	+ values(ref1)
			+ "[Key = " + ref1 + "]")
		println("exps")
		tab._2.foreach(println)
		println("values")
		values.foreach(println) */
	}
}