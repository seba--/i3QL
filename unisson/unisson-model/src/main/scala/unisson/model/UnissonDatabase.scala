package unisson.model

import constraints.{NormalizedConstraint, ConstraintType}
import kinds.{KindResolver, DependencyKind, KindParser}
import kinds.primitive._
import unisson.query.code_model.SourceElement
import sae.bytecode.Database
import sae.collections.Table
import sae.{Observer, LazyView}
import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble}
import de.tud.cs.st.vespucci.interfaces.ICodeElement
import sae.bytecode.model.dependencies._
import de.tud.cs.st.bat.ArrayType
import sae.bytecode.model.dependencies.parameter
import sae.bytecode.model.dependencies.return_type
import sae.bytecode.model.dependencies.read_field
import sae.bytecode.model.dependencies.write_field
import sae.bytecode.model.dependencies.field_type
import unisson.query.UnissonQuery
import unisson.query.parser.QueryParser
import unisson.query.ast.{OrQuery, DerivedQuery, EmptyQuery}


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.12.11
 * Time: 16:24
 *
 * Validation of the model, queries and dependency kinds is assumed to be in the scope of the Eclipse UI.
 *
 * The database relies on the IEnsemble and IConstraint interfaces.
 * Objects implementing these interfaces must provide the following equality guarantees:
 *
 * IEnsemble elements are equal if their names are equal, additional equality may include inner ensembles.
 * Under no circumstances must the queries be part of the equality.
 *
 */
