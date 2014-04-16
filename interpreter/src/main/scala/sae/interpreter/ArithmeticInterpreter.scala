package sae.interpreter



/**
 * @author Mirko Köhler
 */
object ArithmeticInterpreter extends Interpreter[Double] {

	override def interpret(syntax: Syntax, values: Seq[Double]): Double = {
		syntax match {
			case Plus => values.fold(0.0)(_ + _)
			case Max => values.fold(0.0)(Math.max)
			case Abs => Math.abs(values(0))
		}
	}



}
