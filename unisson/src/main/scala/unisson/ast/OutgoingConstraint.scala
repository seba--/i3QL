package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:00
 *
 */

case class OutgoingConstraint(source: Ensemble, targets: Seq[Ensemble], kind: String)
    extends DependencyConstraint
{

}