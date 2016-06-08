import sbt._

object sae extends Build {

  /*
    Project IDB
  */
	lazy val idb = Project(id = "idb", base = file("idb"))
		.aggregate (runtime, annotations, intermediateRepresentation, schemaExamples, runtimeCompiler, syntax, integrationTest)

	lazy val runtime = Project(id = "idb-runtime", base = file("idb/runtime"))

	lazy val annotations = Project(id = "idb-annotations", base = file("idb/annotations"))

	lazy val intermediateRepresentation = Project(id = "idb-intermediate-representation", base = file("idb/intermediate-representation"))
		.dependsOn (annotations % "compile;test")

	lazy val schemaExamples = Project(id = "idb-schema-examples", base = file("idb/schema-examples"))
		.dependsOn (annotations % "compile;test")

	lazy val runtimeCompiler = Project(id = "idb-runtime-compiler", base = file("idb/runtime-compiler"))
		.dependsOn (schemaExamples % "compile;test", runtime % "compile;test", intermediateRepresentation % "compile;test")

	lazy val syntax = Project(id = "idb-syntax-iql", base = file("idb/syntax-iql"))
		.dependsOn (runtimeCompiler % "compile;test", schemaExamples % "compile;test")

	lazy val integrationTest = Project(id = "idb-integration-test", base = file("idb/integration-test"))
		.dependsOn (schemaExamples % "test", syntax % "test", intermediateRepresentation % "test")
		

  /*
    Project Hospital example
   */
  lazy val hospitalExample = Project(id = "hospital-example", base = file("hospital-example"))
	  .dependsOn(syntax % "compile;test").dependsOn(runtime % "compile;test")

  /*
    Project Remote playground
   */
  lazy val remotePlayground = Project(id = "remote-playground", base = file("remote-playground"))
    .dependsOn(syntax % "compile;test")

  /*
    Project Test Data
  */ 
  
  lazy val testData = Project(id = "test-data", base = file("test-data"))
   
  /*
    Root Project
  */  
  lazy val root = Project(id = "sae", base = file("."))
		.aggregate (runtime, annotations, intermediateRepresentation, schemaExamples, runtimeCompiler, syntax, integrationTest, hospitalExample, testData)
  
    

	

  val virtScala = Option(System.getenv("SCALA_VIRTUALIZED_VERSION")).getOrElse("2.11.2")

  val akkaVersion = "2.4.4"
}
