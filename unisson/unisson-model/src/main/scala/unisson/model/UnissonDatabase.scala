package unisson.model

import de.tud.cs.st.vespucci.model.{IArchitectureModel, IConstraint, IEnsemble}
import unisson.model.kinds.{KindParser, KindResolver, DependencyKind}
import unisson.query.code_model.SourceElement
import sae.{Observer, DefaultLazyView, MaterializedView, LazyView}
import sae.syntax.RelationalAlgebraSyntax._
import unisson.model.constraints.{ConstraintType, ProxyNormalizedConstraint, NormalizedConstraint}
import sae.bytecode.model.dependencies.Dependency
import unisson.model.kinds.primitive._
import sae.bytecode.Database
import unisson.query.compiler.QueryCompiler
import de.tud.cs.st.vespucci.interfaces.IViolation


/**
 *
 * Author: Ralf Mitschke
 * Date: 31.12.11
 * Time: 16:24
 *
 */
class UnissonDatabase(bc: Database)
{

    val global_ensembles: MaterializedView[IEnsemble] = new DefaultLazyView[IEnsemble]

    val local_ensembles: MaterializedView[(IEnsemble, String)] = new DefaultLazyView[(IEnsemble, String)]

    val contexts: MaterializedView[String] = δ(Π {
        (_: (IEnsemble, String))._2
    }(local_ensembles))

    /**
     * constraints are always local, there is no global list of constraints
     */
    val local_constraints: LazyView[(IConstraint, String)] = new DefaultLazyView[(IConstraint, String)]

    // TODO emulation of subquery should be removed
    // todo lazy initialization
    val global_ensemble_elements: LazyView[(IEnsemble, SourceElement[AnyRef])] = new LazyView[(IEnsemble, SourceElement[AnyRef])]
    {

        private val queryCompiler = new QueryCompiler(bc)

        def lazy_foreach[T](f: ((IEnsemble, SourceElement[AnyRef])) => T) {


        }

        def lazyInitialize {


        }

        var queries: Map[IEnsemble, MaterializedView[SourceElement[AnyRef]]] = Map.empty

        var elementObservers: Map[IEnsemble, ElementObserver] = Map.empty

        val ensembleObserver = new Observer[IEnsemble]
        {
            def updated(oldV: IEnsemble, newV: IEnsemble) {
                removed(oldV)
                added(newV)
            }

            def removed(v: IEnsemble) {
                val query = queries(v)
                query.foreach(
                    (e: SourceElement[AnyRef]) => element_removed((v, e))
                )
                queries -= v
                query.removeObserver(elementObservers(v))
                elementObservers -= v
            }

            def added(v: IEnsemble) {
                val query: MaterializedView[SourceElement[AnyRef]] = queryCompiler.parseAndCompile(v.getQuery)
                query.foreach(
                    (e: SourceElement[AnyRef]) => element_added((v, e))
                )
                val oo = new ElementObserver(v)

                query.addObserver(oo)
                elementObservers += {
                    v -> oo
                }
                queries += {
                    v -> query
                }
            }
        }

        class ElementObserver(val ensemble: IEnsemble) extends Observer[SourceElement[AnyRef]]
        {
            def updated(oldV: SourceElement[AnyRef], newV: SourceElement[AnyRef]) {
                element_updated((ensemble, oldV), (ensemble, newV))
            }

            def removed(v: SourceElement[AnyRef]) {
                element_removed((ensemble, v))
            }

            def added(v: SourceElement[AnyRef]) {
                element_added((ensemble, v))
            }
        }


        global_ensembles.addObserver(ensembleObserver)
    }


