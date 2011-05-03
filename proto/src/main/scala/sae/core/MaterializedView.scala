package sae.core


import sae.View


/**
 * 
 */
trait MaterializedView[T <: AnyRef] 
	extends View[T]
{
	
	def asMaterialized : MaterializedView[T] = this 
	
	/**
	 * The materialize method initializes this view
	 * Implementors must guarantee that no update/add/remove event is fired during the materialization
	 */
	protected def materialize()

}