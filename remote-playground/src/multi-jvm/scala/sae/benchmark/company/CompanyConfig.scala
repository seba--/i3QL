package sae.benchmark.company

import idb.query.taint.Taint
import sae.benchmark.BenchmarkConfig

/**
  * Created by mirko on 08.11.16.
  */
trait CompanyConfig extends BenchmarkConfig {
	val priorityPublic : Int
	val priorityProduction : Int
	val priorityPurchasing : Int
	val priorityEmployees : Int
	val priorityClient : Int

	val permissionsPublic : Set[String]
	val permissionsProduction : Set[String]
	val permissionsPurchasing : Set[String]
	val permissionsEmployees : Set[String]
	val permissionsClient : Set[String]

	val labelPublic : Taint
	val labelProduction : Taint
	val labelPurchasing : Taint
	val labelEmployees : Taint
}

sealed trait DefaultPriorityConfig extends CompanyConfig {
	override val priorityPublic : Int = 4
	override val priorityProduction : Int = 4
	override val priorityPurchasing : Int = 4
	override val priorityEmployees : Int = 4
	override val priorityClient : Int = 12

	override val permissionsPublic : Set[String] = Set("lab:public")
	override val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient : Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	override val labelPublic : Taint = Taint("lab:public")
	override val labelProduction : Taint = Taint("lab:production")
	override val labelPurchasing : Taint = Taint("lab:purchasing")
	override val labelEmployees : Taint = Taint("lab:employees")
}

sealed trait ClientPriorityConfig extends CompanyConfig {
	override val priorityPublic : Int = 1
	override val priorityProduction : Int = 1
	override val priorityPurchasing : Int = 1
	override val priorityEmployees : Int = 1
	override val priorityClient : Int = 0

	override val permissionsPublic : Set[String] = Set("lab:public")
	override val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	override val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	override val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	override val permissionsClient : Set[String] = Set("lab:client")

	override val labelPublic : Taint = Taint("lab:client")
	override val labelProduction : Taint = Taint("lab:client")
	override val labelPurchasing : Taint = Taint("lab:client")
	override val labelEmployees : Taint = Taint("lab:client")
}

trait Measure4000DefaultPriorityConfig extends DefaultPriorityConfig {
	override val benchmarkConfig : String = "measure-4000-all"
	override val measureIterations : Int = 4000
	override val warmup = true
}

trait AWS4000DefaultPriorityConfig extends DefaultPriorityConfig {
	override val benchmarkConfig : String = "aws-4000-all"
	override val measureIterations : Int = 4000
	override val warmup = true

	override val benchmarkNumber: Int = 6
}

trait AWS4000ClientPriorityConfig extends ClientPriorityConfig {
	override val benchmarkConfig : String = "aws-4000-client"
	override val measureIterations : Int = 4000
	override val warmup = true

	override val benchmarkNumber: Int = 8
}

trait Measure4000ClientPriorityConfig extends ClientPriorityConfig {
	override val benchmarkConfig : String = "measure-4000-client"
	override val measureIterations : Int = 4000
	override val warmup = true


}

trait Test4000DefaultPriorityConfig extends DefaultPriorityConfig {
	override val benchmarkConfig : String = "test-4000-all"
	override val measureIterations : Int = 4000
	override val warmup = false
}

trait Test4000ClientPriorityConfig extends ClientPriorityConfig {
	override val benchmarkConfig : String = "test-4000-client"
	override val measureIterations : Int = 4000
	override val warmup = false
}

trait Test10DefaultPriorityConfig extends DefaultPriorityConfig {
	override val benchmarkConfig : String = "test-10-all"
	override val measureIterations : Int = 10
	override val warmup = false
}

trait Test10ClientPriorityConfig extends ClientPriorityConfig {
	override val benchmarkConfig : String = "test-10-client"
	override val measureIterations : Int = 10
	override val warmup = false
}