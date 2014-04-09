package sae.interpreter

import idb.syntax.iql.IR._
import sae.interpreter.schema.{Plus, Literal, Syntax}


/**
 * @author Mirko Köhler
 */
object ArithmeticInterpreter extends Interpreter[Int] {

	override def literal(in: Int): Int =
		in

	override def interpret(syntax: Syntax, values: Int*): Int = {
		syntax match {
			case Literal => values(0)
			case Plus => values(0) + values(1)
		}
	}

}
