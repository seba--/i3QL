package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:08
 *
 */

case class Ensemble(name : String, query : UnissionQuery)
    extends UnissonDefinition
{
    var outgoingConstraints : Seq[DependencyConstraint] = Nil

    var incomingConstraints : Seq[DependencyConstraint] = Nil
}