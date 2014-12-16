package sae.interpreter.regexps.incr

import sae.interpreter.regexps.RegExp

/**
 * @author Mirko Köhler
 */
trait RegExpInterpreter[ListKey, StringKey, ExpKey, ValueKey] {

	def defineString(s : String) : StringKey
	def defineRegExp(e :RegExp) : ExpKey
	def defineList(l : Seq[Any]) : ListKey

	def incrStringSize(s : StringKey) : ValueKey
	def incrSubString(s : StringKey, i : Int) : ValueKey
	def incrStartsWith(s0 : StringKey, s1 : StringKey) : ValueKey
	def incrAppend(s0 : ListKey, s1 : ListKey) : ListKey
	def incrInterpret(e : ExpKey, s : StringKey) : ValueKey

	def keyToString(k : StringKey) : String

	def valToStringSet(k : ValueKey) : Set[String]
	def valToString(k : ValueKey) : String
	def valToBoolean(k : ValueKey) : Boolean
	def valToInt(k : ValueKey) : Int
	def valToList(k : ValueKey) : Seq[Any]
}
