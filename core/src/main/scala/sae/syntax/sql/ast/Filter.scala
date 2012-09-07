package sae.syntax.sql.ast


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 */

case class Filter[Domain <: AnyRef](filter: Domain => Boolean)
    extends Predicate
{

}
