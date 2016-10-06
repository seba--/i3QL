/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.demo.chartparser

import idb.algebra.print.RelationalAlgebraPrintPlan
import scala.virtualization.lms.common.{TupledFunctionsExp, StaticDataExp, StructExp, ScalaOpsPkgExp}
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.lms.extensions.FunctionUtils
import idb.algebra.ir.{RelationalAlgebraIRRecursiveOperators, RelationalAlgebraIRSetTheoryOperators, RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators}
import idb.Relation
import idb.observer.Observer

/**
 *
 * @author Ralf Mitschke
 */
object Main
{

    val printer = new RelationalAlgebraPrintPlan
    {
        val IR = idb.syntax.iql.IR
    }

    def main (args: Array[String]) {

		val parser = SentenceParser

		val words = List("green", "ideas", "sleep").zipWithIndex

		val result = parser.success.asMaterialized

		val passiveEdges = parser.passiveEdges.asMaterialized
	//	val unknownEdges = parser.unknownEdges.asMaterialized

		words.foreach(parser.input.add)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		println(printer.quoteRelation(parser.passiveEdges))

		val pp = new RelationPrinter(parser.passiveEdges)

		println("result")
		result.asList.foreach(println)

		println("passive")
		passiveEdges.foreach(println)

		parser.input remove (("green", 0))

		println("result")
		result.asList.foreach(println)

		println("passive")
		passiveEdges.foreach(println)



		/*
		val inputString = args(0)

        val words = inputString.split(" ")

        val parser = KilburySentenceParser


        val result = parser.success.asMaterialized


        words.zipWithIndex.foreach (parser.input += _)

        implicit val ord = parser.edgeOrdering

        //result.asList.sorted.foreach (println)

		val isValid = result.asList.asInstanceOf[List[(Int, Int, String, Seq[String])]].foldLeft(0)((prev : Int,e : (Int, Int, String, Seq[String])) => Math.max(prev,e._2)) == words.length
		println("The sentence is valid? " + isValid)

                                                */

		/*     parser.input -=("like", 2)

				parser.input +=("with", 2)

				result.asList.sorted.foreach (println)  */
    }

	class RelationPrinter[A](relation : Relation[A]) extends Observer[A] {
		relation.addObserver(this)

		override def updated(oldV: A, newV: A): Unit =
			println("update : " + oldV + " -> " + newV)

		override def endTransaction(): Unit =
			println("endTransaction")

		override def removed(v: A): Unit =
			println("removed : " + v)

		override def added(v: A): Unit =
			println("added : " + v)

	//	override def removedAll(vs: Seq[A]): Unit = println("addedAll : " + vs)

	//	override def addedAll(vs: Seq[A]): Unit =  println("removedAll : " + vs)
	}

}
