package sae.interpreter.regexps

import idb.{SetTable, Table}
import idb.observer.{NotifyObservers, Observer}
import sae.interpreter.regexps.Interpreter.TaskKey

import scala.collection.mutable



object Interpreter {

	def main(args : Array[String]) {
		val tab : ITable = createIExp

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

		val (values, tasks) = getValuesTest(tab)
		val matTasks = tab._3.asMaterialized

		val ref1 = addExp(exp3, Set.empty[String] + s1, tab)

/*			println("Before update: "
				+ values(ref1)
				+ "[Key = " + ref1 + "]") */
			println("exps")
			tab._1.foreach(println)
			println("contexts")
			tab._2.foreach(println)
			println("tasks")
		    tasks.foreach(println)
			println("matTasks")
			matTasks.foreach(println)
			println("values")
			values.foreach(println)

			println("##########################################################")

/*		tab._2.update(ref1, Set.empty[String] + s2)

		println("After update: "
			+ values(ref1)
			+ "[Key = " + ref1 + "]")
		println("exps")
		tab._1.foreach(println)
		println("values")
		values.foreach(println)   */
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
		case Alt(r1, Alt(r2, r3)) if r2 == r3 => interp(Alt(r1,r2),c)
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

	def valueToContext(v : Value) : Context =
		v

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
	type ExpKey = Int
	type ExpNode = (ExpKind, Seq[ExpKey])
	type ExpLeaf = (ExpKind, Seq[Any])

	type ContextKey = Int

	type TaskKey = Int
	type Task = (ExpKey, ContextKey)

	type IExpKindTable = KeyMapTable[ExpKey, Either[ExpNode, ExpLeaf]]
	type IContextTable = KeyMapTable[ContextKey, Context]
	type ITaskTable = KeyMapTable[TaskKey,Task]

	//	type IExpKindMap = mutable.Map[ExpKey, Either[ExpNode, ExpLeaf]]
	type ITable = (IExpKindTable, IContextTable, ITaskTable)
//	type IValue = Relation[(TaskKey, Value)]

	private def expKindTable(tab : ITable) : IExpKindTable = tab._1
	private def contextTable(tab : ITable) : IContextTable = tab._2
	private def taskTable(tab : ITable) : ITaskTable = tab._3


	def addExp(e : Exp, c : Context, tab : ITable) : TaskKey = {
		val expKey = insertExp(e, tab)
		val contextKey = contextTable(tab).add(c)
		taskTable(tab).add((expKey, contextKey))

	}

	private def insertExp(e : Exp, tab : ITable) : ExpKey = e match {
		case Terminal(s) => {
			insertLiteral(TerminalKind, Seq(s), tab)
		}
		case Alt(e1,e2) => {
			val t1 = insertExp(e1, tab)
			val t2 = insertExp(e2, tab)

			insertNode(AltKind, Seq(t1, t2), tab)
		}
		case Sequence(e1, e2) => {
			//Add subtasks
			val t1 = insertExp(e1, tab)
			val t2 = insertExp(e2, tab)
			//Add this task with reference to the subtasks
			insertNode(SequenceKind, Seq(t1, t2), tab)
		}
	}

	def insertLiteral(k: ExpKind, param: Seq[Any], tab: ITable): ExpKey = {
		val key = expKindTable(tab).add(Right(k, param))
		key
	}

	def insertNode(k: ExpKind, kids: Seq[ExpKey], tab: ITable): ExpKey = {
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

	def getValues(tab : ITable) : PartialFunction[TaskKey,Value] with Iterable[(TaskKey,Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def getValuesTest(tab : ITable) : (Relation[(TaskKey,Value)], Relation[(TaskKey,Task)]) = {
		val interpreter = new IncrementalInterpreter(tab)
		(interpreter.values, interpreter.tasks)
	}

	def createIExp : ITable = (
		new KeyMapTable[ExpKey, Either[ExpNode, ExpLeaf]](new IntKeyGenerator),
		new KeyMapTable[ExpKey, Context](new IntKeyGenerator),
		new KeyMapTable[ExpKey, Task](new IntKeyGenerator)
		)

	private class IncrementalInterpreter(val tab : ITable) {

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
		type IExp = (ExpKey, Either[ExpNode,ExpLeaf])
		type INode = (ExpKey, ExpNode)
		type ILeaf = (ExpKey, ExpLeaf)
		type IContext = (ContextKey, Context)
		type ITask = (TaskKey, Task)

		type IValue = (TaskKey, Value)

		val expressions : Relation[IExp] = tab._1

		protected val iDefAsILit : Rep[IExp => ILeaf] = staticData (
			(d : IExp) => {
				val e = d._2.right.get
				(d._1, e)
			}
		)

		protected val iDefAsINode : Rep[IExp => INode] = staticData (
			(d : IExp) => {
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

		protected val definitionIsLiteral : Rep[IExp => Boolean] = staticData (
			(s : IExp) => s._2.isRight
		)

		protected val definitionIsNode : Rep[IExp => Boolean] = staticData (
			(s : IExp) => s._2.isLeft
		)

		protected val children : Rep[INode => Seq[ExpKey]] = staticData (
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

		protected val isAsteriskKind : Rep[INode => Boolean] = staticData (
			(n : INode) => n._2._1 == AsteriskKind
		)


		protected val valueToContextRep : Rep[Value => Context] = staticData (
			valueToContext
		)


		protected val seqToSeqWithCount : Rep[Seq[ExpKey] => Seq[(Int, ExpKey)]] = staticData (
			(s : Seq[ExpKey]) => {
				var i = 0
				s.map(e => {i = i + 1 ; (i,e)})
			}
		)

		protected val literals : IR.Relation[ILeaf] =
			SELECT (iDefAsILit(_ : Rep[IExp])) FROM expressions WHERE ((d : Rep[IExp]) => d._2.isRight)

		protected val nonLiterals : IR.Relation[INode] =
			SELECT (iDefAsINode(_ : Rep[IExp])) FROM expressions WHERE ((d : Rep[IExp]) => d._2.isLeft)

		protected val nonLiteralsOneArgument : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 1)

		protected val nonLiteralsTwoArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 2)

		protected val nonLiteralsThreeArguments : Relation[INode] =
			SELECT (*) FROM nonLiterals WHERE ((d : Rep[INode]) => children(d).length == 3)




		type RecType = Either[Either[ITask, IExp], Either[IValue,IContext]]

		private val recTypeIsValue : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isRight && r.right.get.isLeft
		)

		private val recTypeIsContext : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isRight && r.right.get.isRight
		)

		private val recTypeIsTask : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isLeft && r.left.get.isLeft
		)

		private val recTypeIsExp : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isLeft && r.left.get.isRight
		)

		private val recTypeIsLeaf : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isLeft && r.left.get.isRight && r.left.get.right.get._2.isRight
		)

		private val recTypeIsNode : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isLeft && r.left.get.isRight && r.left.get.right.get._2.isLeft
		)

