package idb.demo.routine

import java.io.{FileReader, BufferedReader, File}
import java.nio.file.{Files, Path}

import idb.demo.count.WordCount

/**
 * @author Mirko Köhler
 */
trait ChangeRoutine {

	val wordcount : WordCount

	def initialize()

	def process() : Long

	def hasNext : Boolean
	def next()

	def getTimes : List[Long]

	protected def processDirectory(path : Path, f : (Int, File) => Any): Unit = {
		var dirstream = Files.newDirectoryStream(path)
		System.out.flush()
		val it = dirstream.iterator
		var i = 0
		while (it.hasNext) {
			val next = it.next()
			i = i + 1
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

}
