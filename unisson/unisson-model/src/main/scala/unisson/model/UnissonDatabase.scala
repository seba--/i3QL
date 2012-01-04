package unisson.model

import de.tud.cs.st.vespucci.model.{IArchitectureModel, IConstraint, IEnsemble}
import kinds.{KindParser, KindResolver, DependencyKind}
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

    val ensembles: MaterializedView[IEnsemble] = new DefaultLazyView[IEnsemble]

    val global_ensembles: MaterializedView[IEnsemble] = new DefaultLazyView[IEnsemble]

    val constraints: LazyView[IConstraint] = new DefaultLazyView[IConstraint]

    // TODO emulation of subquery should be removed
    // todo lazy initialization
    val ensembleElements: LazyView[(IEnsemble, SourceElement[AnyRef])] = new LazyView[(IEnsemble, SourceElement[AnyRef])]
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

        class ElementObserver(val ensemble:IEnsemble) extends Observer[SourceElement[AnyRef]]
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


        ensembles.addObserver(ensembleObserver)
    }



    // TODO emulation of subquery should be removed
    val normalizedConstraints = new DefaultLazyView[NormalizedConstraint] {

        private val kindParser = new KindParser()

        val observer = new Observer[IConstraint]
        {
            def updated(oldV: IConstraint, newV: IConstraint) {
                removed(oldV)
                added(newV)
            }

            def removed(v: IConstraint) {
                // TODO error handling for kinds
                val kinds = KindResolver(kindParser.parse(v.getDependencyKind).get)
                for (kind <- kinds) {
                    element_removed(ProxyNormalizedConstraint(v, kind))
                }
            }

            def added(v: IConstraint) {
                // TODO error handling for kinds
                val kinds = KindResolver(kindParser.parse(v.getDependencyKind).get)
                for (kind <- kinds) {
                    element_added(ProxyNormalizedConstraint(v, kind))
                }
            }
        }

        constraints.addObserver(observer)
    }

    val kindAsDependency: LazyView[(DependencyKind, Dependency[AnyRef, AnyRef])] = {
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


    val violations_not_allowed: LazyView[IViolation] = {
        val not_allowed = σ {
            (_: NormalizedConstraint).constraintType == ConstraintType.NotAllowed
        }(normalizedConstraints)
        val dependencyRelation: LazyView[(Dependency[AnyRef, AnyRef], NormalizedConstraint)] = (
                (
                        kindAsDependency,
                        (_: (DependencyKind, Dependency[AnyRef, AnyRef]))._1
                        ) ⋈(
                        (_: NormalizedConstraint).kind,
                        not_allowed
                        )
                ) {
            (d: (DependencyKind, Dependency[AnyRef, AnyRef]),
             c: NormalizedConstraint) =>
                (d._2, c)
        }

        val sourceElements = (
                (
                        not_allowed,
                        (_: NormalizedConstraint).source
                        ) ⋈(
                        (_: (IEnsemble, SourceElement[AnyRef]))._1,
                        ensembleElements
                        )
                ) {(con: NormalizedConstraint, e: (IEnsemble, SourceElement[AnyRef])) => e}

        val targetElements = (
                (
                        not_allowed,
                        (_: NormalizedConstraint).target
                        ) ⋈(
                        (_: (IEnsemble, SourceElement[AnyRef]))._1,
                        ensembleElements
                        )
                ) {(con: NormalizedConstraint, e: (IEnsemble, SourceElement[AnyRef])) => e}

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

        val violations = Π[(Dependency[AnyRef, AnyRef], NormalizedConstraint), IViolation] {
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

        violations
    }

    val violations: LazyView[IViolation] = violations_not_allowed

    def addModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            ensembles.element_added(ensemble)
        }
        for (contraint <- model.getConstraints) {
            constraints.element_added(contraint)
        }
    }

    def removeModel(model: IArchitectureModel) {
        import scala.collection.JavaConversions.collectionAsScalaIterable
        for (ensemble <- model.getEnsembles) {
            ensembles.element_removed(ensemble)
        }
        for (contraint <- model.getConstraints) {
            constraints.element_removed(contraint)
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