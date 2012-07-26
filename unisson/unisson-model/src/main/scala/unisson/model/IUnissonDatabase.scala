package unisson.model

import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble}
import sae.{MaterializedView, LazyView}
import de.tud.cs.st.vespucci.interfaces.{IViolationSummary, IViolation, ICodeElement}
import sae.syntax.RelationalAlgebraSyntax._
import sae.operators.Conversions
import sae.functions.Count

/**
 *
 * Author: Ralf Mitschke
 * Date: 22.06.12
 * Time: 17:02
 *
 */
trait IUnissonDatabase
{
    private val defaultSlice = "_default"

    /**
     * A violation is a tuple consisting of:<br>
     * 1. the constraint that is violated,<br>
     * 2. the source and target ensembles,<br>
     * 3. the source and target code elements,<br>
     * 4. the dependency kind (as String),<br>
     * 5. the slice (as String)
     */
    //type Violation = (IConstraint, IEnsemble, IEnsemble, ICodeElement, ICodeElement, String, String)

    /**
     * Add the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def addEnsemble(ensemble: IEnsemble)

    /**
     * Add the <code>ensemble</code> and it's children to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def addEnsembleToSlice(ensemble: IEnsemble)(implicit slice: String = defaultSlice)

    /**
     * Add the <code>constraint</code> and to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def addConstraintToSlice(constraint: IConstraint)(implicit slice: String = defaultSlice)

    /**
     * Remove the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def removeEnsemble(ensemble: IEnsemble)

    /**
     * Remove the <code>ensemble</code> and it's children to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def removeEnsembleFromSlice(ensemble: IEnsemble)(implicit slice: String = defaultSlice)

    /**
     * Remove the <code>constraint</code> and to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def removeConstraintFromSlice(constraint: IConstraint)(implicit slice: String = defaultSlice)


    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the global list of defined ensembles.
     */
    def updateEnsemble(oldE: IEnsemble, newE: IEnsemble)

    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def updateEnsembleInSlice(oldE: IEnsemble, newE: IEnsemble)(implicit slice: String = defaultSlice)

    /**
     * Convenience function for list of ensembles
     */
    def addEnsembles(ensembles: Iterable[IEnsemble]) {
        ensembles.foreach(addEnsemble)
    }

    /**
     * Convenience function for list of ensembles
     */
    def addEnsemblesToSlice(ensembles: Iterable[IEnsemble])(implicit slice: String = defaultSlice) {
        ensembles.foreach(addEnsembleToSlice)
    }

    /**
     * Convenience function for list of constraints
     */
    def addConstraintsToSlice(constraints: Iterable[IConstraint])(implicit slice: String = defaultSlice) {
        constraints.foreach(addConstraintToSlice)
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
    def removeEnsemblesFromSlice(ensembles: Iterable[IEnsemble])(implicit slice: String = defaultSlice) {
        ensembles.foreach(removeEnsembleFromSlice)
    }

    /**
     * Convenience function for list of constraints
     */
    def removeConstraintsFromSlice(constraints: Iterable[IConstraint])
                                    (implicit slice: String = defaultSlice) {
        constraints.foreach(removeConstraintFromSlice)
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
     * A list of ensembles in one slice
     */
    def slice_ensembles: LazyView[(IEnsemble, String)]

    /**
     * A list of constraints in one slice
     */
    def slice_constraints: LazyView[(IConstraint, String)]

    /**
     * A list naming all slices
     */
    val slices: LazyView[String] = δ(Π {
        (_: (IEnsemble, String))._2
    }(slice_ensembles))

    /**
     * A list of dependencies between the source code elements
     */
    def source_code_dependencies: LazyView[(ICodeElement, ICodeElement, String)]


    /**
     * A list of all descendants of an ensemble in the form (parent,child)
     */
    def children: LazyView[(IEnsemble, IEnsemble)]

    /**
     * A list of all descendants of an ensemble in the form (ancestor,descendant)
     */
    val descendants: LazyView[(IEnsemble, IEnsemble)] = {
        TC(children)(_._1, _._2)
    }


    /**
     * A list of dependencies between the ensembles (lifting of the dependencies between the source code elements).
     * Each entry can be included multiple times, since two ensembles can have multiple dependencies to the same element
     */
    val ensemble_dependencies: LazyView[(IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)] = {
        val indexed_ensemble_element = Conversions.lazyViewToIndexedView(ensemble_elements)

        val indexed_source_code_dependencies = Conversions.lazyViewToIndexedView(source_code_dependencies)

        val sourceEnsembleDependencies = (
                (
                        indexed_ensemble_element,
                        (_: (IEnsemble, ICodeElement))._2
                        ) ⋈
                        (
                                (_: (ICodeElement, ICodeElement, String))._1,
                                indexed_source_code_dependencies
                                )
                ) {
            (source: (IEnsemble, ICodeElement), dependency: (ICodeElement, ICodeElement, String)) =>
                (source, dependency)
        }
        val targetEnsembleDependencies = (
                (
                        indexed_ensemble_element,
                        (_: (IEnsemble, ICodeElement))._2
                        ) ⋈
                        (
                                (_: ((IEnsemble, ICodeElement), (ICodeElement, ICodeElement, String)))._2._2,
                                sourceEnsembleDependencies
                                )
                ) {
            (target: (IEnsemble, ICodeElement),
             sourceDependency: ((IEnsemble, ICodeElement), (ICodeElement, ICodeElement, String))) =>
                (sourceDependency._1._1, target._1, sourceDependency._1._2, target._2, sourceDependency._2._3)
        }
        val filteredSelfRef = σ((dependency: (IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)) =>
            (dependency._1 != dependency._2)
        )(targetEnsembleDependencies)


        val descendantsAndAncestors = descendants ∪ Π((_: (IEnsemble, IEnsemble)).swap)(descendants)

        (
                filteredSelfRef,
                (entry: (IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)) => (entry._1, entry._2)
                ) ⊳(
                identity[(IEnsemble, IEnsemble)],
                descendantsAndAncestors
                )
    }


    /**
     * A list of ensemble dependencies that are not allowed in the form:
     * (E_src, E_trgt, kind, constraint, slice)
     */
    protected[model] def notAllowedEnsembleDependencies: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)]

