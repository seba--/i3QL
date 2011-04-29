package sae.core

import sae.Observable
import sae.Observer
import sae.View

/**
 * An index in a database provides fast access to the values <code>V</code> 
 * of a relation <code>R</code> via keys of type <code>K</code>.</br>
 * The index assumes that the underlying relation can change.
 * Thus each index is required to register as an observer of the base relation.
 * Updates to the relation must be propagated to observers of the index.
 * </br>
 * Due to the contravariant nature of observers 
 * (i.e., an Observer[Object] can still register as an observer for this index),
 * the values have to remain invariant. But the relation may still vary.
 */
trait Index[K, V, R <: Relation[_ <: V]] 
 extends Observer[V]
	with View[(K,V)]
{
   def relation : R
   
   def get(key: K): Option[V]
   
   def isDefinedAt(key: K): Boolean
   
   def getOrElse(key: K, f: => V): V
   
}