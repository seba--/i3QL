== Table of Contents ==
	1. Abstract
	2. Tools
	3. Install guide for i3Ql
	4. Package Overview
	5. Parser example walkthrough
	6. Step-by-step guides
	

== 1. Abstract ==	
	
	An incremental computation updates its result based on a change to its input, which is often an order of magnitude faster than a recomputation from scratch. In particular, incrementalization can make expensive computations feasible for settings that require short feedback cycles, such as interactive systems, IDEs, or (soft) real-time systems. 
	We presented i3QL, a general-purpose programming language for specifying incremental computations. i3QL provides a declarative SQL-like syntax and is based on incremental versions of operators from relational algebra, enriched with support for general recursion. We integrated i3QL into Scala as a library, which enables programmers to use regular Scala code for non-incremental subcomputations of an i3QL query and to easily integrate incremental computations into larger software projects. To improve performance, i3QL optimizes user-defined queries by applying algebraic laws and partial evaluation.
	
== 2. Tools ==
	
	We implemented i3Ql in Scala (http://www.scala-lang.org). Scala is an object-oriented, functional programming language that run on the Java Virtual Machine (JVM). i3Ql queries can be directly used in your Scala code.
	We use Lightweight Modular Staging (LMS, http://scala-lms.github.io) in order to compile our queries. LMS allows us to generate tree representations of functions and queries. This intermediate representation is used to optimize our queries. Further, LMS can produce Scala code out of these representations and compile the code during runtime. 
	In order to built our project we use the SBT (http://www.scala-sbt.org), a build tool for Scala projects. 
	
== 3. Install guide for i3Ql ==
		In order to install the project, you have to follow these steps:

		1. To build this project you need to have JDK, SBT and GIT installed
			Download and install JDK: http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
			Download and install SBT: http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html
			Download and install GIT: https://github.com

		2. Download and build the LMS project

				$ git clone https://github.com/TiarkRompf/virtualization-lms-core.git

			Go to the root directory and install the project using SBT. You need to be on the branch 'develop'

				$ cd virtualization-lms-core
				$ git checkout develop
				$ sbt publish-local

		3. Download and build the i3Ql project.

				$ git clone https://github.com/seba--/i3QL.git

			Go to the root directory and install the project using SBT. Currently you need to be on the branch 'master' (the default one).

				$ cd i3Ql
				$ git checkout master
				$ sbt publish-local
	
	-- 1.2 Using i3Ql in your own project
		1. Add the dependencies to your SBT build file
			In order to use i3Ql for your own project you have to add the following library dependencies to your SBT build:		
			
				libraryDependencies in ThisBuild ++= Seq(
					"de.tud.cs.st" %% "idb-syntax-iql" % "latest.integration",
					"de.tud.cs.st" %% "idb-runtime" % "latest.integration"
				)
				
			Additionally, you have to add Scala Virtualized as Scala organization to your SBT build file:
			
				scalaVersion in ThisBuild := "2.10.2"
				scalaOrganization in ThisBuild := "org.scala-lang.virtualized"
		
		2. Import i3Ql in your Scala project
			If you want to write a query, you have to write the following import statements
				import idb.syntax.iql._
				import idb.syntax.iql.IR._
			In order to use tables you also have to import them
				import idb.SetTable
		
		3. Use i3Ql
			Now, you can use i3Ql.
				Example:
				def foo() {
					import idb.syntax.iql._
					import idb.syntax.iql.IR._
					import idb.SetTable
					
					//Create a new table. Note, that this has changed slightly from the paper.
					val table : Table[Int] = SetTable.empty[Int]()
					
					//Write your query.
					val query = SELECT (*) FROM table	

					//Add elements to the table.
					table += 1
				}

== 4. Package Overview ==

	This section gives an overview of all packages of the i3ql projects.
	
	Following are all packages of the i3Ql project:
		idb - Incremental Database
		idb/runtime - Runtime classes/engine (e.g. operators) for relations
		idb/syntax-iql - Syntax of the query language and transforming of queries into intermediate representation
		idb/intermediate-representation - Representation and optimization of queries as syntax trees. Uses LMS.
		idb/runtime-compiler - Transforms syntax trees to runnable "code"
		idb/integration-test - End-to-end user tests of queries
		idb/schema-examples - University database example that is used for testing
		idb/annotations - Implements custom annotations
		
		bytecode-database - Database for Java Bytecode which is used for static analyses
		bytecode-database/interface - Implements the interface for Java bytecode databases
		bytecode-database/binding-asm - Concrete implementation of the bytecode database interface using ASM (http://asm.ow2.org)
		
		analyses - Demo static analyses of Findbugs and Metrics
		analyses/findbugs - Contains the static analyses
		analyses/metrics - Implements metrics 
		analyses/profiler - Contains classes to profile the static analyses (time and memory profiler)
		
== 5. Parser example walkthrough ==

	This section describes how the parser can be run within the Scala interpreter. The parser project is located /demo/chart-parser.

	-- 5.1 Executing the parser --
		1. Open a terminal in the main directory of the project.
		
		2. Move to the project folder of the parser
			$ cd /demo/chart-parser
			
		3. Start the SBT-Scala console
			$ sbt console
			
		Now the Scala console is started with access to all classes of the parser. In order to use the parser consider the following example:
		
		4. Import the parser.
			> import idb.demo.chartparser.SentenceParser 

		5. Create a new value to have easier access to the parser
			> val parser = SentenceParser

		6. Enter a sentence as list and zip the list with its indices. The zip is necessary since the parser expects tuples of words and their position in the sentence.
			> val words = List("green", "ideas", "sleep").zipWithIndex

		7. Materialize the result relation in order to store its elements.
			> val result = parser.success.asMaterialized

		8. Add the words to the parser.
			> words.foreach(parser.input.add)

		9. Print the result relation 
			> result.foreach(println)
			
		The result relation stores the indices for all possible sub-sentences, i.e. the index of the first word and the index of the last word for every possible sentence. In our example, there are the pairs (0,2) and (1,2). The pair (0,2) says that there is a valid sentence from word 0 ("green") to word 2 ("sleep"), which is the whole sentence. That means that the input sentence is valid. Additionally, there is the edge (1,2). That means that there is also a valid sub-sentence, i.e. List("ideas", "sleep") would also be a valid sentence.
	
	-- 5.2 Incremental addition --
		10. Add a new word to the sentence
			> parser.input += ("furiously", 3)
			
		11. Print the result relation 
			> result.foreach(println)
			
		The result relation has been updated. The result relation contains the pair (0,3) and thus the whole sentence is valid.
	
	-- 5.3 Incremental update --
		12. Update the first element from green to yellow
			> parser.input update (("green",0),("yellow",0))
			
		13. Print the result relation 
			> result.foreach(println)

		As you can see, the pair (0,3) has disappeared from the result relation and thus the sentence is not valid anymore.	However, we can add a new rule to our grammar.	
		
		14. Add the rule for adjective "yellow" to the grammar
			> parser.terminals += ("yellow", "Adj")
			
		15. Print the result relation 
			> result.foreach(println)
			
		Now, the pair (0,3) is in our result relation and thus the sentence is valid.
		
		
== 6. Executing analyses ==

	This section contains step-by-step guides for executing the analyses described in the paper (Section 6). 

	-- 6.1. Step-by-step instructions for executing a single analysis  and output the results

		1. Open a terminal in the main directory of the project.

		2. Execute sbt in the terminal
			$ sbt
			
		3. Change project to the findbugs analyses
			> project analyses-findbugs
			
		4. Run the analysis with its name as argument. The analysis will be executed on JDK 1.7.0 64-bit. The names of all analyses are shown in the list below (Section 7.3).
			> run-main sae.analyses.findbugs.AnalysisRunner FI_USELESS
	
	-- 6.2 Step-by-step instructions for executing the analyses benchmarks:

		1. Open a terminal in the main directory of the project.
	
		2. Execute sbt in the terminal
			$ sbt
			
		3. Change project to the analysis profiler
			> project analyses-profiler
			
		4. Run the profiler with a properties file as argument. There is a properties file for every analysis, which is named <analysis-name>.properties, e.g. FI_USELESS.properties. Analysis names can be found in the list below (Section 7.3).
			> run-main sae.analyses.profiler.ASMDatabaseReplayTimeRunner FI_USELESS.properties
			
		7. The results can be found as .csv file at /benchmarks/time/default/<analsis-name>.properties.csv 
			
	-- 6.3 List of possible analyses
		List of possible analyses (the intent of the analyses can be found at http://findbugs.sourceforge.net/bugDescriptions.html):
			BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION
			CI_CONFUSED_INHERITANCE
			CN_IDIOM
			CN_IDIOM_NO_SUPER_CALL
			CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE
			CO_ABSTRACT_SELF
			CO_SELF_NO_OBJECT
			DM_GC
			DM_RUN_FINALIZERS_ON_EXIT
			DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT
			DP_DO_INSIDE_DO_PRIVILEGED
			EQ_ABSTRACT_SELF
			FI_PUBLIC_SHOULD_BE_PROTECTED
			FI_USELESS
			IMSE_DONT_CATCH_IMSE
			MS_PKGPROTECT
			MS_SHOULD_BE_FINAL
			NONE
			SE_BAD_FIELD_INNER_CLASS
			SE_NO_SUITABLE_CONSTRUCTOR
			SS_SHOULD_BE_STATIC
			SW_SWING_METHODS_INVOKED_IN_SWING_THREAD
			UG_SYNC_SET_UNSYNC_GET
			UR_UNINIT_READ_CALLED_FROM_SUPER_CONSTRUCTOR
			UUF_UNUSED_FIELD
		
	
