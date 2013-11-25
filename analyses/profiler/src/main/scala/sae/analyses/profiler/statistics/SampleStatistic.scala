package sae.analyses.profiler.statistics


trait SampleStatistic
  extends SampleStatisticResult
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

}