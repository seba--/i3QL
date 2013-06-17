package idb.macrosrun

import idb.macrosimpl._
import idb.syntax.iql._
import UniversityDatabase._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.06.13
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */
object Main {

	def main(args : Array[String]) {
		//val b = Macros.map[Int,Int](List(1,2,3,4,5),n => n * n)

		//println(b)

	//	Macros.printf("hello %s!", "world")

		IQLMacros.compile(SELECT (*) FROM students)
	}

}
