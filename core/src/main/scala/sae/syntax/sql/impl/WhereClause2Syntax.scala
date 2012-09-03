package sae.syntax.sql.impl

import sae.syntax.sql.WHERE_CLAUSE_2
import sae.syntax.sql.ast._
import sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class WhereClause2Syntax[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](whereClause: WhereClause2[DomainA, DomainB, Range])
    extends WHERE_CLAUSE_2[DomainA, DomainB, Range]
{
    def AND(predicateA: (DomainA) => Boolean) =
        WhereClause2Syntax (
            WhereClause2 (
                whereClause.fromClause,
                whereClause.conditionsA ++ Seq (AndOperator, Filter(predicateA)),
                whereClause.conditionsB,
                whereClause.joinConditions
            )
        )

    def OR(predicateA: (DomainA) => Boolean) =
        WhereClause2Syntax (
            WhereClause2 (
                whereClause.fromClause,
                whereClause.conditionsA ++ Seq (OrOperator, Filter(predicateA)),
                whereClause.conditionsB,
                whereClause.joinConditions
            )
        )

    def AND[RangeA <: AnyRef, RangeB <: AnyRef](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            WhereClause2 (
                whereClause.fromClause,
                whereClause.conditionsA,
                whereClause.conditionsB,
                whereClause.joinConditions ++ Seq (AndOperator, join)
            )
        )

    def OR[RangeA <: AnyRef, RangeB <: AnyRef](join: sql.JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) =
        WhereClause2Syntax (
            WhereClause2 (
                whereClause.fromClause,
                whereClause.conditionsA,
                whereClause.conditionsB,
                whereClause.joinConditions ++ Seq (OrOperator, join)
            )
        )

    def compile() = null
}
