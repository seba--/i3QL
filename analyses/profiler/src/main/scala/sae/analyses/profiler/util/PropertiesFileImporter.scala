package sae.analyses.profiler.util

import java.util.Properties
import java.io.FileInputStream

/**
 * @author Mirko Köhler
 */
trait PropertiesFileImporter {

	def basePath : String

	def isFile(propertiesFile: String): Boolean = {
		val file = new java.io.File(propertiesFile)
		file.exists() && file.canRead// && !file.isDirectory

	}

	def isResource(propertiesFile: String): Boolean = {
		this.getClass.getClassLoader.getResource(propertiesFile) != null
	}

	def getProperties(propertiesFile: String): Option[Properties] = {

		if (isFile(basePath + propertiesFile)) {
			println("Loading properties file "+propertiesFile +"...")
			val file = new java.io.File(basePath + propertiesFile)
			val properties = new Properties()
			properties.load(new FileInputStream(file))
			return Some(properties)
		}
		if (isResource(propertiesFile)) {
			println("Loading properties resource "+propertiesFile +"...")
			val properties = new Properties()
			properties.load(this.getClass.getClassLoader.getResource(propertiesFile).openStream())
			return Some(properties)
		}
		None
	}

}
