package idb.schema.tpch.generation

import scala.util.Random

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object StringGenerator {

	private val random = new Random()

	def generateString(min : Int, max : Int) : String = {
		val length = random.nextInt(max - min + 1) + min
		return random.nextString(length)
	}

}
