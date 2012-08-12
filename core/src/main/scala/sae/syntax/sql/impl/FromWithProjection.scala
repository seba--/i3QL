package sae.syntax.sql.impl

import sae.LazyView
import sae.operators.{SetDuplicateElimination, BagProjection}
import sae.syntax.sql.FROM_CLAUSE

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromWithProjection[Domain <: AnyRef, Range <: AnyRef](
                                                                    projection: Domain => Range,
                                                                    relation: LazyView[Domain],
                                                                    distinct: Boolean
                                                                    )
    extends FROM_CLAUSE[Domain, Range]
{

    def compile() = if (distinct) {
        new SetDuplicateElimination[Range](new BagProjection[Domain, Range](projection, relation))
    }
                    else
                    {
                        new BagProjection[Domain, Range](projection, relation)
                    }

    def WHERE(predicate: (Domain) => Boolean) = WhereWithProjection (projection, predicate, relation, distinct)
}