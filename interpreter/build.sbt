/** Project */
name := "interpreter"

version := "0.0.1"

organization := "de.tud.cs.st"

parallelExecution in Test := false

logBuffered in Test := false

libraryDependencies ++= Seq(
	"EPFL" %% "lms" % "latest.integration"
)