package sae.interpreter




/**
 * @author Mirko Köhler
 */
object ArithmeticInterpreter extends IntKeyInterpreter[ArithmeticSyntax, Null, Double] {

	override def interpret(syntax: ArithmeticSyntax, c : Null, values: Seq[Double]): Double = {
		syntax match {
			case Plus => values.fold(0.0)(_ + _)
			case Max => values.fold(0.0)(Math.max)
			case Abs => Math.abs(values(0))
		}
	}

	import idb.syntax.iql.IR
	import idb.SetTable
	override val expressions : IR.Table[(Int, Either[(ArithmeticSyntax,Seq[Int]),Double])] = SetTable.empty[(Int, Either[(ArithmeticSyntax,Seq[Int]),Double])]


}
