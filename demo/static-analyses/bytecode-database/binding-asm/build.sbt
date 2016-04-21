/** Project */
name := "db-binding-asm"

version := "0.0.1"

organization := "de.tud.cs.st"

javaOptions in Test += "-Xmx4G"

parallelExecution in Test := false

logBuffered in Test := false

libraryDependencies in ThisBuild ++= Seq(
	"org.ow2.asm" % "asm-all" % "latest.integration",
	"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
	"de.tud.cs.st" %% "idb-runtime" % "latest.integration",
	"de.tud.cs.st" %% "test-data" % "latest.integration"
)