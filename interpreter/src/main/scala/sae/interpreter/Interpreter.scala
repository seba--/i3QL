package sae.interpreter

import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
abstract class Interpreter[Key : Manifest, SyntaxKind : Manifest, Value : Manifest] {

	type IDef = (Key, Either[(SyntaxKind,Seq[Key]),Value])
	type IValue = (Key, Value)
	type IComp = (Key, SyntaxKind, Seq[Key])

	def interpret(syntax : SyntaxKind, values : Seq[Value]) : Value

	protected val definitionAsValue : Rep[IDef => IValue] = staticData (
		(d : IDef) => (d._1, d._2.right.get)
	)

	protected val definitionAsComposite : Rep[IDef => IComp] = staticData (
		(d : IDef) => {
			val e : (SyntaxKind, Seq[Key]) = d._2.left.get
			(d._1,e._1,e._2)
		}
	)

	protected val interpretPriv : Rep[((SyntaxKind, Seq[Value])) => Value] = staticData (
		(t : (SyntaxKind, Seq[Value])) => interpret(t._1, t._2)
	)

	protected val definitionIsLiteral : Rep[IDef => Boolean] = staticData (
		(s : IDef) => s._2.isRight
	)

	val expressions : Table[IDef]

	protected val literals : Relation[(Key, Value)] =
		SELECT (definitionAsValue(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => definitionIsLiteral(d))

	protected val nonliterals : Relation[(Key, SyntaxKind, Seq[Key])] =
		SELECT (definitionAsComposite(_ : Rep[IDef])) FROM expressions WHERE ((d : Rep[IDef]) => NOT (definitionIsLiteral(d)))

	protected val nonLiteralsOneArgument : Relation[IComp] =
		SELECT (*) FROM nonliterals WHERE ((d : Rep[IComp]) => d._3.length == 1)

	protected val nonLiteralsTwoArguments : Relation[IComp] =
		SELECT (*) FROM nonliterals WHERE ((d : Rep[IComp]) => d._3.length == 2)

	protected val nonLiteralsThreeArguments : Relation[IComp] =
		SELECT (*) FROM nonliterals WHERE ((d : Rep[IComp]) => d._3.length == 3)

	val values : Relation[IValue] =
		WITH RECURSIVE (
			(vQuery : Rep[Query[IValue]]) => {
				literals UNION ALL (
					queryToInfixOps (
						SELECT (
							(d  : Rep[IComp], v1 : Rep[IValue], v2 : Rep[IValue]) =>
								(d._1, interpretPriv (d._2, Seq(v1._2, v2._2)))
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
									(d._1, interpretPriv (d._2, Seq(v1._2, v2._2, v3._2)))
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
									(d._1, interpretPriv (d._2, Seq(v1._2)))
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


