package sae.core

import sae.core.operators._

import RelationalAlgebraSyntax._

/**
 * σ(Filter1)(R) corresponds to the following SQL query:
 *   SELECT *
 *   FROM R
 *   WHERE Filter1
 *
 * Π(Projection)(R) corresponds to the following SQL query:
 *   SELECT DISTINCT Projection
 *   FROM R
 * Note: Non distinct projections do not fulfill all relational algebra properties (see Projection.scala)
 */
trait InfixSQLSyntax[Domain <: AnyRef] {
    self : Relation[Domain] =>

    import SQLSyntax._

    def where(filter : Domain => Boolean) : Relation[Domain] = σ(filter, this)

}
object SQLSyntax {

    object select {
        def apply[Domain <: AnyRef, Range <: AnyRef](projection : Domain => Range) : SelectFunctor[Domain, Range] = new SelectFunctor(projection)

        def apply[Domain <: AnyRef](star : SQLSyntax.*.type) : SQLSyntax.*.type = SQLSyntax.* // or.. = star

        def * = SQLSyntax.*

        def distinct[Domain <: AnyRef, Range <: AnyRef](projection : Domain => Range) : DistinctFunctor[Domain, Range] = new DistinctFunctor[Domain, Range](projection)

        def distinct(star : SQLSyntax.*.type) : SQLSyntax.*.type = SQLSyntax.*
    }

    // * will return the relation itself and no projection takes place
    object * {
        def from[Domain <: AnyRef](relation : Relation[Domain]) : Relation[Domain] = relation;
    }

    /* helper classes for sql style infix syntax */
    final class SelectFunctor[Domain <: AnyRef, Range <: AnyRef](projection : Domain => Range) {
        def from(relation : Relation[Domain]) = new BagProjection[Domain, Range](projection, relation);

    }

    final class DistinctFunctor[Domain <: AnyRef, Range <: AnyRef](projection : Domain => Range) {
        def from(relation : Relation[Domain]) : Relation[Range] = Π(projection, relation);
    }

}