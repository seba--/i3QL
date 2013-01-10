package unisson.model

import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IConstraint, IViolationSummary}

/**
 *
 * Author: Ralf Mitschke
 * Date: 07.02.12
 * Time: 10:00
 *
 */
case class ViolationSummary(
                               constraint: IConstraint,
                               sourceEnsemble: IEnsemble,
                               targetEnsemble: IEnsemble,
                               context: String,
                               count: Int
                               )
    extends IViolationSummary
{
    def getDiagramFile = context

    def getSourceEnsemble = sourceEnsemble

    def getTargetEnsemble = targetEnsemble

    def getConstraint = constraint

    def numberOfViolations = count
}