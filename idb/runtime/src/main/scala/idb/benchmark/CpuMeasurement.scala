package idb.benchmark

import java.lang.management.ManagementFactory

/**
  * Created by mirko on 25.10.16.
  */
case class CpuMeasurement(val f : () => Unit, val result : (Long, Long, Double) => Unit, val interval : Int = 50) extends (() => Unit) {


	override def apply(): Unit = {
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

		f.apply()

		running = false
	}

}
