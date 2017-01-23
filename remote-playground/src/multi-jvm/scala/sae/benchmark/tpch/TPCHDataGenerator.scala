package sae.benchmark.tpch

import java.time.Instant
import java.util.Date

import idb.schema.tpch.generation.{CommentGenerator, ListStringGenerator, PhoneGenerator, StringGenerator}
import idb.schema.tpch._
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
	override val measureIterations: Int = 1


}

trait DefaultDataGenerator extends TPCHDataGenerator {

	override val benchmarkType = "default"

	val SF1000TH = 1

	object CustomerDBNode extends DBNode {
		override val nodeName = "customer-node"
		override val dbNames = Seq("db")

		override val iterations: Int = SF1000TH * 100
		override val isPredata : Boolean = false

		private val random = new Random()

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs(0)

			val custKey = index
			val name = "Customer#%09d".format(custKey)
			val address = StringGenerator.generateString(10, 40)
			val nationKey = random.nextInt(25)
			val phone = PhoneGenerator.generateNumber(nationKey)
			val acctbal = random.nextDouble() * (9999.99 + 999.99) - 999.99
			val mktsegment = ListStringGenerator.Segments.generate()
			val comment = CommentGenerator.generateText(29, 116)

			db += Customer(custKey, name, address, nationKey, phone, acctbal, mktsegment, comment)
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
		//This node ius responisible for orders and line item generations.

		override val nodeName = "orders-node"
		override val dbNames = Seq("orders", "lineitem")

		override val iterations: Int = SF1000TH * 1500
		override val isPredata : Boolean = false

		private val random = new Random()

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val ordersDb = dbs(0)
			val lineItemDb = dbs(1)

			val o_orderKey = index
			val o_custKey = {
				var k = 0
				while (k % 3 == 0) {
					k = random.nextInt(SF1000TH * 150) + 1
				}
				k
			}
			 //TODO: Change this to the price of all line items in this order

			val days151inMs = 13046400000L
			import ListStringGenerator._
			val timeinterval = ((END_DATE - days151inMs) - START_DATE).toInt
			val orderDateLong = START_DATE + random.nextInt(timeinterval)
			val o_orderDate = new Date(orderDateLong)
			val o_orderPriority = ListStringGenerator.Priorities.generate()
			val o_clerk = "Clerk#%09d".format(random.nextInt(SF1000TH * 1) + 1)
			val o_shipPriority = 0
			val o_comment = CommentGenerator.generateText(19,78)

			//Compute these values during generation of line items
			var o_orderStatus = ' '
			var o_totalPrice = 0.00

			//generate line items
			val numItems = random.nextInt(7) + 1
			val S = SF1000TH * 10
			for (i <- 1 to numItems) {
				val l_orderKey = o_orderKey
				val l_partKey = random.nextInt(SF1000TH * 200) + 1

				val supp = random.nextInt(4)

				val l_suppKey = (l_partKey + (i * (( S/4 ) + (l_partKey - 1 )/S))) % S + 1
				val l_linenumber = i
				val l_quantity = random.nextInt(50) + 1
				val l_extendedPrice = l_quantity * 50.0 //TODO: Change this to: l_quantity * part.retailPrice
				val l_discount = random.nextDouble() * 10
				val l_tax = random.nextDouble() * 0.08

				o_totalPrice = o_totalPrice + (l_extendedPrice * (1 + l_tax) * (1 - l_discount))

				val shipDateLong = orderDateLong + 86400000L * (random.nextInt(121) + 1)
				val l_shipDate = new Date(shipDateLong)
				val commitDateLong = orderDateLong + 86400000L * (random.nextInt(61) + 30)
				val l_commitDate = new Date(commitDateLong)
				val receiptDateLong = shipDateLong + 86400000L * (random.nextInt(30) + 1)
				val l_receiptDate = new Date(receiptDateLong)


				val l_returnFlag = if (receiptDateLong <= CURRENT_DATE) List('R', 'A')(random.nextInt(2)) else 'N'
				val l_lineStatus = if (shipDateLong > CURRENT_DATE) {
					if (o_orderStatus == ' ')
						o_orderStatus = 'O'
					else if (o_orderStatus == 'F')
						o_orderStatus = 'P'
					'O'
				}  else {
					if (o_orderStatus == ' ')
						o_orderStatus = 'F'
					else if (o_orderStatus == 'O')
						o_orderStatus = 'P'
					'F'
				}

				val l_shipInstruct = ListStringGenerator.Instructions.generate()
				val l_shipMode = ListStringGenerator.Modes.generate()

				val l_comment = CommentGenerator.generateText(10, 43)

				lineItemDb += new LineItem(l_orderKey, l_partKey, l_suppKey, l_linenumber, l_quantity,
					l_extendedPrice, l_discount, l_tax, l_returnFlag, l_lineStatus, l_shipDate,
					l_commitDate, l_receiptDate, l_shipInstruct, l_shipMode, l_comment
				)
			}

