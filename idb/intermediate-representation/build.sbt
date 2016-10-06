/** Project */
name := "idb-intermediate-representation"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % "latest.integration"
)

parallelExecution in Test := false

logBuffered in Test := false