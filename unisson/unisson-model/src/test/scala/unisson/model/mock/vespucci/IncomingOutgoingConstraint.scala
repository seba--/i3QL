package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.{IInAndOut, IEnsemble, IIncoming}


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class IncomingOutgoingConstraint(kind: String, source: IEnsemble, target: IEnsemble)
        extends IInAndOut
{
    source.getSourceConnections.add(this)

    target.getTargetConnections.add(this)

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "InOutConstraint(" + source.getName + ", " + target.getName + ")"
}