		private val recTypeToValue : Rep[RecType => IValue] = staticData (
			(r : RecType) => r.right.get.left.get
		)

		private val recTypeToContext : Rep[RecType => IContext] = staticData (
			(r : RecType) => r.right.get.right.get
		)

		private val recTypeToTask : Rep[RecType => ITask] = staticData (
			(r : RecType) => r.left.get.left.get
		)

		private val recTypeToExp : Rep[RecType => IExp] = staticData (
			(r : RecType) => r.left.get.right.get
		)

		private val recTypeToLeaf : Rep[RecType => ILeaf] = staticData (
			(r : RecType) => {
				val e = r.left.get.right.get
				(e._1, e._2.right.get)
			}
		)

		private val recTypeToNode : Rep[RecType => INode] = staticData (
			(r : RecType) => {
				val e = r.left.get.right.get
				(e._1, e._2.left.get)
			}
		)


		private def taskGetId(t : Rep[ITask]) : Rep[TaskKey] = t._1
		private def taskGetExpKey(t : Rep[ITask]) : Rep[TaskKey] = t._2._1
		private def taskGetContextKey(t : Rep[ITask]) : Rep[ContextKey] = t._2._2


		protected def valueRelation(r : Rep[Query[RecType]]) :  Rep[Query[IValue]] =
			SELECT ((e : Rep[RecType]) => recTypeToValue(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsValue(e))

		protected def contextRelation(r : Rep[Query[RecType]]) :  Rep[Query[IContext]] =
			SELECT ((e : Rep[RecType]) => recTypeToContext(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsContext(e))

		protected def taskRelation(r : Rep[Query[RecType]]) :  Rep[Query[ITask]] =
			SELECT ((e : Rep[RecType]) => recTypeToTask(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsTask(e))

		protected def expRelation(r : Rep[Query[RecType]]) :  Rep[Query[IExp]] =
			SELECT ((e : Rep[RecType]) => recTypeToExp(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsExp(e))

		protected def leafRelation(r : Rep[Query[RecType]]) :  Rep[Query[ILeaf]] =
			SELECT ((e : Rep[RecType]) => recTypeToLeaf(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsLeaf(e))

		protected def nodeRelation(r : Rep[Query[RecType]]) :  Rep[Query[INode]] =
			SELECT ((e : Rep[RecType]) => recTypeToNode(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsNode(e))


		private val valueToRecType : Rep[IValue => RecType] = staticData (
			(r : IValue) => scala.Right (scala.Left (r))
		)

		private val contextToRecType : Rep[IContext => RecType] = staticData (
			(r : IContext) => scala.Right (scala.Right (r))
		)

		private val taskToRecType : Rep[ITask => RecType] = staticData (
			(r : ITask) => scala.Left (scala.Left (r))
		)

		private val expToRecType : Rep[IExp => RecType] = staticData (
			(r : IExp) => scala.Left (scala.Right (r))
		)

		private val leafToRecType : Rep[ILeaf => RecType] = staticData (
			(r : ILeaf) =>  scala.Left (scala.Right ((r._1, scala.Right (r._2))))
		)

		private val nodeToRecType : Rep[INode => RecType] = staticData (
			(r : INode) =>  scala.Left (scala.Right ((r._1, scala.Left (r._2))))
		)

		protected def makeValue(k : Rep[TaskKey], v : Rep[Value]) : Rep[RecType] =
			valueToRecType((k,v))

		protected def makeContext(k : Rep[ContextKey], c : Rep[Context]) : Rep[RecType] =
			contextToRecType((k,c))

		protected def makeTask(k : Rep[TaskKey], t : Rep[Task]) : Rep[RecType] =
			taskToRecType((k,t))

		protected def makeExp(k : Rep[ExpKey], e : Rep[Either[ExpNode,ExpLeaf]]) =
			expToRecType((k,e))

		protected def makeLeaf(k : Rep[ExpKey], e : Rep[ExpLeaf]) : Rep[RecType] =
			leafToRecType((k,e))

		protected def makeNode(k : Rep[ExpKey], e : Rep[ExpNode]) : Rep[RecType] =
			nodeToRecType((k,e))


		protected val freshContextKey : Rep[Int => ContextKey] = staticData (
			(i : Int) => tab._2.keyGenerator.fresh()
		)

		protected val freshTaskKey : Rep[Int => TaskKey] = staticData (
			(i : Int) => tab._3.keyGenerator.fresh()
		)

		protected def newTask(ek : Rep[ExpKey]) : Rep[RecType] =
			makeTask(freshTaskKey(0) , (ek, freshContextKey(0)))

		protected def newTask(ek : Rep[ExpKey], ck : Rep[ContextKey]) : Rep[RecType] =
			makeTask(freshTaskKey(0) , (ek, ck))


		protected val unnestedNonLiterals : Rep[Query[(INode, (Int, ExpKey))]] =
			UNNEST(nonLiterals, (n: Rep[INode]) => seqToSeqWithCount(n._2._2))

		protected val contextsRec : Relation[RecType] =
			SELECT ((e : Rep[IContext]) => makeContext(e._1, e._2)) FROM tab._2

		protected val tasksRec : Relation[RecType] =
			SELECT ((e : Rep[ITask]) => makeTask(e._1, e._2)) FROM tab._3

		protected val expRec : Relation[RecType] =
			SELECT ((e : Rep[IExp]) => makeExp(e._1, e._2)) FROM expressions

		protected val recursionBase :  Relation[RecType] =
			tasksRec UNION ALL (contextsRec) UNION ALL (expRec)

		private def unionPrivate[T : Manifest](queries : Rep[Query[T]]*) : Rep[Query[T]] =
			if (queries.length == 1)
				queries.head
			else
				queries.head UNION ALL (unionPrivate[T](queries.tail : _*))

		val valuesInternal : Rep[Query[RecType]] = {
			WITH RECURSIVE (
				(rec:  Rep[Query[RecType]]) => {
					recursionBase UNION ALL (
						unionPrivate (
							//Terminal context -> value
							SELECT (
								(l : Rep[ILeaf], c : Rep[IContext], t : Rep[ITask]) =>
									makeValue(taskGetId(t), interpretLit(l._2._1, l._2._2, c._2))
							) FROM (
								leafRelation(rec), contextRelation(rec), taskRelation(rec)
							) WHERE (
								(l : Rep[ILeaf], c : Rep[IContext], t : Rep[ITask]) =>
									isTerminalKind(l) AND
									l._1 == taskGetExpKey(t) AND
									c._1 == taskGetContextKey(t)
							)
						,
							//Alt task propagation left
							SELECT (
								(e: Rep[IExp], parent: Rep[INode], parentContext: Rep[IContext], parentTask : Rep[ITask]) =>
									newTask(e._1, parentContext._1)
							) FROM (
								expRelation(rec), nodeRelation(rec), contextRelation(rec), taskRelation(rec)
							) WHERE (
								(e: Rep[IExp], parent: Rep[INode], parentContext: Rep[IContext], parentTask : Rep[ITask]) =>
									isAltKind(parent) AND
									(e._1 == parent._2._2(0) OR e._1 == parent._2._2(1))  AND
									parent._1 == taskGetExpKey(parentTask) AND
									parentContext._1 == taskGetContextKey(parentTask)
							)
						,
							//Alt value propagation
							SELECT (
								(e: Rep[INode], c : Rep[IContext],  childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									makeValue(taskGetId(tasks._1), interpretPriv(e._2._1, Seq(childVals._1._2, childVals._2._2), c._2))
							) FROM (
								nonLiterals, contextRelation(rec), SELECT (*) FROM (valueRelation(rec), valueRelation(rec)), SELECT (*) FROM (taskRelation(rec), taskRelation(rec), taskRelation(rec))
							) WHERE (
								(e: Rep[INode], c : Rep[IContext], childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									isAltKind(e) AND
									e._1 == taskGetExpKey(tasks._1) AND
									c._1 == taskGetContextKey(tasks._1) AND
									e._2._2(0) == taskGetExpKey(tasks._2) AND
									childVals._1._1 == taskGetId(tasks._2) AND
									e._2._2(1) == taskGetExpKey(tasks._3) AND
									childVals._2._1 == taskGetId(tasks._3)
							)
						,
							//Seq task propagation left
							SELECT(
								(e: Rep[IExp], parent: Rep[INode], parentContext: Rep[IContext], t : Rep[ITask]) =>
									newTask(e._1, parentContext._1)
							) FROM(
								expRelation(rec), nodeRelation(rec), contextRelation(rec), taskRelation(rec)
							) WHERE (
								(e: Rep[IExp], parent: Rep[INode], parentContext: Rep[IContext], t : Rep[ITask]) =>
									isSequenceKind(parent) AND
									e._1 == parent._2._2(0) AND
									parent._1 == t._1 AND
									parentContext._1 == taskGetContextKey(t)
							)
						,
							//Seq task creation right
							SELECT(
								(e: Rep[IExp], parent: Rep[INode], child1Val: Rep[IValue], t : Rep[ITask]) =>
									newTask(e._1)
							) FROM (
								expRelation(rec), nodeRelation(rec), valueRelation(rec), taskRelation(rec)
							) WHERE (
								(e: Rep[IExp], parent: Rep[INode], child1Val: Rep[IValue], t : Rep[ITask]) =>
									isSequenceKind(parent) AND
									e._1 == parent._2._2(1) AND
									parent._2._2(0) == taskGetExpKey(t) AND
									child1Val._1 == taskGetId(t)
							)
						,
							//Seq child(0) value -> child(1) context
							SELECT(
								(e: Rep[IExp], parent: Rep[INode], child0Val: Rep[IValue], child0Task : Rep[ITask], child1Task : Rep[ITask]) =>
									makeContext(taskGetContextKey(child1Task), valueToContextRep(child0Val._2))
							) FROM (
								expRelation(rec), nodeRelation(rec), valueRelation(rec), taskRelation(rec), taskRelation(rec)
							) WHERE (
								(e: Rep[IExp], parent: Rep[INode], child0Val: Rep[IValue], child0Task : Rep[ITask], child1Task : Rep[ITask]) =>
									isSequenceKind(parent) AND
									e._1 == parent._2._2(1) AND
									parent._2._2(0) == child0Task._1 AND
									child0Val._1 == taskGetId(child0Task) AND
									e._1 == child1Task._1
								)
						,
							//Seq value propagation
							SELECT (
								(e: Rep[INode], c : Rep[IContext],  childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									makeValue(taskGetId(tasks._1), interpretPriv(e._2._1, Seq(childVals._1._2, childVals._2._2), c._2))
							) FROM (
								nodeRelation(rec), contextRelation(rec), SELECT (*) FROM (valueRelation(rec), valueRelation(rec)), SELECT (*) FROM (taskRelation(rec), taskRelation(rec), taskRelation(rec))
							) WHERE (
								(e: Rep[INode], c : Rep[IContext], childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									isSequenceKind(e) AND
									e._1 == taskGetExpKey(tasks._1) AND
									c._1 == taskGetContextKey(tasks._1) AND
									e._2._2(0) == taskGetExpKey(tasks._2) AND
									childVals._1._1 == taskGetId(tasks._2) AND
									e._2._2(1) == taskGetExpKey(tasks._3) AND
									childVals._2._1 == taskGetId(tasks._3)
								)
						)
					)
				}
			)
		}


		val values : Relation[IValue] = valueRelation(valuesInternal)
		val tasks = taskRelation(valuesInternal).asMaterialized
		val result = new MaterializedMap[TaskKey, Value]

		values.addObserver(result)
	}



}

trait KeyGenerator[T] {

	private var freshKey : T = startKey

	protected def startKey : T
	protected def nextKey(k : T) : T

	def fresh() : T = {
		val key = freshKey
		freshKey = nextKey(freshKey)
		key
	}
}

class IntKeyGenerator extends KeyGenerator[Int] {
	def startKey = 0
	def nextKey(k : Int) = k + 1
}



import idb.Relation
class KeyMapTable[K,V](val keyGenerator : KeyGenerator[K]) extends Relation[(K, V)] with NotifyObservers[(K, V)] {

	private val materializedMap : mutable.Map[K,V] = mutable.HashMap.empty[K,V]

	/**
	 * Runtime information whether a compiled query is a set or a bag
	 */
	override def isSet: Boolean = true

	override protected def children: Seq[Relation[_]] = Seq()

	override protected def lazyInitialize() { }


	override def foreach[T](f: ((K, V)) => T) {
		materializedMap.foreach(f)
	}

	def apply(k : K) : V = {
		materializedMap(k)
	}

	def add(v : V) : K = {
		val id  = keyGenerator.fresh()
		materializedMap.put(id, v)
		notify_added((id,v))
		id
	}

	def update(oldKey : K, newV : V) {
		if (!materializedMap.contains(oldKey))
			throw new IllegalStateException("Key could not be updated, because it does not exist. Key: " + oldKey)
		val Some(oldV) = materializedMap.put(oldKey,newV)

		notify_updated((oldKey, oldV), (oldKey, newV))
	}

	/*def put(k : K, v : V) = {
		if (materializedMap.contains(k)) {
			println("Warning[IntMapTable.put]: Key already existed")
			update(k, v)
		}

		freshInt = k
		materializedMap.put(k, v)
		notify_added((k,v))
	}  */

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