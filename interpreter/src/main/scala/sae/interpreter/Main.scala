package sae.interpreter

import idb.MaterializedView
import sae.interpreter.schema.{Plus, Literal}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.syntax.iql._
import idb.syntax.iql.IR._

/**
 * @author Mirko Köhler
 */
object Main {

	def main(args: Array[String]) {

		val matval = ArithmeticInterpreter.values.asMaterialized
		val matexp = ArithmeticInterpreter.expressions.asMaterialized


		val lit42 = ArithmeticInterpreter.define(Literal, 42)
		val lit23 = ArithmeticInterpreter.define(Literal, 23)
		//val lit100 = ArithmeticInterpreter.define(Literal, 100)
		val plus42_23 = ArithmeticInterpreter.define(Plus, lit42, lit23)
		ArithmeticInterpreter.expressions.update((lit42, Literal, 42 :: Nil), (lit42, Literal, 1111 :: Nil))

		//ArithmeticInterpreter.expressions.add((4, Plus, 3 :: 2 :: Nil))


		ArithmeticInterpreter.expressions.endTransaction()

		Predef.println("Expressions ###############################")
		matexp.foreach(Predef.println)
		Predef.println("Values ####################################")
		matval.foreach(Predef.println)

	}

}
