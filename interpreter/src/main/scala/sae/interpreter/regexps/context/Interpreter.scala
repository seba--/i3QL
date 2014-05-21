package sae.interpreter.regexps.context

import scala.collection.mutable
import idb.SetTable
import sae.interpreter.utils.MaterializedMap
import idb.observer.{Observer, NotifyObservers}
import com.google.common.collect.ArrayListMultimap
import idb.syntax.iql.IR._
import scala.Left
import scala.Some
import scala.Predef.println
import scala.Seq
import scala.Predef.Set
import scala.Predef.String
import scala.Right
import scala.Array
import scala.Int


object Interpreter {

	var printUpdates = true

	trait RegExp
	case class Terminal(s: String) extends RegExp
	case class Alt(r1: RegExp, e2: RegExp) extends RegExp
	case class Asterisk(r1: RegExp) extends RegExp
	case class Sequence(r1: RegExp, r2: RegExp) extends RegExp



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
			} else
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



	/*
		Type Declarations
	 */
	import idb.syntax.iql.IR.{Table, Relation}
	type Exp = RegExp
	type Context = Set[String]
	type Value = Set[String]

	type ExpKind = RegExpKind
	type Key = Int
	type ExpNode = (ExpKind, Seq[Key])
	type ExpLeaf = (ExpKind, Seq[Any])

	//type ContextKey = String

	type IExpKindTable = IntMapTable[Either[ExpNode, ExpLeaf]]
	type IContextTable = IntMapTable[Context]

	//	type IExpKindMap = mutable.Map[ExpKey, Either[ExpNode, ExpLeaf]]
	type IExpMap = mutable.Map[Key, Exp]
	type IExp = (IExpKindTable, IContextTable)
	type IValue = Relation[(Key, Value)]

	private def expKindTable(tab : IExp) : IExpKindTable = tab._1
	private def contextTable(tab : IExp) : IContextTable = tab._2

	def propagateContext(k : ExpKind, c : Context) : Seq[Option[Context]] = k match {
		case TerminalKind => Seq()
		case SequenceKind => Seq(Some(c), None)
	}

	def propagateValue(k : ExpKind, c : Context, v : Value, index : Int) : (Seq[Option[Context]], Option[Value])  = (k, index) match {
		case (SequenceKind, 0) => (Seq(None, Some(v)), None)
		case (SequenceKind, 1) => (Seq(None, None), Some(v))
	}

	def addExp(e : Exp, c : Context, tab : IExp) : Key = {
		val k = addTask(e, tab)
		updateContext(k, c, tab)
		k
	}

	private def addTask(e : Exp, tab : IExp) : Key = e match {
		case Terminal(s) => {
			insertLiteral(e, TerminalKind, Seq(s), tab)
		}
		case Sequence(e1, e2) => {
			//Add subtasks
			val t1 = addTask(e1, tab)
			val t2 = addTask(e2, tab)
			//Add this task with reference to the subtasks
			insertNode(e, SequenceKind, Seq(t1, t2), tab)
		}
	}

	private def updateContext(key : Key, c : Context, tab : IExp) {
		contextTable(tab).update(key, c)

		val e = expKindTable(tab)(key)

		//Propagate contexts straightforward
		e match {
			case Left( (expKind, kids) ) =>
				propagateContext(expKind, c).zip(kids).foreach((t : (Option[Context], Key)) => t match {
					case (Some(c1), k) => updateContext(k, c1, tab)
					case (None, k) =>
				} )
			case _ =>
		}
	}



	/*def insertExp(e : Exp, c : Context, tab : IExp) : ExpKey = insertExp(e, Some(c), tab)

	def insertExp(e: Exp, c : Option[Context], tab: IExp): ExpKey = e match {
		//1. Build case distinction according to interp
		case Terminal(s2) => insertLiteral(e, TerminalKind, c, Seq(s2), tab) //No recursive call => insertLiteral, param is the sequence of parameters of the literal
		case Alt(r1, r2) => insertNode(e, AltKind, c, Seq(insertExp(r1, c, tab), insertExp(r2, c, tab)), tab) //Recursive call => insertNode, (r1, s) emerges from the definition of interp
		case Asterisk(r) => insertNode(e, AsteriskKind, c, Seq(insertExp(Sequence(r, Asterisk(r)), c, tab)), tab) //Use same parameter as the recursive call in interp
		case Sequence(r1,r2) => insertNode(e, SequenceKind, c, Seq(insertExp(r1, c, tab), insertExp(r2, None, tab)), tab) //Use none if the context differs from the context in the parameter
	}  */

	def insertLiteral(e : Exp, k: ExpKind, param: Seq[Any], tab: IExp): Key = {
		val key = expKindTable(tab).add(Right(k, param))
		key
	}

	def insertNode(e : Exp, k: ExpKind, kids: Seq[Key], tab: IExp): Key = {
		val key = expKindTable(tab).add(Left(k, kids))
		key
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

	def getValues(tab : IExp) : PartialFunction[(ExpKey, Context), Value] with Iterable[((ExpKey, Context),Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def createIExp : IExp = (SetTable.empty, SetTable.empty, mutable.HashMap.empty, mutable.HashMap.empty)

	private class IncrementalInterpreter(val tab : IExp) { //extends Interpreter[Key, ExpKind, Context, Value] {

		val expressions : Relation[IDef] = null
		val contexts : Relation[IContext] = null

		def interpretLiteral(ref : Key, k : ExpKind, s : Seq[Any], c : Context): Value = k match {
			case n => null
		}

		def interpretNode(ref : Key, k : ExpKind, s: Seq[Value],  c : Context): Value = k match {
			case n => null
		}

		import idb.syntax.iql.IR._
		import idb.syntax.iql._
		type IDef = (Key, Either[ExpNode,ExpLeaf])
		type INode = (Key, ExpNode)
		type ILeaf = (Key, ExpLeaf)
		type IContext = (Key, Context)

		type IValue = (Key, Value)


		//def interpret(ref : Key, k : ExpKind, c : Context, values : Seq[Value]) : Value

		protected val iDefAsILit : Rep[IDef => ILeaf] = staticData (
			(d : IDef) => {
				val e = d._2.right.get
				(d._1, e)
			}
		)

		protected val iDefAsINode : Rep[IDef => INode] = staticData (
			(d : IDef) => {
				val e = d._2.left.get
				(d._1, e)
			}
		)

		protected val interpretILit : Rep[((ILeaf, Context)) => Value] = staticData (
			(t : (ILeaf, Context)) => {
				val lit = t._1
				val c = t._2
				interpretLiteral(lit._1, lit._3._1, lit._3._2, c)
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

		protected val contextPropagation : Rep[((ExpKind, Context)) => Seq[Option[Context]]] = staticData (
			(t : (ExpKind, Context)) => propagateContext(t._1, t._2)
		)

		protected val valuePropagation : Rep[((ExpKind, Context, Value, Int)) => (Seq[Option[Context]], Option[Value])] = staticData (
			(t : (ExpKind, Context, Value, Int)) => propagateValue(t._1, t._2, t._3, t._4)
		)



		protected val seqToSeqWithCount : Rep[Seq[Key] => Seq[(Int, Key)]] = staticData (
			(s : Seq[Key]) => {
				var i = 0
				s.map(e => {i = i + 1 ; (i,e)})
			}
		)

		protected val literals : IR.Relation[ILeaf] =
			SELECT (iDefAsILit(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsLiteral(d))

		protected val nonLiterals : IR.Relation[INode] =
			SELECT (iDefAsINode(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsNode(d))

		protected val nonLiteralsOneArgument : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 1)

		protected val nonLiteralsTwoArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 2)

		protected val nonLiteralsThreeArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 3)

		def propagatedContexts(vQuery : Rep[Query[IValue]]) : Relation[IContext] =
			WITH RECURSIVE (
				(cQuery : Rep[Query[IContext]]) => {
					contexts UNION ALL (
						SELECT ((k : Rep[Key], c : Rep[Option[Context]]) => (k, c.get)) FROM (
							queryToInfixOps (
								SELECT (
									(e : Rep[(INode, (Int, Key))], c : Rep[IContext]) => (e._2._2, contextPropagation(e._1._2._1, c._2).apply(e._2._1))
								) FROM (
									UNNEST (nonLiterals, (n : Rep[INode]) => seqToSeqWithCount(n._2._2)), cQuery
								) WHERE (
								(e : Rep[(INode, (Int, Key))], c : Rep[IContext]) =>
									e._1._1 == c._1 //e.contextKey == c.contextKey
								)
							) UNION ALL (
								SELECT (
									(e : Rep[(INode, (Int, Key))], c : Rep[IContext], v : Rep[IValue]) => (e._2._2, valuePropagation(e._1._2._1, c._2, v._2, e._2._1)._1.apply(e._2._1))
								) FROM (
									UNNEST (nonLiterals, (n : Rep[INode]) => seqToSeqWithCount(n._2._2)), cQuery, vQuery
								) WHERE (
									(e : Rep[(INode, (Int, Key))], c : Rep[IContext], v : Rep[IValue]) =>
										(e._1._1 == c._1) AND
										(e._2._2 == v._1)

								)
							)
						) WHERE (
							(k : Rep[Key], c : Rep[Option[Context]]) =>
								c.isDefined
						)
					)
				}
			)


		protected val literalsInterpreted : Relation[IValue] =
			SELECT (
				(d : Rep[ILeaf], c : Rep[IContext]) => (d._1, interpretILit(d, c._2))
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
							) FROM (
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


import idb.Relation
class IntMapTable[V] extends Relation[(Int, V)] with NotifyObservers[(Int, V)] {

	private val materializedMap : mutable.Map[Int,V] = mutable.HashMap.empty[Int,V]

	/**
	 * Runtime information whether a compiled query is a set or a bag
	 */
	override def isSet: Boolean = true

	override protected def children: Seq[Relation[_]] = Seq()

	/**
	 * Each view must be able to
	 * materialize it's content from the underlying
	 * views.
	 * The laziness allows a query to be set up
	 * on relations (tables) that are already filled.
	 * The lazy initialization must be performed prior to processing the
	 * first add/delete/update events or foreach calls.
	 */
	override protected def lazyInitialize() { }

	private var freshInt = 0

	private def freshId() : Int = {
		freshInt = freshInt + 1
		freshInt
	}

	override def foreach[T](f: ((Int, V)) => T) {
		materializedMap.foreach(f)
	}

	def apply(k : Int) : V = {
		materializedMap(k)
	}

	def add(v : V) : Int = {
		val id  = freshId
		materializedMap.put(id, v)
		notify_added((id,v))
		id
	}

	def update(oldKey : Int, newV : V) {
		if (!materializedMap.contains(oldKey))
			throw new IllegalStateException("Key could not be updated, because it does not exist. Key: " + oldKey)
		val Some(oldV) = materializedMap.put(oldKey,newV)

		notify_updated((oldKey, oldV), (oldKey, newV))
	}

	def put(k : Int, v : V) = {
		if (materializedMap.contains(k)) {
			println("Warning[IntKeyMap.put]: Key already existed")
			update(k, v)
		}

		freshInt = k
		materializedMap.put(k, v)
		notify_added((k,v))
	}

}

class MaterializedMap[Key, Context, Value] extends Observer[(Key, Context, Value)] with PartialFunction[(Key, Context), Value] with Iterable[((Key, Context), Value)] {

	private val materializedMap : ArrayListMultimap[(Key, Context),Value] = ArrayListMultimap.create[(Key, Context),Value]

	override def added(v: (Key, Context, Value)) {
		val key = v._1
		val c = v._2
		val value = v._3
		if (! (materializedMap.containsKey((key, c)) && materializedMap.get((key,c)).contains(value)))
			materializedMap.put((key, v._2), v._3)
	}

	override def removed(v: (Key, Context, Value)) {
		materializedMap.remove((v._1, v._2), v._3)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + v._2)
			case Some(e) if e != v._2 => throw new IllegalStateException("There is another value " + e + " than the removed value " + v._2)
			case _ => {}
		}   */
	}

	override def updated(oldV: (Key, Context, Value), newV: (Key, Context, Value)): Unit = {
		if (oldV._1 != newV._1)
			throw new IllegalArgumentException("oldKey != newKey")
		removed(oldV)
		added(newV)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + oldV._2)
			case Some(e) if e != oldV._2 => throw new IllegalStateException("There is another value " + e + " than the updated value " + oldV._2)
			case _ => {}
		} */
	}


	override def endTransaction() { }

	override def apply(t : (Key, Context)) : Value = apply(t._1, t._2)

	def apply(k: Key, c : Context): Value = {
		val result = materializedMap.get((k,c))
		if (result.size != 1)
			throw new IllegalStateException("There are more values at this position. Key = " + k + ", Context = " + c + ", Value = " + result)
		result.get(0)
	}

	override def isDefinedAt(t : (Key, Context)): Boolean = materializedMap.containsKey(t)

	override def iterator: Iterator[((Key, Context), Value)] = new Iterator[((Key, Context), Value)](){
		val it = materializedMap.entries().iterator()

		override def hasNext: Boolean = it.hasNext

		override def next(): ((Key, Context), Value) = {
			val e = it.next()
			(e.getKey, e.getValue)
		}
	}


}