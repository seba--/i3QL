package idb.integration.test.operators

import idb.{Extent, Relation}

/**
 * @author Mirko Köhler
 */
trait AbstractOperatorTest[Domain, Range] {

	def query : Relation[Range]

	def extent : Extent[Domain]

  def printRelation(r : Relation[Any]) {
    println("********************************************")
    r.foreach(println(_))
    println("********************************************")

  }

}
