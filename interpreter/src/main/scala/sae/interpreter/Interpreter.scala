package sae.interpreter

import idb.{IndexService, BagTable, SetTable}
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.interpreter.schema.{Syntax, Max, Plus}



/**
 * @author Mirko Köhler
 */
abstract class Interpreter[V : Manifest] {

	type Key = Int
	type Value = (Key, V)
	type Definition = (Key, Either[(Syntax,Seq[Int]),V])


	def interpret(syntax : Syntax, values : Seq[V]) : V

	private def keyFunction(e : Either[(Syntax,Seq[Int]),V]) : Key =
		e.hashCode()

	protected val definitionAsValue : Rep[Definition => (Int, V)] = staticData (
		(d : Definition) => (d._1, d._2.right.get)
	)

	protected val definitionAsComposite : Rep[Definition => (Int, Syntax, Seq[Int])] = staticData (
		(d : Definition) => {
			val e : (Syntax, Seq[Int]) = d._2.left.get
			(d._1,e._1,e._2)
		}
	)

	protected val interpretPriv : Rep[((Syntax, Seq[V])) => V] = staticData (
		(t : (Syntax, Seq[V])) => interpret(t._1, t._2)
	)

	protected val definitionIsLiteral : Rep[Definition => Boolean] = staticData (
		(s : Definition) => s._2.isRight
	)

	//protected val expressionTable : Table[Either[(Syntax,Seq[Int]),V]] = BagTable[Either[(Syntax,Seq[Int]),V]]

	val expressions : Table[Definition] = SetTable.empty()

	protected val literals : Relation[(Int, V)] =
		SELECT (definitionAsValue(_ : Rep[Definition])) FROM expressions WHERE ((d : Rep[Definition]) => definitionIsLiteral(d))

	protected val nonliterals : Relation[(Int, Syntax, Seq[Int])] =
		SELECT (definitionAsComposite(_ : Rep[Definition])) FROM expressions WHERE ((d : Rep[Definition]) => NOT (definitionIsLiteral(d)))

	protected val nonLiteralsTwoArguments : Relation [(Int, Syntax, Seq[Int])] =
		SELECT (*) FROM nonliterals WHERE ((d : Rep[(Int, Syntax, Seq[Int])]) => d._3.length == 2)

	protected val nonLiteralsThreeArguments : Relation[(Int, Syntax, Seq[Int])] =
		SELECT (*) FROM nonliterals WHERE ((d : Rep[(Int, Syntax, Seq[Int])]) => d._3.length == 3)

	val values : Relation[Value] =
			WITH RECURSIVE (
				(vQuery : Rep[Query[Value]]) => {
					literals UNION ALL (
						QueryInfixOps(
							SELECT (
								(d  : Rep[(Int, Syntax, Seq[Int])], v1 : Rep[Value], v2 : Rep[Value]) =>
									(d._1, interpretPriv (d._2, Seq(v1._2, v2._2)))
							) FROM (
								nonLiteralsTwoArguments, vQuery, vQuery
							) WHERE (
								(d  : Rep[(Int, Syntax, Seq[Int])], v1 : Rep[Value], v2 : Rep[Value]) =>
									(d._3(0) == v1._1) AND
									(d._3(1) == v2._1)
							)
						).UNION(
							ALL(
								SELECT (
									(d  : Rep[(Int, Syntax, Seq[Int])], v1 : Rep[Value], v2 : Rep[Value], v3 : Rep[Value]) =>
										(d._1, interpretPriv (d._2, Seq(v1._2, v2._2, v3._2)))
								) FROM (
									nonLiteralsThreeArguments, vQuery, vQuery, vQuery
								) WHERE (
									(d  : Rep[(Int, Syntax, Seq[Int])], v1 : Rep[Value], v2 : Rep[Value], v3 : Rep[Value]) =>
										(d._3(0) == v1._1) AND
											(d._3(1) == v2._1) AND
											(d._3(2) == v3._1)
								)
							)
						)
					)
				}
			)

	/*UNION (
			WITH RECURSIVE (
				(vQuery : Rep[Query[Value]]) => {
					literals UNION ALL (
						SELECT (
							(d  : Rep[(Int, Syntax, Seq[Int])], v1 : Rep[Value], v2 : Rep[Value], v3 : Rep[Value]) =>
								(d._1, interpretPriv (d._2, Seq(v1._2, v2._2, v3._2)))
						) FROM (
							nonLiteralsThreeArguments, vQuery, vQuery, vQuery
						) WHERE (
							(d  : Rep[(Int, Syntax, Seq[Int])], v1 : Rep[Value], v2 : Rep[Value], v3 : Rep[Value]) =>
								(d._3(0) == v1._1) AND
								(d._3(1) == v2._1) AND
								(d._3(2) == v3._1)
						)
					)
				}
			)
		)*/

	private var freshID = 0

	private def fresh() : Int = {
		freshID = freshID + 1
		freshID
	}

	def define(syntax : Syntax, reference : Int*) : Key = {
		val exp = Left ((syntax, reference))
		val id = fresh()
		expressions add (id, exp)
		id
	}

	def define(value : V) : Key = {
		val exp = Right(value)
		val id = fresh()
		expressions add (id, exp)
		id
	}
}


