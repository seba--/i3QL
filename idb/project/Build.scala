import sbt._

object idb extends Build {

	lazy val root = Project(id = "idb", base = file("."))
		.aggregate (runtime, annotations, intermediateRepresentation, schemaExamples, runtimeCompiler, syntax)

	lazy val runtime = Project(id = "idb-runtime", base = file("runtime"))

	lazy val annotations = Project(id = "idb-annotations", base = file("annotations"))

	lazy val intermediateRepresentation = Project(id = "idb-intermediate-representation", base = file("intermediate-representation"))
		.dependsOn (annotations)

	lazy val schemaExamples = Project(id = "idb-schema-examples", base = file("schema-examples"))
		.dependsOn (annotations)

	lazy val runtimeCompiler = Project(id = "idb-runtime-compiler", base = file("runtime-compiler"))
		.dependsOn (schemaExamples % "test->compile;compile->compile", runtime, intermediateRepresentation)

	lazy val syntax = Project(id = "idb-syntax-iql", base = file("syntax-iql"))
		.dependsOn (runtimeCompiler, schemaExamples % "test->compile;compile->compile")

	lazy val integrationTest = Project(id = "idb-integration-test", base = file("integration-test"))
		.dependsOn (schemaExamples % "test->compile;compile->compile", syntax)

  val virtScala = "2.10.2-RC1"


}
