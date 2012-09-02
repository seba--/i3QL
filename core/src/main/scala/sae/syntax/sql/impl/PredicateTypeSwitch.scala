package sae.syntax.sql.impl

import sae.syntax.sql.WHERE_CLAUSE_PREDICATE_TYPE_SWITCH

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 20:41
 *
 * TODO this needs operator precedence
 */
case class PredicateTypeSwitch[Domain <: AnyRef](predicate: Domain => Boolean)
    extends WHERE_CLAUSE_PREDICATE_TYPE_SWITCH[Domain]
{
}
