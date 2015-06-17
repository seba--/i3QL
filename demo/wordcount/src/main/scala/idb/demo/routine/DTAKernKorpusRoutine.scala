package idb.demo.routine

import java.io.{BufferedReader, File, FileReader}
import java.nio.file.{Files, Path, Paths}

import idb.demo.count.WordCount

import scala.collection.mutable

/**
 * @author Mirko Köhler
 */
class DTAKernKorpusRoutine(val wordcount : WordCount) extends ChangeRoutine {

	private var step = 0

	private val dir0 = Paths.get("res","dta_kernkorpus_2014-03-10")
	private val dir1 = Paths.get("res","dta_kernkorpus_2014-03-10","abel_leibmedicus_1699.TEI-P5.xml")

	private var times : List[Long] = List()

	var cache : mutable.MutableList[(Int, File, String)] = null

	override def initialize(): Unit = {
		println("Cache files...")
		cache = mutable.MutableList.empty[(Int, File, String)]

		step match {
			case 0 => processDirectory(dir0, readFile(_,_)((i,f,l) => cache += ((i,f,l)) ))
			case 1 => readFile(0, dir1.toFile)((i,f,l) => cache += ((i,f,l)))
		}
	}

	override def next(): Unit = {
		cache = null
		step = step + 1
	}

	override def process(): Long = {
		println("Process data...")
		val before = System.currentTimeMillis()
		step match {
			case 0 => cache.foreach(t => wordcount.linetable.add(t._3))
			case 1 => cache.foreach(t => wordcount.linetable.remove(t._3))
		}

		val after = System.currentTimeMillis()
		times = after - before :: times
		after - before
	}


	override def hasNext: Boolean = step < 1

	override def getTimes = times.reverse


}
