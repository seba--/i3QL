package sae.core


import sae.View


/**
 * 
 */
trait MaterializedView[T <: AnyRef] 
	extends View[T]
{
	
	private var materialized : Boolean = false

	protected def materialize()

	abstract override def foreach[U](f: (T) => U) : Unit =
	{
		if( !materialized )
		{
			materialize()
			materialized = true
		}
		super.foreach(f)
	}
   
	abstract override def size : Int =
	{
		if( !materialized )
		{
			materialize()
			materialized = true
		}
		super.size
	}
   
	abstract override def asList : List[T] =
	{
		if( !materialized )
		{
			materialize()
			materialized = true
		}
		super.asList
	}
   
	abstract override def uniqueValue : Option[T]  =
	{
		if( !materialized )
		{
			materialize()
			materialized = true
		}
		super.uniqueValue
	}
}