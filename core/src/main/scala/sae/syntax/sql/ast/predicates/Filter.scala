package sae.syntax.sql.ast.predicates


/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 16:42
 *
 * A predicate the makes a test which evaluates to a "constant" boolean, the relation that must be filtered is indicated by the number
 * ,i.e., 1 == first relation etc.
 */
case class Filter[-Domain <: AnyRef](filter: Domain => Boolean, relation: Int)
    extends Predicate
{

}
