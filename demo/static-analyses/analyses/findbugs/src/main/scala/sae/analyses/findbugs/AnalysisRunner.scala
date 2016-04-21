package sae.analyses.findbugs

import sae.bytecode.ASMDatabaseFactory
import idb.syntax.iql._
import idb.syntax.iql.IR._
import idb.MaterializedView

/**
 * @author Mirko Köhler
 */
object AnalysisRunner {

	def main(args : Array[String]) {

		val analysisName = args(0)

		val database = ASMDatabaseFactory.create
		val analysis : Relation[_] = new Analyses(database).apply(analysisName)

		val counter = count(analysis)

		val stream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")

		database.addArchive(stream)

		Predef.println("Number of bugs found: " + counter.asList.head)



	}

	private def count(r : Relation[_]) : MaterializedView[Int] = {
		val c : Relation[Int] = SELECT (COUNT(*)) FROM r
		c.asMaterialized
	}

}
