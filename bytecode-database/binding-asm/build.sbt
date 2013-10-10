/** Project */
name := "db-binding-asm"

version := "0.0.1"

organization := "de.tud.cs.st"

libraryDependencies += "org.ow2.asm" % "asm-all" % "latest.integration"

parallelExecution in Test := false

logBuffered in Test := false