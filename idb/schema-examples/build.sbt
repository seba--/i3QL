/** Project */
name := "idb-schema-examples"

version := "0.0.1"

organization := "de.tud.cs.st.idb"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
    "org.scala-lang" % "virtualization-lms-core" % "latest.integration",
    "junit" % "junit" % "latest.integration" % "test"
)