/** Project */
name := "interpreter"

version := "0.0.1"

organization := "de.tud.cs.st"

parallelExecution in Test := false

logBuffered in Test := false

scalaVersion := "2.11.2"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

libraryDependencies ++= Seq(
	"EPFL" %% "lms" % "latest.integration",
  "de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
  "de.tud.cs.st" %% "idb-runtime" % "latest.integration"
)

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
