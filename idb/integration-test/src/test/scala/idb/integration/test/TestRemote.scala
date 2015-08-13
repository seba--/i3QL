package idb.integration.test

import java.util

import idb.algebra.ir._
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.annotations.RemoteHost
import idb.lms.extensions.FunctionUtils
import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import org.junit.Assert._
import org.junit.Test
import org.junit.Ignore
import idb.syntax.iql._
import UniversityDatabase._

import scala.virtualization.lms.common.{TupledFunctionsExp, StaticDataExp, StructExp, ScalaOpsPkgExp}


/**
 * @author Mirko Köhler
 */
class TestRemote extends UniversityTestData {



	@Test
	def testRemote(): Unit = {
		val q = plan(
			SELECT (*) FROM (remoteStudents, remoteRegistrations)
		)

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}

		val compiledQ = compile(q).asMaterialized

		println(printer.quoteRelation(q))
		println("\n\n")
		println(compiledQ.prettyprint("\t"))

		remoteStudents.add(johnDoe)
		remoteRegistrations.add(johnTakesEise)

		compiledQ.foreach(x => println(x))
	}

}
