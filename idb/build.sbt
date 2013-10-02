name := "idb"

scalaVersion in ThisBuild := "2.10.2-RC1"

organization in ThisBuild := "de.tud.cs.st"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

scalacOptions in ThisBuild ++= Seq(
    "-feature"
)

libraryDependencies in ThisBuild ++= Seq(
    "com.novocode" % "junit-interface" % "latest.integration" % "test" ,
    "org.scala-lang.virtualized" % "scala-library" % "2.10.2-RC1",
    "org.scala-lang.virtualized" % "scala-compiler" % "2.10.2-RC1",
    "org.scala-lang" % "scala-actors" % "2.10.2-RC1"
)


// tests are not thread safe
parallelExecution in Test := false

