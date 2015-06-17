package idb.demo.count

import java.io.File
import java.nio.file.Path

/**
 * @author Mirko Köhler
 */
class WordCountPerWord(val isMaterialized : Boolean) extends WordCount {

	import idb.syntax.iql.IR._
	import idb.syntax.iql._

	private val wordcount = SELECT ((x : Rep[String]) => x, COUNT((x : Rep[String]) => x)) FROM wordtable GROUP BY ((x : Rep[String]) => x)

	val getResult : idb.Relation[(String, Int)] =
		if (isMaterialized) wordcount.asMaterialized else wordcount



}
