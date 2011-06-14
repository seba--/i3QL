package sae.profiler.util
import sae.util.Timer

object Write {
	def apply(name : String, profile : Array[Timer]) : Unit = {
        print(name + " : ")
        val t = Timer.median(profile)
        println(t.elapsedSecondsWithUnit)
    }
}