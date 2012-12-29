package unisson.model.impl

import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IGlobalIncoming}


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class GlobalIncomingConstraint(kind: String, source: IEnsemble, target: IEnsemble)
        extends IGlobalIncoming
{
    source.getSourceConnections.add(this)

    target.getTargetConnections.add(this)

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "GlobalIncomingConstraint(" + source.getName + ", " + target.getName + ")"
}