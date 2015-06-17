package idb.demo.count

import idb.{BagTable, Relation}

/**
 * @author Mirko Köhler
 */
trait WordCount {

/*	def dir : Path


	def cacheFiles : Boolean


	protected def processLine(index: Int, file : File, line : String)

	var cache : mutable.MutableList[(Int, File, String)] = mutable.MutableList.empty[(Int, File, String)]



	def initialize(): Unit = {
		if (cacheFiles) {
			processDirectory(dir, readFile(_,_)((i,f,l) => cache += ((i,f,l)) ))
		}
	}

	def close(): Unit = {
		cache = null
	}

	private def processDirectory(path : Path, f : (Int, File) => Any): Unit = {
		var dirstream = Files.newDirectoryStream(dir)
		print(s"Process directory ${dir.getFileName}")
		System.out.flush()
		val it = dirstream.iterator
		var i = 0
		while (it.hasNext) {
			val next = it.next()
			i = i + 1
			print(".")
			System.out.flush()
			f(i, next.toFile)
		}
		println()
		dirstream.close()
		dirstream = null
	}

	protected def readFile(index : Int, file : File)(f : (Int, File, String) => Any): Unit = {
		var reader = new BufferedReader(new FileReader(file))
		var s = reader.readLine()

		while(s != null) {
			f(index, file, s)
			s = reader.readLine()
		}
		reader.close()
		reader = null

	}

	def addDir() : Unit = {
		if (cacheFiles)
			cache.foreach((t) => processLine(t._1, t._2, t._3))
		else
			processDirectory(dir, readFile(_,_)(processLine))
	}  */

	def getResult : Relation[_]
	def isMaterialized : Boolean


	import idb.syntax.iql.IR._
	import idb.syntax.iql._
	private val splitString : Rep[String => Seq[String]] = staticData(
		(s : String) => s.replaceAll("\\p{Punct}"," ").split("\\s+")
	)

	val linetable = BagTable.empty[String]

	protected val wordtable : Rep[Query[String]] = SELECT ((x : Rep[(String, String)]) => x._2) FROM UNNEST(linetable, (s : Rep[String]) => splitString(s))



}
