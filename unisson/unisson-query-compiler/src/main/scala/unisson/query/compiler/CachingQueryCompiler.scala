package unisson.query.compiler

import sae.bytecode.Database
import collection.mutable.WeakHashMap
import unisson.query.UnissonQuery
import unisson.query.code_model.SourceElement
import unisson.query.ast._
import sae.{Observable, LazyView}

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.01.12
 * Time: 12:47
 *
 */
class CachingQueryCompiler(db: Database)
        extends QueryCompiler(db)
{

    protected[compiler] var cachedQueries: WeakHashMap[UnissonQuery, LazyView[SourceElement[AnyRef]]] =
        new WeakHashMap[UnissonQuery, LazyView[SourceElement[AnyRef]]]()

    override def compile(query: UnissonQuery): LazyView[SourceElement[AnyRef]] = {
        for (compiledQuery <- getChachedQuery(query)) {
            return compiledQuery
        }
        val compiledQuery = super.compile(query)
        cachedQueries += {
            query -> compiledQuery
        }
        compiledQuery
    }

    private def getChachedQuery(query: UnissonQuery): Option[LazyView[SourceElement[AnyRef]]] = {
        val cachedCompiledQuery: Option[LazyView[SourceElement[AnyRef]]] =
            cachedQueries.collectFirst {
                case (cachedQuery, compiledQuery) if (cachedQuery.isSyntacticEqual(query)) => compiledQuery
            }
        cachedCompiledQuery
    }

    def dispose(query: UnissonQuery) {
        for (
            compiledQuery <- getChachedQuery(query);
            if !compiledQuery.hasObservers
        ) {

            query match {
                case ClassSelectionQuery(_, _) =>
                    removeAllSingleObserversOnPath(
                        List(db.classfiles),
                        compiledQuery
                    )
                case ClassQuery(innerQuery) => {
                    for (innerCompiledQuery <- getChachedQuery(innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath(
                            List(db.classfiles, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose(innerQuery)
                    }
                }
                case ClassWithMembersQuery(innerQuery) => {
                    for (innerCompiledQuery <- getChachedQuery(innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath(
                            List(db.classfiles, definitions.transitive_class_members, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose(innerQuery)
                    }
                }
                case PackageQuery(_) => {
                    removeAllSingleObserversOnPath(
                        List(db.classfiles, db.classfile_methods, db.classfile_fields),
                        compiledQuery
                    )
                }
                case OrQuery(left, right) => {
                    // we do not know the root sources of the sub-queries so we delegate to sub-queries
                    // or query should be a union with direct children, hence we could directly remove the current query as observer
                    // but using the transitive removal is more stable, since it relies only on the knowledge of the sources of the union and not the implementation as a singular operator
                    for (innerCompiledLeft <- getChachedQuery(left);
                         innerCompiledRight <- getChachedQuery(right)
                    ) {
                        removeAllSingleObserversOnPath(
                            List(innerCompiledLeft, innerCompiledRight),
                            compiledQuery
                        )
                    }
                    dispose(left)
                    dispose(right)
                }
                case WithoutQuery(left, right) => {
                    // we do not know the root sources of the sub-queries so we delegate to sub-queries
                    // the without query should be a set difference with direct children, hence we could directly remove the current query as observer
                    // but using the transitive removal is more stable, since it relies only on the knowledge of the sources of the difference and not the implementation as a singular operator
                    for (innerCompiledLeft <- getChachedQuery(left);
                         innerCompiledRight <- getChachedQuery(right)
                    ) {
                        removeAllSingleObserversOnPath(
                            List(innerCompiledLeft, innerCompiledRight),
                            compiledQuery
                        )
                    }
                    dispose(left)
                    dispose(right)
                }
                case TransitiveQuery(SuperTypeQuery(innerQuery)) => {
                    for (innerCompiledQuery <- getChachedQuery(innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath(
                            List(definitions.supertypeTrans, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose(innerQuery)
                    }
                }
                case SuperTypeQuery(innerQuery) => {
                    for (innerCompiledQuery <- getChachedQuery(innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath(
                            List(db.implements, db.`extends`, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose(innerQuery)
                    }
                }
                case _ => // do nothing
            }
            if (!compiledQuery.hasObservers) {
                cachedQueries -= query
            }
        }
    }


    /**
     * removes every observer on the path from source to target, beginning at the target and stopping
     * either if source is reached or a view with multiple other observers is reached
     */
    private def removeAllSingleObserversOnPath(sources: List[LazyView[_ <: AnyRef]], target: LazyView[_ <: AnyRef]) {
        target.clearObserversForChildren(
            (o: Observable[_ <: AnyRef]) => !sources.contains(o)
        )
    }
}