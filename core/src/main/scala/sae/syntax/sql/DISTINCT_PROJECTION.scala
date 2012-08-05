package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 13:41
 */

trait DISTINCT_PROJECTION[Domain <: AnyRef, Range <: AnyRef]
{

    def function: Domain => Range

}
