package sae
package core

trait LazyInitializer[T <: AnyRef]
        extends LazyView[T] {
    private var initialized : Boolean = false

    abstract override def foreach[U](f : (T) => U) : Unit =
        {
            if (!initialized) {
                lazyInitialize
                initialized = true
            }
            super.foreach(f)
        }
    /*   
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
*/
}