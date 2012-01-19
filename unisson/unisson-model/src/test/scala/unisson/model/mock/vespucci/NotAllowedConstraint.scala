package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.{IEnsemble, INotAllowed}

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class NotAllowedConstraint(kind: String, source: IEnsemble, target: IEnsemble)
        extends INotAllowed
{
    source.getSourceConnections.add(this)

    target.getTargetConnections.add(this)

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "NotAllowedConstraint(" + source.getName + ", " + target.getName + ")"
}