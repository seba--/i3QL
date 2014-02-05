package sae.analyses.profiler.statistics

import sae.analyses.profiler.measure.units.MeasurementUnit


/**
 * @author Ralf Mitschke
 */
trait SampleStatisticResult
{
  def mean: Double

  def variance: Double

  def coVariance: Double

  def standardDeviation: Double

  def standardError: Double

  private val defaultLocale = java.util.Locale.UK

  def summary(implicit unit: MeasurementUnit): String = {
    var values = Seq (mean, standardDeviation, standardError, standardDeviation / mean, standardError / mean)
    values = values.map (unit.fromBase)
    "%.3f +- %.3f (stdErr = %.3f) " + unit.descriptor + "; relative std.dev: %.3f, std.err: %.3f" formatLocal (
      defaultLocale,
      values: _*
      )
  }

  /**
   * Return the summary for a given number of units that were measured as a whole by the statistic
   */
  def summaryPerUnit(numberOfUnits: Int)(implicit unit: MeasurementUnit): String = {
    var values = Seq (mean, standardDeviation, standardError, standardDeviation / mean, standardError / mean).map(
      _ / numberOfUnits
    )
    values = values.map (unit.fromBase)
    "%.3f +- %.3f (stdErr = %.3f) " + unit.descriptor + "; relative std.dev: %.3f, std.err: %.3f" formatLocal (
      defaultLocale,
      values: _*
      )
  }

  /**
   * Subtract the measure of another statistic.
   * Errors are accumulated
   */
  def - (other: SampleStatisticResult): SampleStatisticResult = new SampleStatisticResult
  {
    def mean = SampleStatisticResult.this.mean - other.mean

    def variance = SampleStatisticResult.this.variance + other.variance

    def coVariance = SampleStatisticResult.this.coVariance + other.coVariance

    def standardDeviation = SampleStatisticResult.this.standardDeviation + other.standardDeviation

    def standardError = SampleStatisticResult.this.standardError + other.standardError
  }
}