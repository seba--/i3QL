/** Project */
name := "db"

version := "0.0.1"

javaOptions in Test += "-Xmx4G"

parallelExecution in Test := false

fork in Test := true

logBuffered in Test := false