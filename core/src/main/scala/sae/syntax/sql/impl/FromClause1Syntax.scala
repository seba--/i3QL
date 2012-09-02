package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.{JOIN_CONDITION_UNBOUND_RELATION_1, EXISTS_SUB_CLAUSE, SQL_SUB_QUERY_WHERE_OPEN_1, FROM_CLAUSE}
import sae.syntax.sql.ast.{WhereClause1, Filter, FromClause1, SelectClause1}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 * Abstract implementation of the from selectClause.
 * Concrete Subclasses without projection have to define their own compile method since Domain == Range can not be guaranteed in one implementation
 */
case class FromClause1Syntax[Domain <: AnyRef, Range <: AnyRef](selectClause: SelectClause1[_ >: Domain, Range],
                                                                relation: LazyView[Domain])
    extends FROM_CLAUSE[Domain, Range]
{
    def WHERE(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            WhereClause1 (
                FromClause1 (
                    selectClause,
                    relation
                ),
                Seq (Filter (predicate))
            )
        )

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[Domain, UnboundDomain, RangeA, UnboundRange]) = null

    def WHERE[SubDomain <: AnyRef, SubRange <: AnyRef](subQuery: SQL_SUB_QUERY_WHERE_OPEN_1[SubDomain, SubRange, Domain] with EXISTS_SUB_CLAUSE) {}

    def compile() = null
}