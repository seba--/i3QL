package sae.analyses.profiler.statistics

/**
 * @author Mirko Köhler
 */
trait ReplayStatistic {

    def add(iteration : Int, sample: Long)

    def apply(iteration : Int) : Long

}
