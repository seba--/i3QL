package sae.interpreter

import sae.interpreter.schema.{Max, Plus}
import idb.algebra.print.RelationalAlgebraPrintPlan


/**
 * @author Mirko Köhler
 */
object Main {

	def main(args: Array[String]) {

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}


		val matval = ArithmeticInterpreter.values.asMaterialized
		val matexp = ArithmeticInterpreter.expressions.asMaterialized


		val ref1 = ArithmeticInterpreter.define(42)
		val ref2 = ArithmeticInterpreter.define(23)
		val ref3 = ArithmeticInterpreter.define(100)
		val ref4 = ArithmeticInterpreter.define(3)
		val ref5 = ArithmeticInterpreter.define(6)

		Predef.println("Values ####################################")
		matval.foreach(Predef.println)

		val ref6 = ArithmeticInterpreter.define(Plus, ref1, ref2)
		val ref7 = ArithmeticInterpreter.define(Plus, ref6, ref3, ref4)

		Predef.println("Values ####################################")
		matval.foreach(Predef.println)

		val ref8 = ArithmeticInterpreter.define(Max, ref4, ref5)

		Predef.println("Values ####################################")
		matval.foreach(Predef.println)

		ArithmeticInterpreter.expressions.update( (ref1, Right(42)), (ref1, Right(420)) )

		Predef.println("Values ####################################")
		matval.foreach(Predef.println)

		ArithmeticInterpreter.expressions.update( (ref7, Left(Plus, ref6 :: ref3 :: ref4 :: Nil)), (ref7, Left(Plus, ref6 :: ref3 :: ref8 :: Nil)) )

		Predef.println("Values ####################################")
		matval.foreach(Predef.println)

	}

}
