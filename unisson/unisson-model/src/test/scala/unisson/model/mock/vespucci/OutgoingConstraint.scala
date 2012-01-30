package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.{IEnsemble, IOutgoing}


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class OutgoingConstraint(kind: String, source: IEnsemble, target: IEnsemble)
        extends IOutgoing
{
    source.getSourceConnections.add(this)

    target.getTargetConnections.add(this)

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "OutgoingConstraint(" + source.getName + ", " + target.getName + ")"
}