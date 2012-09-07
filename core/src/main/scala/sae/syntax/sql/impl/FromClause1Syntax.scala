package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.{JOIN_CONDITION_UNBOUND_RELATION_1, EXISTS_SUB_CLAUSE, SQL_SUB_QUERY_WHERE_OPEN_1, FROM_CLAUSE}
import sae.syntax.sql.ast._
import predicates.Filter
import sae.syntax.sql.ast.FromClause1
import sae.syntax.sql.ast.SelectClause1

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 * The syntax representation of a from clause for one relation.
 *
 * In the from clause we curre
 */
case class FromClause1Syntax[Domain <: AnyRef, Range <: AnyRef](selectClause: SelectClause1[_ >: Domain <: AnyRef, Range],
                                                                relation: LazyView[Domain])
    extends FROM_CLAUSE[Domain, Range]
{
    private def toAst = FromClause1[Domain](relation)

    def WHERE(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            SQLQuery (
                selectClause,
                this.toAst,
                Some (
                    WhereClauseSequence (Seq (Filter (predicate)))
                )
            )
        )

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[Domain, UnboundDomain, RangeA, UnboundRange]) =
        null

    def WHERE[SubDomain <: AnyRef, SubRange <: AnyRef](subQuery: SQL_SUB_QUERY_WHERE_OPEN_1[SubDomain, SubRange, Domain] with EXISTS_SUB_CLAUSE) {}

    def compile() = Compiler (
        SQLQuery (selectClause, this.toAst, None)
    )
}