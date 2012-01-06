package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.model.IDocumentedViolation


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class DocumentedViolation(kind: String, source: Ensemble, target: Ensemble)
        extends IDocumentedViolation
{
    source.sourceConnections += this

    target.targetConnections += this

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "DocumentedViolation(" + source.getName + ", " + target.getName + ")"
}