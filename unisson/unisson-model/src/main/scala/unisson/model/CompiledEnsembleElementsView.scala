package unisson.model

import unisson.query.compiler.CachingQueryCompiler
import unisson.query.parser.QueryParser
import de.tud.cs.st.vespucci.model.IEnsemble
import unisson.query.code_model.SourceElement
import sae.{LazyView, Observer}
import sae.bytecode.Database
import de.tud.cs.st.vespucci.interfaces.ICodeElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 25.06.12
 * Time: 12:58
 *
 */
class CompiledEnsembleElementsView(bc: Database,
                                   ensembleView: LazyView[IEnsemble])
        extends LazyView[(IEnsemble, ICodeElement)]
{

    private val queryCompiler = new CachingQueryCompiler(bc)

    private val queryParser = new QueryParser()

    initialized = true

    ensembleView.addObserver(ensembleObserver)

    def lazy_foreach[T](f: ((IEnsemble, ICodeElement)) => T) {
        ensembleView.lazy_foreach((e:IEnsemble) =>
                queryCompiler.parseAndCompile(e.getQuery).lazy_foreach[Unit](
                    f(e, _)
                )
        )
    }

    def lazyInitialize() {
        // should never be used
        throw new UnsupportedOperationException("lazyInitialize not supported for global_ensemble_elements")
    }

    var elementObservers: Map[IEnsemble, ElementObserver] = Map.empty

    val ensembleObserver = new Observer[IEnsemble] {
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
            for (oldObserver <- elementObservers.get(oldV)) {
                oldCompiledQuery.removeObserver(oldObserver)
            }
            // dispose of obsolete views and observers
            val oldObserver = elementObservers(oldV)
            oldCompiledQuery.removeObserver(oldObserver)
            elementObservers -= oldV

            queryCompiler.dispose(oldQuery)

            // add new elements
            val newCompiledQuery = queryCompiler.compile(newQuery)
            newCompiledQuery.lazy_foreach(
                (e: SourceElement[AnyRef]) => element_added((newV, e))
            )
            val newObserver = new ElementObserver(newV)
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

}