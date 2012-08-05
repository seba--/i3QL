package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 13:55
 */
object DISTINCT
{

    def apply[Domain <: AnyRef, Range <: AnyRef](projection: Domain => Range): DISTINCT_PROJECTION[Domain, Range] =
        new DISTINCT_PROJECTION[Domain, Range]
        {
            def function = projection
        }

    def apply(x: STAR): DISTINCT_NO_PROJECTION.type = DISTINCT_NO_PROJECTION

}
