package idb.demo.count

/**
 * @author Mirko Köhler
 */
trait WordCountFactory {
	def create(isMaterialized : Boolean) : WordCount
}

object WordCountPerWordFactory extends WordCountFactory {
	def create(isMaterialized : Boolean) = new WordCountPerWord(isMaterialized)
}

object WordCountTotalFactory extends WordCountFactory {
	def create(isMaterialized : Boolean) = new WordCountTotalWords(isMaterialized)
}

object WordCountDistinctWordsFactory extends WordCountFactory {
	def create(isMaterialized : Boolean) = new WordCountDistinctWords(isMaterialized)
}


