package idb

/**
  * Created by mirko on 07.11.16.
  */
package object util {

	def printEvents(relation : Relation[_], tag : String = null): Unit = {
		PrintEvents(relation, tag)
	}

}
