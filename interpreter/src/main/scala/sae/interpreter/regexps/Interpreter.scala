package sae.interpreter.regexps

import idb.observer.{NotifyObservers, Observer}

import scala.collection.mutable



object Interpreter {

	def main(args : Array[String]) {
		val tab : IExp = createIExp

		val exp1 = Alt(Terminal("a"), Terminal("b"))
		val exp2 =
			Sequence(
				Sequence(
					Terminal("b"),
					Terminal("a")
				),
				Alt(
					Terminal("c"),
					Terminal("b")
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

		val s1 = "babac"
		val s2 = "bacab"

		val values = getValues(tab)

		val ref1 = addExp(exp2, Set.empty[String] + s1, tab)

			println("Before update: "
				+ values(ref1)
				+ "[Key = " + ref1 + "]")
			println("exps")
			tab._1.foreach(println)
			println("values")
			values.foreach(println)

			println("##########################################################")

		tab._2.update(ref1, Set.empty[String] + s2)

		println("After update: "
			+ values(ref1)
			+ "[Key = " + ref1 + "]")
		println("exps")
		tab._1.foreach(println)
		println("values")
		values.foreach(println)
	}


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
		case Terminal(s2) => c flatMap (s => if (s.startsWith(s2)) Some(s.substring(s2.length)) else Some(s))
		case Alt(r1, r2) => interp(r1, c) ++ interp(r2, c)
		case ths@Asterisk(r) => {
			if (c.nonEmpty) {
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
	import idb.syntax.iql.IR.Relation
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


	def addExp(e : Exp, c : Context, tab : IExp) : Key = {
		val k = addTask(e, tab)
		contextTable(tab).put(k,c)
		k
	}

	private def addTask(e : Exp, tab : IExp) : Key = e match {
		case Terminal(s) => {
			insertLiteral(e, TerminalKind, Seq(s), tab)
		}
		case Alt(e1,e2) => {
			val t1 = addTask(e1, tab)
			val t2 = addTask(e2, tab)

			insertNode(e, AltKind, Seq(t1, t2), tab)
		}
		case Sequence(e1, e2) => {
			//Add subtasks
			val t1 = addTask(e1, tab)
			val t2 = addTask(e2, tab)
			//Add this task with reference to the subtasks
			insertNode(e, SequenceKind, Seq(t1, t2), tab)
		}
	}

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

	def getValues(tab : IExp) : PartialFunction[Key,  Value] with Iterable[(Key,Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def getValuesTest(tab : IExp) : Relation[_] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.valuesInternal
	}

	def createIExp : IExp = (new IntMapTable[Either[ExpNode, ExpLeaf]], new IntMapTable[Context])

	private class IncrementalInterpreter(val tab : IExp) { //extends Interpreter[Key, ExpKind, Context, Value] {



		def interpretLiteral(k : ExpKind, seq : Seq[Any], c : Context): Value = k match {
			case TerminalKind => {
				val s2 = seq(0).asInstanceOf[String]
				c flatMap (s => if (s.startsWith(s2)) Some(s.substring(s2.length)) else None)
			}
		}

		def interpretNode(k : ExpKind, seq: Seq[Value],  c : Context): Value = k match {
			case AltKind => seq(0) ++ seq(1)
			case SequenceKind => {
				val v1 = seq(0)
				val v2 = seq(1)
				v2
			}
		}

		import idb.syntax.iql.IR._
		import idb.syntax.iql._
		type IDef = (Key, Either[ExpNode,ExpLeaf])
		type INode = (Key, ExpNode)
		type ILeaf = (Key, ExpLeaf)
		type IContext = (Key, Context)

		type IValue = (Key, Value)


		val expressions : Relation[IDef] = tab._1



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

		protected val interpretLit : Rep[((ExpKind, Seq[Any], Context)) => Value] = staticData (
			(t : (ExpKind, Seq[Any], Context)) => {
				interpretLiteral(t._1, t._2, t._3)
			}
		)

		protected val interpretPriv : Rep[((ExpKind, Seq[Value], Context)) => Value] = staticData (
			(t : (ExpKind, Seq[Value], Context)) => interpretNode(t._1, t._2, t._3)
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

		protected val isTerminalKind : Rep[ILeaf => Boolean] = staticData (
			(t : ILeaf) => t._2._1 == TerminalKind
		)

		protected val isSequenceKind : Rep[INode => Boolean] = staticData (
			(t : INode) => t._2._1 == SequenceKind
		)

		protected val isAltKind : Rep[INode => Boolean] = staticData (
			(n : INode) => n._2._1 == AltKind
		)


		protected val seqToSeqWithCount : Rep[Seq[Key] => Seq[(Int, Key)]] = staticData (
			(s : Seq[Key]) => {
				var i = 0
				s.map(e => {i = i + 1 ; (i,e)})
			}
		)

		protected val literals : IR.Relation[ILeaf] =
			SELECT (iDefAsILit(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => d._2.isRight)

		protected val nonLiterals : IR.Relation[INode] =
			SELECT (iDefAsINode(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => d._2.isLeft)

		protected val nonLiteralsOneArgument : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 1)

		protected val nonLiteralsTwoArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 2)

		protected val nonLiteralsThreeArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 3)

	/*	def propagatedContexts(vQuery : Rep[Query[IValue]]) : Relation[IContext] =
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
			)  */


	/*	protected val literalsInterpreted : Rep[Query[IValue]] =
			SELECT (
				(d : Rep[ILeaf], c : Rep[IContext]) =>  (d._1, interpretILit(d, c._2))
			) FROM (
				literals, contexts
			) WHERE (
					(d : Rep[ILeaf], c : Rep[IContext]) => c._1 == d._1
				)

		protected val literalsInterpreted2 : Rep[Query[Either[IValue, IContext]]] =
			SELECT (
				(d : Rep[ILeaf], c : Rep[IContext]) =>  Left[IValue,IContext](d._1, interpretILit(d, c._2))
			) FROM (
				literals, contexts
			) WHERE (
				(d : Rep[ILeaf], c : Rep[IContext]) => c._1 == d._1
			) */

		protected def valueRelation(r : Rep[Query[Either[IValue, IContext]]]) : Rep[Query[IValue]] =
			SELECT ((e : Rep[Either[IValue, IContext]]) => e.leftGet) FROM r WHERE ( (e : Rep[Either[IValue, IContext]]) => e.isLeft)

		protected def contextRelation(r : Rep[Query[Either[IValue, IContext]]]) : Rep[Query[IContext]] =
			SELECT ((e : Rep[Either[IValue, IContext]]) => e.rightGet) FROM r WHERE ( (e : Rep[Either[IValue, IContext]]) => e.isRight)

		protected val unnestedNonLiterals : Rep[Query[(INode, (Int, Key))]] =
			UNNEST(nonLiterals, (n: Rep[INode]) => seqToSeqWithCount(n._2._2))

		protected val contexts : Rep[Query[Either[IValue,IContext]]] =
			SELECT (Right[IValue, IContext](_ : Rep[IContext])) FROM tab._2

		val valuesInternal : Rep[Query[Either[IValue,IContext]]] = {
			WITH RECURSIVE (
				(vQuery: Rep[Query[Either[IValue, IContext]]]) => {
					contexts UNION ALL (
						//Terminal
						queryToInfixOps (
							//Terminal context -> value
							SELECT (
								(l : Rep[ILeaf], c : Rep[IContext]) =>
									Left[IValue, IContext]((l._1, interpretLit(l._2._1, l._2._2, c._2)))
							) FROM (
								literals, contextRelation(vQuery)
							) WHERE (
								(l : Rep[ILeaf], c : Rep[IContext]) =>
									isTerminalKind(l) AND
									l._1 == c._1
							)
						) UNION ALL (
							queryToInfixOps (
								//Alt
								queryToInfixOps (
									queryToInfixOps (
										//Alt Context propagation Left
										SELECT(
											(e: Rep[IDef], parent: Rep[INode], parentContext: Rep[IContext]) =>
												Right[IValue, IContext]((e._1, parentContext._2))
										) FROM(
											expressions, nonLiterals, contextRelation(vQuery)
										) WHERE (
											(e: Rep[IDef], parent: Rep[INode], parentContext: Rep[IContext]) =>
												isAltKind(parent) AND
												e._1 == parent._2._2(0) AND
												parentContext._1 == parent._1
										)
									) UNION ALL (
										//Alt Context propagation Right
										SELECT(
											(e: Rep[IDef], parent: Rep[INode], parentContext: Rep[IContext]) =>
												Right[IValue, IContext]((e._1, parentContext._2))
										) FROM(
											expressions, nonLiterals, contextRelation(vQuery)
										) WHERE (
											(e: Rep[IDef], parent: Rep[INode], parentContext: Rep[IContext]) =>
												isAltKind(parent) AND
												e._1 == parent._2._2(1) AND
												parentContext._1 == parent._1
										)
									)

								) UNION ALL (
									//Alt value propagation
									SELECT(
										(e: Rep[INode], c : Rep[IContext], child1Val : Rep[IValue], child2Val : Rep[IValue]) =>
											Left[IValue, IContext]((e._1, interpretPriv(e._2._1, Seq(child1Val._2, child2Val._2), c._2)))
									) FROM (
										nonLiterals, contextRelation(vQuery), valueRelation(vQuery), valueRelation(vQuery)
									) WHERE (
										(e: Rep[INode], c : Rep[IContext], child1Val : Rep[IValue], child2Val : Rep[IValue]) =>
											isAltKind(e) AND
											e._1 == c._1 AND
											e._2._2(0) == child1Val._1 AND
											e._2._2(1) == child2Val._1
									)
								)
							) UNION ALL (
								//Seq
								queryToInfixOps(
									//Seq Context propagation
									SELECT(
										(e: Rep[IDef], parent: Rep[INode], parentContext: Rep[IContext]) =>
											Right[IValue, IContext]((e._1, parentContext._2))
									) FROM(
										expressions, nonLiterals, contextRelation(vQuery)
										) WHERE (
										(e: Rep[IDef], parent: Rep[INode], parentContext: Rep[IContext]) =>
											isSequenceKind(parent) AND
												e._1 == parent._2._2(0) AND
												parentContext._1 == parent._1
										)
								) UNION ALL (
									queryToInfixOps (
										//Seq value child(0) -> context child(1)
										SELECT(
											(e: Rep[IDef], parent: Rep[INode], child1Val: Rep[IValue]) =>
												Right[IValue, IContext]((e._1, child1Val._2)) //TODO: How to convert value to context?
										) FROM (
											expressions, nonLiterals, valueRelation(vQuery)
										) WHERE (
											(e: Rep[IDef], parent: Rep[INode], child1Val: Rep[IValue]) =>
												isSequenceKind(parent) AND
													e._1 == parent._2._2(1) AND
													child1Val._1 == parent._2._2(0)
											)
									) UNION ALL (
										//Seq value propagation
										SELECT(
											(e: Rep[INode], c : Rep[IContext], child1Val : Rep[IValue], child2Val : Rep[IValue]) =>
												Left[IValue, IContext]((e._1, interpretPriv(e._2._1, Seq(child1Val._2, child2Val._2), c._2)))
										) FROM (
											nonLiterals, contextRelation(vQuery), valueRelation(vQuery), valueRelation(vQuery)
											) WHERE (
											(e: Rep[INode], c : Rep[IContext], child1Val : Rep[IValue], child2Val : Rep[IValue]) =>
												isSequenceKind(e) AND
													e._1 == c._1 AND
													e._2._2(0) == child1Val._1 AND
													e._2._2(1) == child2Val._1
											)
									)
								)
							)
						)


					)
				}
			)
		}


		val values : Relation[IValue] = valueRelation(valuesInternal)
	/*		WITH RECURSIVE (
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
			) */


		val result = new MaterializedMap[Key, Value]

		values.addObserver(result)
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
			println("Warning[IntMapTable.put]: Key already existed")
			update(k, v)
		}

		freshInt = k
		materializedMap.put(k, v)
		notify_added((k,v))
	}

}

class MaterializedMap[Key, Value] extends Observer[(Key, Value)] with PartialFunction[Key, Value] with Iterable[(Key, Value)] {
	import com.google.common.collect.ArrayListMultimap

	private val materializedMap : ArrayListMultimap[Key, Value] = ArrayListMultimap.create[Key, Value]

	override def added(v: (Key, Value)) {
		val key = v._1
		val value = v._2
		if (! (materializedMap.containsKey(key) && materializedMap.get(key).contains(value)))
			materializedMap.put(key, value)
	}

	override def removed(v: (Key, Value)) {
		materializedMap.remove(v._1, v._2)/* match {
			case None => throw new IllegalStateException("Value not contained in map: " + v._2)
			case Some(e) if e != v._2 => throw new IllegalStateException("There is another value " + e + " than the removed value " + v._2)
			case _ => {}
		}   */
	}

	override def updated(oldV: (Key, Value), newV: (Key, Value)): Unit = {
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



	def apply(k: Key): Value = {
		val result = materializedMap.get(k)
		if (result.size != 1)
			throw new IllegalStateException("There are more values at this position. Key = " + k + ", Value = " + result)
		result.get(0)
	}

	override def isDefinedAt(t : Key): Boolean = materializedMap.containsKey(t)

	override def iterator: Iterator[(Key,  Value)] = new Iterator[(Key, Value)](){
		val it = materializedMap.entries().iterator()

		override def hasNext: Boolean = it.hasNext

		override def next(): (Key, Value) = {
			val e = it.next()
			(e.getKey, e.getValue)
		}
	}


}