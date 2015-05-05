package idb.demo

import java.io.{FileReader, BufferedReader, File}
import java.nio.file.{Files, Path}

import idb.{Relation, BagTable}

import scala.collection.mutable

/**
 * @author Mirko Köhler
 */
trait WordCount {

	def dir : Path

	def isMaterialized : Boolean
	def cacheFiles : Boolean

	def getResult : Relation[(String, Int)]

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
	}



}
