package sae.interpreter.regexps.incr

import idb.algebra.ir.{RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.{SetTable, Table}
import idb.observer.{NotifyObservers, Observer}
import sae.interpreter.regexps._
import sae.interpreter.utils.{IntKeyGenerator, KeyMapTable, MaterializedMap}



object Interpreter {

	def main(args : Array[String]) {
		val table = createITable
		val values = getValues(table)

		def stringList(tab : ITable, k : ListKey) : List[String] =
			list(tab,k).map((e) => string(tab,e.asInstanceOf[ListKey]))


		println("> val s0 = insertString(\"abbac\",table)")
		val s0 = insertString(table, "abbac")
		println()
		println("> val s1 = insertString(\"ab\",table)")
		val s1 = insertString(table, "ab")
		println()
		println("> val s2 = insertString(\"ac\",table)")
		val s2 = insertString(table, "ac")
		println()
		println("> val s3 = insertString(\"bccb\",table)")
		val s3 = insertString(table, "bccb")
		println()
		println("> val s4 = insertString(\"cccba\",table)")
		val s4 = insertString(table, "cccba")
		println()


		println("> val e0 = insertExp(table, Terminal(\"a\"))")
		val e0 = insertExp(table, Terminal("a"))
		println()
		println("> val e1 = insertExp(table, Alt(Terminal(\"b\"), Terminal(\"a\")))")
		val e1 = insertExp(table, Alt(Terminal("b"), Terminal("a")))
		println()
		println("> val e2 = insertExp(table, Sequence(Terminal(\"a\"), Terminal(\"b\")))")
		val e2 = insertExp(table, Sequence(Terminal("a"), Terminal("b")))
		println()
		println("> val e3 = insertExp(table, Asterisk(Terminal(\"c\")))")
		val e3 = insertExp(table, Asterisk(Terminal("c")))
		println()

		println("> val l0 = insertList(table, Seq(s0,s1,s3))")
		val l0 = insertList(table, Seq(s0,s1,s3))
		println()

		println("> val t0 = insertNewTask(startsWithTag,Seq(s0,s1),table)")
		val t0 = insertTask(table, startsWithTag,Seq(s0,s1))
		println("return -> " + values(t0))
		println()
		println("> val t1 = insertNewTask(startsWithTag,Seq(s0,s2),table)")
		val t1 = insertTask(table, startsWithTag,Seq(s0,s2))
		println("return -> " + values(t1))
		println()
		println("> val t2 = insertNewTask(startsWithTag,Seq(s0,s0),table)")
		val t2 = insertTask(table, startsWithTag,Seq(s0,s0))
		println("return -> " + values(t2))
		println()

		println("> val t3 = insertNewTask(sizeTag,Seq(s0),table)")
		val t3 = insertTask(table, sizeTag,Seq(s0))
		println("return -> " + values(t3))
		println()
		println("> val t4 = insertNewTask(sizeTag,Seq(s2),table)")
		val t4 = insertTask(table, sizeTag,Seq(s2))
		println("return -> " + values(t4))
		println()

		println("> val t4_1 = insertNewTask(table, appendTag,Seq(s1,s2))")
		val t4_1 = insertTask(table, appendTag,Seq(s1,s2))
		println("return -> " + string(table, values(t4_1).asInstanceOf[ListKey]))
		println()
		println("> val t4_2 = insertNewTask(table, appendTag,Seq(s0,-1))")
		val t4_2 = insertTask(table, appendTag,Seq(s0,-1))
		println("return -> " + string(table, values(t4_2).asInstanceOf[ListKey]))
		println()

		println("> val t5 = insertNewTask(sublistTag,Seq(s0,3),table)")
		val t5 = insertTask(table, sublistTag,Seq(s0,3))
		println("return -> " + string(table, values(t5).asInstanceOf[ListKey]))
		println()
		println("> val t6 = insertNewTask(sublistTag,Seq(s1,2),table)")
		val t6 = insertTask(table, sublistTag,Seq(s1,2))
		println("return -> " + string(table, values(t6).asInstanceOf[ListKey]))
		println()


		println("> val t6_1 = insertNewTask(table, flatMapInterpTag,Seq(l0,e0))")
		val t6_1 = insertTask(table, flatMapInterpTag,Seq(l0,e0))
		println("return -> " + stringList(table, values(t6_1).asInstanceOf[ListKey]))
		println()


		println("> val t7 = insertNewTask(table, interpTag,Seq(e0,s0))")
		val t7 = insertTask(table, interpTag,Seq(e0,s0))
		println("return -> " + stringList(table, values(t7).asInstanceOf[ListKey]))
		println()
		println("> val t8 = insertNewTask(table, interpTag,Seq(e1,s0))")
		val t8 = insertTask(table, interpTag,Seq(e1,s0))
		println("return -> " + stringList(table, values(t8).asInstanceOf[ListKey]))
		println()
		println("> val t9 = insertNewTask(table, interpTag,Seq(e2,s0))")
		val t9 = insertTask(table, interpTag,Seq(e2,s0))
		println("return -> " + stringList(table, values(t9).asInstanceOf[ListKey]))
		println()
		println("> val t10 = insertNewTask(table, interpTag,Seq(e3,s4))")
		val t10 = insertTask(table, interpTag,Seq(e3,s4))
		println("return -> " + stringList(table, values(t10).asInstanceOf[ListKey]))
		println()


	  /*
		val exp0 = Terminal("a")
		val exp1 = Alt(Alt(Terminal("a"), Terminal("b")), Terminal("c"))
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

		val exp5 =
			Asterisk(
				Terminal("b")
			)

		val exp6 =
			Sequence(
				Terminal("b"),
				Sequence(
					Terminal("a"),
					Sequence(
						Asterisk(
							Alt(
								Terminal("a"),
								Terminal("b")
							)
						),
						Terminal("c")
					)
				)
			)


		val e0 = insertExp(exp3, table)

		val task0 = insertNewTask("interp", Seq(exp0, s0), table)   */
	}


	var printUpdates = true



	def matchRegexp(e: Exp, c: Text): Boolean = interp(e, c).contains("")

	def interp(e : Exp, c : Text): InterpValue = e match {
		case Terminal(s2) => if (c.startsWith(s2)) Set(c.substring(s2.length)) else Set()
		case Alt(r1, r2) => interp(r1, c) ++ interp(r2, c)
		case Asterisk(r) => interp(Sequence(r, Asterisk(r)),c) + c
		case Sequence(r1, r2) => interp(r1, c) flatMap (s2 => interp(r2, s2))
	}

	def interpNorm(e : (Exp, Text)) : InterpValue = {
		if (e._1.isInstanceOf[Terminal] && e._2.startsWith(e._1.asInstanceOf[Terminal].s)) {
			val t1 : Terminal = e._1.asInstanceOf[Terminal]
			val res : InterpValue = Set(e._2.substring(t1.s.length))
			return res
		} else if (e._1.isInstanceOf[Terminal] && !(e._2.startsWith(e._1.asInstanceOf[Terminal].s))) {
			val res : InterpValue = Set()
			return res
		} else if (e._1.isInstanceOf[Alt]) {
			val t1 : Alt = e._1.asInstanceOf[Alt]
			val v1 : InterpValue = interpNorm((t1.r1,e._2)) //Task1
			val v2 : InterpValue = interpNorm((t1.r2,e._2)) //Task2
			val res : InterpValue = v1 ++ v2
			return res
		} else if (e._1.isInstanceOf[Asterisk]) {
			val t1 : Asterisk = e._1.asInstanceOf[Asterisk]
			val v1 : InterpValue = interpNorm((Sequence(t1.r1, t1),e._2)) //Task3
			val res : InterpValue = v1 + e._2
			return res
		} else if (e._1.isInstanceOf[Sequence]) {
			val t1 : Sequence = e._1.asInstanceOf[Sequence]
			val v1 : InterpValue = interpNorm((t1.r1,e._2)) //Task5
		//	val f : Function[String,InterpValue] = s => interpNorm((t1.r2,s))
		//	val v2 : InterpValue = v1 flatMap f //Task6
			val v2 : InterpValue = flatMapInterpNorm(v1,t1.r2)
			val res : InterpValue = v2
			return res
		}

		return null
	}


	def flatMapInterpNorm(v1: InterpValue, r2: RegExp): InterpValue =
		if (v1.isEmpty)
			Set()
		else
			interpNorm((r2, v1.head)) ++ flatMapInterpNorm(v1.tail, r2)

/*	abstract class Defun[In, Out] {
		def apply(in: In): Out
	}
	case class runInterpNorm(r2: RegExp) extends Defun[String, InterpValue] {
		def apply(s: String) = interpNorm((r2, s))
	}  */

/*	def interpNorm(e : (Exp, Text)) : Value = {
		if (e._1.isInstanceOf[Terminal]) {
			val t1 : Terminal = e._1.asInstanceOf[Terminal]
			val res : Value = if (e._2.startsWith(t1.s)) Set(e._2.substring(t1.s.length)) else Set()
			return res
		} else if (e._1.isInstanceOf[Alt]) {
			val t1 : Alt = e._1.asInstanceOf[Alt]
			val v1 : Value = interpNorm((t1.r1,e._2)) //Task1
			val v2 : Value = interpNorm((t1.r2,e._2)) //Task2
			val res : Value = v1 ++ v2
			return res
		} else if (e._1.isInstanceOf[Asterisk]) {
			val t1 : Asterisk = e._1.asInstanceOf[Asterisk]
			val v1 : Value = interpNorm((Sequence(t1.r1, t1),e._2)) //Task3
			val res : Value = Set(e._2) ++ v1
			return res
		} else if (e._1.isInstanceOf[Sequence]) {
			val t1 : Sequence = e._1.asInstanceOf[Sequence]
			val v1 : Value = interpNorm((t1.r1,e._2)) //Task5
			//			val f : Function[String,Value] = s => interpNorm((t1.r2,s))
			val f = runInterpNorm(t1.r2)
			val v2 : Value = flatMapInterpNorm(v1, t1.r2)
			val res : Value = v2
			return res
		}    */

	/*	def flatMapDefun[T](f: Defun[T, Set[T]], c: Set[T]): Set[T] =
			if (c.isEmpty)
				Set()
			else
				f(c.head) ++ flatMapDefun(f, c.tail)  */





	//  def interpk[T](e : Exp, c : Text, k: Value => T): T = e match {
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




	/*
		Type Declarations
	 */
	type Exp = RegExp
	type Text = String

	type ExpKind = RegExpKind
	type ExpKey = Int
	type ExpNode = (ExpKind, Seq[ExpKey], Seq[Any])

	type ListElement = (Any, ListKey)
	type ListKey = Int
	type IList = (ListKey, ListElement)

	type Input = Seq[Any]
	type InputKey = Int

	type TaskKey = Int
	type TaskIndex = Int
	type TaskTag = String
	type Task = (TaskKey, TaskTag, TaskIndex, InputKey) //Parent Task, Index of the task within one branch, input parameters

	type InterpValue = Set[String]

	type IExp = (ExpKey, ExpNode)
	type ITask = (TaskKey, Task)
	type IValue = (TaskKey, Any)
	type IInput = (InputKey, Seq[Any])

	type IExpTable = KeyMapTable[ExpKey, ExpNode]
	type ITaskTable = KeyMapTable[TaskKey,Task]
	type IInputTable = KeyMapTable[InputKey, Input]
	type IListTable = KeyMapTable[ListKey, ListElement]

	type ITable = (IInputTable, ITaskTable, IExpTable, IListTable)

	private def expTable(tab : ITable) : IExpTable = tab._3
	private def taskTable(tab : ITable) : ITaskTable = tab._2
	private def inputTable(tab : ITable) : IInputTable = tab._1
	private def listTable(tab : ITable) : IListTable = tab._4

	val interpTag : TaskTag = "Interpreter.interp"
	val startsWithTag : TaskTag = "List.startsWith"
	val sizeTag : TaskTag = "List.size"
	val sublistTag : TaskTag = "List.sublist"
	val appendTag : TaskTag = "List.append"
	val flatMapInterpTag : TaskTag = "List.flatMapInterp"

	def insertTask(tab : ITable, tag : TaskTag, params : Seq[Any]) : TaskKey = {
		val input = inputTable(tab).add(params)
		taskTable(tab).add((tab._2.keyGenerator.fresh(), tag, -1, input))
	}

	def insertExp(tab : ITable, e : Exp) : ExpKey = e match {
		case Terminal(s) =>
			expTable(tab).add((TerminalKind, Seq(), Seq(insertString(tab,s))))

		case Alt(e1,e2) => {
			val t1 = insertExp(tab, e1)
			val t2 = insertExp(tab, e2)
			expTable(tab).add((AltKind, Seq(t1,t2), Seq()))

		}
		case Sequence(e1, e2) => {
			val t1 = insertExp(tab, e1)
			val t2 = insertExp(tab, e2)
			expTable(tab).add((SequenceKind, Seq(t1,t2), Seq()))
		}

		case Asterisk(e1) => {
			val t1 = insertExp(tab, e1)
			expTable(tab).add((AsteriskKind, Seq(t1), Seq()))
		}
	}

	def insertString(tab : ITable, s : String) : ListKey = 	insertList(tab,s.toList)

	def insertList(tab : ITable, s : Seq[Any]) : ListKey =
		if (s.isEmpty)
			-1
		else
			listTable(tab).add((s.head,insertList(tab,s.tail)))

	def list(tab : ITable, k : ListKey) : List[Any] =
		if (k == -1)
			Nil
		else {
			val e = listTable(tab)(k)
			e._1 :: list(tab, e._2)
		}

	def string(tab : ITable, k : ListKey) : String =
		if (k == -1)
			""
		else {
			val e = listTable(tab)(k)
			e._1.toString + string(tab, e._2)
		}

/*	def diffList(tab : ITable, oldKey : ListKey, newList : Seq[Any]) {
		//TODO add empty list element


		val oldIList : ListElement = listTable(tab)(oldKey)
		(oldIList, newList) match {
			case (())
		}

	}

	def diffExp(tab : ITable, oldKey : ExpKey, newExp : Exp) {
		val oldIExp : ExpNode = expTable(tab)(oldKey)
		(oldIExp, newExp) match {
			case ((TerminalKind, _, Seq(s0)), Terminal(s1)) => expTable(tab).update(oldKey, (TerminalKind, Seq(), Seq(s1)))

			case ((AltKind, Seq(a0,a1), _), Alt(b0, b1)) => {
				diffExp(tab, a0, b0)
				diffExp(tab, a1, b1)
			}




		}
	}   */








	/*	def updateExp(oldKey : Key, newExp : Exp, newC : Text, tab : IExp) : Key = updateExp(oldKey, newExp, Some(newC), tab)

		def updateExp(oldKey : Key, newExp : Exp, newC : Option[Text], tab: IExp) : Key = {
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

		def updateLiteral(oldKey : Key, e : Exp, k : ExpKind, c : Option[Text], param : Seq[Any], tab : IExp): Key = {
			if (printUpdates) println("updateLiteral: oldKey = " + oldKey + ", k = " + k + ", param = " + param)
			val exp = Right(k, c, param)
			tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
			tab._2.put(oldKey, exp)
			tab._3.put(oldKey, e)
			oldKey
		}

		def updateNode(oldKey : Key, e : Exp, k : ExpKind, c : Option[Text], kids : Seq[Key], tab : IExp): Key = {
			if (printUpdates) println("updateNode: oldKey = " + oldKey + ", k = " + k + ", kids = " + kids)
			val exp = Left(k, c, kids)
			tab._1.update((oldKey, tab._2(oldKey)), (oldKey, exp))
			tab._2.put(oldKey, exp)
			tab._3.put(oldKey, e)
			oldKey
		}  */

	def getValues(tab : ITable) : PartialFunction[TaskKey,Any] with Iterable[(TaskKey,Any)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def createITable : ITable = (
		new KeyMapTable[InputKey, Input](new IntKeyGenerator),
		new KeyMapTable[TaskKey, Task](new IntKeyGenerator),
		new KeyMapTable[ExpKey, ExpNode](new IntKeyGenerator),
		new KeyMapTable[ListKey, ListElement](new IntKeyGenerator)
	)


	/**
	 * This class implements the incremental part of the interpreter.
	 * @param tab The underlying table for tasks and incremental data structures.
	 */
	private class IncrementalInterpreter(val tab : ITable) {

		import idb.syntax.iql.IR._
		import idb.syntax.iql._

		protected val conditionTerminal : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == TerminalKind
		)

		protected val conditionSequence : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == SequenceKind
		)

		protected val conditionAlt : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == AltKind
		)

		protected val conditionAsterisk : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == AsteriskKind
		)

		private val recTypeIsInput : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isInstanceOf[InputType]
		)

		private val recTypeIsValue : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isInstanceOf[ValueType]
		)

		private val recTypeIsTask : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isInstanceOf[TaskType]
		)

		private val recTypeIsExp : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isInstanceOf[ExpType]
		)

		private val recTypeIsList : Rep[RecType => Boolean] = staticData (
			(r : RecType) => r.isInstanceOf[ListType]  		)


		private val recTypeToInput : Rep[RecType => IInput] = staticData (
			(r : RecType) => r.asInstanceOf[InputType].input
		)

		private val recTypeToValue : Rep[RecType => IValue] = staticData (
			(r : RecType) => r.asInstanceOf[ValueType].value
		)

		private val recTypeToTask : Rep[RecType => ITask] = staticData (
			(r : RecType) => r.asInstanceOf[TaskType].task
		)

		private val recTypeToExp : Rep[RecType => IExp] = staticData (
			(r : RecType) => r.asInstanceOf[ExpType].exp
		)

		private val recTypeToList : Rep[RecType => IList] = staticData (
			(r : RecType) => r.asInstanceOf[ListType].l
		)

		protected def inputRelation(r : Rep[Query[RecType]]) : Rep[Query[IInput]] =
			SELECT ((e : Rep[RecType]) => recTypeToInput(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsInput(e))

		protected def valueRelation(r : Rep[Query[RecType]]) : Rep[Query[IValue]] =
			SELECT ((e : Rep[RecType]) => recTypeToValue(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsValue(e))

		protected def taskRelation(r : Rep[Query[RecType]]) : Rep[Query[ITask]] =
			SELECT ((e : Rep[RecType]) => recTypeToTask(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsTask(e))

		protected def expRelation(r : Rep[Query[RecType]]) : Rep[Query[IExp]] =
			SELECT ((e : Rep[RecType]) => recTypeToExp(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsExp(e))

		protected def listRelation(r : Rep[Query[RecType]]) : Rep[Query[IList]] =
			SELECT ((e : Rep[RecType]) => recTypeToList(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsList(e))


		private val valueToRecType : Rep[((String, IValue)) => RecType] = staticData (
			(r : (String, IValue)) => {
				Predef.println("Value[" + r._1 + "]\t-> " + r._2)
				ValueType(r._2)
			}
		)

		private val taskToRecType : Rep[((String, ITask)) => RecType] = staticData (
			(r : (String, ITask)) => {
				Predef.println("Task[" + r._1 + "]\t-> " + r._2)
				TaskType(r._2)
			}
		)

		private val expToRecType : Rep[IExp => RecType] = staticData (
			(r : IExp) => {
				Predef.println("Exp\t-\t-\t-> " + r)
				ExpType(r)
			}
		)

		private val inputToRecType : Rep[IInput => RecType] = staticData (
			(r : IInput) => {
				Predef.println("Input\t-\t-\t-> " + r)
				InputType(r)
			}
		)

		private val listToRecType : Rep[IList => RecType] = staticData (
			(r : IList) => {
				Predef.println("List\t-\t-\t-> " + r)
				ListType(r)
			}
		)


		/*
			Create a new entry in a table
		 */
		private val createInput : Rep[Seq[Any] => InputKey] = staticData (
			(e : Seq[Any]) => inputTable(tab).add(e)
		)

		private val createList : Rep[Seq[Any] => ListKey] = staticData (
			(e : Seq[Any]) => insertList(tab,e)
		)

		private val consList : Rep[((Any, ListKey)) => ListKey] = staticData (
			(e : (Any, ListKey)) => listTable(tab).add(e)
		)

		private val createSequenceExpression : Rep[((Seq[ExpKey], Seq[Any])) => ExpKey] = staticData (
			(e : ((Seq[ExpKey], Seq[Any]))) => expTable(tab).add((SequenceKind, e._1, e._2))
		)

		protected def makeInput(k : Rep[InputKey], in : Rep[Input]) : Rep[RecType] =
			inputToRecType((k,in))


		protected def makeValue(info : Rep[String])(k : Rep[TaskKey], v : Rep[Any]) : Rep[RecType] =
			valueToRecType(info,(k,v))

		protected def makeTask(info : Rep[String])(k : Rep[TaskKey], t : Rep[Task]) : Rep[RecType] =
			taskToRecType(info,(k,t))

		protected def makeExp(k : Rep[ExpKey], e : Rep[ExpNode]) =
			expToRecType((k,e))

		protected def makeStringList(k : Rep[ListKey], e : Rep[ListElement]) : Rep[RecType] =
			listToRecType((k,e))

		protected def newTask(info : Rep[String])(t : Rep[Task]) : Rep[RecType] =
			taskToRecType(info,(freshTaskKey(0), t))


		protected val freshTaskKey : Rep[Int => TaskKey] = staticData (
			(i : Int) => tab._2.keyGenerator.fresh()
		)

		protected val inputRec : Relation[RecType] =
			SELECT ((e : Rep[IInput]) => makeInput(e._1, e._2)) FROM tab._1

		protected val tasksRec : Relation[RecType] =
			SELECT ((e : Rep[ITask]) => makeTask("init")(e._1, e._2)) FROM tab._2

		protected val expRec : Relation[RecType] =
			SELECT ((e : Rep[IExp]) => makeExp(e._1, e._2)) FROM tab._3

		protected val listRec : Relation[RecType] =
			SELECT ((e : Rep[IList]) => makeStringList(e._1,e._2)) FROM tab._4

		protected val recursionBase :  Relation[RecType] =
			tasksRec UNION ALL (expRec) UNION ALL (inputRec) UNION ALL (listRec)

		private def unionPrivate[T : Manifest](queries : Rep[Query[T]]*) : Rep[Query[T]] = {
			if (queries.length == 1)
				queries.head
			else
				queries.head UNION ALL (unionPrivate[T](queries.tail : _*))
		}

		private def taskGetId(t : Rep[ITask]) : Rep[TaskKey] = t._1
		private def taskGetParentId(t : Rep[ITask]) : Rep[TaskKey] = t._2._1
		private def taskGetInputKey(t : Rep[ITask]) : Rep[InputKey] = t._2._4
		private def taskGetIndex(t : Rep[ITask]) : Rep[TaskIndex] = t._2._3
		private def taskGetTag(t : Rep[ITask]) : Rep[TaskTag] = t._2._2

		private def inputGetInputKey(t : Rep[IInput]) : Rep[InputKey] = t._1
		private def inputGetParams(t : Rep[IInput]) : Rep[Seq[Any]] = t._2

		private def expGetExpKey(e : Rep[IExp]) : Rep[ExpKey] = e._1
		private def expGetExp(e : Rep[IExp]) : Rep[ExpNode] = e._2
		private def expGetKind(e : Rep[IExp]) : Rep[ExpKind] = e._2._1
		private def expGetChildren(e : Rep[IExp]) : Rep[Seq[ExpKey]] = e._2._2
		private def expGetParams(e : Rep[IExp]) : Rep[Seq[Any]] = e._2._3

		private def valGetId(v : Rep[IValue]) : Rep[TaskKey] = v._1
		private def valGetValue(v : Rep[IValue]) : Rep[Any] = v._2

		private def listGetListKey(s : Rep[IList]) : Rep[ListKey] = s._1
		private def listGetHead(s : Rep[IList]) : Rep[Any] = s._2._1
		private def listGetTail(s : Rep[IList]) : Rep[ListKey] = s._2._2

		private val valuesInternal : Rep[Query[RecType]] = {
			WITH RECURSIVE (
				(rec:  Rep[Query[RecType]]) => {
					val expressionRel = expRelation(rec)
					val taskRel = taskRelation(rec)
					val valueRel = valueRelation(rec)
					val inputRel = inputRelation(rec)
					val listRel = listRelation(rec)

					recursionBase UNION ALL (
						unionPrivate (
							/*Function: List.startsWith(s1,s2) //List s1 starts with list s2
							 	if (s2 == "") true
							 	else if (s1 == "" && s2 != "") false
							 	else if (s1 != "" && s2 != "" && s1(0) != s2(0) false
							 	else if (s1 != "" && s2 != "" && s1(0) == s2(0) startsWith(s1.tail, s2.tail)
							*/
							 	//if (s2 == "")
								SELECT (
									(t : Rep[ITask], in : Rep[IInput]) =>
										makeValue(startsWithTag + "#val0")(taskGetId(t),__anythingAsUnit(true))
								) FROM (
									taskRel, inputRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput]) =>
										taskGetTag(t) == __anythingAsUnit(startsWithTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(1) == __anythingAsUnit(-1)
									)
								,
								//if (s1 == "" && s2 != "")
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], s1 : Rep[IList], s2 : Rep[IList]) =>
										makeValue(startsWithTag + "#val1")(taskGetId(t),__anythingAsUnit(false))
								) FROM (
									taskRel, inputRel, listRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], s1 : Rep[IList], s2 : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(startsWithTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == __anythingAsUnit(-1) AND
											inputGetParams(in)(1) == listGetListKey(s2) AND
											listGetListKey(s2) != __anythingAsUnit(-1)

									)
								,
								//if (s1 != "" && s2 != "" && s1(0) != s2(0))
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], s1 : Rep[IList], s2 : Rep[IList]) =>
										makeValue(startsWithTag + "#val2")(taskGetId(t),__anythingAsUnit(false))
								) FROM (
									taskRel, inputRel, listRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], s1 : Rep[IList], s2 : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(startsWithTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(s1) AND
											inputGetParams(in)(1) == listGetListKey(s2) AND
											listGetListKey(s1) != __anythingAsUnit(-1) AND
											listGetListKey(s2) != __anythingAsUnit(-1) AND
											listGetHead(s1) != listGetHead(s2)
									)
								,
								//if (s1 != "" && s2 != "" && s1(0) == s2(0) startsWith(s1.tail, s2.tail)
								//Task creation
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], s1 : Rep[IList], s2 : Rep[IList]) =>
										newTask(startsWithTag + "#rec3")(
											taskGetId(t),
											__anythingAsUnit(startsWithTag),
											__anythingAsUnit(0),
											createInput(Seq(listGetTail(s1), listGetTail(s2)))
										)
								) FROM (
									taskRel, inputRel, listRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], s1 : Rep[IList], s2 : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(startsWithTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(s1) AND
											inputGetParams(in)(1) == listGetListKey(s2) AND
											listGetListKey(s1) != __anythingAsUnit(-1) AND
											listGetListKey(s2) != __anythingAsUnit(-1) AND
											listGetHead(s1) == listGetHead(s2)
								)
								,
						        //Value propagation
								SELECT (
									(t : Rep[ITask], parent : Rep[ITask], v : Rep[IValue]) => {
										makeValue(startsWithTag + "#val3")(taskGetId(parent), valGetValue(v))
									}
								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], parent : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(parent) == __anythingAsUnit(startsWithTag) AND
											taskGetParentId(t) == taskGetId(parent) AND
											taskGetIndex(t) == __anythingAsUnit(0) AND
											valGetId(v) == taskGetId(t)
								)
								,

							/*Function: List.size(l1) //Number of elements of list l1
									 if (l1 == nil) 0
									 else if (l1 != nil) 1 + size(s1.tail)
							*/
								//if (l1 == nil)
								SELECT (
									(t : Rep[ITask], in : Rep[IInput]) =>
										makeValue(sizeTag + "#val0")(taskGetId(t),__anythingAsUnit(0))
								) FROM (
									taskRel, inputRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput]) =>
										taskGetTag(t) == __anythingAsUnit(sizeTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == __anythingAsUnit(-1)
									)
								,
								//if (l1 != "")
								//Task creation
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList]) =>
										newTask(sizeTag + "#rec1")(
											taskGetId(t),
											__anythingAsUnit(sizeTag),
											__anythingAsUnit(0),
											createInput(Seq(listGetTail(l1)))
										)
								) FROM (
									taskRel, inputRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(sizeTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(l1)

									)
								,
								//Value propagation
								SELECT (
									(t : Rep[ITask], parent : Rep[ITask], v : Rep[IValue]) => {
										makeValue(sizeTag + "#val1")(taskGetId(parent), valGetValue(v).AsInstanceOf[Int] + 1)
									}
								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], parent : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(parent) == __anythingAsUnit(sizeTag) AND
											taskGetParentId(t) == taskGetId(parent) AND
											taskGetIndex(t) == __anythingAsUnit(0) AND
											valGetId(v) == taskGetId(t)
								)
								,

						    /* Function: List.sublist(l1,n) //The list l1 without the first n elements
						    	if (l1 == nil || n == 0) l1
						    	else if (l1 != nil && n != 0) sublist(l1.tail, n - 1)
						    */
								 //if (l1 == nil || n == 0)
								SELECT (
									(t : Rep[ITask], in : Rep[IInput]) =>
										makeValue(sublistTag + "#val0")(taskGetId(t), inputGetParams(in)(0))
								) FROM (
									taskRel, inputRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput]) =>
										taskGetTag(t) == __anythingAsUnit(sublistTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											(inputGetParams(in)(0) == __anythingAsUnit(-1) OR inputGetParams(in)(1) == __anythingAsUnit(0))
								)
								,
								//if (l1 != nil && n != 0)
								//Task creation
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList]) =>
										newTask(sublistTag + "#rec1")(
											taskGetId(t),
											__anythingAsUnit(sublistTag),
											__anythingAsUnit(0),
											createInput(Seq(listGetTail(l1),inputGetParams(in)(1).AsInstanceOf[Int] - 1)))
								) FROM (
									taskRel, inputRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(sublistTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(l1) AND
											inputGetParams(in)(1) != __anythingAsUnit(0)
								)
								,
								//Value propagation
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) => {
										makeValue(sublistTag + "#val1")(taskGetId(t), valGetValue(v))
									}
								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(sublistTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(0) AND
											valGetId(v) == taskGetId(child)
								)
								,
							/* Function: List.append(l1, l2) //Appends lists l1 and l2
								if (l1 == nil) l2
								else if (l1 != nil) l1.head + append(l1.tail, l2)
							*/
								//if (l1 == nil)
								SELECT (
									(t : Rep[ITask], in : Rep[IInput]) =>
										makeValue(appendTag + "#val0")(taskGetId(t),inputGetParams(in)(1))
								) FROM (
									taskRel, inputRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput]) =>
										taskGetTag(t) == __anythingAsUnit(appendTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == __anythingAsUnit(-1)
								)
								,
								//if (l1 != nil)
								//Task creation
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList]) =>
										newTask(appendTag + "#rec1")(
											taskGetId(t),
											__anythingAsUnit(appendTag),
											__anythingAsUnit(0),
											createInput(Seq(listGetTail(l1),inputGetParams(in)(1))))
								) FROM (
									taskRel, inputRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(appendTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(l1)
								)
								,
								//Value propagation
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], l : Rep[IList]) =>
										makeValue(appendTag + "#val1")(taskGetId(t), consList(listGetHead(l), valGetValue(v).AsInstanceOf[ListKey]))

								) FROM (
									taskRel, taskRel, valueRel, inputRel, listRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], l : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(appendTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(0) AND
											valGetId(v) == taskGetId(child) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(l)
								)
								,

							/* Function: flatMapInterp(l1, e1) //interprets the regular expression e1 with every element in list l1
								if (l1.isEmpty)	Set()
								if !(l1.isEmpty) interpNorm((r2, v1.head)) ++ flatMapInterpNorm(v1.tail, r2)
							*/
								//if (l1.isEmpty)
								SELECT (
									(t : Rep[ITask], in : Rep[IInput]) =>
										makeValue(flatMapInterpTag + "#val0.0")(taskGetId(t),__anythingAsUnit(-1))
								) FROM (
									taskRel, inputRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput]) =>
										taskGetTag(t) == __anythingAsUnit(flatMapInterpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == __anythingAsUnit(-1)
								)
								,
								//if !(l1.isEmpty)
								//Task creation
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList], e1 : Rep[IExp]) =>
										newTask(flatMapInterpTag + "#call1.0")(
											taskGetId(t),
											__anythingAsUnit(interpTag),
											__anythingAsUnit(0),
											createInput(Seq(expGetExpKey(e1),listGetHead(l1))))
								) FROM (
									taskRel, inputRel, listRel, expressionRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList], e1 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(flatMapInterpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(l1) AND
											inputGetParams(in)(1) == expGetExpKey(e1)
								)
								,
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList], e1 : Rep[IExp]) =>
										newTask(flatMapInterpTag + "#rec1.1")(
											taskGetId(t),
											__anythingAsUnit(flatMapInterpTag),
											__anythingAsUnit(1),
											createInput(Seq(listGetTail(l1),expGetExpKey(e1))))
								) FROM (
									taskRel, inputRel, listRel, expressionRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], l1 : Rep[IList], e1 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(flatMapInterpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == listGetListKey(l1) AND
											inputGetParams(in)(1) == expGetExpKey(e1)
								)
								,
								SELECT (
									(t : Rep[ITask], child1 : Rep[ITask], child2 : Rep[ITask], v1 : Rep[IValue], v2 : Rep[IValue]) =>
										newTask(flatMapInterpTag + "#call1.2")(
											taskGetId(t),
											__anythingAsUnit(appendTag),
											__anythingAsUnit(2),
											createInput(Seq(valGetValue(v1),valGetValue(v2)))
										)
								) FROM (
									taskRel, taskRel, taskRel, valueRel, valueRel
								) WHERE (
									(t : Rep[ITask], child1 : Rep[ITask], child2 : Rep[ITask], v1 : Rep[IValue], v2 : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(flatMapInterpTag) AND
											taskGetParentId(child1) == taskGetId(t) AND
											taskGetIndex(child1) == __anythingAsUnit(0) AND
											taskGetParentId(child2) == taskGetId(t) AND
											taskGetIndex(child2) == __anythingAsUnit(1) AND
											valGetId(v1) == taskGetId(child1) AND
											valGetId(v2) == taskGetId(child2)
								)
								,
								//propagate value
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										makeValue(flatMapInterpTag + "#val1.3")(taskGetId(t), valGetValue(v))

								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(flatMapInterpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(2) AND
											valGetId(v) == taskGetId(child)
								)
								,


						    /* Function: Interpreter.interp(e1, s1) //Matches the regular expression e1 on string s1
						    	if (e1.isInstanceOf[Terminal] && s1.startsWith(e1.s))  Set(s1.substring(e1.s.length))
								else if (e1.isInstanceOf[Terminal] && !(s1.startsWith(e1.s))) Set()
								else if (e1.isInstanceOf[Alt]) interpNorm((e1.r1,s1)) ++ interpNorm((e1.r2,s1))
								else if (e._1.isInstanceOf[Sequence]) flatMapInterpNorm(interpNorm((t1.r1,s1)),t1.r2)
								else if (e1.isInstanceOf[Asterisk]) interpNorm((Sequence(e1.r1, e1),s1)) + s1
						    */
								//if (e1.isInstanceOf[Terminal] ...
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], e : Rep[IExp], s : Rep[IList]) =>
										newTask(interpTag + "#call0.0")(
											taskGetId(t),
											__anythingAsUnit(startsWithTag),
										    __anythingAsUnit(0),
										    createInput(Seq(listGetListKey(s),expGetParams(e)(0)))
										)
								) FROM (
									taskRel, inputRel, expressionRel, listRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], e : Rep[IExp], s : Rep[IList]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == expGetExpKey(e) AND
											inputGetParams(in)(1) == listGetListKey(s) AND
											conditionTerminal(e)
								)
								,
								//... && s1.startsWith(e1.s))
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], e : Rep[IExp]) =>
										newTask(interpTag + "#call0.1")(
											taskGetId(t),
											__anythingAsUnit(sizeTag),
										    __anythingAsUnit(1),
											createInput(Seq(expGetParams(e)(0)))
										)
								) FROM (
									taskRel, taskRel, valueRel, inputRel, expressionRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], e : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetId(child) == valGetId(v) AND
											taskGetIndex(child) == __anythingAsUnit(0) AND
											valGetValue(v) == true AND
											inputGetInputKey(in) == taskGetInputKey(t) AND
											expGetExpKey(e) == inputGetParams(in)(0)

								)
								,
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], e : Rep[IExp]) =>
										newTask(interpTag + "#call0.2")(
											taskGetId(t),
											__anythingAsUnit(sublistTag),
											__anythingAsUnit(2),
											createInput(Seq(inputGetParams(in)(1),valGetValue(v)))
										)
								) FROM (
									taskRel, taskRel, valueRel, inputRel, expressionRel
								) WHERE (
								(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], e : Rep[IExp]) =>
									taskGetTag(t) == __anythingAsUnit(interpTag) AND
										taskGetParentId(child) == taskGetId(t) AND
										taskGetId(child) == valGetId(v) AND
										taskGetIndex(child) == __anythingAsUnit(1) AND
										inputGetInputKey(in) == taskGetInputKey(t) AND
										expGetExpKey(e) == inputGetParams(in)(0)

								)
								,
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										makeValue(interpTag + "#val0.0")(taskGetId(t),createList(Seq(valGetValue(v))))
								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetId(child) == valGetId(v) AND
											taskGetIndex(child) == __anythingAsUnit(2)
								)
								,
						        //... && !s1.startsWith(e1.s))
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										makeValue(interpTag + "#val0.1")(taskGetId(t), __anythingAsUnit(-1))
								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetId(child) == valGetId(v) AND
											taskGetIndex(child) == __anythingAsUnit(0) AND
											valGetValue(v) == false
								)
								,

								//if (e1.isInstanceOf[Alt]) interpNorm((e1.r1,s1)) ++ interpNorm((e1.r2,s1))
								//interpret left child
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp], r1 : Rep[IExp]) =>
										newTask(interpTag + "#rec1.0")(
											taskGetId(t),
											__anythingAsUnit(interpTag),
											__anythingAsUnit(3),
											createInput(Seq(expGetExpKey(r1),inputGetParams(in)(1)))
										)
								) FROM (
									taskRel, inputRel, expressionRel, expressionRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp], r1 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == expGetExpKey(e1) AND
											conditionAlt(e1) AND
											expGetChildren(e1)(0) == expGetExpKey(r1)
								)
						        ,
						        //interpret right child
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp], r2 : Rep[IExp]) =>
										newTask(interpTag + "#rec1.1")(
											taskGetId(t),
											__anythingAsUnit(interpTag),
											__anythingAsUnit(4),
											createInput(Seq(expGetExpKey(r2),inputGetParams(in)(1)))
										)
								) FROM (
									taskRel, inputRel, expressionRel, expressionRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp], r2 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == expGetExpKey(e1) AND
											conditionAlt(e1) AND
											expGetChildren(e1)(1) == expGetExpKey(r2)
								)
								,
								//append interp(r1) and interp(r2)
								SELECT (
									(t : Rep[ITask], child1 : Rep[ITask], child2 : Rep[ITask], v1 : Rep[IValue], v2 : Rep[IValue]) =>
										newTask(interpTag + "#call1.2")(
											taskGetId(t),
											__anythingAsUnit(appendTag),
											__anythingAsUnit(5),
											createInput(Seq(valGetValue(v1),valGetValue(v2)))
										)
								) FROM (
									taskRel, taskRel, taskRel, valueRel, valueRel
								) WHERE (
									(t : Rep[ITask], child1 : Rep[ITask], child2 : Rep[ITask], v1 : Rep[IValue], v2 : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child1) == taskGetId(t) AND
											taskGetIndex(child1) == __anythingAsUnit(3) AND
											taskGetParentId(child2) == taskGetId(t) AND
											taskGetIndex(child2) == __anythingAsUnit(4) AND
											valGetId(v1) == taskGetId(child1) AND
											valGetId(v2) == taskGetId(child2)
								)
								,
								//propagate value
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										makeValue(interpTag + "#val1.3")(taskGetId(t), valGetValue(v))

								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(5) AND
											valGetId(v) == taskGetId(child)
								)
								,

								//if (e._1.isInstanceOf[Sequence]) flatMapInterpNorm(interpNorm((t1.r1,s1)),t1.r2)
								//Interpret left child
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp]) =>
										newTask(interpTag + "#rec2.0")(
											taskGetId(t),
											__anythingAsUnit(interpTag),
											__anythingAsUnit(6),
											createInput(Seq(expGetChildren(e1)(0),inputGetParams(in)(1)))
										)
								) FROM (
									taskRel, inputRel, expressionRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == expGetExpKey(e1) AND
											conditionSequence(e1)
								)
								,
								//flatmap
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], e1 : Rep[IExp]) =>
										newTask(__anythingAsUnit(interpTag) + "#call2.1")(
											taskGetId(t),
											__anythingAsUnit(flatMapInterpTag),
											__anythingAsUnit(7),
											createInput(Seq(valGetValue(v),expGetChildren(e1)(1)))
										)
								) FROM (
									taskRel, taskRel, valueRel, inputRel, expressionRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput], e1 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == expGetExpKey(e1) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(6) AND
											valGetId(v) == taskGetId(child)
								)
								,
								//Propagate value
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										makeValue(__anythingAsUnit(interpTag) + "#val2.2")(taskGetId(t), valGetValue(v))

								) FROM (
									taskRel, taskRel, valueRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(7) AND
											valGetId(v) == taskGetId(child)
								)
								,

								//if (e1.isInstanceOf[Asterisk]) interpNorm((Sequence(e1.r1, e1),s1)) + s1
								SELECT (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp]) =>
										newTask(__anythingAsUnit(interpTag) + "#rec3.0")(
											taskGetId(t),
											__anythingAsUnit(interpTag),
											__anythingAsUnit(8),
											createInput(Seq(createSequenceExpression(Seq(expGetChildren(e1)(0), expGetExpKey(e1)),Seq()) ,inputGetParams(in)(1)))
										)
								) FROM (
									taskRel, inputRel, expressionRel
								) WHERE (
									(t : Rep[ITask], in : Rep[IInput], e1 : Rep[IExp]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetInputKey(t) == inputGetInputKey(in) AND
											inputGetParams(in)(0) == expGetExpKey(e1) AND
											conditionAsterisk(e1)
								)
								,
								//Propagate value
								SELECT (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput]) =>
										makeValue(__anythingAsUnit(interpTag) + "#val3.1")(taskGetId(t), consList(inputGetParams(in)(1), valGetValue(v).AsInstanceOf[ListKey]))
								) FROM (
									taskRel, taskRel, valueRel, inputRel
								) WHERE (
									(t : Rep[ITask], child : Rep[ITask], v : Rep[IValue], in : Rep[IInput]) =>
										taskGetTag(t) == __anythingAsUnit(interpTag) AND
											taskGetParentId(child) == taskGetId(t) AND
											taskGetIndex(child) == __anythingAsUnit(8) AND
											valGetId(v) == taskGetId(child) AND
											inputGetInputKey(in) == taskGetInputKey(t)
								)
						)
					)
				}
			)

		}


		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		Predef.println(printer.quoteRelation(valuesInternal))

		val values : Relation[IValue] = valueRelation(valuesInternal)
		val tasks = taskRelation(valuesInternal).asMaterialized
		val result = new MaterializedMap[TaskKey, Any]

		values.addObserver(result)
	}


}

trait RegExpKind
case object TerminalKind extends RegExpKind
case object AltKind extends RegExpKind
case object AsteriskKind extends RegExpKind
case object SequenceKind extends RegExpKind

trait RecType
case class ValueType(value : Interpreter.IValue) extends RecType
case class InputType(input : Interpreter.IInput) extends RecType
case class TaskType(task : Interpreter.ITask) extends RecType
case class ExpType(exp : Interpreter.IExp) extends RecType
case class ListType(l : Interpreter.IList) extends RecType


