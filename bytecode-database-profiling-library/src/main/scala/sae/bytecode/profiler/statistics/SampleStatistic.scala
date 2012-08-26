package sae.bytecode.profiler.statistics

import collection.mutable.ArrayBuffer

trait SampleStatistic
{
    protected var sum       : Double = 0
    protected var squaresSum: Double = 0

    //Total iterations whose results are kept (used to compute the mean. and std.dev.)
    def count: Int

    //Total iterations executed (counts the calls to add)
    def iterations: Int

    /**
     * The mean over the sampled values
     */
    def mean = sum / count

    /**
     * The variance is equal to the mean of the square minus the square of the mean:
     */
    def variance = (squaresSum / count) - (mean * mean)

    def coVariance = math.sqrt (variance) / mean

    def standardDeviation = math.sqrt (variance)

    def standardError = standardDeviation / math.sqrt (count.toDouble)

    def add(sample: Long)

    protected[this] def updateSumsForNewSample(sample: Long) {
        sum += sample
        squaresSum += sample.asInstanceOf[Double] * sample
    }

    val defaultLocale = java.util.Locale.UK
    def summary(implicit unit : MeasurementUnit) : String =
        "%.3f +- %.3f (stdErr = %.3f) " + unit.descriptor + "; relative std.dev: %.3f, std.err: %.3f" formatLocal (defaultLocale,
            Seq(mean, standardDeviation, standardError, standardDeviation / mean, standardError / mean).map(unit.fromBase(_)) : _*
    )

}

//This class adds the contract that samples are in nanoseconds - but that is only a preconditions for its new methods.
//Hence, this precondition does not violate the LSP.
/*
trait VarianceNsec2Msec extends SampleStatistic
{
    def avgMs = mean / math.pow (10, 6)

    def devStdMs = standardDeviation / math.pow (10, 6)

    def stdErrMs = standardError / math.pow (10, 6)
}
*/