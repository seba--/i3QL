package idb.evaluator

import java.text.SimpleDateFormat
import java.util.Date

import idb.Relation
import idb.observer.Observer

/**
  * Created by mirko on 18.10.16.
  */
case class PrintRows[Domain](relation : Relation[Domain], tag : String = null) extends Observer[Domain] {
	relation.addObserver(this)

	override def updated(oldV: Domain, newV: Domain): Unit =
		println(s"$prefix updated $oldV -> $newV")

	override def removed(v: Domain): Unit =
		println(s"$prefix removed $v")

	override def removedAll(vs: Seq[Domain]): Unit =
		println(s"$prefix removedAll $vs")

	override def added(v: Domain): Unit =
		println(s"$prefix added $v")

	override def addedAll(vs: Seq[Domain]): Unit =
		println(s"$prefix addedAll $vs")


	val sdf = new SimpleDateFormat("hh:mm:ss.SSS")
	private def prefix : String = {
		val tagString = if (tag != null) s"[$tag]" else ""
		s"[${sdf.format(new Date())}]$tagString:"
	}


}
