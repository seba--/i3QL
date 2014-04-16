package sae.interpreter



/**
 * @author Mirko Köhler
 */
object BooleanInterpreter extends Interpreter[Boolean] {

	override def interpret(syntax: Syntax, values: Seq[Boolean]): Boolean = {
		syntax match {
			case And => values.fold(true)(_ & _)
		}
	}

}
