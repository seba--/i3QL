package sae.core.impl

import sae.core.MaterializedRelation
import scala.collection.immutable.HashSet

/**
 * A relation backed by a set for efficient access to elements.
 * Each element has only one occurrence in this relation.
 */
trait SetRelation[T <: AnyRef] 
	 extends MaterializedRelation[T]
{
	private var data : Set[T] = new HashSet[T]()
   
	def size : Int = data.size

	def uniqueValue : Option[T] = data.headOption

	def add_element(v : T) : Unit = 
	{
		data = data + v
	}
	
	def remove_element(v : T) : Unit = 
	{
		data = data - v
	}
	
	def foreach[U](f: T => U) : Unit = data.foreach(f)

}