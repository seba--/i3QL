/** Project */
name := "db-interface"

version := "0.0.1"

organization := "de.tud.cs.st"

javaOptions in Test += "-Xmx4G"

parallelExecution in Test := false

fork in Test := true

logBuffered in Test := false

libraryDependencies in ThisBuild ++= Seq(
	"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
	"de.tud.cs.st" %% "idb-runtime" % "latest.integration"
)

