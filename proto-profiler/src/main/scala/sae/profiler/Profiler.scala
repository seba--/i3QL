package sae.profiler

import sae.util.Timer

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 10:34
 *
 */

object Profiler
{

    def profile(f: => Unit)(implicit times: Int): Array[Timer] =
    {
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

    def profile[Setup](setup : Int => Setup)(f: Setup => Unit)(tearDown : Setup => Unit)(implicit times: Int): Array[Timer] =
    {
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
}