package sae.benchmark.tpch

import idb.benchmark.Measurement
import idb.query.QueryEnvironment
import idb.schema.hospital
import idb.schema.hospital._
import idb.schema.tpch.TPCHSchema
import idb.{BagTable, Relation, Table}
import sae.benchmark.{Benchmark, BenchmarkConfig, CSVPrinter}

/**
  * Barriers that are used in the hospital benchmark:
  *
  * deployed - The tables have been deployed on their servers and the printer has been initialized.
  * compiled - The query has been compiled and deployed to the servers.
  *
  * sent-warmup - The warmup events have been sent (from the tables)
  *
  * resetted - The warmup events have been received and the data structures have been resetted.
  *
  * ready-measure - The classes needed for measurements have been initialized.
  * sent-measure - The measure events have been sent (from the tables).
  *
  * finished - The measurement has been finished.
  *
  *
  * deploy
  * compile
  * warmup-predata
  * warmup-data
  * warmup-finish
  * reset
  * measure-predata
  * measure-init
  * measure-data
  * measure-finish
  * finish
  */
trait TPCHDataGenerator extends Benchmark {

	override val benchmarkGroup = "tpch"

	object DataSchema extends TPCHSchema {
		override val IR = idb.syntax.iql.IR
	}

}

trait TPCHMeasureConfig extends BenchmarkConfig

trait TestMeasureConfig extends TPCHMeasureConfig {
	override val benchmarkConfig : String = "test-10"
	override val measureIterations : Int = 10
	override val warmup = false
}

trait TestDataGenerator extends TPCHDataGenerator {

	override val benchmarkType = "testdata"

	object CustomerDBNode extends DBNode {
		override val nodeName = "customer-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object LineItemDBNode extends DBNode {
		override val nodeName = "lineitem-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object NationDBNode extends DBNode {
		override val nodeName = "nation-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object OrdersDBNode extends DBNode {
		override val nodeName = "orders-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object PartDBNode extends DBNode {
		override val nodeName = "part-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object PartSuppDBNode extends DBNode {
		override val nodeName = "partsupp-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object RegionDBNode extends DBNode {
		override val nodeName = "region-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object SupplierDBNode extends DBNode {
		override val nodeName = "supplier-node"
		override val dbNames = Seq("db")

		override val iterations: Int = measureIterations
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

}
