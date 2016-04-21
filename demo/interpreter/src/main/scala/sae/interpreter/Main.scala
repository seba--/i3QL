package sae.interpreter



/**
 * @author Mirko Köhler
 */
object Main {

	def main(args: Array[String]) {
		import ArithmeticInterpreter._

		val matval = values(null).asMaterialized

		val ref1 = define(42)
		val ref3 = define(Abs, define(-100))
		val ref4 = define(3)
		val ref5 = define(6)

		println("Values ####################################")
		matval.foreach(println)

		val ref7 = define(Plus, ref1, define(-23))
		val ref8 = define(Plus, ref7, ref3, ref4)

		println("Values ####################################")
		matval.foreach(println)

		val ref9 = define(Max, ref4, ref5)

		println("Values ####################################")
		matval.foreach(println)

		update( (ref1, Right(42)), (ref1, Right(420)) )

		println("Values ####################################")
		matval.foreach(println)

		update( (ref8, Left(Plus, Seq(ref7, ref3, ref4))), (ref8, Left(Plus, Seq(ref7, ref9))) )

		println("Values ####################################")
		matval.foreach(println)

	}

}
