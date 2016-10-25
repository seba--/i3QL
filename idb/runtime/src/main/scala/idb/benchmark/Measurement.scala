package idb.benchmark

import java.lang.management.ManagementFactory

/**
  * Created by mirko on 25.10.16.
  */
object Measurement {

	object CPU {
		def apply(result : (Long, Long, Double) => Unit, interval : Int = 50)(f : => Unit) : Unit = {
			var running = true

			val thr = new Thread(new Runnable {
				override def run(): Unit = {
					val myOsBean= ManagementFactory.getOperatingSystemMXBean.asInstanceOf[com.sun.management.OperatingSystemMXBean]

					while (running) {
						result(System.currentTimeMillis(), myOsBean.getProcessCpuTime(), myOsBean.getProcessCpuLoad())
						Thread.sleep(interval)
					}
				}
			})
			thr.start()

			f

			running = false
		}
	}

	object Memory {
		def apply(result : (Long, Long) => Unit, sleepAfterGc : Int = 5000)(f : => Unit) : Unit = {
			val rt = Runtime.getRuntime

			System.gc()
			Thread.sleep(sleepAfterGc)
			val memBefore = rt.totalMemory() - rt.freeMemory()

			f

			System.gc()
			Thread.sleep(sleepAfterGc)
			val memAfter = rt.totalMemory() - rt.freeMemory()
			result(memBefore, memAfter)
		}
	}

}