class UnissonDatabase(val bc: Database)
        extends IUnissonDatabase with IUnissonArchitectureModelDatabase
{

    import sae.syntax.RelationalAlgebraSyntax._


    import scala.collection.JavaConversions._

    /**
     * Utility for parsing the kinds in a constraint.
     */
    private val kindParser = new KindParser()

    /**
     * Utility for parsing queries of an ensemble
     */
    private val queryParser = new QueryParser()

    /**
     * Returns the parsed query or an empty query if the query could not be parsed.
     * In the later event, the error is logged in the database.
     * Queries are normalized, such that an ensemble with a derived query, has a query equal to the queries of it's children
     */
    private def getNormalizedQuery(ensemble: IEnsemble): UnissonQuery = {
        val query = queryParser.parse(ensemble.getQuery) match {
            case queryParser.Success(result, _) => result
            case queryParser.Failure(msg, next) => {
                errors += new IllegalArgumentException(msg + next.pos.longString)
                EmptyQuery()
            }
        }
        if (query.isSyntacticEqual(DerivedQuery())) {
            val childrenQueries = ensemble.getInnerEnsembles.map(getNormalizedQuery)
            childrenQueries.fold(EmptyQuery())(OrQuery(_, _))
        }
        else {
            query
        }
    }

    /**
     * The table of all ensembles, including children
     */
    lazy val ensembles = new Table[IEnsemble]()

    /**
     * A list of all descendants of an ensemble in the form (parent,child)
     */
    lazy val children = new Table[(IEnsemble, IEnsemble)]

    /**
     * The table of concerns and their ensembles
     */
    lazy val concern_ensembles = new Table[(IEnsemble, String)]()

    /**
     * The table of concerns and their constraints
     */
    lazy val concern_constraints = new Table[(IConstraint, String)]()

    /**
     * The queries for each ensemble
     */
    lazy val ensemble_queries: LazyView[(IEnsemble, UnissonQuery)] =
        Π((e: IEnsemble) => (e, getNormalizedQuery(e)))(ensembles)

    /**
     * Queries of ensembles are compiled from a string that is a value in the database.
     * Hence they are wrapped in their own view implementation
     */
    lazy val ensemble_elements = lazyViewToResult(new CompiledEnsembleElementsView(bc, ensemble_queries))


    /**
     * Add the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def addEnsemble(ensemble: IEnsemble) {
        ensembles += ensemble
        for (child <- ensemble.getInnerEnsembles) {
            children +=(ensemble, child)
            addEnsemble(child)
        }
    }

    /**
     * Add the <code>ensemble</code> and it's children to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def addEnsembleToConcern(ensemble: IEnsemble)(implicit concern: String) {
        concern_ensembles +=(ensemble, concern)
        for (child <- ensemble.getInnerEnsembles) {
            addEnsembleToConcern(child)
        }
    }

    /**
     * Add the <code>constraint</code> and to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def addConstraintToConcern(constraint: IConstraint)(implicit concern: String) {
        concern_constraints +=(constraint, concern)
    }

    /**
     * Remove the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def removeEnsemble(ensemble: IEnsemble) {
        ensembles -= ensemble
        for (child <- ensemble.getInnerEnsembles) {
            children -=(ensemble, child)
            removeEnsemble(child)
        }
    }

    /**
     * Remove the <code>ensemble</code> and it's children to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def removeEnsembleFromConcern(ensemble: IEnsemble)(implicit concern: String) {
        concern_ensembles -=(ensemble, concern)
        for (child <- ensemble.getInnerEnsembles) {
            removeEnsembleFromConcern(child)
        }
    }

    /**
     * Remove the <code>constraint</code> and to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def removeConstraintFromConcern(constraint: IConstraint)(implicit concern: String) {
        concern_constraints -=(constraint, concern)
    }

    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the global list of defined ensembles.
     */
    def updateEnsemble(oldE: IEnsemble, newE: IEnsemble) {
        ensembles.update(oldE, newE)
        val oldEChildren: scala.collection.mutable.Set[IEnsemble] = oldE.getInnerEnsembles
        val newEChildren: scala.collection.mutable.Set[IEnsemble] = newE.getInnerEnsembles

        val (retainedOldChildren, notExistingChildren) = oldEChildren.partition(
            (e: IEnsemble) => newEChildren.exists((_: IEnsemble).getName == e.getName)
        )
        val (retainedNewChildren, newChildren) = newEChildren.partition(
            (e: IEnsemble) => oldEChildren.exists((_: IEnsemble).getName == e.getName)
        )

        // remove old children
        for (child <- notExistingChildren) {
            // remove the parent-child relation
            children -=(oldE, child)
            // transitively remove all further children
            removeEnsemble(child)
        }
        // update existing children
        for (oldChild <- retainedOldChildren;
             newChild <- retainedNewChildren.find(_.getName == oldChild.getName)) {
            //val newChild = retainedNewChildren.find( _.getName == oldChild.getName).get
            // update the parent child relation
            children.update((oldE, oldChild), (newE, newChild))
            // transitively update the child
            updateEnsemble(oldChild, newChild)
        }
        // add new children
        for (child <- newChildren) {
            // add the parent-child relation
            children +=(newE, child)
            // transitively add all further children
            addEnsemble(child)
        }
    }

    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the local <code>concern</code>.
     * The concern can be bound to a string via <code>implicit</code>.
     * The concern can be omitted, resulting in the name: "default concern".
     */
    def updateEnsembleInConcern(oldE: IEnsemble, newE: IEnsemble)(implicit concern: String) {
        concern_ensembles.update((oldE, concern), (newE, concern))
        val oldEChildren: scala.collection.mutable.Set[IEnsemble] = oldE.getInnerEnsembles
        val newEChildren: scala.collection.mutable.Set[IEnsemble] = newE.getInnerEnsembles

        val (retainedOldChildren, notExistingChildren) = oldEChildren.partition(
            (e: IEnsemble) => newEChildren.exists((_: IEnsemble).getName == e.getName)
        )
        val (retainedNewChildren, newChildren) = newEChildren.partition(
            (e: IEnsemble) => oldEChildren.exists((_: IEnsemble).getName == e.getName)
        )

        // remove old children
        for (child <- notExistingChildren) {
            // transitively remove all further children
            removeEnsembleFromConcern(child)
        }
        // update existing children
        for (oldChild <- retainedOldChildren;
             newChild <- retainedNewChildren.find(_.getName == oldChild.getName)) {
            // transitively update the child
            updateEnsembleInConcern(oldChild, newChild)
        }
        // add new children
        for (child <- newChildren) {
            // transitively add all further children
            addEnsembleToConcern(child)
        }
    }

    private def dependencyView_to_tupleView[S <: AnyRef, T <: AnyRef](dependencyView: LazyView[_ <: Dependency[S, T]],
                                                                      kind: DependencyKind
                                                                             ): LazyView[(ICodeElement, ICodeElement, String)] = {
        Π[Dependency[S, T], (ICodeElement, ICodeElement, String)](
            (d: Dependency[S, T]) => (SourceElement(d.source), SourceElement(d.target), kind.asVespucciString)
        )(dependencyView.asInstanceOf[LazyView[Dependency[S, T]]])
    }

    /**
     * A list of dependencies between the source code elements
     */
    lazy val source_code_dependencies =
        dependencyView_to_tupleView(bc.`extends`, ExtendsKind) ∪
                dependencyView_to_tupleView(bc.implements, ImplementsKind) ∪
                dependencyView_to_tupleView(bc.invoke_interface, InvokeInterfaceKind) ∪
                dependencyView_to_tupleView(bc.invoke_special, InvokeSpecialKind) ∪
                dependencyView_to_tupleView(bc.invoke_static, InvokeStaticKind) ∪
                dependencyView_to_tupleView(bc.invoke_virtual, InvokeVirtualKind) ∪
                dependencyView_to_tupleView(bc.create, CreateKind) ∪
                dependencyView_to_tupleView(bc.class_cast, ClassCastKind) ∪
                dependencyView_to_tupleView(bc.thrown_exceptions, ThrowsKind) ∪
                dependencyView_to_tupleView(// parameter
                    σ(
                        (v: parameter) => !(v.target.isBaseType)
                    )(
                        Π[parameter, parameter] {
                            case (parameter(m, ArrayType(component))) => parameter(m, component)
                            case x => x
                        }(bc.parameter)
                    ),
                    ParameterKind) ∪
                dependencyView_to_tupleView(// return types
                    σ(
                        (v: return_type) => !(v.target.isBaseType || v.target.isVoidType)
                    )(
                        Π[return_type, return_type] {
                            case (return_type(m, ArrayType(component))) => return_type(m, component)
                            case x => x
                        }(bc.return_type)
                    ),
                    ReturnTypeKind) ∪
                dependencyView_to_tupleView(// field types
                    σ(
                        (v: field_type) => !(v.target.isBaseType)
                    )(
                        Π[field_type, field_type] {
                            case (field_type(m, ArrayType(component))) => field_type(m, component)
                            case x => x
                        }(bc.field_type)
                    ),
                    FieldTypeKind) ∪
                dependencyView_to_tupleView(// read_field instructions
                    σ(
                        (v: read_field) => !(v.target.fieldType.isBaseType)
                    )(
                        // TODO what about arrays with component types of not allowed elements?
                        bc.read_field
                    ),
                    ReadFieldKind) ∪
                dependencyView_to_tupleView(// write_field instructions
                    σ(
                        // TODO what about arrays with component types of not allowed elements?
                        (v: write_field) => !v.target.fieldType.isBaseType
                    )(
                        bc.write_field
                    ),
                    WriteFieldKind)
    /*
                // TODO instance of checks
                Π {
                    (InstanceOfKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.inner_classes.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
    */


    private def parseQueryKinds(constraint: IConstraint): Set[DependencyKind] = {
        kindParser.parse(constraint.getDependencyKind) match {
            case kindParser.Success(result, _) => KindResolver(result)
            case kindParser.Failure(msg, next) => {
                errors += new IllegalArgumentException(msg + next.pos.longString)
                Set()
            }
        }
    }

    /**
     * Returns a list of constraints, that only contain one dependency kind.
     * InAndOut constraints are normalized as one incoming and one outgoing constraint
     */
    private def getNormalizedConstraints[T](v: (IConstraint, String)): List[NormalizedConstraint] = {
        val kinds = parseQueryKinds(v._1)
        val typ = ConstraintType(v._1)
        var result: List[NormalizedConstraint] = Nil
        for (kind <- kinds) {
            if (typ != ConstraintType.IncomingAndOutgoing) {
                result = NormalizedConstraint(
                    v._1,
                    kind,
                    typ,
                    v._1.getSource,
                    v._1.getTarget,
                    v._2
                ) :: result
            }
            else {
                result = NormalizedConstraint(
                    v._1,
                    kind,
                    ConstraintType.Incoming,
                    v._1.getSource,
                    v._1.getTarget,
                    v._2
                ) :: NormalizedConstraint(
                    v._1,
                    kind,
                    ConstraintType.Outgoing,
                    v._1.getSource,
                    v._1.getTarget,
                    v._2
                ) :: result
            }
        }
        result
    }

    lazy val normalized_constraints = new LazyView[(NormalizedConstraint)] {

        concern_constraints.addObserver(new Observer[(IConstraint, String)] {
            def updated(oldV: (IConstraint, String), newV: (IConstraint, String)) {
                removed(oldV)
                added(newV)
            }

            def removed(v: (IConstraint, String)) {
                val normalizedConstraints = getNormalizedConstraints(v)
                normalizedConstraints.foreach(element_removed)
            }

            def added(v: (IConstraint, String)) {
                val normalizedConstraints = getNormalizedConstraints(v)
                normalizedConstraints.foreach(element_added)
            }

        })

        def lazy_foreach[T](f: ((NormalizedConstraint)) => T) {
            concern_constraints.foreach(
                getNormalizedConstraints(_).foreach(f)
            )
        }

        def lazyInitialize() {
            initialized = true
        }
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the not_allowed constraints in the form:
     * (E_src, E_trgt, kind, constraint, concern).
     */
    private lazy val disallowed_dependencies_by_not_allowed: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)] = {
        Π(
            (constraint: NormalizedConstraint) =>
                (constraint.source, constraint.target, constraint.kind.asVespucciString, constraint.origin, constraint
                        .context)
        )(
            σ((_: NormalizedConstraint).constraintType == ConstraintType.NotAllowed)(normalized_constraints)
        )
    }

    private lazy val local_incoming = σ((_: NormalizedConstraint).constraintType == ConstraintType
            .Incoming)(normalized_constraints)

    private lazy val global_incoming = σ((_: NormalizedConstraint).constraintType == ConstraintType
            .GlobalIncoming)(normalized_constraints)

    private lazy val local_outgoing = σ((_: NormalizedConstraint).constraintType == ConstraintType
            .Outgoing)(normalized_constraints)

    private lazy val global_outgoing = σ((_: NormalizedConstraint).constraintType == ConstraintType
            .GlobalOutgoing)(normalized_constraints)


    private lazy val incoming: LazyView[NormalizedConstraint] = {
        val allIncoming = local_incoming ∪ global_incoming
        allIncoming ∪ (
                (
                        (
                                descendants,
                                (_: (IEnsemble, IEnsemble))._1
                                ) ⋈(
                                (_: NormalizedConstraint).source,
                                allIncoming
                                )
                        ) {
                    (parentChild: (IEnsemble, IEnsemble), c: NormalizedConstraint) =>
                        NormalizedConstraint(
                            c.origin,
                            c.kind,
                            c.constraintType,
                            parentChild._2,
                            c.target,
                            c.context
                        )
                })
    }


    private lazy val outgoing: LazyView[NormalizedConstraint] = local_outgoing ∪ global_outgoing // TODO make this joint with descentandts

    /**
     * Returns a view of the disallowed ensemble dependencies derived from the local_incoming constraints in the form:
     * (E_src, E_trgt, kind, constraint, concern).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    lazy val constrained_ensemble_combinations_by_local_incoming: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)] = {
        (
                (
                        concern_ensembles,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        local_incoming
                        )
                ) {
            (e: (IEnsemble, String), c: NormalizedConstraint) => (e._1, c.target, c.kind.asVespucciString, c.origin, c
                    .context)
        }
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the global_incoming constraints in the form:
     * (E_src, E_trgt, kind, constraint, concern).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    lazy val constrained_ensemble_combinations_by_global_incoming: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)] = {
        Π(
            (entry: (IEnsemble, NormalizedConstraint)) =>
                (entry._1, entry._2.target, entry._2.kind.asVespucciString, entry._2.origin, entry._2.context)
        )(ensembles × global_incoming)
    }


    private lazy val disallowed_dependencies_by_incoming = {
        val source_target_ensemble_combinations =
            constrained_ensemble_combinations_by_local_incoming ∪
                    constrained_ensemble_combinations_by_global_incoming

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Z,Y) where Z,Y in Ensembles and Incoming(_,Y, ctx) ;
         * if !exists (Z,Y) with Incoming(Z,Y, ctx) or GlobalIncoming(Z,Y, ctx) then Z may not use Y
         */
        (
                source_target_ensemble_combinations,
                (entry: (IEnsemble, IEnsemble, String, IConstraint, String)) => (entry._1, entry._2, entry._3, entry._5)
                ) ⊳(
                (c: NormalizedConstraint) => (c.source, c.target, c.kind.asVespucciString, c.context),
                incoming
                )
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the local_outgoing constraints in the form:
     * (E_src, E_trgt, kind, constraint, concern).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    lazy val constrained_ensemble_combinations_by_local_outgoing: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)] = {
        (
                (
                        concern_ensembles,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        local_outgoing
                        )
                ) {
            (e: (IEnsemble, String), c: NormalizedConstraint) => (c.source, e._1, c.kind.asVespucciString, c.origin, c
                    .context)
        }
    }

    /**
     * Returns a view of the disallowed ensemble dependencies derived from the local_outgoing constraints in the form:
     * (E_src, E_trgt, kind, constraint, concern).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    lazy val constrained_ensemble_combinations_by_global_outgoing: LazyView[(IEnsemble, IEnsemble, String, IConstraint, String)] = {
        Π(
            (entry: (IEnsemble, NormalizedConstraint)) =>
                (entry._2.source, entry._1, entry._2.kind.asVespucciString, entry._2.origin, entry._2.context)
        )(ensembles × global_outgoing)
    }


    private lazy val disallowed_dependencies_by_outgoing = {
        val source_target_ensemble_combinations =
            constrained_ensemble_combinations_by_local_outgoing ∪
                    constrained_ensemble_combinations_by_global_outgoing

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Y, Z) where Z,Y in Ensembles and Outgoing(Y,_, ctx) ;
         * if !exists (Y, Z) with Outgoing(Y,Z, ctx) or GlobalOutgoing(Y,Z, ctx) then Y may not use Z
         */
        (
                source_target_ensemble_combinations,
                (entry: (IEnsemble, IEnsemble, String, IConstraint, String)) => (entry._1, entry._2, entry._3, entry._5)
                ) ⊳(
                (c: NormalizedConstraint) => (c.source, c.target, c.kind.asVespucciString, c.context),
                outgoing
                )
    }


    /**
     * A list of ensemble dependencies that are not allowed in the form:
     * (E_src, E_trgt, kind, constraint, concern)
     */
    def notAllowedEnsembleDependencies = disallowed_dependencies_by_not_allowed ∪
            disallowed_dependencies_by_incoming ∪
            disallowed_dependencies_by_outgoing

    /**
     * A list of ensemble dependencies that are expected
     */
    def expectedEnsembleDependencies = null

    /**
     * A list of violating ensembles dependencies
     */
    def consistencyViolations = null


    /**
     * A list of violations summing up individual source code dependencies
     */
    def violation_summary = null


    /*



        lazy val source_target_violations_by_local_outgoing: LazyView[((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)] = {
            // all source target combinations that have to do with an ensemble where a constraint is declared
            val source_target_combinations_with_selfref = (
                    (
                            leaf_local_ensembles,
                            (_: (IEnsemble, String))._2
                            ) ⋈(
                            (_: NormalizedConstraint).context,
                            local_outgoing
                            )
                    ) {
                (e: (IEnsemble, String), c: NormalizedConstraint) => ((c.source, e._1, c.kind), c)
            }

            // filter obviously allowed combinations
            // Allowed are all (A, Outgoing(_, A) and (A, Outgoing(A, _)
            val source_target_combinations = δ(σ {
                (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                    (e._1._1 != e._1._2)
            }(source_target_combinations_with_selfref))

            /**
             * all disallowed combinations taking all constraints to an ensemble into account
             * for all (Y, Z) where Z,Y in Ensembles and Outgoing(Y,_, ctx) ;
             * if !exists (Y, Z) with Outgoing(Y,Z, ctx) or GlobalOutgoing(Y,Z, ctx) then Y may not use Z
             */
            val disallowedEnsemblesWithSameContext = (
                    (
                            source_target_combinations,
                            (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                                (e._1, e._2.context)
                            ) ⊳(
                            (c: NormalizedConstraint) => ((c.source, c.target, c.kind), c.context),
                            local_outgoing ∪ global_outgoing
                            )

                    )
            disallowedEnsemblesWithSameContext
        }


        lazy val source_target_violations_by_global_outgoing: LazyView[((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)] = {
            // all source target combinations that have to do with an ensemble where a constraint is declared
            val source_target_combinations_with_selfref = (
                    (
                            leaf_ensembles × contexts,
                            (_: (IEnsemble, String))._2
                            ) ⋈(
                            (_: NormalizedConstraint).context,
                            global_outgoing
                            )
                    ) {
                (e: (IEnsemble, String), c: NormalizedConstraint) => ((c.source, e._1, c.kind), c)
            }

            // filter obviously allowed combinations
            // Allowed are all (A, Outgoing(_, A) and (A, Outgoing(A, _)
            val source_target_combinations = δ(σ {
                (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                    (e._1._1 != e._1._2)
            }(source_target_combinations_with_selfref))

            /**
             * all disallowed combinations taking all constraints to an ensemble into account
             * for all (Y, Z) where Z,Y in Ensembles and Outgoing(Y,_, ctx) ;
             * if !exists (Y, Z) with Outgoing(Y,Z, ctx) or GlobalOutgoing(Y,Z, ctx) then Y may not use Z
             */
            val disallowedEnsemblesWithSameContext = (
                    (
                            source_target_combinations,
                            (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                                (e._1, e._2.context)
                            ) ⊳(
                            (c: NormalizedConstraint) => ((c.source, c.target, c.kind), c.context),
                            local_outgoing ∪ global_outgoing
                            )

                    )
            disallowedEnsemblesWithSameContext
        }

        lazy val source_target_violations: LazyView[IViolation] = {
            val source_target_disallowed =
                source_target_violations_by_not_allowed ∪
                        source_target_violations_by_local_incoming ∪
                        source_target_violations_by_global_incoming ∪
                        source_target_violations_by_local_outgoing ∪
                        source_target_violations_by_global_outgoing

            val violations = (
                    (
                            source_target_disallowed,
                            (t: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) => t._1
                            ) ⋈(
                            (t: ((IEnsemble, IEnsemble, DependencyKind), Dependency[AnyRef, AnyRef])) => t._1,
                            ensemble_dependencies
                            )
                    ) {
                (disallowed: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint),
                 dependency: ((IEnsemble, IEnsemble, DependencyKind), Dependency[AnyRef, AnyRef])) =>
                    new Violation(
                        disallowed._2.origin,
                        disallowed._1._1,
                        disallowed._1._2,
                        SourceElement(dependency._2.source),
                        SourceElement(dependency._2.target),
                        disallowed._2.kind.asVespucciString,
                        disallowed._2.context
                    ).asInstanceOf[IViolation]
            }
            violations
        }


        lazy val violations: LazyView[IViolation] =
            source_target_violations ∪ violations_expected

        lazy val violations_expected: LazyView[IViolation] = {
            new DefaultLazyView[IViolation]
        }


        lazy val violation_summary: LazyView[IViolationSummary] =
            γ(violations,
                (v: IViolation) => (v.getDiagramFile, v.getSourceEnsemble, v.getTargetEnsemble, v.getConstraint),
                Count[IViolation](),
                (elem: (String, IEnsemble, IEnsemble, IConstraint), count: Int) =>
                    ViolationSummary(elem._4, elem._2, elem._3, elem._1, count)
            )


        lazy val unmodeled_elements: LazyView[ICodeElement] = (
                (
                        Π(SourceElement(_: ObjectType))(bc.declared_types) ∪
                                Π(SourceElement(_: FieldDeclaration))(bc.declared_fields) ∪
                                Π(SourceElement(_: MethodDeclaration))(bc.declared_methods)
                        ) ∖
                        δ(Π((_: (IEnsemble, SourceElement[AnyRef]))._2)(leaf_ensemble_elements))
                ).asInstanceOf[LazyView[ICodeElement]]


        lazy val ensembleDependencies: MaterializedView[(IEnsemble, IEnsemble, Int)] = {
            val sourceEnsembleDependencies = (
                    (
                            leaf_ensemble_elements,
                            (_: (IEnsemble, SourceElement[AnyRef]))._2.element
                            ) ⋈
                            (
                                    (_: (DependencyKind, Dependency[AnyRef, AnyRef]))._2.source,
                                    kind_and_dependency
                                    )
                    ) {
                (e1: (IEnsemble, SourceElement[AnyRef]), e2: (DependencyKind, Dependency[AnyRef, AnyRef])) =>
                    (e1._1, e2._2)
            }
            val targetEnsembleDependencies = (
                    (
                            leaf_ensemble_elements,
                            (_: (IEnsemble, SourceElement[AnyRef]))._2.element
                            ) ⋈
                            (
                                    (_: (IEnsemble, Dependency[AnyRef, AnyRef]))._2.target,
                                    sourceEnsembleDependencies
                                    )
                    ) {
                (e1: (IEnsemble, SourceElement[AnyRef]), e2: (IEnsemble, Dependency[AnyRef, AnyRef])) =>
                    (e2._1, e1._1, e2._2)
            }
            val leafDependencies = γ(targetEnsembleDependencies,
                (v: (IEnsemble, IEnsemble, Dependency[AnyRef, AnyRef])) => (v._1, v._2),
                Count[(IEnsemble, IEnsemble, Dependency[AnyRef, AnyRef])](),
                (elem: (IEnsemble, IEnsemble), count: Int) => (elem._1, elem._2, count)
            )
            leafDependencies
        }
    */

    /**
     * A list of errors that occurred during database updates
     */
    lazy val errors = new Table[Exception]

    /**
     * Clear the list of errors from the database
     */
    def clear_errors() {
        val oldErrors = errors.copy
        oldErrors.foreach(errors -= _)
    }
}