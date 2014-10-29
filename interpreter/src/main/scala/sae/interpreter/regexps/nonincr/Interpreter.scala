package sae.interpreter.regexps.nonincr

import idb.algebra.print.RelationalAlgebraPrintPlan
import sae.interpreter.regexps._
import sae.interpreter.utils.{IntKeyGenerator, KeyMapTable, MaterializedMap}


object Interpreter {

	def main(args : Array[String]) {
		val tab : ITable = createIExp

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

		val s1 = "babbac"
		val s2 = "bacab"

		val values = getValues(tab)
		val matTasks = tab._2.asMaterialized

		val ref1 = addInput(exp6, Set.empty[String] + s1, tab)

/*			println("Before update: "
				+ values(ref1)
				+ "[Key = " + ref1 + "]") */
			println("exps")
			tab._1.foreach(println)
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

	def matchRegexp(e: Exp, c: Text): Boolean = interp(e, c).contains("")

	def interp(e : Exp, c : Text): Value = e match {
		case Terminal(s2) => c flatMap (s => if (s.startsWith(s2)) Some(s.substring(s2.length)) else None)
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

	def interp2(e : (Exp, Text)) : Value = {
		var result : Value = null
		if (e._1.isInstanceOf[Terminal]) {
			val t1 : Terminal = e._1.asInstanceOf[Terminal]
			val res : Value = e._2 flatMap (s => if (s.startsWith(t1.s)) Some(s.substring(t1.s.length)) else None)
			result = res
		} else if (e._1.isInstanceOf[Alt]) {
			val t1 : Alt = e._1.asInstanceOf[Alt]
			val v1 : Value = interp2((t1.r1,e._2)) //Task1
			val v2 : Value = interp2((t1.r2,e._2)) //Task2
			val res : Value = v1 ++ v2
			result = res
		} else if (e._1.isInstanceOf[Asterisk] && e._2.nonEmpty) {
			val t1 : Asterisk = e._1.asInstanceOf[Asterisk]
			val v1 : Value = interp2((t1.r1,e._2)) //Task3
			val v2 : Value = interp2((t1, valueToText(v1))) //Task4
			val res : Value = e._2 ++ v2
			result = res
		} else if (e._1.isInstanceOf[Asterisk] && !e._2.nonEmpty) {
			val res : Value = e._2
			result = res
		} else if (e._1.isInstanceOf[Sequence]) {
			val t1 : Sequence = e._1.asInstanceOf[Sequence]
			val v1 : Value = interp2((t1.r1,e._2)) //Task5
			val v2 : Value = interp2((t1.r2,valueToText(v1))) //Task6
			val res : Value = v2
			result = res
		}

		return result
	}

	def valueToText(v : Value) : Text =
		v

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


	trait RegExpKind
	case object TerminalKind extends RegExpKind
	case object AltKind extends RegExpKind
	case object AsteriskKind extends RegExpKind
	case object SequenceKind extends RegExpKind



	/*
		Type Declarations
	 */
	type Exp = RegExp
	type Text = Set[String]

	type ExpKind = RegExpKind
	type ExpKey = Int
	type ExpNode = (ExpKind, Seq[ExpKey], Seq[Any])

	type Input = (ExpKey, Text)
	type InputKey = Int

	type TaskKey = Int
	type TaskIndex = Int
	type Value = Set[String]
	type Task = (TaskKey, TaskIndex, InputKey) //Parent Task, Index of the task within one branch, input parameters

	type IExp = (ExpKey, ExpNode)
	type ITask = (TaskKey, Task)
	type IValue = (TaskKey, Value)
	type IInput = (InputKey, Input)

	type IExpKindTable = KeyMapTable[ExpKey, ExpNode]
	type ITaskTable = KeyMapTable[TaskKey,Task]
	type IInputTable = KeyMapTable[InputKey, Input]

	type ITable = (IInputTable, ITaskTable, IExpKindTable)

	private def expKindTable(tab : ITable) : IExpKindTable = tab._3
	//private def TextTable(tab : ITable) : ITextTable = tab._2
	private def taskTable(tab : ITable) : ITaskTable = tab._2
	private def inputTable(tab : ITable) : IInputTable = tab._1



	def addInput(exp : Exp, text : Text, tab : ITable) : TaskKey = {
		val expKey = insertExp(exp, tab)
		val inKey = inputTable(tab).add((expKey, text))
	//	val TextKey = TextTable(tab).add(c)
		taskTable(tab).add((tab._2.keyGenerator.fresh(), -1, inKey))

	}

	private def insertExp(e : Exp, tab : ITable) : ExpKey = e match {
		case Terminal(s) => {
			insertExp(TerminalKind, Seq(), Seq(s), tab)
		}
		case Alt(e1,e2) => {
			val t1 = insertExp(e1, tab)
			val t2 = insertExp(e2, tab)

			insertExp(AltKind, Seq(t1, t2), Seq(), tab)
		}
		case Sequence(e1, e2) => {
			//Add subtasks
			val t1 = insertExp(e1, tab)
			val t2 = insertExp(e2, tab)
			//Add this task with reference to the subtasks
			insertExp(SequenceKind, Seq(t1, t2), Seq(), tab)
		}

		case Asterisk(e1) => {
			val t1 = insertExp(e1, tab)
			insertExp(AsteriskKind, Seq(t1), Seq(), tab)
		}
	}

	private def insertExp(k: ExpKind, kids: Seq[ExpKey], param: Seq[Any], tab: ITable): ExpKey = {
		println("Insert expression: " + k)
		val key = expKindTable(tab).add((k, kids, param))
		key
	}






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

	def getValues(tab : ITable) : PartialFunction[TaskKey,Value] with Iterable[(TaskKey,Value)] = {
		val interpreter = new IncrementalInterpreter(tab)
		interpreter.result
	}

	def createIExp : ITable = (
		new KeyMapTable[InputKey, Input](new IntKeyGenerator),
		new KeyMapTable[TaskKey, Task](new IntKeyGenerator),
		new KeyMapTable[ExpKey, ExpNode](new IntKeyGenerator)
	)


	/**
	 * This class implements the incremental part of the interpreter.
	 * @param tab The underlying table for tasks and incremental data structures.
	 */
	private class IncrementalInterpreter(val tab : ITable) {

		import idb.syntax.iql.IR._
		import idb.syntax.iql._

	/*	protected val interpretRep : Rep[((ExpNode, Text, Seq[Value])) => Value] = staticData (
			(t : (ExpNode, Text, Seq[Value])) => {
				val e : ExpNode = t._1
				val c : Text = t._2
				val values : Seq[Value] = t._3
				var result : Value = null
				if (e._1 == TerminalKind) {
					val param0 : String = e._3(0).asInstanceOf[String]
					val res : Value = c flatMap (s => if (s.startsWith(param0)) scala.Some(s.substring(param0.length)) else scala.Some(s))
					result = res
				} else if (e._1 == AltKind) {
					val value0: Value = values(0)
					val value1: Value = values(1)
					val res: Value = value0 ++ value1
					result = res
				} else if (e._1 == AsteriskKind && c.nonEmpty) {
					val value0: Value = values(0)
					val value1: Value = values(1)
					val res: Value = c ++ value1
					result = res
				} else if (e._1 == AsteriskKind && !c.nonEmpty) {
					val res: Value = c
					result = res
				} else if (e._1 == SequenceKind) {
					val value0: Value = values(0)
					val value1: Value = values(1)
					val res: Value = value1
					result = res
				}
				result
			}
		)        */


		protected val createValueCond1 : Rep[((ExpNode, Text, Seq[Value])) => Value] = staticData (
			(t : (ExpNode, Text, Seq[Value])) => {
				val e : ExpNode = t._1
				val c : Text = t._2
				val values : Seq[Value] = t._3

				val param0 : String = e._3(0).asInstanceOf[String]
				val res : Value = c flatMap (s => if (s.startsWith(param0)) scala.Some(s.substring(param0.length)) else scala.None)
				res
			}
		)

		protected val createValueCond2 : Rep[((ExpNode, Text, Seq[Value])) => Value] = staticData (
			(t : (ExpNode, Text, Seq[Value])) => {
				val e : ExpNode = t._1
				val c : Text = t._2
				val values : Seq[Value] = t._3

				val value0: Value = values(0)
				val value1: Value = values(1)
				val res: Value = value0 ++ value1
				res
			}
		)

		protected val createValueCond3 : Rep[((ExpNode, Text, Seq[Value])) => Value] = staticData (
			(t : (ExpNode, Text, Seq[Value])) => {
				val e : ExpNode = t._1
				val c : Text = t._2
				val values : Seq[Value] = t._3

				//val value0: Value = values(0)
				val value1: Value = values(0)
				val res: Value = c ++ value1
				res
			}
		)

		protected val createValueCond4 : Rep[((ExpNode, Text, Seq[Value])) => Value] = staticData (
			(t : (ExpNode, Text, Seq[Value])) => {
				val e : ExpNode = t._1
				val c : Text = t._2
				val values : Seq[Value] = t._3

				val res: Value = c
				res
			}
		)

		protected val createValueCond5 : Rep[((ExpNode, Text, Seq[Value])) => Value] = staticData (
			(t : (ExpNode, Text, Seq[Value])) => {
				val e : ExpNode = t._1
				val c : Text = t._2
				val values : Seq[Value] = t._3

				val value0: Value = values(0)
				val value1: Value = values(1)
				val res: Value = value1
				res
			}
		)

		protected val isTerminalKind : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == TerminalKind
		)

		protected val isSequenceKind : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == SequenceKind
		)

		protected val isAltKind : Rep[IExp => Boolean] = staticData (
			(e : IExp) => e._2._1 == AltKind
		)

		protected val isAsteriskKindCond1 : Rep[((IInput, IExp, ITask)) => Boolean] = staticData (
			(t : (IInput, IExp, ITask)) => t._2._2._1 == AsteriskKind && t._1._2._2.nonEmpty
		)

		protected val isAsteriskKindCond2 : Rep[((IInput, IExp, ITask)) => Boolean] = staticData (
			(t : (IInput, IExp, ITask)) => t._2._2._1 == AsteriskKind && !t._1._2._2.nonEmpty
		)


		protected val valueToTextRep : Rep[Value => Text] = staticData (
			valueToText
		)

		protected val asteriskCond1MakeValue : Rep[((IInput, ITask, IValue)) => Value] = staticData (
			(t : (IInput, ITask, IValue)) => t._1._2._2 ++ t._3._2
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
			(r : RecType) => {
				//Predef.println(r)
				r.asInstanceOf[ExpType].exp
			}
		)


		private def taskGetId(t : Rep[ITask]) : Rep[TaskKey] = t._1
		private def taskGetParentId(t : Rep[ITask]) : Rep[TaskKey] = t._2._1
		private def taskGetInputKey(t : Rep[ITask]) : Rep[InputKey] = t._2._3
		private def taskGetIndex(t : Rep[ITask]) : Rep[TaskIndex] = t._2._2
	//	private def taskGetText(t : Rep[ITask]) : Rep[Text] = t._2._4

		private def inputGetInputKey(t : Rep[IInput]) : Rep[InputKey] = t._1
		private def inputGetExpKey(t : Rep[IInput]) : Rep[ExpKey] = t._2._1
		private def inputGetText(t : Rep[IInput]) : Rep[Text] = t._2._2


		private def expGetExpKey(e : Rep[IExp]) : Rep[ExpKey] = e._1
		private def expGetExp(e : Rep[IExp]) : Rep[ExpNode] = e._2
		private def expGetKind(e : Rep[IExp]) : Rep[ExpKind] = e._2._1
		private def expGetChildren(e : Rep[IExp]) : Rep[Seq[ExpKey]] = e._2._2
		private def expGetParams(e : Rep[IExp]) : Rep[Seq[Any]] = e._2._3

		private def valGetId(v : Rep[IValue]) : Rep[TaskKey] = v._1
		private def valGetValue(v : Rep[IValue]) : Rep[Value] = v._2


		protected def inputRelation(r : Rep[Query[RecType]]) : Rep[Query[IInput]] =
			SELECT ((e : Rep[RecType]) => recTypeToInput(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsInput(e))

		protected def valueRelation(r : Rep[Query[RecType]]) : Rep[Query[IValue]] =
			SELECT ((e : Rep[RecType]) => recTypeToValue(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsValue(e))

		protected def taskRelation(r : Rep[Query[RecType]]) : Rep[Query[ITask]] =
			SELECT ((e : Rep[RecType]) => recTypeToTask(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsTask(e))

		protected def expRelation(r : Rep[Query[RecType]]) : Rep[Query[IExp]] =
			SELECT ((e : Rep[RecType]) => recTypeToExp(e)) FROM r WHERE ( (e : Rep[RecType]) => recTypeIsExp(e))


		private val valueToRecType : Rep[IValue => RecType] = staticData (
			(r : IValue) => {
				Predef.println("Value -> " + r)
				ValueType(r)
			}
		)

		private val taskToRecType : Rep[((String, ITask)) => RecType] = staticData (
			(r : (String, ITask)) => {
				Predef.println("Task[" + r._1 + "]\t-> " + r)
				TaskType(r._2)
			}
		)

		private val expToRecType : Rep[IExp => RecType] = staticData (
			(r : IExp) => {
				Predef.println("Exp   -> " + r)
				ExpType(r)
			}
		)

		private val inputToRecType : Rep[IInput => RecType] = staticData (
			(r : IInput) => {
				Predef.println("Input -> " + r)
				InputType(r)
			}
		)

		private val createInput : Rep[((ExpKey, Text)) => InputKey] = staticData (
			(e : (ExpKey, Text)) => inputTable(tab).add(e._1,e._2)
		)

		protected def makeInput(k : Rep[InputKey], in : Rep[Input]) : Rep[RecType] =
			inputToRecType((k,in))


		protected def makeValue(k : Rep[TaskKey], v : Rep[Value]) : Rep[RecType] =
			valueToRecType((k,v))

		protected def makeTask(k : Rep[TaskKey], t : Rep[Task]) : Rep[RecType] =
			taskToRecType("",(k,t))

		protected def makeExp(k : Rep[ExpKey], e : Rep[ExpNode]) =
			expToRecType((k,e))

		protected def newTask(tag : Rep[String])(t : Rep[Task]) : Rep[RecType] =
			taskToRecType(tag,(freshTaskKey(0), t))


		protected val freshTaskKey : Rep[Int => TaskKey] = staticData (
			(i : Int) => tab._2.keyGenerator.fresh()
		)

		protected val inputRec : Relation[RecType] =
			SELECT ((e : Rep[IInput]) => makeInput(e._1, e._2)) FROM tab._1

		protected val tasksRec : Relation[RecType] =
			SELECT ((e : Rep[ITask]) => makeTask(e._1, e._2)) FROM tab._2

		protected val expRec : Relation[RecType] =
			SELECT ((e : Rep[IExp]) => makeExp(e._1, e._2)) FROM tab._3

		protected val recursionBase :  Relation[RecType] =
			tasksRec UNION ALL (expRec) UNION ALL (inputRec)

		private def unionPrivate[T : Manifest](queries : Rep[Query[T]]*) : Rep[Query[T]] = {
			if (queries.length == 1)
				queries.head
			else
				queries.head UNION ALL (unionPrivate[T](queries.tail : _*))
		}

		private val ZERO : Rep[Int] = __anythingAsUnit(0)
		private val ONE : Rep[Int] = 1
		private val TWO : Rep[Int] = 2


				private val valuesInternal : Rep[Query[RecType]] = {
			WITH RECURSIVE (
				(rec:  Rep[Query[RecType]]) => {
					val expressionRel = expRelation(rec)
					val taskRel = taskRelation(rec)
					val valueRel = valueRelation(rec)
					val inputRel = inputRelation(rec)
					recursionBase UNION ALL (
						unionPrivate (
							//Terminal Text -> value
							SELECT (
								(in : Rep[IInput], e : Rep[IExp], t : Rep[ITask]) =>
									makeValue(taskGetId(t), createValueCond1(expGetExp(e), inputGetText(in), Seq()))
							) FROM (
								inputRel, expressionRel, taskRel
							) WHERE (
								(in : Rep[IInput], e : Rep[IExp], t : Rep[ITask]) =>
									isTerminalKind(e) AND
									inputGetInputKey(in) == taskGetInputKey(t) AND
									inputGetExpKey(in) == expGetExpKey(e)

							)
						,
							//Alt task creation left
							SELECT (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									newTask("Alt1")(taskGetId(parentTask), ZERO, createInput(e._1, inputGetText(in)))
							) FROM (
								inputRel, expressionRel, expressionRel, taskRel
							) WHERE (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									isAltKind(parent) AND
									inputGetInputKey(in) == taskGetInputKey(parentTask) AND
									expGetExpKey(parent) == inputGetExpKey(in) AND
									expGetExpKey(e) == expGetChildren(parent)(0)
							)
						,
							//Alt task creation right
							SELECT (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									newTask("Alt2")(taskGetId(parentTask), ONE, createInput(e._1, inputGetText(in)))
							) FROM (
								inputRel, expressionRel, expressionRel, taskRel
								) WHERE (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									isAltKind(parent) AND
										inputGetInputKey(in) == taskGetInputKey(parentTask) AND
										expGetExpKey(parent) == inputGetExpKey(in) AND
										expGetExpKey(e) == expGetChildren(parent)(1)
								)
						,
							//Alt value propagation
							SELECT (
								(in : Rep[IInput], e: Rep[IExp], childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									makeValue(taskGetId(tasks._1), createValueCond2(expGetExp(e), inputGetText(in), Seq(childVals._1._2, childVals._2._2)))
							) FROM (
								inputRel, expressionRel, SELECT (*) FROM (valueRel, valueRel), SELECT (*) FROM (taskRel, taskRel, taskRel)
							) WHERE (
								(in : Rep[IInput], e: Rep[IExp], childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									isAltKind(e) AND
									inputGetInputKey(in) == taskGetInputKey(tasks._1) AND
									expGetExpKey(e) == inputGetExpKey(in) AND
									valGetId(childVals._1) == taskGetId(tasks._2) AND
									valGetId(childVals._2) == taskGetId(tasks._3) AND
									taskGetParentId(tasks._2) == taskGetId(tasks._1) AND
									taskGetParentId(tasks._3) == taskGetId(tasks._1) AND
									taskGetIndex(tasks._2) == ZERO AND
									taskGetIndex(tasks._3) == ONE

								)
						,
							//Ast Cond1 task 1
							SELECT (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									newTask("Ast1")(taskGetId(parentTask), ZERO, createInput(e._1,inputGetText(in)))
							) FROM (
								inputRel, expressionRel, expressionRel, taskRel
							) WHERE (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									isAsteriskKindCond1((in, parent, parentTask)) AND
									inputGetInputKey(in) == taskGetInputKey(parentTask) AND
									expGetExpKey(parent) == inputGetExpKey(in) AND
									expGetExpKey(e) == expGetChildren(parent)(0)
							)
						,
							//Ast Cond1 task 2
							SELECT (
								(in : Rep[IInput], parent: Rep[IExp], childVal : Rep[IValue],  tasks : Rep[(ITask, ITask)]) =>
									newTask("Ast2")(taskGetId(tasks._1), ONE, createInput(expGetExpKey(parent), valueToTextRep(valGetValue(childVal))))
							) FROM (
								inputRel, expressionRel, valueRel, SELECT (*) FROM (taskRel, taskRel)
							) WHERE (
								(in : Rep[IInput], parent: Rep[IExp], childVal : Rep[IValue], tasks : Rep[(ITask, ITask)]) =>
									isAsteriskKindCond1((in, parent,  tasks._1)) AND
									inputGetInputKey(in) == taskGetInputKey(tasks._1) AND
									expGetExpKey(parent) == inputGetExpKey(in) AND
									taskGetId(tasks._1) == taskGetParentId(tasks._2) AND
									taskGetIndex(tasks._2) == ZERO AND
									taskGetId(tasks._2) == valGetId(childVal)
								)
						,
							//Ast Cond1 Value propagation
							SELECT (
								(in : Rep[IInput], e : Rep[IExp], childVal : Rep[IValue], tasks : Rep[(ITask, ITask)]) =>
									makeValue(taskGetId(tasks._1),createValueCond3(expGetExp(e), inputGetText(in), Seq(valGetValue(childVal))))
							) FROM (
								inputRel, expressionRel, valueRel, SELECT(*) FROM (taskRel, taskRel)
							) WHERE (
								(in : Rep[IInput], e : Rep[IExp], childVal : Rep[IValue], tasks : Rep[(ITask, ITask)]) =>
									isAsteriskKindCond1((in,e,tasks._1)) AND
									inputGetInputKey(in) == taskGetInputKey(tasks._1) AND
									expGetExpKey(e) == inputGetExpKey(in) AND
									valGetId(childVal) == taskGetId(tasks._2) AND
									taskGetParentId(tasks._2) == taskGetId(tasks._1) AND
									taskGetIndex(tasks._2) == ONE
							)
						,
							//Ast Cond2 value creation
							SELECT(
								(in : Rep[IInput], e : Rep[IExp], t : Rep[ITask]) =>
									makeValue(taskGetId(t), inputGetText(in))
							) FROM (
								inputRel, expressionRel, taskRel
							) WHERE (
								(in : Rep[IInput], e : Rep[IExp], t : Rep[ITask]) =>
								isAsteriskKindCond2((in,e,t)) AND
								inputGetInputKey(in) == taskGetInputKey(t) AND
								expGetExpKey(e) == inputGetExpKey(in)
							)
						,
							//Seq task propagation left
							SELECT(
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									newTask("Seq1")(taskGetId(parentTask), ZERO, createInput(e._1,inputGetText(in)))
							) FROM(
								inputRel, expressionRel, expressionRel, taskRel
							) WHERE (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], parentTask : Rep[ITask]) =>
									isSequenceKind(parent) AND
									inputGetInputKey(in) == taskGetInputKey(parentTask) AND
									expGetExpKey(parent) == inputGetExpKey(in) AND
									expGetExpKey(e) == expGetChildren(parent)(0)
							)
						,
							//Seq child(0) value -> child(1) Text
							SELECT(
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], child0Val: Rep[IValue], tasks : Rep[(ITask, ITask)]) =>
									newTask("Seq2")(taskGetId(tasks._1), ONE, createInput(e._1, valueToTextRep(valGetValue(child0Val)))) //TODO How to create new input
							) FROM (
								inputRel, expressionRel, expressionRel, valueRel, SELECT (*) FROM (taskRel, taskRel)
							) WHERE (
								(in : Rep[IInput], e: Rep[IExp], parent: Rep[IExp], child0Val: Rep[IValue], tasks : Rep[(ITask, ITask)]) =>
									isSequenceKind(parent) AND
									inputGetInputKey(in) == taskGetInputKey(tasks._1) AND
									expGetExpKey(parent) == inputGetExpKey(in) AND
									expGetExpKey(e) == expGetChildren(parent)(1) AND
									taskGetId(tasks._1) == taskGetParentId(tasks._2) AND
									taskGetIndex(tasks._2) == ZERO AND
									taskGetId(tasks._2) == valGetId(child0Val)

							)
						,
							//Seq value propagation
							SELECT (
								(in : Rep[IInput], e: Rep[IExp], childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									makeValue(taskGetId(tasks._1), createValueCond5(expGetExp(e), inputGetText(in), Seq(childVals._1._2, childVals._2._2)))
							) FROM (
								inputRel, expressionRel, SELECT (*) FROM (valueRel, valueRel), SELECT (*) FROM (taskRel, taskRel, taskRel)
							) WHERE (
								(in : Rep[IInput], e: Rep[IExp], childVals : Rep[(IValue, IValue)], tasks : Rep[(ITask, ITask, ITask)]) =>
									isSequenceKind(e) AND
									inputGetInputKey(in) == taskGetInputKey(tasks._1) AND
									expGetExpKey(e) == inputGetExpKey(in) AND
									valGetId(childVals._1) == taskGetId(tasks._2) AND
									valGetId(childVals._2) == taskGetId(tasks._3) AND
									taskGetParentId(tasks._2) == taskGetId(tasks._1) AND
									taskGetParentId(tasks._3) == taskGetId(tasks._1) AND
									taskGetIndex(tasks._2) == ZERO AND
									taskGetIndex(tasks._3) == ONE
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
		val result = new MaterializedMap[TaskKey, Value]

		values.addObserver(result)
	}


}

trait RecType { }

case class ValueType(value : Interpreter.IValue) extends RecType
case class InputType(input : Interpreter.IInput) extends RecType
case class TaskType(task : Interpreter.ITask) extends RecType
case class ExpType(exp : Interpreter.IExp) extends RecType



