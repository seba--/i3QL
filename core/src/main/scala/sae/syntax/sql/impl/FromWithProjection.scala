package sae.syntax.sql.impl

import sae.LazyView
import sae.operators.{SetDuplicateElimination, BagProjection}
import sae.syntax.sql.{JOIN_CONDITION_UNBOUND_RELATION_1, EXISTS_SUB_CLAUSE, SQL_SUB_QUERY_WHERE_OPEN_1, FROM_CLAUSE}

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

    def WHERE[SubDomain <: AnyRef, SubRange <: AnyRef](subQuery: SQL_SUB_QUERY_WHERE_OPEN_1[SubDomain, SubRange, Domain] with EXISTS_SUB_CLAUSE) {}

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[Domain, UnboundDomain, RangeA, UnboundRange]) = null
}