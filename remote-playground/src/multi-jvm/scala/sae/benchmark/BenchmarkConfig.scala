package sae.benchmark

/**
  * Created by mirko on 07.11.16.
  */
trait BenchmarkConfig {
	val benchmarkName = getClass.getSimpleName
	val benchmarkType : String
	val benchmarkNumber : Int
	val measureIterations : Int
	val warmup : Boolean
}

trait MeasureConfig1 extends BenchmarkConfig {
	override val benchmarkType : String = "measure_20000"
	override val measureIterations : Int = 20000
	override val warmup = true
}

trait TestConfig1 extends BenchmarkConfig {
	override val benchmarkType : String = "test"
	override val measureIterations : Int = 10
	override val warmup = false
}
