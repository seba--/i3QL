package sae.benchmark.company

import idb.query.colors.Color
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

	val labelPublic : Color
	val labelProduction : Color
	val labelPurchasing : Color
	val labelEmployees : Color
}

trait Measure1000DefaultPriorityConfig extends CompanyConfig {
	override val benchmarkConfig : String = "measure-1000-all"
	override val measureIterations : Int = 1000
	override val warmup = true

	val priorityPublic : Int = 1
	val priorityProduction : Int = 1
	val priorityPurchasing : Int = 1
	val priorityEmployees : Int = 1
	val priorityClient : Int = 0

	val permissionsPublic : Set[String] = Set("lab:public")
	val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	val permissionsClient : Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	val labelPublic : Color = Color("lab:public")
	val labelProduction : Color = Color("lab:production")
	val labelPurchasing : Color = Color("lab:purchasing")
	val labelEmployees : Color = Color("lab:employees")
}

trait Measure1000ClientPriorityConfig extends CompanyConfig {
	override val benchmarkConfig : String = "measure-1000-client"
	override val measureIterations : Int = 1000
	override val warmup = true

	val priorityPublic : Int = 0
	val priorityProduction : Int = 0
	val priorityPurchasing : Int = 0
	val priorityEmployees : Int = 0
	val priorityClient : Int = 1

	val permissionsPublic : Set[String] = Set("lab:public")
	val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	val permissionsClient : Set[String] = Set("lab:client")

	val labelPublic : Color = Color("lab:client")
	val labelProduction : Color = Color("lab:client")
	val labelPurchasing : Color = Color("lab:client")
	val labelEmployees : Color = Color("lab:client")
}

trait Test10DefaultPriorityConfig extends CompanyConfig {
	override val benchmarkConfig : String = "test-10-all"
	override val measureIterations : Int = 10
	override val warmup = false

	val priorityPublic : Int = 1
	val priorityProduction : Int = 1
	val priorityPurchasing : Int = 1
	val priorityEmployees : Int = 1
	val priorityClient : Int = 0

	val permissionsPublic : Set[String] = Set("lab:public")
	val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	val permissionsClient : Set[String] = Set("lab:public", "lab:production", "lab:purchasing", "lab:employees")

	val labelPublic : Color = Color("lab:public")
	val labelProduction : Color = Color("lab:production")
	val labelPurchasing : Color = Color("lab:purchasing")
	val labelEmployees : Color = Color("lab:employees")
}

trait Test10ClientPriorityConfig extends CompanyConfig {
	override val benchmarkConfig : String = "test-10-client"
	override val measureIterations : Int = 10
	override val warmup = false

	val priorityPublic : Int = 0
	val priorityProduction : Int = 0
	val priorityPurchasing : Int = 0
	val priorityEmployees : Int = 0
	val priorityClient : Int = 1

	val permissionsPublic : Set[String] = Set("lab:public")
	val permissionsProduction : Set[String] = Set("lab:public", "lab:production")
	val permissionsPurchasing : Set[String] = Set("lab:public", "lab:purchasing")
	val permissionsEmployees : Set[String] = Set("lab:public", "lab:employees")
	val permissionsClient : Set[String] = Set("lab:client")

	val labelPublic : Color = Color("lab:client")
	val labelProduction : Color = Color("lab:client")
	val labelPurchasing : Color = Color("lab:client")
	val labelEmployees : Color = Color("lab:client")
}