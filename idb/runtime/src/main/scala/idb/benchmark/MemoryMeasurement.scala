package idb.benchmark

/**
  * Created by mirko on 25.10.16.
  */
case class MemoryMeasurement(val f : () => Unit, val result : (Long, Long) => Unit, val sleepAfterGc : Int = 5000) extends (() => Unit) {


	override def apply(): Unit = {
		val rt = Runtime.getRuntime

		System.gc()
		Thread.sleep(sleepAfterGc)
		val memBefore = rt.totalMemory() - rt.freeMemory()

		f.apply()

		System.gc()
		Thread.sleep(sleepAfterGc)
		val memAfter = rt.totalMemory() - rt.freeMemory()
		result(memBefore, memAfter)
	}

}
