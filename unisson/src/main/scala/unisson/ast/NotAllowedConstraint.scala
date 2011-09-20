package unisson.ast

import unisson.model.kinds.DependencyKind

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:00
 *
 */

case class NotAllowedConstraint(source: Ensemble, target: Ensemble, kind: DependencyKind)
    extends DependencyConstraint
{

    source.outgoingConnections = source.outgoingConnections :+ this

    target.incomingConnections = target.incomingConnections :+ this

    val sources : Seq[Ensemble] = List(source)

    val targets : Seq[Ensemble] = List(target)
}