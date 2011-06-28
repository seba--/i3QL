package sae.profiler

import sae.util.Timer
import scala.Array
import reflect.New

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 10:34
 *
 */

object Profiler {

  def profile(f: => Unit)(implicit times: Int): Array[Timer] = {
    var i = 0;
    val timers = new Array[Timer](times)
    while (i < times) {
      val timer = new Timer()
      f
      timer.stop();
      timers(i) = timer
      i += 1
    }
    timers
  }

  def profile[Setup](setup: Int => Setup)(f: Setup => Unit)(tearDown: Setup => Unit)(implicit times: Int): Array[Timer] = {
    var i = 0;
    val timers = new Array[Timer](times)
    while (i < times) {
      val s = setup(i + 1)
      val timer = new Timer()
      f(s)
      timer.stop();
      timers(i) = timer
      tearDown(s)
      i += 1
    }
    timers
  }

  def profile2[Setup](initSetup: () => Unit, beforeMeasurement: Int => Setup, f: Setup => Unit, afterMeasurement: Setup => Unit, tearDown: => Unit, countF: Int, times: Int)

  : Array[Array[Timer]] = {
    var i = 0;

    val timers : Array[Array[Timer]] = new Array[Array[Timer]](countF)
    for (i <- 0 until timers.length)
      timers(i) = new Array[Timer](times)
    while (i < times) {
      initSetup()
      var j = 0
      while (j < countF) {
        val s = beforeMeasurement(j)
        val timer = new Timer()
        f(s)
        timer.stop()
        timers
        timers(j)(i) = timer
        afterMeasurement(s)
        j += 1
      }
      tearDown
      i += 1
    }
    timers
  }
}