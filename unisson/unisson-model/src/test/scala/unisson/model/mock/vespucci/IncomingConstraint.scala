package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.IIncoming


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class IncomingConstraint(kind: String, source: Ensemble, target: Ensemble)
        extends IIncoming
{
    source.sourceConnections += this

    target.targetConnections += this

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "Constraint(" + source.getName + ", " + target.getName + ")"
}