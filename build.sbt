name := "sae"

scalaVersion in ThisBuild := virtScala

organization in ThisBuild := "de.tud.cs.st"

scalaOrganization in ThisBuild := "org.scala-lang.virtualized"

scalacOptions in ThisBuild ++= Seq(
    "-feature",
    "-Yvirtualize"
)

libraryDependencies in ThisBuild ++= Seq(
    "com.novocode" % "junit-interface" % "latest.integration" % "test->default" ,
    "org.scala-lang.virtualized" % "scala-library" % virtScala,
    "org.scala-lang.virtualized" % "scala-compiler" % virtScala,
    "junit" % "junit" % "4.8.1" % "test"
)

//SHow additional test info
testOptions in ThisBuild += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")
