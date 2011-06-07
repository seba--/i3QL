package sae.profiler.util
import sae.util.Timer

object Profile {
    
	def apply(f : => Unit)(implicit times : Int = 1) : Array[Timer] = {
        var i = 0;
        val timers = new Array[Timer](times)
        while (i < times) {
            val timer = new Timer()
            f
            timer.stop;
            timers(i) = timer
            i += 1
        }
        return timers
    }

}

object Profile2 {
    

    def apply[T <: AnyRef](f1 : => (sae.test.helpFunctions.ObservableList[T], Any), f2 : sae.test.helpFunctions.ObservableList[T] => Unit)(implicit times : Int = 1) : Array[Timer] = {
        var i = 0;
        val timers = new Array[Timer](times)
        while (i < times) {
            val s = f1
            val timer = new Timer()
            f2(s._1)
            timer.stop;
            timers(i) = timer
            i += 1
        }
        return timers
    }
}