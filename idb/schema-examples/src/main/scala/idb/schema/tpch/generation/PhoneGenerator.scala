package idb.schema.tpch.generation

import scala.util.Random

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object PhoneGenerator {

	private val random = new Random()

	def generateNumber(countryIndex : Int) : String = {
		val countryCode = countryIndex + 10
		val num1 = random.nextInt(900) + 100
		val num2 = random.nextInt(900) + 100
		val num3 = random.nextInt(9000) + 1000
		return s"$countryCode-$num1-$num2-$num3"
	}


}
