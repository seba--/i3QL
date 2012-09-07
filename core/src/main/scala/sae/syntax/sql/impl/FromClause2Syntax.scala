package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql._
import ast._
import ast.FromClause2
import ast.SelectClause2
import predicates.Filter

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause2Syntax[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](selectClause: SelectClause2[_ >: DomainA <: AnyRef, _ >: DomainB <: AnyRef, Range],
                                                                                    relationA: LazyView[DomainA],
                                                                                    relationB: LazyView[DomainB])
    extends FROM_CLAUSE_2[DomainA, DomainB, Range]
{

    private def toAst = FromClause2[DomainA, DomainB](
        relationA,
        relationB
    )


    def WHERE(predicate: (DomainA) => Boolean) =
        WhereClause2Syntax (
            SQLQuery (
                selectClause,
                this.toAst,
                Some (
                    WhereClauseSequence (Seq (Filter (predicate)))
                )
            )
        )

    def WHERE[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            SQLQuery (
                selectClause,
                this.toAst,
                Some (
                    WhereClauseSequence (Seq (join))
                )
            )
        )

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, UnboundDomain, RangeA, UnboundRange]) = null

    def compile() = Compiler (
        SQLQuery (
            selectClause,
            this.toAst,
            None
        )
    )
}