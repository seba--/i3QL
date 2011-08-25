package sae.core

/**
 * The execution plan takes a query in the form of a Relation object and returns a new relation with an optimized plan. 
 * 
 */
object ExecutionPlan
{
	
	def apply[V <: AnyRef](query : Relation[V]) : Relation[V] =
	{
		var last = query
		var opt = optimized(last)
		// each optimization step will return a real new object, thus we are finished with optimizing once no new object is returned
		while(last ne opt)
		{
			last = opt
			opt = optimized(last)
		}
		opt
	}
	
	import sae.core.RelationalAlgebraSyntax._
	
	/**
	 * Return an optimized query.
	 * if no optimization is applicable return the query itself.
	 * otherwise return a new object
	 */
	def optimized[V <: AnyRef](query : Relation[V]) : Relation[V] = query match
	{
		// multiple projections are usually optimized to the last projection, this is always optimal
		// for type safety we apply all projection functions, thus removing the need for unnecessary projection objects and materializations.
		case Π(f, Π(g,inner)) =>  
			{
				// some type coercions
				val fun : Function1[inner.tupleType,V]= g andThen f
				val rel : Relation[inner.tupleType] = inner
				Π(fun, rel)
			}
		
		// default return the query itself, i.e., no optimization applied
		case _ => query
	}
}