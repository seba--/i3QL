package sae.syntax.sql.impl

import sae.syntax.sql.WHERE_CLAUSE
import sae.syntax.sql.ast._
import sae.syntax.sql.ast.WhereClause1
import sae.syntax.sql.ast.Filter

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause1Syntax[Domain <: AnyRef, Range <: AnyRef](whereClause: WhereClause1[Domain, Range])
    extends WHERE_CLAUSE[Domain, Range]
{
    /*
    def compile() = withDistinct (
        new BagProjection[Domain, Range](projection, new LazySelection[Domain](filter, relation)),
        distinct
    )

    def AND(inlineWhereClause: INLINE_WHERE_CLAUSE[Domain]) = WhereWithProjection (projection, (x: Domain) => filter (x) && inlineWhereClause.function (x), relation, distinct)

    def OR(inlineWhereClause: INLINE_WHERE_CLAUSE[Domain]) = WhereWithProjection (projection, (x: Domain) => filter (x) || inlineWhereClause.function (x), relation, distinct)
    */
    def AND(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            WhereClause1 (
                whereClause.fromClause,
                whereClause.conditions ++ Seq (AndOperator, Filter (predicate))
            )
        )

    def OR(predicate: (Domain) => Boolean) =
        WhereClause1Syntax (
            WhereClause1 (
                whereClause.fromClause,
                whereClause.conditions ++ Seq (OrOperator, Filter (predicate))
            )
        )
}