    // TODO emulation of subquery should be removed
    val normalized_constraints = new DefaultLazyView[NormalizedConstraint] {

        private val kindParser = new KindParser()

        val observer = new Observer[(IConstraint, String)]
        {
            def updated(oldV: (IConstraint, String), newV: (IConstraint, String)) {
                removed(oldV)
                added(newV)
            }

            def removed(v: (IConstraint, String)) {
                // TODO error handling for kinds
                // TODO normalize in and out
                val kinds = KindResolver(kindParser.parse(v._1.getDependencyKind).get)
                for (kind <- kinds) {
                    element_removed(ProxyNormalizedConstraint(v._1, kind, v._2))
                }
            }

            def added(v: (IConstraint, String)) {
                // TODO error handling for kinds
                // TODO normalize in and out
                val kinds = KindResolver(kindParser.parse(v._1.getDependencyKind).get)
                for (kind <- kinds) {
                    element_added(ProxyNormalizedConstraint(v._1, kind, v._2))
                }
            }

        }

        local_constraints.addObserver(observer)
    }

    val kind_and_dependency: LazyView[(DependencyKind, Dependency[AnyRef, AnyRef])] = {
        Π {
            (ClassCastKind, (_: Dependency[AnyRef, AnyRef]))
        }(bc.class_cast.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (CreateKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.create.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (ExtendsKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.`extends`.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (FieldTypeKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.field_type.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (ImplementsKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.implements.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (InstanceOfKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.inner_classes.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (InvokeInterfaceKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.invoke_interface
                        .asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (InvokeSpecialKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.invoke_special.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (InvokeStaticKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.invoke_static.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (InvokeVirtualKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.invoke_virtual.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (ParameterKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.parameter.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (ReadFieldKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.read_field.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (ReturnTypeKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.return_type.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (ThrowsKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.thrown_exceptions
                        .asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                Π {
                    (WriteFieldKind, (_: Dependency[AnyRef, AnyRef]))
                }(bc.write_field.asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]])
    }


    val local_incoming = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.Incoming
    }(normalized_constraints)

    val global_incoming = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.GlobalIncoming
    }(normalized_constraints)

    val not_allowed = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.NotAllowed
    }(normalized_constraints)


    /**
     * all not_allowed violations
     */
    val violations_not_allowed: LazyView[IViolation] = {

        val sourceElements = sourcesByConstraint(not_allowed)

        val targetElements = targetsByConstraint(not_allowed)

        val dependencyRelation = dependenciesByConstraintKind(not_allowed)

        val violatingDependencies = (
                (
                        dependencyRelation,
                        (_: (Dependency[AnyRef, AnyRef], NormalizedConstraint))._1.source
                        ) ⋉(
                        (_: (IEnsemble, SourceElement[AnyRef]))._2.element,
                        sourceElements)
                ) ∩ (
                (
                        dependencyRelation,
                        (_: (Dependency[AnyRef, AnyRef], NormalizedConstraint))._1.target
                        ) ⋉(
                        (_: (IEnsemble, SourceElement[AnyRef]))._2.element,
                        targetElements
                        )
                )

        val violations = dependenciesAsViolations(violatingDependencies)

        violations
    }

    /**
     * all local incoming violations
     */
    val violations_local_incoming: LazyView[IViolation] = {
        // all dependencies that have the same kind as the constraint
        val dependencyByKind = dependenciesByConstraintKind(local_incoming)

        // all dependencies that have a target to an ensemble with a local incoming constraint
        val dependenciesWithTargetToIncoming = (
                (
                        global_ensemble_elements,
                        identity(_: (IEnsemble, SourceElement[AnyRef]))
                        ) ⋈(
                        (dep: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (dep._2.target, SourceElement(dep
                                    ._1.target)),
                        dependencyByKind
                        )
                ) {(elem: (IEnsemble, SourceElement[AnyRef]),
                    dep: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
            dep
        }


        // all local ensembles joined by their contexts to the incoming constraints
        val ensemblesWithConstraints = (
                (
                        local_ensembles,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        local_incoming
                        )
                ) {(e: (IEnsemble, String), c: NormalizedConstraint) => (e._1, e._2, c)}

        // filter obviously allowed combinations
        // Allowed are all (A, Incoming(_, A) and (A, Incoming(A, _)
        val filteredEnsemblesWithConstraints = σ {(e: (IEnsemble, String, NormalizedConstraint)) =>
            (e._1 != e._3.target && e._3.source != e._1)
        }(ensemblesWithConstraints)

        // all disallowed combinations taking all constraints to an ensemble into account
        // i.e. for all incoming(X,Y,ctx) && Z where X,Y,Z in Ensembles(ctx), if !exists incoming(Z,Y, ctx) => Z is disallowed to use Y
        val disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        (e: (IEnsemble, String, NormalizedConstraint)) => (e._1, e._3.target, e._2)
                        ) ⊳(
                        (c: NormalizedConstraint) => (c.source, c.target, c.context),
                        local_incoming ∪ global_incoming
                        )

                )

        // all source elements that may not use the target
        val disallowedSources = (
                (
                        disallowedEnsemblesPerConstraint,
                        (disallowed: (IEnsemble, String, NormalizedConstraint)) => disallowed._1
                        ) ⋈(
                        (elem: (IEnsemble, SourceElement[AnyRef])) => elem._1,
                        global_ensemble_elements
                        )
                ) {
            (disallowed: (IEnsemble, String, NormalizedConstraint), elem: (IEnsemble, SourceElement[AnyRef])) =>
                (disallowed._1, disallowed._3.target, disallowed._2, elem._2)
        }

        // every dependency that has a target to a local incoming constraint and is not allowed to use the target
        val violations = (
                (
                        dependenciesWithTargetToIncoming,
                        (entry: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (entry._1.source, entry._2.target, entry._2.context)
                        ) ⋈(
                        (entry: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) => (entry._4.element, entry
                                ._2, entry._3),
                        disallowedSources
                        )

                ) {
            (v: (Dependency[AnyRef, AnyRef], NormalizedConstraint),
             e: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
                new Violation(
                    v._2.origin,
                    e._1,
                    e._2,
                    e._4,
                    SourceElement(v._1.target),
                    ""
                ).asInstanceOf[IViolation]
        }

        violations

    }

    val violations_global_incoming: LazyView[IViolation] = {
        // all dependencies that have the same kind as the constraint
        val dependencyByKind = dependenciesByConstraintKind(global_incoming)

        // all dependencies that have a target to an ensemble with a local incoming constraint
        val dependenciesWithTargetToIncoming = (
                (
                        global_ensemble_elements,
                        identity(_: (IEnsemble, SourceElement[AnyRef]))
                        ) ⋈(
                        (dep: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (dep._2.target, SourceElement(dep
                                    ._1.target)),
                        dependencyByKind
                        )
                ) {(elem: (IEnsemble, SourceElement[AnyRef]),
                    dep: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
            dep
        }

        // treat all global ensembles as if they were present in the context by joining all contexts to the global incoming constraints

        val ensemblesWithConstraints = (
                (
                        global_ensembles × contexts,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        global_incoming
                        )
                ) {(e: (IEnsemble, String), c: NormalizedConstraint) => (e._1, e._2, c)}


        // filter obviously allowed combinations
        // Allowed are all (A, GlobalIncoming(_, A) and (A, GlobalIncoming(A, _)
        val filteredEnsemblesWithConstraints = σ {(e: (IEnsemble, String, NormalizedConstraint)) =>
            (e._1 != e._3.target && e._3.source != e._1)
        }(ensemblesWithConstraints)

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Z,Y) where Z,Y in Ensembles and GlobalIncoming(_,Y, ctx) ;
         * if !exists (Z,Y) with GlobalIncoming(Z,Y, ctx) then Z may not use Y
         */
        val disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        (e: (IEnsemble, String, NormalizedConstraint)) => (e._1, e._3.target, e._2)
                        ) ⊳(
                        (c: NormalizedConstraint) => (c.source, c.target, c.context),
                        local_incoming ∪ global_incoming
                        )
                )

        // all source elements that may not use the target
        val disallowedSources = (
                (
                        disallowedEnsemblesPerConstraint,
                        (disallowed: (IEnsemble, String, NormalizedConstraint)) => disallowed._1
                        ) ⋈(
                        (elem: (IEnsemble, SourceElement[AnyRef])) => elem._1,
                        global_ensemble_elements
                        )
                ) {
            (disallowed: (IEnsemble, String, NormalizedConstraint), elem: (IEnsemble, SourceElement[AnyRef])) =>
                (disallowed._1, disallowed._3.target, disallowed._2, elem._2)
        }

        // every dependency that has a target to a local incoming constraint and is not allowed to use the target
        val violations = (
                (
                        dependenciesWithTargetToIncoming,
                        (entry: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (entry._1.source, entry._2.target, entry._2.context)
                        ) ⋈(
                        (entry: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) => (entry._4.element, entry
                                ._2, entry._3),
                        disallowedSources
                        )

                ) {
            (v: (Dependency[AnyRef, AnyRef], NormalizedConstraint),
             e: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
                new Violation(
                    v._2.origin,
                    e._1,
                    e._2,
                    e._4,
                    SourceElement(v._1.target),
                    ""
                ).asInstanceOf[IViolation]
        }

        violations
    }

    val violations_local_outgoing: LazyView[IViolation] = {
        val local_outgoing = σ {
            (_: NormalizedConstraint).constraintType == ConstraintType.Outgoing
        }(normalized_constraints)

        // all dependencies that have the same kind as the constraint
        val dependencyByKind = dependenciesByConstraintKind(local_outgoing)

        // all dependencies that have a source from an ensemble with a local outgoing constraint
        val dependenciesWithSourceFromOutgoing = (
                (
                        global_ensemble_elements,
                        identity(_: (IEnsemble, SourceElement[AnyRef]))
                        ) ⋈(
                        (dep: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (dep._2.source, SourceElement(dep._1.source)),
                        dependencyByKind
                        )
                ) {(elem: (IEnsemble, SourceElement[AnyRef]),
                    dep: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
            dep
        }


        // all local ensembles joined by their contexts to the outgoing constraints
        val ensemblesWithConstraints = (
                (
                        local_ensembles,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        local_outgoing
                        )
                ) {(e: (IEnsemble, String), c: NormalizedConstraint) => (e._1, e._2, c)}

        // filter obviously allowed combinations
        // Allowed are all (A, Outgoing(A, _) and (A, Outgoing(_, A)
        val filteredEnsemblesWithConstraints = σ {(e: (IEnsemble, String, NormalizedConstraint)) =>
            (e._1 != e._3.source && e._1 != e._3.target)
        }(ensemblesWithConstraints)

        // all disallowed combinations taking all constraints to an ensemble into account
        // i.e. for all outgoing(X,Y,ctx) && Z where X,Y,Z in Ensembles(ctx), if !exists outgoing(X,Z, ctx) => X is disallowed to use Z
        val disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        (e: (IEnsemble, String, NormalizedConstraint)) => (e._3.source, e._1, e._2)
                        ) ⊳(
                        (c: NormalizedConstraint) => (c.source, c.target, c.context),
                        local_outgoing
                        )

                )

        disallowedEnsemblesPerConstraint.addObserver(new Observer[(IEnsemble, String, NormalizedConstraint)] {
            def updated(oldV: (IEnsemble, String, NormalizedConstraint),
                        newV: (IEnsemble, String, NormalizedConstraint)) {

            }

            def removed(v: (IEnsemble, String, NormalizedConstraint)) {
                println("-" + v)
            }

            def added(v: (IEnsemble, String, NormalizedConstraint)) {
                println("+" + v)
            }
        })

        // all source elements that may not use be used by the source
        val disallowedTargets = (
                (
                        disallowedEnsemblesPerConstraint,
                        (disallowed: (IEnsemble, String, NormalizedConstraint)) => disallowed._1
                        ) ⋈(
                        (elem: (IEnsemble, SourceElement[AnyRef])) => elem._1,
                        global_ensemble_elements
                        )
                ) {
            (disallowed: (IEnsemble, String, NormalizedConstraint), elem: (IEnsemble, SourceElement[AnyRef])) =>
                (disallowed._1, disallowed._2, elem._2)
        }

        disallowedTargets.addObserver(new Observer[(IEnsemble, String, SourceElement[AnyRef])] {
            def updated(oldV: (IEnsemble, String, SourceElement[AnyRef]),
                        newV: (IEnsemble, String, SourceElement[AnyRef])) {

            }

            def removed(v: (IEnsemble, String, SourceElement[AnyRef])) {
                println("-" + v)
            }

            def added(v: (IEnsemble, String, SourceElement[AnyRef])) {
                println("+" + v)
            }
        })

        // every dependency that has a target to a local outgoing constraint and is not allowed to use it
        val violations = (
                (
                        dependenciesWithSourceFromOutgoing,
                        (entry: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (entry._2.context, entry._1.target)
                        ) ⋈(
                        (entry: (IEnsemble, String, SourceElement[AnyRef])) => (entry._2, entry._3.element),
                        disallowedTargets
                        )

                ) {
            (v: (Dependency[AnyRef, AnyRef], NormalizedConstraint), e: (IEnsemble, String, SourceElement[AnyRef])) =>
                new Violation(
                    v._2.origin,
                    v._2.source,
                    e._1,
                    SourceElement(v._1.source),
                    e._3,
                    ""
                ).asInstanceOf[IViolation]
        }

        violations
    }

    val violations_global_outgoing: LazyView[IViolation] = {
        new DefaultLazyView[IViolation]
    }

    val violations_expected: LazyView[IViolation] = {
        new DefaultLazyView[IViolation]
    }


    val violations: LazyView[IViolation] =
        violations_not_allowed ∪
                violations_local_incoming ∪
                violations_global_incoming ∪
                violations_local_outgoing ∪
                violations_global_outgoing ∪
                violations_expected

    /**
     * all source elements that match the source ensembles in a given view of constraints
     */
    def sourcesByConstraint(constraints: LazyView[NormalizedConstraint]) = (
            (
                    constraints,
                    (_: NormalizedConstraint).source
                    ) ⋈(
                    (_: (IEnsemble, SourceElement[AnyRef]))._1,
                    global_ensemble_elements
                    )
            ) {(con: NormalizedConstraint, e: (IEnsemble, SourceElement[AnyRef])) => e}

    /**
     * all source elements that match the target ensembles in a given view of constraints
     */
    def targetsByConstraint(constraints: LazyView[NormalizedConstraint]) = (
            (
                    constraints,
                    (_: NormalizedConstraint).target
                    ) ⋈(
                    (_: (IEnsemble, SourceElement[AnyRef]))._1,
                    global_ensemble_elements
                    )
            ) {(con: NormalizedConstraint, e: (IEnsemble, SourceElement[AnyRef])) => e}

    /**
     * all dependencies that conform to the given constraints' kinds
     */
    def dependenciesByConstraintKind(constraints: LazyView[NormalizedConstraint]): LazyView[(Dependency[AnyRef, AnyRef], NormalizedConstraint)] = (
            (
                    kind_and_dependency,
                    (_: (DependencyKind, Dependency[AnyRef, AnyRef]))._1
                    ) ⋈(
                    (_: NormalizedConstraint).kind,
                    constraints
                    )
            ) {
        (d: (DependencyKind, Dependency[AnyRef, AnyRef]),
         c: NormalizedConstraint) =>
            (d._2, c)
    }

    /**
     * convert dependencies to Violation objects
     */
    def dependenciesAsViolations(violatingDependencies: LazyView[(Dependency[AnyRef, AnyRef], NormalizedConstraint)]) = Π[(Dependency[AnyRef, AnyRef], NormalizedConstraint), IViolation] {
        t: (Dependency[AnyRef, AnyRef], NormalizedConstraint) =>
            new Violation(
                t._2.origin,
                t._2.source,
                t._2.target,
                SourceElement(t._1.source),
                SourceElement(t._1.target),
                ""
            )
    }(violatingDependencies)

    def addModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            local_ensembles.element_added((ensemble, model.getName))
        }
        for (constraint <- model.getConstraints) {
            local_constraints.element_added((constraint, model.getName))
        }
    }

    def removeModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            local_ensembles.element_removed((ensemble, model.getName))
        }
        for (constraint <- model.getConstraints) {
            local_constraints.element_removed((constraint, model.getName))
        }
    }

    // TODO should be part of the model?
    def addGlobalModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            global_ensembles.element_added(ensemble)
        }
    }

    def removeGlobalModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            global_ensembles.element_removed(ensemble)
        }
    }

}
