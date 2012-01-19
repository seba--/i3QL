package unisson.model

import constraints.{NormalizedConstraintWithType, ConstraintType, ProxyNormalizedConstraint, NormalizedConstraint}
import de.tud.cs.st.vespucci.model.{IArchitectureModel, IConstraint, IEnsemble}
import debug.PrintingObserver
import unisson.model.kinds.{KindParser, KindResolver, DependencyKind}
import unisson.query.code_model.SourceElement
import sae.{Observer, DefaultLazyView, MaterializedView, LazyView}
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.Dependency
import unisson.model.kinds.primitive._
import sae.bytecode.Database
import de.tud.cs.st.vespucci.interfaces.IViolation
import unisson.query.compiler.CachingQueryCompiler
import unisson.query.parser.QueryParser
import sae.collections.Table


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
class UnissonDatabase(bc: Database)
{

    val global_ensembles = new Table[IEnsemble]()

    val local_ensembles = new Table[(IEnsemble, String)]()

    val contexts: MaterializedView[String] = δ(Π {
        (_: (IEnsemble, String))._2
    }(local_ensembles))

    /**
     * constraints are always local, there is no global list of constraints
     */
    val local_constraints = new Table[(IConstraint, String)]()

    /**
     * Queries of ensembles are compiled from a string that is a value in the database.
     * Hence they are wrapped in their own view implementation
     */
    val global_ensemble_elements: LazyView[(IEnsemble, SourceElement[AnyRef])] = new LazyView[(IEnsemble, SourceElement[AnyRef])] {

        private val queryCompiler = new CachingQueryCompiler(bc)

        private val queryParser = new QueryParser()

        def lazy_foreach[T](f: ((IEnsemble, SourceElement[AnyRef])) => T) {
            for (e <- global_ensembles) {
                queryCompiler.parseAndCompile(e.getQuery).foreach[Unit](
                    f(e, _)
                )
            }
        }

        def lazyInitialize {
            // should never be used
            throw new UnsupportedOperationException("lazyInitialize not supported for global_ensemble_elements")
            /*
            global_ensembles.foreach((e: IEnsemble) => {
                ensembleObserver.added(e)
            })
            */
        }

        var elementObservers: Map[IEnsemble, ElementObserver] = Map.empty

        val ensembleObserver = new Observer[IEnsemble]
        {
            def updated(oldV: IEnsemble, newV: IEnsemble) {
                val oldQuery = queryParser.parse(oldV.getQuery).get
                val newQuery = queryParser.parse(newV.getQuery).get
                if (oldQuery.isSyntacticEqual(newQuery))
                    return

                // remove old elements
                val oldCompiledQuery = queryCompiler.compile(oldQuery)
                oldCompiledQuery.lazy_foreach(
                    (e: SourceElement[AnyRef]) => element_removed((oldV, e))
                )
                for( oldObserver <- elementObservers.get(oldV) )
                {
                    oldCompiledQuery.removeObserver(oldObserver)
                }
                // dispose of obsolete views and observers
                val oldObserver= elementObservers(oldV)
                oldCompiledQuery.removeObserver(oldObserver)
                elementObservers -= oldV

                queryCompiler.dispose(oldQuery)

                // add new elements
                val newCompiledQuery = queryCompiler.compile(newQuery)
                newCompiledQuery.lazy_foreach(
                    (e: SourceElement[AnyRef]) => element_added((newV, e))
                )
                val newObserver= new ElementObserver(newV)
                newCompiledQuery.addObserver(newObserver)
                elementObservers += {
                    newV -> newObserver
                }
            }

            def removed(v: IEnsemble) {
                val oldQuery = queryParser.parse(v.getQuery).get
                val compiledQuery = queryCompiler.compile(oldQuery)
                compiledQuery.lazy_foreach(
                    (e: SourceElement[AnyRef]) => element_removed((v, e))
                )
                compiledQuery.removeObserver(elementObservers(v))
                elementObservers -= v
                // dispose of obsolete views
                queryCompiler.dispose(oldQuery)
            }

            def added(v: IEnsemble) {
                val compiledQuery = queryCompiler.parseAndCompile(v.getQuery)
                compiledQuery.lazy_foreach(
                    (e: SourceElement[AnyRef]) => element_added((v, e))
                )
                val oo = new ElementObserver(v)

                compiledQuery.addObserver(oo)
                elementObservers += {
                    v -> oo
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
    // TODO normalize constraints to sub-ensembles
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
                val kinds = KindResolver(kindParser.parse(v._1.getDependencyKind).get)
                if (ConstraintType(v._1) != ConstraintType.IncomingAndOutgoing) {
                    for (kind <- kinds) {
                        element_removed(ProxyNormalizedConstraint(v._1, kind, v._2))
                    }
                }
                else {
                    for (kind <- kinds) {
                        element_removed(NormalizedConstraintWithType(v._1, ConstraintType.Incoming, kind, v._2))
                        element_removed(NormalizedConstraintWithType(v._1, ConstraintType.Outgoing, kind, v._2))
                    }
                }
            }

            def added(v: (IConstraint, String)) {
                // TODO error handling for kinds
                val kinds = KindResolver(kindParser.parse(v._1.getDependencyKind).get)
                if (ConstraintType(v._1) != ConstraintType.IncomingAndOutgoing) {
                    for (kind <- kinds) {
                        element_added(ProxyNormalizedConstraint(v._1, kind, v._2))
                    }
                }
                else {
                    for (kind <- kinds) {
                        element_added(NormalizedConstraintWithType(v._1, ConstraintType.Incoming, kind, v._2))
                        element_added(NormalizedConstraintWithType(v._1, ConstraintType.Outgoing, kind, v._2))
                    }
                }
            }

        }

        local_constraints.addObserver(observer)
    }

    //normalized_constraints.addObserver(new PrintingObserver[NormalizedConstraint]())

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

    //kind_and_dependency.addObserver(new PrintingObserver[(DependencyKind, Dependency[AnyRef, AnyRef])]())

    val local_incoming = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.Incoming
    }(normalized_constraints)

