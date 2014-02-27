import sbt.Keys._

/** Project */
name := "analyses-profiler"

version := "0.0.1"

organization := "de.tud.cs.st"

parallelExecution in Test := false

logBuffered in Test := false

libraryDependencies ++= Seq(
    "com.google.code.findbugs" % "findbugs" % "latest.integration"
)