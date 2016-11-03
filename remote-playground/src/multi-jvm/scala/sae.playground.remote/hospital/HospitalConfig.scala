package sae.playground.remote.hospital

/**
  * Created by mirko on 13.10.16.
  */
trait HospitalConfig {
	val benchmarkName = getClass.getSimpleName
	val benchmarkType : String
	val benchmarkNumber : Int

	val measureIterations : Int
}

trait BenchmarkConfig1 extends HospitalConfig {

	override val benchmarkType : String = "measure_20000"

	val measureIterations : Int = 20000
}

trait TestConfig1 extends HospitalConfig {

	override val benchmarkType : String = "test"

	override val measureIterations : Int = 10
}
