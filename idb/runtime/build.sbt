/** Project */
name := "idb-runtime"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % "latest.integration",
    "com.google.guava" % "guava" % "latest.integration",
    "junit" % "junit" % "latest.integration" % "test"
)