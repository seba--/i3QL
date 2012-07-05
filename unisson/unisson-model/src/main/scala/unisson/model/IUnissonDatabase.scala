package unisson.model

import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble}
import sae.LazyView
import de.tud.cs.st.vespucci.interfaces.{IViolationSummary, IViolation, ICodeElement}

/**
 *
 * Author: Ralf Mitschke
 * Date: 22.06.12
 * Time: 17:02
 *
 */
trait IUnissonDatabase
{
    private val defaultConcern = "default concern"

    /**
     * A violation is a tuple consisting of:<br>
     * 1. the constraint that is violated,<br>
     * 2. the source and target ensembles,<br>
     * 3. the source and target code elements,<br>
     * 4. the dependency kind (as String),<br>
     * 5. the concern (as String)
     */
    //type Violation = (IConstraint, IEnsemble, IEnsemble, ICodeElement, ICodeElement, String, String)

    /**
     * Add the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def addEnsemble(ensemble: IEnsemble)

    /**
     * Add the <code>ensemble</code> and it's children to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def addEnsembleToConcern(ensemble: IEnsemble)(implicit concern: String = defaultConcern)

    /**
     * Add the <code>constraint</code> and to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def addConstraintToConcern(constraint: IConstraint)(implicit concern: String = defaultConcern)

    /**
     * Remove the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def removeEnsemble(ensemble: IEnsemble)

    /**
     * Remove the <code>ensemble</code> and it's children to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def removeEnsembleFromConcern(ensemble: IEnsemble)(implicit concern: String = defaultConcern)

    /**
     * Remove the <code>constraint</code> and to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def removeConstraintFromConcern(constraint: IConstraint)(implicit concern: String = defaultConcern)


    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the global list of defined ensembles.
     */
    def updateEnsemble(oldE: IEnsemble, newE: IEnsemble)

    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def updateEnsembleInConcern(oldE: IEnsemble, newE: IEnsemble)(implicit concern: String = defaultConcern)

    /**
     * Convenience function for list of ensembles
     */
    def addEnsembles(ensembles: Iterable[IEnsemble]) {
        ensembles.foreach(addEnsemble)
    }

    /**
     * Convenience function for list of ensembles
     */
    def addEnsemblesToConcern(ensembles: Iterable[IEnsemble])(implicit concern: String = defaultConcern) {
        ensembles.foreach(addEnsembleToConcern)
    }

    /**
     * Convenience function for list of constraints
     */
    def addConstraintsToConcern(constraints: Iterable[IConstraint])(implicit concern: String = defaultConcern) {
        constraints.foreach(addConstraintToConcern)
    }

    /**
     * Convenience function for list of ensembles
     */
    def removeEnsembles(ensembles: Iterable[IEnsemble]) {
        ensembles.foreach(removeEnsemble)
    }

    /**
     * Convenience function for list of ensembles
     */
    def removeEnsemblesFromConcern(ensembles: Iterable[IEnsemble])(implicit concern: String = defaultConcern) {
        ensembles.foreach(removeEnsembleFromConcern)
    }

    /**
     * Convenience function for list of constraints
     */
    def removeConstraintsFromConcern(constraints: Iterable[IConstraint])
                                    (implicit concern: String = defaultConcern) {
        constraints.foreach(removeConstraintFromConcern)
    }


    /**
     * A global list of all ensembles, including children
     */
    def ensembles: LazyView[IEnsemble]

    /**
     * Queries of ensembles are compiled from a string that is a value in the database.
     * Hence they are wrapped in their own view implementation
     */
    def ensemble_elements: LazyView[(IEnsemble, ICodeElement)]

    /**
     * A list of ensembles in one concern
     */
    def concern_ensembles: LazyView[(IEnsemble, String)]

    /**
     * A list of constraints in one concern
     */
    def concern_constraints: LazyView[(IConstraint, String)]

    /**
     * A list naming all concerns
     */
    def concerns: LazyView[String]

    /**
     * A list of dependencies between the source code elements
     */
    def sourceCodeDependencies: LazyView[(ICodeElement, ICodeElement, String)]

    /**
     * A list of dependencies between the ensembles (lifting of the dependencies between the source code elements).
     */
    def ensembleDependencies: LazyView[(IEnsemble, IEnsemble, String)]

    /**
     * A list of ensemble dependencies that are not allowed
     */
    def notAllowedEnsembleDependencies: LazyView[(IEnsemble, IEnsemble, String)]

    /**
     * A list of ensemble dependencies that are expected
     */
    def expectedEnsembleDependencies: LazyView[(IEnsemble, IEnsemble, String)]

    /**
     * A list of violating ensembles dependencies
     */
    def consistencyViolations: LazyView[(IEnsemble, IEnsemble, String)]

    /**
     * A list of violations with full information on source code dependencies and violating constraint
     */
    def violations: LazyView[IViolation]

    /**
     * A list of violations summing up individual source code dependencies
     */
    def violation_summary : LazyView[IViolationSummary]
}