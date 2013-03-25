Wrappers for the org.scala.virtualiuez projects.
The wrappers re-install the projects under the original scala artifactId and groupId, but with a different version number (xxxx-virtualized)
The reason is that the maven scala plugin only allows to change version numbers in the maven coordinates of the scala compiler.