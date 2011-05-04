package sae.core.operators

import sae.core.Relation
import sae.core.SelfMaintainedView
import sae.core.MaterializedView
import sae.core.impl.BagRelation
import sae.core.impl.SetRelation
import sae.core.impl.MaterializedViewImpl
import sae.core.impl.SizeCachedMaintainedView
import sae.Observer
import sae.View

/**
 * A join 

 * The general projection class implemented here used the relational algebra semantics.
 * Specialized classes for SQL semantics are available (see further below).
 */
class HashEquiJoin[A <: AnyRef, B <: AnyRef, Z <: AnyRef, K <: AnyRef]
	(
		val left : Relation[A],
		val right : Relation[B],
		val leftKey : A => K,
		val rightKey : B => K,
		val joinFunction : (A,B) => Z
	)
//	 extends BagRelation[(A,B)]
//		with MaterializedViewImpl[(A,B)]
{
	
}