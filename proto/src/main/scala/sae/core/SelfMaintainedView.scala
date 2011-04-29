package sae.core


import sae.Observer
import sae.View


/**
 * A view is self-maintainable, if the maintenance operations are expressible in terms of the tuples 
 * changed during insertions, deletions and updates, as well as the data of the view itself. 
 * No data of the full underlying relation may be used to maintain the view.
 * </br>
 * We consider the self-maintenance problem for arbitrary SPJ (Select, Project, Join) views, i.e., 
 * views as defined by SQL statements.
 * In addition views may be defined for recursive queries using special operators, that will
 * be further defined.
 * TODO reference operators allowed for recursion.
 * </br>
 * A self maintained view observes a relation of type V and provides a view of a relation of type Z. 
 * In case of joins, or other entities that can have entries of multiple types for V, we use tuples.
 *
 * Theory and background:
 * Insertions:
 *
 * T1: An SPJ view, that takes the join of two or more distinct relations is not self-maintainable with
 * respect to insertions. 
 * TODO what does this mean for us, give an example where this is not possible
 * T2: All SP views are self-maintainable with respect to insertions.
 * T3: An SPJ view defined using self-joins over a single relation R is self-maintainable if every join is based on key(R) 
 */
trait SelfMaintainedView[V <: AnyRef, Z <: AnyRef] 
	 extends Observer[V]
		with View[Z]
{
   
}