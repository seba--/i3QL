package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.IOutgoing


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class OutgoingConstraint(kind: String, source: Ensemble, target: Ensemble)
        extends IOutgoing
{
    source.sourceConnections += this

    target.targetConnections += this

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "OutgoingConstraint(" + source.getName + ", " + target.getName + ")"
}