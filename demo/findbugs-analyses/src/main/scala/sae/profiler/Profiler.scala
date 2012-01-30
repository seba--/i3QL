package sae.profiler

/**
 *
 * Author: Ralf Mitschke
 * Date: 27.01.12
 * Time: 13:46
 *
 */
object Profiler
{

    def profile[T, V, U](processTime: Long => V)(profiledFunction: => T)(implicit times: Int = 1): T = {

        def doProfile() = {
            val start = System.nanoTime
            val result = profiledFunction
            val stop = System.nanoTime
            processTime(stop - start)
            result
        }
        var i = 0
        while (i < times - 1) {
            doProfile()
            i += 1
        }
        doProfile()
    }

    def nanoToSeconds[T](nanos: T)(implicit numeric: Numeric[T]): Double = numeric.toDouble(nanos) / 1000000000;
}