package sae.interpreter

import idb.SetTable
import idb.syntax.iql._
import idb.syntax.iql.IR._
import sae.interpreter.schema.{Literal, Syntax}



/**
 * @author Mirko Köhler
 */
abstract class Interpreter[V : Manifest] {

	type Value = (Int, V)
	type Definition = (Int, Syntax, Seq[Int])

	def literal(in : Int) : V

	def interpret(syntax : Syntax, values : V*) : V

	private def literalPriv : Rep[Seq[Int] => V] = staticData (
		(l : Seq[Int]) => literal(l.head)
	)

	private def interpretPriv : Rep[((Syntax, V, V)) => V] = staticData (
		(t : (Syntax, V, V)) => interpret(t._1, t._2, t._3)
	)

	private def syntaxIsLiteral : Rep[Syntax => Boolean] = staticData (
		(s : Syntax) => s == Literal
	)

	val expressions : Table[Definition] = SetTable[Definition]

	val literals : Relation[Definition] =
		SELECT (*) FROM expressions WHERE ((exp : Rep[Definition]) => syntaxIsLiteral(exp._2))

	val nonliterals : Relation[Definition] =
		(SELECT (*) FROM expressions) EXCEPT literals

	private val interpretLiterals : Relation[Value] =
		SELECT ((e : Rep[Definition])=> (e._1, literalPriv(e._3))) FROM literals

	private val unnestedNonLiterals : Relation[(Definition, Int)] =
		UNNEST(nonliterals, (_ : Rep[Definition])._3)

	private var freshID = 0

	private def fresh() : Int = {
		freshID = freshID + 1
		freshID
	}

	def define(syntax : Syntax, reference : Int*) : Int = {
		val id = fresh()
		expressions add (id, syntax, reference)
		id
	}



	val values : Relation[Value] =
		WITH RECURSIVE (
			(vQuery : Rep[Query[Value]]) =>
				interpretLiterals UNION ALL (
					SELECT
						((d1  : Rep[(Definition, Int)], d2 : Rep[(Definition,Int)], v1 : Rep[Value], v2 : Rep[Value]) => (d1._1._1, interpretPriv(d1._1._2, v1._2, v2._2)))
					FROM
						(unnestedNonLiterals, unnestedNonLiterals, vQuery, vQuery)
					WHERE (
						(d1  : Rep[(Definition, Int)], d2 : Rep[(Definition,Int)], v1 : Rep[Value], v2 : Rep[Value]) =>
							(d1._1._1 == d2._1._1) AND //Same expression
							(d1._2 < d2._2) AND  //other parameter
							(v1._1 == d1._2) AND //value 1 exists
							(v2._1 == d2._2)
						)
				)
			)

}


