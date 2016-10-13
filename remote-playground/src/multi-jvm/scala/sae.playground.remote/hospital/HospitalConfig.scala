package sae.playground.remote.hospital

/**
  * Created by mirko on 13.10.16.
  */
trait HospitalConfig {
	val benchmarkName = getClass.getSimpleName

	val warmupIterations : Int
	val measureIterations : Int
}