    val global_incoming = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.GlobalIncoming
    }(normalized_constraints)

    val local_outgoing = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.Outgoing
    }(normalized_constraints)

    val global_outgoing = σ {
        (_: NormalizedConstraint).constraintType == ConstraintType.GlobalOutgoing
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

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Z,Y) where Z,Y in Ensembles and Incoming(_,Y, ctx) ;
         * if !exists (Z,Y) with Incoming(Z,Y, ctx) or GlobalIncoming(Z,Y, ctx) then Z may not use Y
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
                        (entry: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
                            (entry._4.element, entry
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
         * if !exists (Z,Y) with GlobalIncoming(Z,Y, ctx) or LocalIncoming(Z,Y, ctx) then Z may not use Y
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
                        (entry: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
                            (entry._4.element, entry
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

        // all source target combinations that have to do with an ensemble where a constraint is declared
        val source_target_combinations = (
                (
                        local_ensembles,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        local_outgoing
                        )
                ) {(e: (IEnsemble, String), c: NormalizedConstraint) => (c.source, e._1, e._2)}

        // filter obviously allowed combinations
        // Allowed are all (A, Outgoing(_, A) and (A, Outgoing(A, _)
        val filteredEnsemblesWithConstraints = σ {(e: (IEnsemble, IEnsemble, String)) =>
            (e._1 != e._2)
        }(source_target_combinations)

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Y, Z) where Z,Y in Ensembles and Outgoing(Y,_, ctx) ;
         * if !exists (Y, Z) with Outgoing(Y,Z, ctx) or GlobalOutgoing(Y,Z, ctx) then Y may not use Z
         */
        val disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        identity(_: (IEnsemble, IEnsemble, String))
                        ) ⊳(
                        (c: NormalizedConstraint) => (c.source, c.target, c.context),
                        local_outgoing ∪ global_outgoing
                        )

                )

        // all target elements that may not be used by the source
        val disallowedTargets = (
                (
                        disallowedEnsemblesPerConstraint,
                        (disallowed: (IEnsemble, IEnsemble, String)) => disallowed._2
                        ) ⋈(
                        (elem: (IEnsemble, SourceElement[AnyRef])) => elem._1,
                        global_ensemble_elements
                        )
                ) {
            (disallowed: (IEnsemble, IEnsemble, String), elem: (IEnsemble, SourceElement[AnyRef])) =>
                (disallowed._1, disallowed._2, disallowed._3, elem._2)
        }

        // every dependency from the source that uses a disallowed target
        val violations = (
                (
                        dependenciesWithSourceFromOutgoing,
                        (entry: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (entry._2.source, entry._1.target, entry._2.context)
                        ) ⋈(
                        (entry: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
                            (entry._1, entry._4.element, entry._3),
                        disallowedTargets
                        )

                ) {(v: (Dependency[AnyRef, AnyRef], NormalizedConstraint),
                    e: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
            new Violation(
                v._2.origin,
                e._1,
                e._2,
                SourceElement(v._1.source),
                e._4,
                ""
            ).asInstanceOf[IViolation]
        }

        violations
    }

    val violations_global_outgoing: LazyView[IViolation] = {
        // all dependencies that have the same kind as the constraint
        val dependencyByKind = dependenciesByConstraintKind(global_outgoing)

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


        // all source target combinations that have to do with an ensemble where a constraint is declared
        val source_target_combinations = (
                (
                        global_ensembles × contexts,
                        (_: (IEnsemble, String))._2
                        ) ⋈(
                        (_: NormalizedConstraint).context,
                        global_outgoing
                        )
                ) {(e: (IEnsemble, String), c: NormalizedConstraint) => (c.source, e._1, e._2)}

        // filter obviously allowed combinations
        // Allowed are all (A, Outgoing(_, A) and (A, Outgoing(A, _)
        val filteredEnsemblesWithConstraints = σ {(e: (IEnsemble, IEnsemble, String)) =>
            (e._1 != e._2)
        }(source_target_combinations)

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Y, Z) where Z,Y in Ensembles and Outgoing(Y,_, ctx) ;
         * if !exists (Y, Z) with Outgoing(Y,Z, ctx) or GlobalOutgoing(Y,Z, ctx) then Y may not use Z
         */
        val disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        identity(_: (IEnsemble, IEnsemble, String))
                        ) ⊳(
                        (c: NormalizedConstraint) => (c.source, c.target, c.context),
                        local_outgoing ∪ global_outgoing
                        )

                )

        // all target elements that may not be used by the source
        val disallowedTargets = (
                (
                        disallowedEnsemblesPerConstraint,
                        (disallowed: (IEnsemble, IEnsemble, String)) => disallowed._2
                        ) ⋈(
                        (elem: (IEnsemble, SourceElement[AnyRef])) => elem._1,
                        global_ensemble_elements
                        )
                ) {
            (disallowed: (IEnsemble, IEnsemble, String), elem: (IEnsemble, SourceElement[AnyRef])) =>
                (disallowed._1, disallowed._2, disallowed._3, elem._2)
        }

        // every dependency from the source that uses a disallowed target
        val violations = (
                (
                        dependenciesWithSourceFromOutgoing,
                        (entry: (Dependency[AnyRef, AnyRef], NormalizedConstraint)) =>
                            (entry._2.source, entry._1.target, entry._2.context)
                        ) ⋈(
                        (entry: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
                            (entry._1, entry._4.element, entry._3),
                        disallowedTargets
                        )

                ) {(v: (Dependency[AnyRef, AnyRef], NormalizedConstraint),
                    e: (IEnsemble, IEnsemble, String, SourceElement[AnyRef])) =>
            new Violation(
                v._2.origin,
                e._1,
                e._2,
                SourceElement(v._1.source),
                e._4,
                ""
            ).asInstanceOf[IViolation]
        }

        violations
    }

    val violations_expected: LazyView[IViolation] = {
        new DefaultLazyView[IViolation]
    }


    lazy val violations: LazyView[IViolation] =
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
            local_ensembles +=(ensemble, model.getName)
        }
        for (constraint <- model.getConstraints) {
            local_constraints +=(constraint, model.getName)
        }
    }

    def removeModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            local_ensembles -=(ensemble, model.getName)
        }
        for (constraint <- model.getConstraints) {
            local_constraints -=(constraint, model.getName)
        }
    }

    def updateModel(oldModel: IArchitectureModel, newModel: IArchitectureModel) {
        removeModel(oldModel)
        addModel(newModel)
    }

    // TODO should be part of the model?
    def addGlobalModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            global_ensembles += ensemble
        }
    }

    def removeGlobalModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            global_ensembles -= ensemble
        }
    }

    def updateGlobalModel(oldModel: IArchitectureModel, newModel: IArchitectureModel) {
        import scala.collection.JavaConversions.asScalaSet

        // remove old Ensembles
        for (ensemble <- oldModel.getEnsembles.filterNot(
            (e: IEnsemble) => newModel.getEnsembles.exists(_.getName == e.getName)
        )
        ) {
            global_ensembles += ensemble
        }
        // add new Ensembles
        for (ensemble <- newModel.getEnsembles.filterNot(
            (e: IEnsemble) => oldModel.getEnsembles.exists(_.getName == e.getName)
        )
        ) {
            global_ensembles -= ensemble
        }
        // update existing Ensembles
        for (oldE <- oldModel.getEnsembles;
             newE <- newModel.getEnsembles)
            if (oldE.getName == newE.getName
            ) {
                global_ensembles.update(oldE, newE)
            }
    }


}
