package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.{JOIN_CONDITION_UNBOUND_RELATION_1, EXISTS_KEYWORD, SQL_SUB_QUERY_WHERE_OPEN_1, FROM_CLAUSE}

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

    def compile() = withDistinct (relation, distinct)

    def WHERE(predicate: (Domain) => Boolean) = WhereNoProjection (predicate, relation, distinct)

    def WHERE[SubDomain <: AnyRef, SubRange <: AnyRef](subQuery: SQL_SUB_QUERY_WHERE_OPEN_1[SubDomain, SubRange, Domain] with EXISTS_KEYWORD) {}

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[Domain, UnboundDomain, RangeA, UnboundRange]) = null
}