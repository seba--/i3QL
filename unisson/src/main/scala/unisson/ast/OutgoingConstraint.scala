package unisson.ast

import unisson.model.kinds.DependencyKind

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:00
 *
 */

case class OutgoingConstraint(source: Ensemble, targets: Seq[Ensemble], kind: DependencyKind)
        extends DependencyConstraint
{
    source.outgoingConnections = source.outgoingConnections :+ this

    targets.foreach(
            (target: Ensemble) =>
            target.incomingConnections = target.incomingConnections :+ this
    )

    val sources : Seq[Ensemble] = List(source)

}