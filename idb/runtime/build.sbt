/** Project */
name := "idb-runtime"

version := "0.0.1"

organization := "de.tud.cs.st.idb"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
    "EPFL" %% "lms" % "latest.integration",
    "com.google.guava" % "guava" % "latest.integration",
    "junit" % "junit" % "latest.integration" % "test"
)