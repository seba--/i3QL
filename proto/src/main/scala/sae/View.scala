package sae

/**
 * 
 */
trait View[V <: AnyRef] extends Observable[V] {
   
	/** 
	 * Applies f to all elements of the view.
	 */
	def foreach[T](f: (V) => T) : Unit
   
   
	/** 
	 * Returns the size of the view in terms of elements.
	 * This can be a costly operation.
	 * Implementors should cache the value in a self-maintained view, but clients can not rely on this.
	 */
	def size : Int
	
	   
	/**
	 * Converts the data of the view into a list representation.
	 * This can be a costly operation and should mainly be used for testing.
	 */
	   	def asList : List[V] =
   	{
   		var l : List[V] = List()
   		foreach( v => 
   			{ 
   				l = l :+ v
   			} 
   		)
   		l
   	}
	
	
	/**
	 * If the view consists of a single value, Some(value) is returned, i.e. the value wrapped in a Option.
	 * Otherwise this method returns None.
	 * If only one distinct value is contained but in multiple instances None is returned.
	 */
	def uniqueValue : Option[V]

}