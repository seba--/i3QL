package idb.demo

import java.io.{BufferedReader, File, FileReader}
import java.nio.file.{Files, Path}

import idb.{SetTable, BagTable}

/**
 * @author Mirko Köhler
 */
class WordCountWithFiles(val dir : Path, val isMaterialized : Boolean, val cacheFiles : Boolean) extends WordCount {

	override def processLine(index : Int, file : File, line : String) = {
		filetable.add((index, file.getName))
		linetable.add((index, line))
	}

	import idb.syntax.iql._
	import idb.syntax.iql.IR._

	val linetable = BagTable.empty[(Int, String)]
	val filetable = BagTable.empty[(Int, String)]

	val splitString : Rep[String => Seq[String]] = staticData(
		(s : String) => s.replaceAll("\\p{Punct}"," ").split("\\s+")
	)

	private val wordtable : Rep[Query[(Int, String)]] =
		SELECT ((x : Rep[((Int, String), String)]) => (x._1._1, x._2)) FROM UNNEST (linetable, (s : Rep[(Int, String)]) => splitString(s._2))

	private val join : Rep[Query[String]] =
		SELECT ((f : Rep[(Int, String)], w : Rep[(Int, String)]) => w._2) FROM (filetable, wordtable) WHERE ((f : Rep[(Int, String)], w : Rep[(Int, String)]) => f._1 == w._1)

	private val wordcount =
		SELECT ((x : Rep[String]) => x, COUNT((x : Rep[String]) => x)) FROM join WHERE ((x : Rep[String]) => true) GROUP BY ((x : Rep[String]) => x)

	val getResult : idb.Relation[(String, Int)] =
		if (isMaterialized) wordcount.asMaterialized else wordcount
}
