package unisson.model

import de.tud.cs.st.vespucci.model.{IEnsemble, IConstraint}
import de.tud.cs.st.vespucci.interfaces.{IViolation, ICodeElement}

/**
 *
 * Author: Ralf Mitschke
 * Date: 31.12.11
 * Time: 18:24
 *
 */
case class Violation(constraint: IConstraint,
                     sourceEnsemble: IEnsemble,
                     targetEnsemble: IEnsemble,
                     sourceElement: ICodeElement,
                     targetElement: ICodeElement,
                     kind: String,
                     context: String)
        extends IViolation
{
    def getViolatingKind = kind

    def getDiagramFile = context

    def getSourceElement = sourceElement

    def getTargetElement = targetElement

    def getSourceEnsemble = sourceEnsemble

    def getTargetEnsemble = targetEnsemble

    def getConstraint = constraint
}