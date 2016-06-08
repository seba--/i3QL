import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

/** Project */
name := "remote-playground"

version := "0.0.1"

organization := "de.tud.cs.st"

parallelExecution in Test := false

logBuffered in Test := false

scalaVersion := "2.11.2"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

libraryDependencies ++= Seq(
	"EPFL" %% "lms" % "latest.integration",
	"com.typesafe.akka" %% "akka-remote" % akkaVersion,
	"com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
)

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

// make sure that MultiJvm test are compiled by the default test compilation
compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test)

// make sure that MultiJvm tests are executed by the default test target,
// and combine the results from ordinary test and multi-jvm tests
executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
	case (testResults, multiNodeResults)  =>
		val overall =
			if (testResults.overall.id < multiNodeResults.overall.id)
				multiNodeResults.overall
			else
				testResults.overall
		Tests.Output(overall,
			testResults.events ++ multiNodeResults.events,
			testResults.summaries ++ multiNodeResults.summaries)
}