			ordersDb += new Orders(o_orderKey, o_custKey, o_orderStatus, o_totalPrice, o_orderDate,
				o_orderPriority, o_clerk, o_shipPriority, o_comment)

		}
	}

	object PartDBNode extends DBNode {
		override val nodeName = "part-node"
		override val dbNames = Seq("db")

		override val iterations: Int = SF1000TH * 200
		override val isPredata : Boolean = false

		private val random = new Random()
		private val namelist = List("almond", "antique", "aquamarine", "azure", "beige", "bisque", "black", "blanched", "blue",
			"blush", "brown", "burlywood", "burnished", "chartreuse", "chiffon", "chocolate", "coral",
			"cornflower", "cornsilk", "cream", "cyan", "dark", "deep", "dim", "dodger", "drab", "firebrick",
			"floral", "forest", "frosted", "gainsboro", "ghost", "goldenrod", "green", "grey", "honeydew",
			"hot", "indian", "ivory", "khaki", "lace", "lavender", "lawn", "lemon", "light", "lime", "linen",
			"magenta", "maroon", "medium", "metallic", "midnight", "mint", "misty", "moccasin", "navajo",
			"navy", "olive", "orange", "orchid", "pale", "papaya", "peach", "peru", "pink", "plum", "powder",
			"puff", "purple", "red", "rose", "rosy", "royal", "saddle", "salmon", "sandy", "seashell", "sienna",
			"sky", "slate", "smoke", "snow", "spring", "steel", "tan", "thistle", "tomato", "turquoise", "violet",
			"wheat", "white", "yellow")

		private def generateName() : String = {
			val sb = new StringBuilder()
			sb.append(namelist(random.nextInt(namelist.length)))
			for (i <- 1 to 4) {
				sb.append(" ")
				sb.append(namelist(random.nextInt(namelist.length)))
			}
			sb.toString()
		}

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs(0)

			val partKey = index
			val name = generateName()
			val m = random.nextInt(5) + 1
			val mfgr = s"Manufacturer#$m"
			val brand = s"Brand#$m${random.nextInt(5) + 1}"
			val typ = ListStringGenerator.Types.generate()
			val size = random.nextInt(50) + 1
			val container = ListStringGenerator.Containers.generate()
			val retailPrice : Double = (90000.0 + ((partKey / 10) % 20001) + 100.0 *
				(partKey % 1000))/100.0
			val comment = CommentGenerator.generateText(5, 22)

			db += Part(partKey, name, mfgr, brand, typ, size, container, retailPrice, comment)
		}
	}

	object PartSuppDBNode extends DBNode {
		override val nodeName = "partsupp-node"
		override val dbNames = Seq("db")

		override val iterations: Int = SF1000TH * 200
		override val isPredata : Boolean = false

		private val random = new Random()

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs(0)

			for (i <- 0 to 3) {
				val partKey = index
				val S = SF1000TH * 10
				val suppKey = (partKey + (i * ((S / 4) + (partKey - 1) / S ))) % S + 1
				val availqty = random.nextInt(9999) + 1
				val supplycost = random.nextDouble() * 999.00 + 1.00
				val comment = CommentGenerator.generateText(49, 198)

				db += PartSupp(partKey, suppKey, availqty, supplycost, comment)
			}
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

		override val iterations: Int = SF1000TH * 10
		override val isPredata : Boolean = false

		private val random = new Random()

		private val complaintComments : Set[Int] = {
			var res = Set.empty[Int]
			while (res.size < SF1000TH * 5) { //From SF * 5.
				res = res + random.nextInt(iterations)
			}
			res
		}

		private val recommendComments : Set[Int] = {
			var res = Set.empty[Int]
			while (res.size < SF1000TH * 5) { //From SF * 5.
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
