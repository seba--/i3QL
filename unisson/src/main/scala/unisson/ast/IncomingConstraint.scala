package unisson.ast

import unisson.model.kinds.DependencyKind

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 15:00
 *
 */

case class IncomingConstraint(sources: Seq[Ensemble], target: Ensemble, kind: DependencyKind)
        extends DependencyConstraint
{
    sources.foreach(
            (source: Ensemble) =>
            source.outgoingConnections = source.outgoingConnections :+ this
    )

    target.incomingConnections = target.incomingConnections :+ this

    val targets : Seq[Ensemble] = List(target)
}