package idb.demo.routine

import idb.demo.count.WordCount

/**
 * @author Mirko Köhler
 */
trait ChangeRoutineFactory {
	def create(wc : WordCount) : ChangeRoutine
}

object DTAKernKorpusRoutineFactory extends ChangeRoutineFactory {
	override def create(wc: WordCount): ChangeRoutine = new DTAKernKorpusRoutine(wc)
}
