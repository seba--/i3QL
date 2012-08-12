package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql._

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 15:35
 */

case class FromStarting[Domain <: AnyRef](relation: LazyView[Domain])
    extends FROM_CLAUSE_AS_PREFIX[Domain]
{
    def SELECT[Range <: AnyRef](projection: (Domain) => Range) = FromWithProjection (projection, relation, distinct = false)

    def SELECT(x: STAR_KEYWORD) = FromNoProjection (relation, distinct = false)

    def SELECT[Range <: AnyRef](distinct: DISTINCT_INFIX_SELECT_CLAUSE[Domain, Range]) = FromWithProjection (distinct.function, relation, distinct = true)

    def SELECT(distinct: DISTINCT_INFIX_SELECT_CLAUSE_NO_PROJECTION) = FromNoProjection (relation, distinct = true)
}
