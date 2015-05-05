/** Project */
name := "wordcount"

version := "0.0.1"

scalaVersion in ThisBuild := "2.10.2-RC2"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

libraryDependencies in ThisBuild ++= Seq(
	"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
	"de.tud.cs.st" %% "idb-runtime" % "latest.integration",
    "org.apache.spark" %% "spark-core" % "1.3.0"
    //"org.apache.hadoop" %% "hadoop-client" % "latest.integration"
)