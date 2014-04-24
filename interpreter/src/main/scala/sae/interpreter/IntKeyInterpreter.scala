package sae.interpreter

import idb.{Table, SetTable}

/**
 * @author Mirko Köhler
 */
abstract class IntKeyInterpreter[SyntaxKind : Manifest, Value : Manifest] extends Interpreter[Int, SyntaxKind, Value] {

	private var freshID = 0


	def fresh(): Int = {
		freshID = freshID + 1
		freshID
	}

	def define(syntax : SyntaxKind, reference : Int*) : Int = {
		val exp = Left ((syntax, reference))
		val id = fresh()
		expressions add ((id, exp))
		id
	}

	def define(value : Value) : Int = {
		val exp = Right(value)
		val id = fresh()
		expressions add (id, exp)
		id
	}

	def update(oldD : IDef, newD : IDef) = {
		expressions.update(oldD, newD)
	}



}
