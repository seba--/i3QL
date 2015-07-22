/** Project */
name := "idb-runtime"

version := "0.0.1"

organization := "de.tud.cs.st"

scalaOrganization := "org.scala-lang"

scalaVersion := "2.11.7"

scalacOptions := Seq(
    "-feature"
)

libraryDependencies ++= Seq(
    "com.google.guava" % "guava" % "latest.integration"
)

parallelExecution in Test := false

logBuffered in Test := false