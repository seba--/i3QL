package sae.core


import sae.View


/**
 * 
 */
trait MaterializedView[T <: AnyRef] 
	extends View[T]
{
	
	def asMaterialized : MaterializedView[T] = this 
	
	protected def materialize()

}