package sae.interpreter.utils

/**
 * @author Mirko Köhler
 */
trait KeyGenerator[T] {

	private var freshKey : T = startKey

	protected def startKey : T
	protected def nextKey(k : T) : T

	def fresh() : T = {
		val key = freshKey
		freshKey = nextKey(freshKey)
		key
	}
}

class IntKeyGenerator extends KeyGenerator[Int] {
	def startKey = 0
	def nextKey(k : Int) = k + 1
}
