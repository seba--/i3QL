package sae.analyses.profiler.statistics

class ArrayBufferSampleStatistic(sampleSize: Int) extends SampleStatistic
{
  import scala.collection.mutable.ArrayBuffer

  val buf        = ArrayBuffer.fill (sampleSize)(0L)
  var idx        = 0
  var iterations = 0

  override def count = math.min (sampleSize, iterations)

  def samples: Seq[Long] = (buf take count).toList

  def add(sample: Long) {
    iterations += 1

    //Safe to do this
    sum -= buf (idx)
    squaresSum -= buf (idx) * buf (idx)

    buf (idx) = sample

    updateSumsForNewSample (sample)

    idx = (idx + 1) % sampleSize
  }

  override def coVariance =
    if (iterations < sampleSize) 1 else super.coVariance
}