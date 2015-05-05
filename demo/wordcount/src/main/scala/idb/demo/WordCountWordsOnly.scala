package idb.demo

import java.io.{FileReader, BufferedReader, File}
import java.nio.file.{Files, Path}

import idb.{Relation, BagTable}

/**
 * @author Mirko Köhler
 */
class WordCountWordsOnly(val dir : Path, val isMaterialized : Boolean, val cacheFiles : Boolean) extends WordCount {

	override def processLine(index : Int, file : File, line : String) =
		linetable.add(line)

	import idb.syntax.iql._
	import idb.syntax.iql.IR._

	val linetable = BagTable.empty[String]

	val splitString : Rep[String => Seq[String]] = staticData(
		(s : String) => s.replaceAll("\\p{Punct}"," ").split("\\s+")
	)

	private val wordtable : Rep[Query[String]] = SELECT ((x : Rep[(String, String)]) => x._2) FROM UNNEST(linetable, (s : Rep[String]) => splitString(s))
	private val wordcount = SELECT ((x : Rep[String]) => x, COUNT((x : Rep[String]) => x)) FROM wordtable WHERE ((x : Rep[String]) => true) GROUP BY ((x : Rep[String]) => x)

	val getResult : idb.Relation[(String, Int)] =
		if (isMaterialized) wordcount.asMaterialized else wordcount



}
