package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.IExpected


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class ExpectedConstraint(kind: String, source: Ensemble, target: Ensemble)
        extends IExpected
{
    source.sourceConnections += this

    target.targetConnections += this

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "ExpectedConstraint(" + source.getName + ", " + target.getName + ")"
}