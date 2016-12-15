package idb

package object util {

	def printEvents(relation : Relation[_], tag : String = null): Unit = {
		PrintEvents(relation, tag)
	}

}
