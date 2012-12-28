package unisson.model

import constraints.{NormalizedConstraint, ConstraintType}
import kinds.{KindResolver, DependencyKind, KindParser}
import unisson.query.code_model.SourceElementFactory
import sae.collections.Table
import sae.{Observer, Relation}
import de.tud.cs.st.vespucci.interfaces.{IConstraint, IEnsemble, ICodeElement}
import de.tud.cs.st.bat.resolved.ObjectType
import unisson.query.UnissonQuery
import unisson.query.parser.QueryParser
import unisson.query.ast.{OrQuery, DerivedQuery, EmptyQuery}
import sae.functions.Count
import sae.bytecode.BytecodeDatabase
import scala.collection.JavaConversions._
import sae.syntax.sql._
import DependencyFactory._
import sae.bytecode.structure._
import sae.deltas.{Update, Deletion, Addition}


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
class UnissonDatabase(val bc: BytecodeDatabase)
    extends IUnissonDatabase with IUnissonArchitectureModelDatabase
{

    import sae.syntax.RelationalAlgebraSyntax._


    /**
     * Utility for parsing the kinds in a constraint.
     */
    private val kindParser = new KindParser ()

    /**
     * Utility for parsing queries of an ensemble
     */
    private val queryParser = new QueryParser ()

    /**
     * Returns the parsed query or an empty query if the query could not be parsed.
     * In the later event, the error is logged in the database.
     * Queries are normalized, such that an ensemble with a derived query, has a query equal to the queries of it's children
     */
    private def getNormalizedQuery(ensemble: IEnsemble): UnissonQuery = {
        val query: UnissonQuery = queryParser.parse (ensemble.getQuery) match {
            case queryParser.Success (result, _) => result
            case queryParser.Failure (msg, next) => {
                errors += new IllegalArgumentException (msg + next.pos.longString)
                EmptyQuery ()
            }
        }
        if (query.isSyntacticEqual (DerivedQuery ())) {
            val childrenQueries = ensemble.getInnerEnsembles.map (getNormalizedQuery)
            childrenQueries.fold (EmptyQuery ())(OrQuery (_, _))
        }
        else
        {
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
     * The table of slices and their ensembles
     */
    lazy val slice_ensembles = new Table[(IEnsemble, String)]()

    /**
     * The table of slices and their constraints
     */
    lazy val slice_constraints = new Table[(IConstraint, String)]()

    /**
     * The queries for each ensemble
     */
    lazy val ensemble_queries: Relation[(IEnsemble, UnissonQuery)] =
        Π ((e: IEnsemble) => (e, getNormalizedQuery (e)))(ensembles)

    /**
     * Queries of ensembles are compiled from a string that is a value in the database.
     * Hence they are wrapped in their own view implementation
     */
    lazy val ensemble_elements = sae.relationToResult (new CompiledEnsembleElementsView (bc, ensemble_queries))


    /**
     * Add the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def addEnsemble(ensemble: IEnsemble) {
        ensembles += ensemble
        for (child <- ensemble.getInnerEnsembles) {
            children += (ensemble, child)
            addEnsemble (child)
        }
    }

    /**
     * Add the <code>ensemble</code> and it's children to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def addEnsembleToSlice(ensemble: IEnsemble)(implicit slice: String) {
        slice_ensembles += (ensemble, slice)
        for (child <- ensemble.getInnerEnsembles) {
            addEnsembleToSlice (child)
        }
    }

    /**
     * Add the <code>constraint</code> and to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def addConstraintToSlice(constraint: IConstraint)(implicit slice: String) {
        slice_constraints += (constraint, slice)
    }

    /**
     * Remove the <code>ensemble</code> and it's children to the global list of defined ensembles.
     */
    def removeEnsemble(ensemble: IEnsemble) {
        ensembles -= ensemble
        for (child <- ensemble.getInnerEnsembles) {
            children -= (ensemble, child)
            removeEnsemble (child)
        }
    }

    /**
     * Remove the <code>ensemble</code> and it's children to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def removeEnsembleFromSlice(ensemble: IEnsemble)(implicit slice: String) {
        slice_ensembles -= (ensemble, slice)
        for (child <- ensemble.getInnerEnsembles) {
            removeEnsembleFromSlice (child)
        }
    }

    /**
     * Remove the <code>constraint</code> and to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def removeConstraintFromSlice(constraint: IConstraint)(implicit slice: String) {
        slice_constraints -= (constraint, slice)
    }

    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the global list of defined ensembles.
     */
    def updateEnsemble(oldE: IEnsemble, newE: IEnsemble) {
        ensembles.update (oldE, newE)
        val oldEChildren: scala.collection.mutable.Set[IEnsemble] = oldE.getInnerEnsembles
        val newEChildren: scala.collection.mutable.Set[IEnsemble] = newE.getInnerEnsembles

        val (retainedOldChildren, notExistingChildren) = oldEChildren.partition (
            (e: IEnsemble) => newEChildren.exists ((_: IEnsemble).getName == e.getName)
        )
        val (retainedNewChildren, newChildren) = newEChildren.partition (
            (e: IEnsemble) => oldEChildren.exists ((_: IEnsemble).getName == e.getName)
        )

        // remove old children
        for (child <- notExistingChildren) {
            // remove the parent-child relation
            children -= (oldE, child)
            // transitively remove all further children
            removeEnsemble (child)
        }
        // update existing children
        for (oldChild <- retainedOldChildren;
             newChild <- retainedNewChildren.find (_.getName == oldChild.getName))
        {
            //val newChild = retainedNewChildren.find( _.getName == oldChild.getName).get
            // update the parent child relation
            children.update ((oldE, oldChild), (newE, newChild))
            // transitively update the child
            updateEnsemble (oldChild, newChild)
        }
        // add new children
        for (child <- newChildren) {
            // add the parent-child relation
            children += (newE, child)
            // transitively add all further children
            addEnsemble (child)
        }
    }

    /**
     * Updates the ensemble <code>oldE</code> with the values of <code>newE</code>.
     * The update is performed for the ensemble and all children to the local <code>slice</code>.
     * The slice can be bound to a string via <code>implicit</code>.
     * The slice can be omitted, resulting in the name: "default slice".
     */
    def updateEnsembleInSlice(oldE: IEnsemble, newE: IEnsemble)(implicit slice: String) {
        slice_ensembles.update ((oldE, slice), (newE, slice))
        val oldEChildren: scala.collection.mutable.Set[IEnsemble] = oldE.getInnerEnsembles
        val newEChildren: scala.collection.mutable.Set[IEnsemble] = newE.getInnerEnsembles

        val (retainedOldChildren, notExistingChildren) = oldEChildren.partition (
            (e: IEnsemble) => newEChildren.exists ((_: IEnsemble).getName == e.getName)
        )
        val (retainedNewChildren, newChildren) = newEChildren.partition (
            (e: IEnsemble) => oldEChildren.exists ((_: IEnsemble).getName == e.getName)
        )

        // remove old children
        for (child <- notExistingChildren) {
            // transitively remove all further children
            removeEnsembleFromSlice (child)
        }
        // update existing children
        for (oldChild <- retainedOldChildren;
             newChild <- retainedNewChildren.find (_.getName == oldChild.getName))
        {
            // transitively update the child
            updateEnsembleInSlice (oldChild, newChild)
        }
        // add new children
        for (child <- newChildren) {
            // transitively add all further children
            addEnsembleToSlice (child)
        }
    }

    /**
     * A list of dependencies between the source code elements
     */
    def source_code_dependencies = internal_source_code_dependencies


    lazy val internal_source_code_dependencies = compile (
        SELECT (extendsDependency) FROM bc.classInheritance UNION_ALL (
            SELECT (implementsDependency) FROM bc.interfaceInheritance
            ) UNION_ALL (
            SELECT (invokeInterfaceDependency) FROM bc.invokeInterface
            ) UNION_ALL (
            SELECT (invokeSpecialDependency) FROM bc.invokeSpecial
            ) UNION_ALL (
            SELECT (invokeVirtualDependency) FROM bc.invokeVirtual
            ) UNION_ALL (
            SELECT (invokeStaticDependency) FROM bc.invokeStatic
            ) UNION_ALL (
            SELECT (readFieldDependency) FROM bc.readField
            ) UNION_ALL (
            SELECT (writeFieldDependency) FROM bc.writeField
            ) UNION_ALL (
            SELECT (newObjectDependency) FROM bc.newObject
            ) UNION_ALL (
            SELECT (checkCastDependency) FROM bc.checkCast
            ) UNION_ALL (
            SELECT (exceptionDeclarationDependency) FROM bc.exceptionDeclarations
            ) UNION_ALL (
            SELECT (fieldDeclarationDependency) FROM bc.fieldDeclarations
            ) UNION_ALL (
            SELECT (parameterTypeDependency) FROM (
                bc.methodDeclarations,
                (((_: MethodDeclaration).parameterTypes.filterNot (_.isBaseType)) IN bc.methodDeclarations)
                )
            ) UNION_ALL (
            SELECT (returnTypeDependency) FROM (bc.methodDeclarations) WHERE
                (!_.returnType.isBaseType) AND
                (!_.returnType.isVoidType)
            )
    )

    private def parseQueryKinds(constraint: IConstraint): Set[DependencyKind] = {
        kindParser.parse (constraint.getDependencyKind) match {
            case kindParser.Success (result, _) => KindResolver (result)
            case kindParser.Failure (msg, next) => {
                errors += new IllegalArgumentException (msg + next.pos.longString)
                Set ()
            }
        }
    }

    /**
     * Returns a list of constraints, that only contain one dependency kind.
     * InAndOut constraints are normalized as one incoming and one outgoing constraint
     */
    private def getNormalizedConstraints[T](v: (IConstraint, String)): List[NormalizedConstraint] = {
        val kinds = parseQueryKinds (v._1)
        val typ = ConstraintType (v._1)
        var result: List[NormalizedConstraint] = Nil
        for (kind <- kinds) {
            if (typ != ConstraintType.IncomingAndOutgoing) {
                result = NormalizedConstraint (
                    v._1,
                    kind,
                    typ,
                    v._1.getSource,
                    v._1.getTarget,
                    v._2
                ) :: result
            }
            else
            {
                result = NormalizedConstraint (
                    v._1,
                    kind,
                    ConstraintType.Incoming,
                    v._1.getSource,
                    v._1.getTarget,
                    v._2
                ) :: NormalizedConstraint (
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

    private lazy val normalized_constraints = new Relation[(NormalizedConstraint)] {

        slice_constraints.addObserver (new Observer[(IConstraint, String)] {
            def updated(oldV: (IConstraint, String), newV: (IConstraint, String)) {
                removed (oldV)
                added (newV)
            }

            def removed(v: (IConstraint, String)) {
                val normalizedConstraints = getNormalizedConstraints (v)
                normalizedConstraints.foreach (element_removed)
            }

            def added(v: (IConstraint, String)) {
                val normalizedConstraints = getNormalizedConstraints (v)
                normalizedConstraints.foreach (element_added)
            }

            def updated[U <: (IConstraint, String)](update: Update[U]) {
                throw new UnsupportedOperationException
            }

            def modified[U <: (IConstraint, String)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
                throw new UnsupportedOperationException
            }
        })

        def foreach[T](f: ((NormalizedConstraint)) => T) {
            slice_constraints.foreach (
                getNormalizedConstraints (_).foreach (f)
            )
        }

        def lazyInitialize() {
        }

        def isSet = true

        /**
         * Returns true if there is some intermediary storage, i.e., foreach is guaranteed to return a set of values.
         */
        def isStored = false
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the not_allowed constraints in the form:
     * (E_src, E_trgt, kind, constraint, slice).
     */
    private lazy val disallowed_dependencies_by_not_allowed: Relation[(IEnsemble, IEnsemble, String, IConstraint, String)] =
    {
        Π (
            (constraint: NormalizedConstraint) =>
                (constraint.source, constraint.target, constraint.kind.asVespucciString, constraint.origin, constraint
                    .context)
        )(
            σ ((_: NormalizedConstraint).constraintType == ConstraintType.NotAllowed)(normalized_constraints)
        )
    }

    private lazy val local_incoming = σ ((_: NormalizedConstraint).constraintType == ConstraintType
        .Incoming)(normalized_constraints)

    private lazy val global_incoming = σ ((_: NormalizedConstraint).constraintType == ConstraintType
        .GlobalIncoming)(normalized_constraints)

    private lazy val local_outgoing = σ ((_: NormalizedConstraint).constraintType == ConstraintType
        .Outgoing)(normalized_constraints)

    private lazy val global_outgoing = σ ((_: NormalizedConstraint).constraintType == ConstraintType
        .GlobalOutgoing)(normalized_constraints)


    private lazy val incoming: Relation[NormalizedConstraint] = {
        val allIncoming = local_incoming ⊎ global_incoming
        allIncoming ⊎ (
            (
                (
                    descendants,
                    (_: (IEnsemble, IEnsemble))._1
                    ) ⋈ (
                    (_: NormalizedConstraint).source,
                    allIncoming
                    )
                )
            {
                (parentChild: (IEnsemble, IEnsemble), c: NormalizedConstraint) =>
                    NormalizedConstraint (
                        c.origin,
                        c.kind,
                        c.constraintType,
                        parentChild._2,
                        c.target,
                        c.context
                    )
            })
    }


    private lazy val outgoing: Relation[NormalizedConstraint] = {
        val allOutgoing = local_outgoing ⊎ global_outgoing
        allOutgoing ⊎ (
            (
                (
                    descendants,
                    (_: (IEnsemble, IEnsemble))._1
                    ) ⋈ (
                    (_: NormalizedConstraint).target,
                    allOutgoing
                    )
                )
            {
                (parentChild: (IEnsemble, IEnsemble), c: NormalizedConstraint) =>
                    NormalizedConstraint (
                        c.origin,
                        c.kind,
                        c.constraintType,
                        c.source,
                        parentChild._2,
                        c.context
                    )
            })
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the local_incoming constraints in the form:
     * (E_src, E_trgt, kind, constraint, slice).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    private lazy val constrained_ensemble_combinations_by_local_incoming: Relation[(IEnsemble, IEnsemble, String, IConstraint, String)] =
    {
        (
            (
                slice_ensembles,
                (_: (IEnsemble, String))._2
                ) ⋈ (
                (_: NormalizedConstraint).context,
                local_incoming
                )
            )
        {
            (e: (IEnsemble, String), c: NormalizedConstraint) => (e._1, c.target, c.kind.asVespucciString, c.origin, c
                .context)
        }
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the global_incoming constraints in the form:
     * (E_src, E_trgt, kind, constraint, slice).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    private lazy val constrained_ensemble_combinations_by_global_incoming: Relation[(IEnsemble, IEnsemble, String, IConstraint, String)] =
    {
        Π (
            (entry: (IEnsemble, NormalizedConstraint)) =>
                (entry._1, entry._2.target, entry._2.kind.asVespucciString, entry._2.origin, entry._2.context)
        )(ensembles × global_incoming)
    }


    private lazy val disallowed_dependencies_by_incoming = {
        val source_target_ensemble_combinations =
            constrained_ensemble_combinations_by_local_incoming ⊎
                constrained_ensemble_combinations_by_global_incoming

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Z,Y) where Z,Y in Ensembles and Incoming(_,Y, ctx) ;
         * if !exists (Z,Y) with Incoming(Z,Y, ctx) or GlobalIncoming(Z,Y, ctx) then Z may not use Y
         */
        (
            source_target_ensemble_combinations,
            (entry: (IEnsemble, IEnsemble, String, IConstraint, String)) => (entry._1, entry._2, entry._3, entry._5)
            ) ⊳ (
            (c: NormalizedConstraint) => (c.source, c.target, c.kind.asVespucciString, c.context),
            incoming
            )
    }


    /**
     * Returns a view of the disallowed ensemble dependencies derived from the local_outgoing constraints in the form:
     * (E_src, E_trgt, kind, constraint, slice).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    private lazy val constrained_ensemble_combinations_by_local_outgoing: Relation[(IEnsemble, IEnsemble, String, IConstraint, String)] =
    {
        (
            (
                slice_ensembles,
                (_: (IEnsemble, String))._2
                ) ⋈ (
                (_: NormalizedConstraint).context,
                local_outgoing
                )
            )
        {
            (e: (IEnsemble, String), c: NormalizedConstraint) => (c.source, e._1, c.kind.asVespucciString, c.origin, c
                .context)
        }
    }

    /**
     * Returns a view of the disallowed ensemble dependencies derived from the local_outgoing constraints in the form:
     * (E_src, E_trgt, kind, constraint, slice).
     * The view may contain self-references and parent-child relations, since these are already filtered from the dependencies
     */
    private lazy val constrained_ensemble_combinations_by_global_outgoing: Relation[(IEnsemble, IEnsemble, String, IConstraint, String)] =
    {
        Π (
            (entry: (IEnsemble, NormalizedConstraint)) =>
                (entry._2.source, entry._1, entry._2.kind.asVespucciString, entry._2.origin, entry._2.context)
        )(ensembles × global_outgoing)
    }


    private lazy val disallowed_dependencies_by_outgoing = {
        val source_target_ensemble_combinations =
            constrained_ensemble_combinations_by_local_outgoing ⊎
                constrained_ensemble_combinations_by_global_outgoing

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Y, Z) where Z,Y in Ensembles and Outgoing(Y,_, ctx) ;
         * if !exists (Y, Z) with Outgoing(Y,Z, ctx) or GlobalOutgoing(Y,Z, ctx) then Y may not use Z
         */
        (
            source_target_ensemble_combinations,
            (entry: (IEnsemble, IEnsemble, String, IConstraint, String)) => (entry._1, entry._2, entry._3, entry._5)
            ) ⊳ (
            (c: NormalizedConstraint) => (c.source, c.target, c.kind.asVespucciString, c.context),
            outgoing
            )
    }


    /**
     * A list of ensemble dependencies that are not allowed in the form:
     * (E_src, E_trgt, kind, constraint, slice)
     */
    def notAllowedEnsembleDependencies = disallowed_dependencies_by_not_allowed ⊎
        disallowed_dependencies_by_incoming ⊎
        disallowed_dependencies_by_outgoing

    /**
     * A list of ensemble dependencies that are expected
     */
    def expectedEnsembleDependencies: Relation[(IEnsemble, IEnsemble, String, IConstraint, String)] = {
        Π (
            (constraint: NormalizedConstraint) =>
                (constraint.source, constraint.target, constraint.kind.asVespucciString, constraint.origin, constraint
                    .context)
        )(
            σ ((_: NormalizedConstraint).constraintType == ConstraintType.Expected)(normalized_constraints)
        )
    }


    lazy val unmodeled_elements: Relation[ICodeElement] = (
        (
            Π (SourceElementFactory (_: ObjectType).asInstanceOf[ICodeElement])(bc.typeDeclarations) ⊎
                Π (SourceElementFactory (_: FieldDeclaration).asInstanceOf[ICodeElement])(bc.fieldDeclarations) ⊎
                Π (SourceElementFactory (_: MethodDeclaration).asInstanceOf[ICodeElement])(bc.methodDeclarations)
            ) ∖
            δ (Π ((_: (IEnsemble, ICodeElement))._2)(ensemble_elements))
        )


    lazy val ensemble_dependency_count: Relation[(IEnsemble, IEnsemble, Int)] = {
        γ (ensemble_dependencies,
            (dependency: (IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)) => (dependency._1, dependency._2),
            Count[(IEnsemble, IEnsemble, ICodeElement, ICodeElement, String)](),
            (elem: (IEnsemble, IEnsemble), count: Int) => (elem._1, elem._2, count)
        )
    }


    /**
     * A list of errors that occurred during database updates
     */
    lazy val errors = new Table[Exception]

    /**
     * Clear the list of errors from the database
     */
    def clear_errors() {
        val oldErrors = errors.copy
        oldErrors.foreach (errors -= _)
    }
}