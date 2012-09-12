package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql.{JOIN_CONDITION_UNBOUND_RELATION_1, FROM_CLAUSE}
import sae.syntax.sql.ast._
import predicates.{WhereClauseSequence, Filter}
import sae.syntax.sql.ast.FromClause1
import sae.syntax.sql.ast.SelectClause1
import sae.syntax.sql.compiler.Compiler

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
case class FromClause1Syntax[Domain <: AnyRef, Range <: AnyRef](selectClause: SelectClause[Range],
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
                    WhereClauseSequence (Seq (Filter (predicate, 1)))
                )
            )
        )

    def WHERE[UnboundDomain <: AnyRef, RangeA, UnboundRange](join: JOIN_CONDITION_UNBOUND_RELATION_1[Domain, UnboundDomain, RangeA, UnboundRange]) =
        WhereClause2From1Syntax(
            SQLQuery (
                selectClause,
                this.toAst,
                Some (
                    WhereClauseSequence (Seq (join))
                )
            )
        )

    def compile() = Compiler (
        representation
    )

    type Representation = SQLQuery[Range]

    def representation = SQLQuery (selectClause, this.toAst, None)
}