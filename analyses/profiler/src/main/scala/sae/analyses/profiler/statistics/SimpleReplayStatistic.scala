package sae.analyses.profiler.statistics

/**
 * @author Mirko Köhler
 */
class SimpleReplayStatistic(iterations : Int) extends ReplayStatistic {

    val samples = Array.fill[Long](iterations)(-1)

    override def add(iteration: Int, sample: Long) {
        samples(iteration - 1) = sample
    }

    override def apply(iteration : Int) : Long =
        samples(iteration - 1)
}
