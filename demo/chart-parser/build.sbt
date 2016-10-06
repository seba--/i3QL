/** Project */
name := "chart-parser"

version := "0.0.1"

scalaVersion in ThisBuild := "2.11.2"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

libraryDependencies in ThisBuild ++= Seq(
	"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
	"de.tud.cs.st" %% "idb-runtime" % "latest.integration"
)