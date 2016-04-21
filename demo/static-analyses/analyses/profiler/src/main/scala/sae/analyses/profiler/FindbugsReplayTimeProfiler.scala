package sae.analyses.profiler


import sae.analyses.profiler.util.DatabaseReader
import java.io.InputStream
import edu.umd.cs.findbugs._
import edu.umd.cs.findbugs.detect.FindPuzzlers
import edu.umd.cs.findbugs.gui2.BugLoader
import edu.umd.cs.findbugs.config.UserPreferences


/**
 * @author Mirko Köhler
 */
class FindbugsReplayTimeProfiler/* extends AbstractAnalysesReplayTimeProfiler {

	override def benchmarkType: String = "Findbugs Replay Time"

	override def database: DatabaseReader = null

	override def getAnalysis(query: String) = null
}  */

object FindbugsReplayTimeProfiler {
	def main(args : Array[String]) {

		val p : Project = new Project
		p.addFile("C:\\Users\\Mirko\\Documents\\GitHub\\sae\\test-data\\src\\main\\resources\\scala-compiler-2.8.1.jar")

		val bugfinder = new FindBugs2


		val pref : UserPreferences = UserPreferences.createDefaultUserPreferences()
		pref.enableAllDetectors(false)

		val detectorName = "BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION"

		val it = Plugin.getAllPlugins.iterator()
		while (it.hasNext) {
			val	plugin = it.next()
			val it2 = plugin.getDetectorFactories.iterator()
			while (it2.hasNext) {
				val factory = it2.next()
				if (factory.getShortName == "FindPuzzlers")
					pref.enableDetector(factory, true)
			}
		}

		bugfinder.setUserPreferences(pref)
		bugfinder.setProject(p)

		bugfinder.execute()


	}
}
