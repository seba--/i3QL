package sae.playground.remote

object BenchmarkConfig extends akka.remote.testkit.MultiNodeConfig {
  // change the number 10 here and the number of BenchmarkTestMultiJvmNode classes to configure the benchmark
  val nodes = List.tabulate(10)(x => role(s"node$x"))
}