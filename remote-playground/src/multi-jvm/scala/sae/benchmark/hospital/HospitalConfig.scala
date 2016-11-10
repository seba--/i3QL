package sae.benchmark.hospital

import sae.benchmark.BenchmarkConfig

/**
  * Created by mirko on 08.11.16.
  */
trait HospitalConfig extends BenchmarkConfig {

}

trait Measure25000Config extends HospitalConfig {
	override val benchmarkConfig : String = "measure-25000"
	override val measureIterations : Int = 25000
	override val warmup = true
}

trait Measure50000Config extends HospitalConfig {
	override val benchmarkConfig : String = "measure-50000"
	override val measureIterations : Int = 50000
	override val warmup = true
}

trait Measure100000Config extends HospitalConfig {
	override val benchmarkConfig : String = "measure-100000"
	override val measureIterations : Int = 100000
	override val warmup = true
}

trait TestConfig1 extends HospitalConfig {
	override val benchmarkConfig : String = "test-10"
	override val measureIterations : Int = 10
	override val warmup = false
}
