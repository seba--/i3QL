package unisson.model.mock.vespucci

import unisson.model.kinds.DependencyKind
import de.tud.cs.st.vespucci.model.{IEnsemble, INotAllowed}

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class NotAllowedConstraint(kind : String, source:Ensemble, target: Ensemble)
        extends INotAllowed
{
    source.sourceConnections += this

    target.targetConnections += this

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "Constraint(" + source.getName + ", " + target.getName + ")"
}