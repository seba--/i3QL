package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.FROM_CLAUSE
import sae.operators.SetDuplicateElimination

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
private[sql] case class FromNoProjection[Domain <: AnyRef](relation: LazyView[Domain], distinct: Boolean)
    extends FROM_CLAUSE[Domain, Domain]
{

    def compile() = withDistinct(relation, distinct)

    def WHERE(predicate: (Domain) => Boolean) = WhereNoProjection (predicate, relation, distinct)
}