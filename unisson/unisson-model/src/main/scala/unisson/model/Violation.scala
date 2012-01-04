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
                     description: String)
        extends IViolation
{
    def getDescription = description

    def getSourceElement = sourceElement

    def getTargetElement = targetElement

    def getSourceEnsemble = sourceEnsemble

    def getTargetEnsemble = targetEnsemble

    def getConstraint = constraint
}