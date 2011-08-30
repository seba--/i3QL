package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:00
 *
 */

case class OutgoingConstraint(architecture: String, sourceName: String, sourceParams: List[String], targetName: String, targetParams: List[String], kinds: List[String])
    extends DependencyConstraint
{

}