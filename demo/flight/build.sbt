/** Project */
name := "flight"

version := "0.0.1"

scalaVersion in ThisBuild := "2.11.2"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

libraryDependencies in ThisBuild ++= Seq(
	"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
	"de.tud.cs.st" %% "idb-runtime" % "latest.integration",
	"com.typesafe.akka" % "akka-actor_2.11" % akkaVersion
)