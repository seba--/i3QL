package idb.demo

import java.nio.file.Path

/**
 * @author Mirko Köhler
 */
trait WordCountFactory {
	def create(dir : Path, isMaterialized : Boolean, cacheFiles : Boolean) : WordCount
}

object WordCountPerWordFactory extends WordCountFactory {
	def create(dir : Path, isMaterialized : Boolean, cacheFiles : Boolean) = new WordCountWordsOnly(dir, isMaterialized, cacheFiles)
}

object WordCountPerFileFactory extends WordCountFactory {
	def create(dir : Path, isMaterialized : Boolean, cacheFiles : Boolean) = new WordCountWithFiles(dir, isMaterialized, cacheFiles)
}

