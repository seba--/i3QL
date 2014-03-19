package sae.analyses.profiler.interfaces

import sae.analyses.profiler.measure.TimeMeasurement
import sae.analyses.profiler.util.{ReplayEventType, ReplayEvent}
import sae.analyses.profiler.statistics._
import sae.bytecode.profiler.statistics.EventStatistic
import idb.Relation
import sae.bytecode.BytecodeDatabase
import java.io.FileInputStream
import sae.analyses.profiler.util.ReplayEvent
import sae.analyses.profiler.statistics.SimpleDataStatistic
import sae.analyses.profiler.measure.units.{MilliSeconds, MeasurementUnit}

/**
 * @author Mirko Köhler
 */
trait AbstractAnalysesReplayTimeProfiler
	extends AbstractPropertiesFileReplayProfiler
	with AbstractAnalysesReplayProfiler
	with TimeMeasurement
{
	def measurementUnit: MeasurementUnit = MilliSeconds

	def measurement[T](m : Long => Unit)(f : => T) = time(m)(f)
}
