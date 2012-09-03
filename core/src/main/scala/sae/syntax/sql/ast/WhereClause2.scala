package sae.syntax.sql.ast


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class WhereClause2[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](fromClause: FromClause2[DomainA, DomainB, Range],
                                                                               conditionsA: Seq[ConditionExpression],
                                                                               conditionsB: Seq[ConditionExpression],
                                                                               joinConditions: Seq[ConditionExpression])
{

}