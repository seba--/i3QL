package sae.typecheck

/**
 * Created by seba on 06/11/14.
 */
object Util {
  val LOGGING = true
  val LOG_TABLE_OPS = false

  def timed[A](desc: String, f: => A): A = {
    val start = System.nanoTime()
    val a = f
    val end = System.nanoTime()
    if (LOGGING)
      println(s"Time to $desc is ${(end-start)/1000000.0}ms")
    a
  }

  def log(s: String): Unit = {
    if (LOGGING)
      println(s)
  }
}
