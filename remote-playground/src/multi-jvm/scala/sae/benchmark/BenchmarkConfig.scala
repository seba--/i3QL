package sae.benchmark

/**
  * Created by mirko on 07.11.16.
  */
trait BenchmarkConfig {

	//Each benchmark run is identified by group.name.type.config.number
	//e.g. hospital.query1.default.measure20000.1
	val benchmarkGroup : String
	val benchmarkQuery : String
	val benchmarkType : String
	val benchmarkConfig : String
	val benchmarkNumber : Int

	val measureIterations : Int
	val warmup : Boolean

	def identifier : String =
		s"$benchmarkGroup.$benchmarkQuery.$benchmarkType.$benchmarkConfig.$benchmarkNumber"
}

trait MeasureConfig1 extends BenchmarkConfig {
	override val benchmarkConfig : String = "measure20000"
	override val measureIterations : Int = 20000
	override val warmup = true
}

trait TestConfig1 extends BenchmarkConfig {
	override val benchmarkConfig : String = "test10"
	override val measureIterations : Int = 10
	override val warmup = false
}
