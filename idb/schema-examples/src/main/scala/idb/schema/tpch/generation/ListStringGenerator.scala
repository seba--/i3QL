package idb.schema.tpch.generation

import scala.util.Random

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object ListStringGenerator {

	val START_DATE : Long = 694220400000L
	val CURRENT_DATE : Long = 802476000000L
	val END_DATE : Long = 915058800000L

	object Types {
		private val random = new Random()

		private val syl1 = List("STANDARD", "SMALL", "MEDIUM", "LARGE", "ECONOMY", "PROMO")
		private val syl2 = List("ANODIZED", "BURNISHED", "PLATED", "POLISHED", "BRUSHED")
		private val syl3 = List("TIN", "NICKEL", "BRASS", "STEEL", "COPPER")

		def generate() : String = {
			val sb = new StringBuilder()
			sb.append(syl1(random.nextInt(syl1.size)))
				.append(" ")
				.append(syl2(random.nextInt(syl2.size)))
				.append(" ")
				.append(syl3(random.nextInt(syl3.size)))
			sb.toString()
		}
	}

	object Containers {
		private val random = new Random()

		private val syl1 = List("SM", "LG", "MED", "JUMBO", "WRAP")
		private val syl2 = List("CASE", "BOX", "BAG", "JAR", "PKG", "PACK", "CAN", "DRUM")

		def generate() : String = {
			val sb = new StringBuilder()
			sb.append(syl1(random.nextInt(syl1.size)))
				.append(" ")
				.append(syl2(random.nextInt(syl1.size)))
			sb.toString()
		}
	}

	object Segments {
		private val random = new Random()

		private val syl1 = List("AUTOMOBILE", "BUILDING", "FURNITURE", "MACHINERY", "HOUSEHOLD")

		def generate() : String = syl1(random.nextInt(syl1.size))
	}

	object Priorities {
		private val random = new Random()

		private val syl1 = List("1-URGENT", "2-HIGH", "3-MEDIUM", "4-NOT SPECIFIED", "5-LOW")

		def generate() : String = syl1(random.nextInt(syl1.size))
	}

	object Instructions {
		private val random = new Random()

		private val syl1 = List("DELIVER IN PERSON", "COLLECT COD", "NONE", "TAKE BACK RETURN")

		def generate() : String = syl1(random.nextInt(syl1.size))
	}

	object Modes {
		private val random = new Random()

		private val syl1 = List("REG AIR", "AIR", "RAIL", "SHIP", "TRUCK", "MAIL", "FOB")

		def generate() : String = syl1(random.nextInt(syl1.size))
	}

}
