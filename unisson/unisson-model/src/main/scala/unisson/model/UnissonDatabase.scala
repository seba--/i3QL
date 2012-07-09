package unisson.model

import kinds.{DependencyKind, KindParser}
import kinds.primitive._
import unisson.query.code_model.SourceElement
import sae.bytecode.Database
import sae.collections.Table
import sae.LazyView
import de.tud.cs.st.vespucci.model.{IConstraint, IEnsemble}
import de.tud.cs.st.vespucci.interfaces.ICodeElement
import sae.bytecode.model.dependencies._
import de.tud.cs.st.bat.ArrayType
import sae.bytecode.model.dependencies.parameter
import sae.bytecode.model.dependencies.return_type
import sae.bytecode.model.dependencies.read_field
import sae.bytecode.model.dependencies.write_field
import sae.bytecode.model.dependencies.field_type


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
     * Queries of ensembles are compiled from a string that is a value in the database.
     * Hence they are wrapped in their own view implementation
     */
    lazy val ensemble_elements: LazyView[(IEnsemble, ICodeElement)] = new CompiledEnsembleElementsView(bc, ensembles)


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
    lazy val source_code_dependencies = dependencyView_to_tupleView(bc.`extends`, ExtendsKind) ∪
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


    /**
     * A list of ensemble dependencies that are not allowed
     */
    def notAllowedEnsembleDependencies = null

    /**
     * A list of ensemble dependencies that are expected
     */
    def expectedEnsembleDependencies = null

    /**
     * A list of violating ensembles dependencies
     */
    def consistencyViolations = null

    /**
     * A list of violations with full information on source code dependencies and violating constraint
     */
    def violations = null

    /**
     * A list of violations summing up individual source code dependencies
     */
    def violation_summary = null

    /*
        private def normalized_constraint[T](v: (IConstraint, String)) : List[IConstraint] = {
            val kinds = KindResolver(kindParser.parse(v._1.getDependencyKind).get)
            val typ = ConstraintType(v._1)
            for (kind <- kinds) {
                if (typ != ConstraintType.IncomingAndOutgoing) {
                    f(NormalizedConstraint(
                        v._1,
                        kind,
                        typ,
                        source,
                        target,
                        v._2
                    ))
                }
                else {
                    f(NormalizedConstraint(
                        v._1,
                        kind,
                        ConstraintType.Incoming,
                        source,
                        target,
                        v._2
                    ))
                    f(NormalizedConstraint(
                        v._1,
                        kind,
                        ConstraintType.Outgoing,
                        source,
                        target,
                        v._2
                    ))
                }
            }
        }



        val normalized_constraints = new LazyView[NormalizedConstraint] {

            private val kindParser = new KindParser()

            initialized = true


            def lazyInitialize() {
                // should never be used
                throw new UnsupportedOperationException("lazyInitialize not supported for normalized_constraints")
            }

            import EnsembleNormalizer.allLeaves

            private def apply[T](v: (IConstraint, String))(f: (NormalizedConstraint) => T) {
                // TODO error handling for kinds
                val kinds = KindResolver(kindParser.parse(v._1.getDependencyKind).get)
                val typ = ConstraintType(v._1)
                for (kind <- kinds; source <- allLeaves(v._1.getSource); target <- allLeaves(v._1.getTarget)) {
                    if (typ != ConstraintType.IncomingAndOutgoing) {
                        f(NormalizedConstraint(
                            v._1,
                            kind,
                            typ,
                            source,
                            target,
                            v._2
                        ))
                    }
                    else {
                        f(NormalizedConstraint(
                            v._1,
                            kind,
                            ConstraintType.Incoming,
                            source,
                            target,
                            v._2
                        ))
                        f(NormalizedConstraint(
                            v._1,
                            kind,
                            ConstraintType.Outgoing,
                            source,
                            target,
                            v._2
                        ))
                    }
                }
            }

            override def lazy_foreach[T](f: (NormalizedConstraint) => T) {
                top_level_local_constraints.foreach(
                    apply(_)(f)
                )
            }

            val observer = new Observer[(IConstraint, String)] {
                def updated(oldV: (IConstraint, String), newV: (IConstraint, String)) {
                    removed(oldV)
                    added(newV)
                }

                def removed(v: (IConstraint, String)) {
                    apply(v)(element_removed)
                }

                def added(v: (IConstraint, String)) {
                    apply(v)(element_added)
                }

            }

            top_level_local_constraints.addObserver(observer)
        }


        def kind_and_dependency: LazyView[(DependencyKind, Dependency[AnyRef, AnyRef])] =
            kind_and_dependency_view

        // tuples of dependencies with their kinds for later joining.
        // dependencies are filtered and will not contain base types
        // array types are projected to their component types so the dependencies are counted as being to the actual component types
        protected val kind_and_dependency_view: LazyView[(DependencyKind, Dependency[AnyRef, AnyRef])] = {
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
                        (ThrowsKind, (_: Dependency[AnyRef, AnyRef]))
                    }(bc.thrown_exceptions
                            .asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]) ∪
                    Π {
                        (ParameterKind, (_: Dependency[AnyRef, AnyRef]))
                    }(
                        σ(
                            (v: parameter) => !(v.target.isBaseType || v.target.isVoidType)
                        )(
                            Π[parameter, parameter] {
                                case (parameter(m, ArrayType(component))) => parameter(m, component);
                                case x => x
                            }(bc.parameter)
                        ).asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
                    ) ∪
                    Π {
                        (ReturnTypeKind, (_: Dependency[AnyRef, AnyRef]))
                    }(
                        σ(
                            (v: return_type) => !(v.target.isBaseType || v.target.isVoidType)
                        )(
                            Π[return_type, return_type] {
                                case (return_type(m, ArrayType(component))) => return_type(m, component);
                                case x => x
                            }(bc.return_type)
                        ).asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
                    ) ∪
                    Π {
                        (FieldTypeKind, (_: Dependency[AnyRef, AnyRef]))
                    }(
                        σ(
                            (v: field_type) => !(v.target.isBaseType || v.target.isVoidType)
                        )(
                            Π[field_type, field_type] {
                                case (field_type(m, ArrayType(component))) => field_type(m, component);
                                case x => x
                            }(bc.field_type)
                        ).asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
                    ) ∪
                    Π {
                        (ReadFieldKind, (_: Dependency[AnyRef, AnyRef]))
                    }(
                        σ(
                            (v: read_field) => !(v.target.fieldType.isBaseType)
                        )(
                            // TODO what about arrays with component types of not allowed elements?
                            bc.read_field
                        ).asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
                    ) ∪
                    Π {
                        (WriteFieldKind, (_: Dependency[AnyRef, AnyRef]))
                    }(
                        σ(
                            // TODO what about arrays with component types of not allowed elements?
                            (v: write_field) => !v.target.fieldType.isBaseType
                        )(bc.write_field).asInstanceOf[LazyView[Dependency[AnyRef, AnyRef]]]
                    )
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


        lazy val ensemble_dependencies: MaterializedView[((IEnsemble, IEnsemble, DependencyKind), Dependency[AnyRef, AnyRef])] = {
            val indexedElements = Conversions.lazyViewToIndexedView(leaf_ensemble_elements)
            val elementIndex = (t: (IEnsemble, SourceElement[AnyRef])) => t._2.element
            val sourceDependencies = (
                    (
                            indexedElements,
                            elementIndex
                            ) ⋈(
                            (t: (DependencyKind, Dependency[AnyRef, AnyRef])) => t._2.source,
                            kind_and_dependency
                            )
                    ) {
                (source: (IEnsemble, SourceElement[AnyRef]), dep: (DependencyKind, Dependency[AnyRef, AnyRef])) =>
                    (source._1, dep)
            }
            val targetDependencies = (
                    (
                            indexedElements,
                            elementIndex
                            ) ⋈(
                            (t: (IEnsemble, (DependencyKind, Dependency[AnyRef, AnyRef]))) => t._2._2.target,
                            sourceDependencies
                            )
                    ) {
                (target: (IEnsemble, SourceElement[AnyRef]),
                 source: (IEnsemble, (DependencyKind, Dependency[AnyRef, AnyRef]))) =>
                    ((source._1, target._1, source._2._1), source._2._2)
            }
            targetDependencies
        }

        lazy val source_target_violations_by_not_allowed: LazyView[((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)] = {
            Π((c: NormalizedConstraint) => ((c.source, c.target, c.kind), c))(not_allowed)
        }

        lazy val source_target_violations_by_local_incoming: LazyView[((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)] = {

            // all local ensembles joined by their contexts to the incoming constraints
            val source_target_ensemble_combinations_with_selfref = (
                    (
                            leaf_local_ensembles,
                            (_: (IEnsemble, String))._2
                            ) ⋈(
                            (_: NormalizedConstraint).context,
                            local_incoming
                            )
                    ) {
                (e: (IEnsemble, String), c: NormalizedConstraint) => ((e._1, c.target, c.kind), c)
            }

            // filter obviously allowed combinations
            // Allowed are all (A, Incoming(_, A) and (A, Incoming(A, _)
            val source_target_ensemble_combinations = δ(σ {
                (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                    (e._1._1 != e._1._2)
            }(source_target_ensemble_combinations_with_selfref))

            /**
             * all disallowed combinations taking all constraints to an ensemble into account
             * for all (Z,Y) where Z,Y in Ensembles and Incoming(_,Y, ctx) ;
             * if !exists (Z,Y) with Incoming(Z,Y, ctx) or GlobalIncoming(Z,Y, ctx) then Z may not use Y
             */
            val disallowedEnsemblesWithSameContext = (
                    (
                            source_target_ensemble_combinations,
                            (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                                (e._1, e._2.context)
                            ) ⊳(
                            (c: NormalizedConstraint) => ((c.source, c.target, c.kind), c.context),
                            local_incoming ∪ global_incoming
                            )

                    )
            disallowedEnsemblesWithSameContext
        }

        lazy val source_target_violations_by_global_incoming: LazyView[((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)] = {
            // treat all global ensembles as if they were present in the context by joining all contexts to the global incoming constraints
            val source_target_ensemble_combinations_with_selfref = (
                    (
                            leaf_ensembles × contexts,
                            (_: (IEnsemble, String))._2
                            ) ⋈(
                            (_: NormalizedConstraint).context,
                            global_incoming
                            )
                    ) {
                (e: (IEnsemble, String), c: NormalizedConstraint) => ((e._1, c.target, c.kind), c)
            }


            // filter obviously allowed combinations
            // Allowed are all (A, GlobalIncoming(_, A) and (A, GlobalIncoming(A, _)
            val source_target_ensemble_combinations = δ(σ {
                (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                    (e._1._1 != e._1._2)
            }(source_target_ensemble_combinations_with_selfref))

            /**
             * all disallowed combinations taking all constraints to an ensemble into account
             * for all (Z,Y) where Z,Y in Ensembles and GlobalIncoming(_,Y, ctx) ;
             * if !exists (Z,Y) with GlobalIncoming(Z,Y, ctx) or LocalIncoming(Z,Y, ctx) then Z may not use Y
             */
            val disallowedEnsemblesWithSameContext = (
                    (
                            source_target_ensemble_combinations,
                            (e: ((IEnsemble, IEnsemble, DependencyKind), NormalizedConstraint)) =>
                                (e._1, e._2.context)
                            ) ⊳(
                            (c: NormalizedConstraint) => ((c.source, c.target, c.kind), c.context),
                            local_incoming ∪ global_incoming
                            )
                    )
            disallowedEnsemblesWithSameContext
        }


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

        def addConcern(model: IArchitectureModel) {
            import scala.collection.JavaConversions._

            for (ensemble <- model.getEnsembles) {
                top_level_local_ensembles +=(ensemble, model.getName)
            }
            for (constraint <- model.getConstraints) {
                top_level_local_constraints +=(constraint, model.getName)
            }
        }

        def removeConcern(model: IArchitectureModel) {
            import scala.collection.JavaConversions._

            for (ensemble <- model.getEnsembles) {
                top_level_local_ensembles -=(ensemble, model.getName)
            }
            for (constraint <- model.getConstraints) {
                top_level_local_constraints -=(constraint, model.getName)
            }
        }

        def updateConcern(oldModel: IArchitectureModel, newModel: IArchitectureModel) {
            import scala.collection.JavaConversions._
            // remove old Ensembles
            for (ensemble <- oldModel.getEnsembles.filterNot(
                (e: IEnsemble) => newModel.getEnsembles.exists(_.getName == e.getName)
            )
            ) {
                top_level_local_ensembles -=(ensemble, oldModel.getName)
            }
            // remove old constraints
            for (constraint <- oldModel.getConstraints.filterNot(
                (c: IConstraint) => newModel.getConstraints.exists(_ == c)
            )
            ) {
                top_level_local_constraints -=(constraint, oldModel.getName)
            }


            // update existing Ensembles
            for (oldE <- oldModel.getEnsembles;
                 newE <- newModel.getEnsembles)
                if (oldE.getName == newE.getName
                ) {
                    top_level_local_ensembles.update((oldE, oldModel.getName), (newE, newModel.getName))
                }
            // we currently do not update any constraints.
            // This would make sense only for constraint changes w.r.t. kinds,
            // but then removing the old constraint is probably as effective.

            // add new Ensembles
            for (ensemble <- newModel.getEnsembles.filterNot(
                (e: IEnsemble) => oldModel.getEnsembles.exists(_.getName == e.getName)
            )
            ) {
                top_level_local_ensembles +=(ensemble, newModel.getName)
            }

            // add new Constraints
            for (constraint <- newModel.getConstraints.filterNot(
                (c: IConstraint) => oldModel.getConstraints.exists(_ == c)
            )
            ) {
                top_level_local_constraints +=(constraint, newModel.getName)
            }
        }

        def setRepository(model: IArchitectureModel) {
            import scala.collection.JavaConversions._

            for (ensemble <- model.getEnsembles) {
                top_level_ensembles += ensemble
            }
        }

        def unsetRepository(model: IArchitectureModel) {
            import scala.collection.JavaConversions._

            for (ensemble <- model.getEnsembles) {
                top_level_ensembles -= ensemble
            }
        }

        def updateRepository(oldModel: IArchitectureModel, newModel: IArchitectureModel) {
            import scala.collection.JavaConversions._

            // remove old Ensembles
            for (ensemble <- oldModel.getEnsembles.filterNot(
                (e: IEnsemble) => newModel.getEnsembles.exists(_.getName == e.getName)
            )
            ) {
                top_level_ensembles -= ensemble
            }
            // add new Ensembles
            for (ensemble <- newModel.getEnsembles.filterNot(
                (e: IEnsemble) => oldModel.getEnsembles.exists(_.getName == e.getName)
            )
            ) {
                top_level_ensembles += ensemble
            }
            // update existing Ensembles
            for (oldE <- oldModel.getEnsembles;
                 newE <- newModel.getEnsembles)
                if (oldE.getName == newE.getName
                ) {
                    top_level_ensembles.update(oldE, newE)
                }
        }
    */

}


/*
object EnsembleNormalizer
{

    def allLeaves(e: IEnsemble): List[IEnsemble] = {
        import scala.collection.JavaConversions._
        if (e.getInnerEnsembles.isEmpty)
            return List(e)
        e.getInnerEnsembles.map(allLeaves(_)).fold[List[IEnsemble]](Nil)(_ ::: _)
    }
}
*/

/*
val leaf_ensembles = new LazyView[IEnsemble] {

initialized = true

def lazy_foreach[T](f: (IEnsemble) => T) {
for (g <- top_level_ensembles;
leaf <- EnsembleNormalizer.allLeaves(g)) {
f(leaf)
}
}

def lazyInitialize() {
// should never be used
throw new UnsupportedOperationException("lazyInitialize not supported for normalized_global_ensembles")
}


val ensembleObserver = new Observer[IEnsemble]
{

import EnsembleNormalizer.allLeaves

def updated(oldV: IEnsemble, newV: IEnsemble) {
val oldLeaves = allLeaves(oldV)
val newLeaves = allLeaves(newV)
// remove old Ensembles
for (ensemble <- oldLeaves.filterNot(
(e: IEnsemble) => newLeaves.exists(_.getName == e.getName)
)
) {
element_removed(ensemble)
}
// add new Ensembles
for (ensemble <- newLeaves.filterNot(
(e: IEnsemble) => oldLeaves.exists(_.getName == e.getName)
)
) {
element_added(ensemble)
}
// update existing Ensembles
for (oldE <- oldLeaves;
newE <- newLeaves)
if (oldE.getName == newE.getName
) {
element_updated(oldE, newE)
}
}

def removed(v: IEnsemble) {
for (leaf <- allLeaves(v)) {
element_removed(leaf)
}
}

def added(v: IEnsemble) {
for (leaf <- allLeaves(v)) {
element_added(leaf)
}
}
}

top_level_ensembles.addObserver(ensembleObserver)
}
*/