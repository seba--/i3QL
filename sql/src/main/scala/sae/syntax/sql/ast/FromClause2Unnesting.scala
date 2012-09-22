package sae.syntax.sql.ast

import sae.Relation


/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause2Unnesting[FromDomainA <: AnyRef](relationA: Relation[FromDomainA],
                                                       unnestingClause: FromClause)
    extends FromClause
{

    type DomainA = FromDomainA

}