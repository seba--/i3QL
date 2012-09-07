package sae.syntax.sql.ast.predicates


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 *
 * A predicate the makes a test which evaluates to a "constant" boolean
 */
case class Filter[-Domain <: AnyRef](filter: Domain => Boolean)
    extends Predicate
{

}
