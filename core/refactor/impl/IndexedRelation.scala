package sae.core.impl

import sae.core.Relation
import sae.core.Index
import scala.collection.immutable.HashMap

trait HashIndexedRelation[V <: AnyRef] 
	 extends Relation[V]
{


	private var indices : Map[(V => _), Index[_,V]] = new HashMap() //: Map[(V => _ <: AnyRef), Index[_,V]] = new HashMap[V => K, Index[K,V]]() 
	
	def index[K <: AnyRef](keyFunction : V => K) : Index[K,V] = 
	{
		if( indices.isDefinedAt(keyFunction) )
		{
			indices(keyFunction)
		}
		val index = new HashIndex(this, keyFunction)
		indices(keyFunction) = index
		index
	}
}