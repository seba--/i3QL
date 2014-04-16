package sae.interpreter

import sae.interpreter.schema.{Max, Plus, Syntax}



/**
 * @author Mirko Köhler
 */
object ArithmeticInterpreter extends Interpreter[Double] {

	override def interpret(syntax: Syntax, values: Seq[Double]): Double = {
		syntax match {
			case Plus => values.foldLeft(0.0)(_ + _)
			case Max =>  values.foldLeft(0.0)(Math.max)
		}
	}



}
