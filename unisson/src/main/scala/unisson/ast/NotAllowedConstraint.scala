package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:00
 *
 */

case class NotAllowedConstraint(architecture: String, sourceName: String, sourceParams: List[String], targetName: String, targetParams: List[String], kinds: List[String])
    extends SingleDependencyConstraint
{

}