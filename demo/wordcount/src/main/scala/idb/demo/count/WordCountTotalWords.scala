package idb.demo.count

import java.nio.file.Path

/**
 * @author Mirko Köhler
 */
class WordCountTotalWords(val isMaterialized : Boolean) extends WordCount {

	import idb.syntax.iql._

	private val wordcount = SELECT (COUNT(*)) FROM wordtable

	val getResult : idb.Relation[_] =
		if (isMaterialized) wordcount.asMaterialized else wordcount



}

