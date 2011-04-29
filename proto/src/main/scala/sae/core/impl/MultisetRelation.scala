package sae.core.impl

import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;
import sae.core.Relation

/**
 * A relation backed by a multi set for efficient access to elements
 */
trait MultisetRelation[T <: AnyRef] 
	 extends Relation[T]
{
	private val data : Multiset[T] = HashMultiset.create[T]()
   
	def size : Int = data.size()
   
	def asList : List[T] = 
	{
		var list : List[T] = List()
		val it : java.util.Iterator[T] = data.iterator()
		while(it.hasNext())
		{
			list = it.next() :: list
		}
		list
	}
   
	def uniqueValue : Option[T] =
	{
		if( size != 1 )
			None
		else
			Some(data.iterator().next())
	}

	def add_element(v : T) : Unit = 
	{
		data.add(v)
	}
	
	def remove_element(v : T) : Unit = 
	{
		data.remove(v)
	}
	
	def foreach[U](f: T => U) : Unit = 
	{
		val it : java.util.Iterator[T] = data.iterator()
		while(it.hasNext())
		{
			f( it.next() ) 		
		}
	}
}