    /**
     * A list of ensemble dependencies that are expected in the form:
     * (E_src, E_trgt, kind, constraint, slice)
     */
    protected[model] def expectedEnsembleDependencies: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)]

    /**
     * A list of violations with full information on source code dependencies and violating constraint
     */
    val violations: LazyView[IViolation] = {
        val disallowed_dependency_violations = (
                (
                        ensemble_dependencies,
                        (entry: (IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)) => (entry._1, entry
                                ._2, entry._5)
                        ) ⋈(
                        (entry: (IEnsemble, IEnsemble, String, IConstraint, String)) => (entry._1, entry
                                ._2, entry._3),
                        notAllowedEnsembleDependencies
                        )
                ) {
            (dependency: (IEnsemble, IEnsemble, ICodeElement, ICodeElement, String),
             disallowed: (IEnsemble, IEnsemble, String, IConstraint, String)) => {
                new Violation(
                    disallowed._4,
                    dependency._1,
                    dependency._2,
                    dependency._3,
                    dependency._4,
                    dependency._5,
                    disallowed._5
                ).asInstanceOf[IViolation]
            }
        }

        val unfullfilled_dependency_expectations =
            Π(
                (expected: (IEnsemble, IEnsemble, String, IConstraint, String)) => {
                    new Violation(
                        expected._4,
                        expected._1,
                        expected._2,
                        null,
                        null,
                        expected._3,
                        expected._5
                    ).asInstanceOf[IViolation]
                }
            )(
                (
                        expectedEnsembleDependencies,
                        (entry: (IEnsemble, IEnsemble, String, IConstraint, String)) =>
                            (entry._1, entry._2, entry._3)
                        ) ⊳(
                        (entry: (IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)) =>
                            (entry._1, entry._2, entry._5),
                        ensemble_dependencies
                        )
            )

        disallowed_dependency_violations ∪ unfullfilled_dependency_expectations
    }

    /**
     * A list of violations summing up individual source code dependencies
     */
    lazy val violation_summary: LazyView[IViolationSummary] =
        γ(violations,
            (v: IViolation) => (v.getDiagramFile, v.getSourceEnsemble, v.getTargetEnsemble, v.getConstraint),
            Count[IViolation](),
            (elem: (String, IEnsemble, IEnsemble, IConstraint), count: Int) =>
                ViolationSummary(elem._4, elem._2, elem._3, elem._1, count)
        )


    def unmodeled_elements: LazyView[ICodeElement]

    @deprecated("use ensemble_dependency_count")
    def ensembleDependencies: MaterializedView[(IEnsemble, IEnsemble, Int)] =
        ensemble_dependency_count


    def ensemble_dependency_count: MaterializedView[(IEnsemble, IEnsemble, Int)]

    /**
     * A list of errors that occurred during database updates
     */
    def errors: LazyView[Exception]

    /**
     * Clear the list of errors from the database
     */
    def clear_errors()
}