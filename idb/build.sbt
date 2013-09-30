name := "idb"

scalaVersion in ThisBuild := "2.10.2-RC1"

organization in ThisBuild := "de.tud.cs.st"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

scalacOptions in ThisBuild ++= Seq(
    "-feature"
)

libraryDependencies in ThisBuild ++= Seq(
    "junit" % "junit" % "latest.integration" % "test"
)

