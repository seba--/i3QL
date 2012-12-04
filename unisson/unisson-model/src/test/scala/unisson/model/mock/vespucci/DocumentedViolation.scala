package unisson.model.mock.vespucci

import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IDocumentedViolation}


/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:26
 *
 */
case class DocumentedViolation(kind: String, source: IEnsemble, target: IEnsemble)
        extends IDocumentedViolation
{
    source.getSourceConnections.add(this)

    target.getTargetConnections.add(this)

    def getDependencyKind = kind

    def getSource = source

    def getTarget = target

    override def toString = "DocumentedViolation(" + source.getName + ", " + target.getName + ")"
}