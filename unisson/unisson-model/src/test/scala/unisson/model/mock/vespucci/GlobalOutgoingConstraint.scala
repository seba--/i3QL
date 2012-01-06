package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.IGlobalOutgoing


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class GlobalOutgoingConstraint(kind: String, source: Ensemble, target: Ensemble)
        extends IGlobalOutgoing
{
    source.sourceConnections += this

    target.targetConnections += this

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "GlobalOutgoingConstraint(" + source.getName + ", " + target.getName + ")"
}