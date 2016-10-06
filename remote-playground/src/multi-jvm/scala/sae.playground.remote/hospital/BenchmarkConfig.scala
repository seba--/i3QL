package sae.playground.remote.hospital

/**
  * Created by mirko on 06.10.16.
  */
trait BenchmarkConfig {

	val benchmarkName = getClass.getSimpleName

	val warmupIterations : Int
	val measureIterations : Int

}
