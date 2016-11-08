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

trait Measure20000Config extends BenchmarkConfig {
	override val benchmarkConfig : String = "measure-20000"
	override val measureIterations : Int = 20000
	override val warmup = true
}

trait Measure1000Config extends BenchmarkConfig {
	override val benchmarkConfig : String = "measure-1000"
	override val measureIterations : Int = 1000
	override val warmup = true
}

trait TestConfig1 extends BenchmarkConfig {
	override val benchmarkConfig : String = "test-10"
	override val measureIterations : Int = 10
	override val warmup = false
}
