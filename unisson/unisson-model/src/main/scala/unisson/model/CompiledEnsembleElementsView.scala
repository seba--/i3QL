package unisson.model

import unisson.query.compiler.{BaseQueryCompiler, CachingQueryCompiler}
import de.tud.cs.st.vespucci.interfaces.IEnsemble
import unisson.query.code_model.SourceElement
import sae.{Relation, Observer}
import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.vespucci.interfaces.ICodeElement
import unisson.query.UnissonQuery
import sae.deltas.{Update, Deletion, Addition}

/**
 *
 * Author: Ralf Mitschke
 * Date: 25.06.12
 * Time: 12:58
 *
 */
class CompiledEnsembleElementsView(bc: BytecodeDatabase,
                                   ensembleQueries: Relation[(IEnsemble, UnissonQuery)])
    extends Relation[(IEnsemble, ICodeElement)]
{

    def isSet = false

    def isStored = false

    private val queryCompiler = new CachingQueryCompiler (new BaseQueryCompiler (bc))

    private var elementObservers: Map[IEnsemble, CompiledViewObserver] = Map.empty

    lazyInitialize()

    def lazyInitialize() {
        // compile existing ensemble queries and add observers that will announce new elements
        ensembleQueries.foreach (
            (entry: (IEnsemble, UnissonQuery)) => {
                val compiledQuery = queryCompiler.compile (entry._2)
                addCompiledQueryView (entry._1, compiledQuery)
            }
        )
    }

    def foreach[T](f: ((IEnsemble, ICodeElement)) => T) {
        ensembleQueries.foreach (
            (entry: (IEnsemble, UnissonQuery)) => {
                val queryElements = queryCompiler.compile (entry._2)
                queryElements.foreach[Unit](
                    (e: ICodeElement) => f ((entry._1, e))
                )
            }

        )
    }


    private def addCompiledQueryView(v: IEnsemble, view: Relation[SourceElement[AnyRef]]) {
        view.foreach (
            (e: SourceElement[AnyRef]) => element_added ((v, e))
        )
        val oo = new CompiledViewObserver (v)
        view.addObserver (oo)
        elementObservers += {
            v -> oo
        }
    }

    private def removeCompiledQueryView(v: IEnsemble, view: Relation[SourceElement[AnyRef]]) {
        view.foreach (
            (e: SourceElement[AnyRef]) => element_removed ((v, e))
        )
        // dispose of obsolete observers
        view.removeObserver (elementObservers (v))
        elementObservers -= v
    }


    // add an observer that will be alerted to changes in queries
    ensembleQueries.addObserver (new Observer[(IEnsemble, UnissonQuery)] {
        def added(v: (IEnsemble, UnissonQuery)) {
            val compiledQuery = queryCompiler.compile (v._2)
            addCompiledQueryView (v._1, compiledQuery)
        }

        def removed(v: (IEnsemble, UnissonQuery)) {
            val compiledQuery = queryCompiler.compile (v._2)
            removeCompiledQueryView (v._1, compiledQuery)
            // dispose of obsolete views
            queryCompiler.dispose (v._2)
        }

        def updated(oldV: (IEnsemble, UnissonQuery), newV: (IEnsemble, UnissonQuery)) {
            removed (oldV)
            added (newV)
        }

        def updated[U <: (IEnsemble, UnissonQuery)](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: (IEnsemble, UnissonQuery)](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    })


    /**
     * This observer adds entries from a compiled view of source code elements.
     * Since the ensemble is not contained as an information in the compiled view (i.e., they are only a set of code elements)
     * there is one observer per ensemble.
     */
    private class CompiledViewObserver(val ensemble: IEnsemble) extends Observer[SourceElement[AnyRef]]
    {
        def updated(oldV: SourceElement[AnyRef], newV: SourceElement[AnyRef]) {
            element_updated ((ensemble, oldV), (ensemble, newV))
        }

        def removed(v: SourceElement[AnyRef]) {
            element_removed ((ensemble, v))
        }

        def added(v: SourceElement[AnyRef]) {
            element_added ((ensemble, v))
        }

        def updated[U <: SourceElement[AnyRef]](update: Update[U]) {
            throw new UnsupportedOperationException
        }

        def modified[U <: SourceElement[AnyRef]](additions: Set[Addition[U]], deletions: Set[Deletion[U]], updates: Set[Update[U]]) {
            throw new UnsupportedOperationException
        }
    }

}