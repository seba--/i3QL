package sae.benchmark.hospital

import sae.benchmark.BenchmarkConfig

/**
  * Created by mirko on 08.11.16.
  */
trait HospitalConfig extends BenchmarkConfig {

}

trait Measure20000Config extends HospitalConfig {
	override val benchmarkConfig : String = "measure-20000"
	override val measureIterations : Int = 20000
	override val warmup = true
}

trait TestConfig1 extends HospitalConfig {
	override val benchmarkConfig : String = "test-10"
	override val measureIterations : Int = 10
	override val warmup = false
}
