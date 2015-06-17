package idb.demo.count

import java.io.File
import java.nio.file.Path

/**
 * @author Mirko Köhler
 */
class WordCountDistinctWords(val isMaterialized : Boolean) extends WordCount {

	import idb.syntax.iql._

	private val distinct = SELECT DISTINCT * FROM wordtable
	private val wordcount = SELECT (COUNT(*)) FROM distinct

	val getResult : idb.Relation[_] =
		if (isMaterialized) wordcount.asMaterialized else wordcount

}
