import sbt._

object StaticAnalyses extends Build {


	/*
    Project Bytecode Database
	*/
	lazy val db = Project(id = "db", base = file("bytecode-database"))
		.aggregate (databaseInterface, bindingASM)

  lazy val databaseInterface = Project(id = "db-interface", base = file("bytecode-database/interface"))

	lazy val bindingASM = Project(id = "db-binding-asm", base = file("bytecode-database/binding-asm"))
    .dependsOn (databaseInterface % "compile")
    
  /*
    Project Analyses
  */

  	lazy val analyses = Project(id = "analyses", base = file("analyses"))
    	.aggregate (findbugs, metrics, profiler)
  
  	lazy val findbugs = Project(id = "analyses-findbugs", base = file("analyses/findbugs"))
    	.dependsOn(databaseInterface % "compile", bindingASM % "compile")
  
  	lazy val metrics = Project(id = "analyses-metrics", base = file("analyses/metrics"))
    	.dependsOn(databaseInterface % "compile", bindingASM % "compile")

	lazy val profiler = Project(id = "analyses-profiler", base = file("analyses/profiler"))
		.dependsOn(databaseInterface % "compile", bindingASM % "compile", findbugs % "compile;test", metrics % "compile;test")
   


   
  /*
    Root Project
  */  
  lazy val root = Project(id = "static-analyses", base = file("."))
		.aggregate (databaseInterface, bindingASM, findbugs, metrics, profiler)
  
    

	

  val virtScala = Option(System.getenv("SCALA_VIRTUALIZED_VERSION")).getOrElse("2.11.2")

  val akkaVersion = "2.3.12"
}
