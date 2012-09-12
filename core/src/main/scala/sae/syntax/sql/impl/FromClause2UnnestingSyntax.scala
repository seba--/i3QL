package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql._
import ast._
import ast.FromClause2
import compiler.Compiler
import predicates.{WhereClauseSequence, Filter}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause2UnnestingSyntax[DomainA <: AnyRef, DomainB <: AnyRef, UnnestingRange <: AnyRef,Range <: AnyRef](selectClause: SelectClause[Range],
                                                                                             relationA: LazyView[DomainA],
                                                                                             unnestingClause : FromClause)
    extends FROM_CLAUSE_2[DomainA, DomainB, Range]
{

    private def toAst = FromClause2Unnesting[DomainA](
        relationA,
        unnestingClause
    )


    def WHERE(predicate: (DomainA) => Boolean) =
        WhereClause2Syntax (
            SQLQuery (
                selectClause,
                this.toAst,
                Some (
                    WhereClauseSequence (Seq (Filter (predicate, 1)))
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

    def WHERE[UnboundDomain <: AnyRef, RangeA, UnboundRange](join: JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, UnboundDomain, RangeA, UnboundRange]) =
        null


    def compile() = Compiler (
        representation
    )

    type Representation = SQLQuery[Range]

    def representation =
        SQLQuery (
            selectClause,
            this.toAst,
            None
        )
}