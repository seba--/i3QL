package sae.benchmark.tpch

import idb.schema.tpch.generation.{CommentGenerator, PhoneGenerator, StringGenerator}
import idb.schema.tpch.{Nation, Region, Supplier, TPCHSchema}
import idb.{BagTable, Relation, Table}
import sae.benchmark.{Benchmark, BenchmarkConfig, CSVPrinter}

import scala.util.Random

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
	override val benchmarkConfig : String = "test"
	override val warmup = false


}

trait DefaultDataGenerator extends TPCHDataGenerator {

	override val benchmarkType = "default"

	val SF = 1

	object CustomerDBNode extends DBNode {
		override val nodeName = "customer-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object LineItemDBNode extends DBNode {
		override val nodeName = "lineitem-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object NationDBNode extends DBNode {
		override val nodeName = "nation-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs(0)
			db += Nation(0, "ALGERIA", 0, CommentGenerator.generateText(31, 114))
			db += Nation(1, "ARGENTINA", 1, CommentGenerator.generateText(31, 114))
			db += Nation(2, "BRAZIL", 1, CommentGenerator.generateText(31, 114))
			db += Nation(3, "CANADA", 1, CommentGenerator.generateText(31, 114))
			db += Nation(4, "EGYPT", 4, CommentGenerator.generateText(31, 114))
			db += Nation(5, "ETHIOPIA", 0, CommentGenerator.generateText(31, 114))
			db += Nation(6, "FRANCE", 3, CommentGenerator.generateText(31, 114))
			db += Nation(7, "GERMANY", 3, CommentGenerator.generateText(31, 114))
			db += Nation(8, "INDIA", 2, CommentGenerator.generateText(31, 114))
			db += Nation(9, "INDONESIA", 2, CommentGenerator.generateText(31, 114))
			db += Nation(10, "IRAN", 4, CommentGenerator.generateText(31, 114))
			db += Nation(11, "IRAQ", 4, CommentGenerator.generateText(31, 114))
			db += Nation(12, "JAPAN", 2, CommentGenerator.generateText(31, 114))
			db += Nation(13, "JORDAN", 4, CommentGenerator.generateText(31, 114))
			db += Nation(14, "KENYA", 0, CommentGenerator.generateText(31, 114))
			db += Nation(15, "MOROCCO", 0, CommentGenerator.generateText(31, 114))
			db += Nation(16, "MOZAMBIQUE", 0, CommentGenerator.generateText(31, 114))
			db += Nation(17, "PERU", 1, CommentGenerator.generateText(31, 114))
			db += Nation(18, "CHINA", 2, CommentGenerator.generateText(31, 114))
			db += Nation(19, "ROMANIA", 3, CommentGenerator.generateText(31, 114))
			db += Nation(20, "SAUDI ARABIA", 4, CommentGenerator.generateText(31, 114))
			db += Nation(21, "VIETNAM", 2, CommentGenerator.generateText(31, 114))
			db += Nation(22, "RUSSIA", 3, CommentGenerator.generateText(31, 114))
			db += Nation(23, "UNITIED KINGDOM", 3, CommentGenerator.generateText(31, 114))
			db += Nation(24, "UNITED STATES", 1, CommentGenerator.generateText(31, 114))
		}
	}

	object OrdersDBNode extends DBNode {
		override val nodeName = "orders-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object PartDBNode extends DBNode {
		override val nodeName = "part-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object PartSuppDBNode extends DBNode {
		override val nodeName = "partsupp-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {

		}
	}

	object RegionDBNode extends DBNode {
		override val nodeName = "region-node"
		override val dbNames = Seq("db")

		override val iterations: Int = 1
		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs(0)
			db += Region(0, "AFRICA", CommentGenerator.generateText(31, 115))
			db += Region(1, "AMERICA", CommentGenerator.generateText(31, 115))
			db += Region(2, "ASIA", CommentGenerator.generateText(31, 115))
			db += Region(3, "EUROPE", CommentGenerator.generateText(31, 115))
			db += Region(4, "MIDDLE EAST", CommentGenerator.generateText(31, 115))
		}
	}

	object SupplierDBNode extends DBNode {
		override val nodeName = "supplier-node"
		override val dbNames = Seq("db")

		override val iterations: Int = SF * 10000
		override val isPredata : Boolean = false

		private val random = new Random()

		private val complaintComments : Set[Int] = {
			var res = Set.empty[Int]
			while (res.size < SF * 5) {
				res = res + random.nextInt(iterations)
			}
			res
		}

		private val recommendComments : Set[Int] = {
			var res = Set.empty[Int]
			while (res.size < SF * 5) {
				val i = random.nextInt(iterations)
				if (!complaintComments.contains(i))
					res = res + i
			}
			res
		}

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs(0)

			val suppKey = index
			val name = "Supplier#%09d".format(suppKey)
			val address = StringGenerator.generateString(10, 40)
			val nationKey = random.nextInt(25)
			val phoneNumber = PhoneGenerator.generateNumber(nationKey)
			val acctBal = -999.99 + (9999.99 - -999.99) * random.nextDouble()
			val comment : String =
				if (complaintComments.contains(index)) {
					val c : StringBuilder = new StringBuilder(CommentGenerator.generateText(25,100))
					val index1 = random.nextInt(c.size)
					c.insert(index1, "Customer")
					val index2 = random.nextInt(c.size - (index1 + 8)) + index1 + 8
					c.insert(index2, "Complaints")
					c.toString()
				} else if (recommendComments.contains(index)) {
					val c : StringBuilder = new StringBuilder(CommentGenerator.generateText(25,100))
					val index1 = random.nextInt(c.size)
					c.insert(index1, "Customer")
					val index2 = random.nextInt(c.size - (index1 + 8)) + index1 + 8
					c.insert(index2, "Recommends")
					c.toString()
				} else {
					CommentGenerator.generateText(25,100)
				}

				db += Supplier(suppKey, name, address, nationKey, phoneNumber, acctBal, comment)
		}
	}

}
