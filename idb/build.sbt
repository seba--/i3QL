name := "idb"

scalaVersion in ThisBuild := virtScala

organization in ThisBuild := "de.tud.cs.st"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

scalacOptions in ThisBuild ++= Seq(
    "-feature",
    "-Yvirtualize"
)

libraryDependencies in ThisBuild ++= Seq(
    "com.novocode" % "junit-interface" % "latest.integration" % "test" ,
    "org.scala-lang.virtualized" % "scala-library" % virtScala,
    "org.scala-lang.virtualized" % "scala-compiler" % virtScala
)


// tests are not thread safe
parallelExecution in Test := false

