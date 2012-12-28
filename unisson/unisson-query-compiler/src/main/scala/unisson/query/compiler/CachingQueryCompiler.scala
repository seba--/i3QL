package unisson.query.compiler

import collection.mutable.WeakHashMap
import unisson.query.UnissonQuery
import unisson.query.ast._
import sae.{Observable, Relation}
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, SourceElement}

/**
 *
 * Author: Ralf Mitschke
 * Date: 12.01.12
 * Time: 12:47
 *
 */
class CachingQueryCompiler(val decoratee: QueryCompiler)
    extends QueryCompiler
{
    protected[compiler] var cachedQueries: WeakHashMap[UnissonQuery, Relation[ICodeElement]] =
        new WeakHashMap[UnissonQuery, Relation[ICodeElement]]()

    val db = decoratee.db

    val definitions = decoratee.definitions

    def parseAndCompile(query: String)(implicit decorator: QueryCompiler = this): Relation[ICodeElement] =
        decoratee.parseAndCompile (query)(this)

    def compile(query: UnissonQuery)(implicit decorator: QueryCompiler = this): Relation[ICodeElement] = {
        for (compiledQuery <- getChachedQuery (query)) {
            return compiledQuery
        }
        val compiledQuery = decoratee.compile (query)(this)
        cachedQueries += {
            query -> compiledQuery
        }
        compiledQuery
    }

    private def getChachedQuery(query: UnissonQuery): Option[Relation[ICodeElement]] = {
        val cachedCompiledQuery: Option[Relation[ICodeElement]] =
            cachedQueries.collectFirst {
                case (cachedQuery, compiledQuery) if (cachedQuery.isSyntacticEqual (query)) => compiledQuery
            }
        cachedCompiledQuery
    }

    def dispose(query: UnissonQuery) {
        for (
            compiledQuery <- getChachedQuery (query);
            if !compiledQuery.hasObservers
        )
        {

            query match {
                case ClassSelectionQuery (_, _) =>
                    removeAllSingleObserversOnPath (
                        List (db.typeDeclarations),
                        compiledQuery
                    )
                case ClassQuery (innerQuery) => {
                    for (innerCompiledQuery <- getChachedQuery (innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath (
                            List (db.typeDeclarations, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose (innerQuery)
                    }
                }
                case ClassWithMembersQuery (innerQuery) => {
                    for (innerCompiledQuery <- getChachedQuery (innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath (
                            List (db.typeDeclarations, definitions.transitive_class_members, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose (innerQuery)
                    }
                }
                case PackageQuery (_) => {
                    removeAllSingleObserversOnPath (
                        List (db.typeDeclarations, db.methodDeclarations, db.fieldDeclarations),
                        compiledQuery
                    )
                }
                case OrQuery (left, right) => {
                    // we do not know the root sources of the sub-queries so we delegate to sub-queries
                    // or query should be a union with direct children, hence we could directly remove the current query as observer
                    // but using the transitive removal is more stable, since it relies only on the knowledge of the sources of the union and not the implementation as a singular operator
                    for (innerCompiledLeft <- getChachedQuery (left);
                         innerCompiledRight <- getChachedQuery (right)
                    )
                    {
                        removeAllSingleObserversOnPath (
                            List (innerCompiledLeft, innerCompiledRight),
                            compiledQuery
                        )
                    }
                    dispose (left)
                    dispose (right)
                }
                case WithoutQuery (left, right) => {
                    // we do not know the root sources of the sub-queries so we delegate to sub-queries
                    // the without query should be a set difference with direct children, hence we could directly remove the current query as observer
                    // but using the transitive removal is more stable, since it relies only on the knowledge of the sources of the difference and not the implementation as a singular operator
                    for (innerCompiledLeft <- getChachedQuery (left);
                         innerCompiledRight <- getChachedQuery (right)
                    )
                    {
                        removeAllSingleObserversOnPath (
                            List (innerCompiledLeft, innerCompiledRight),
                            compiledQuery
                        )
                    }
                    dispose (left)
                    dispose (right)
                }
                case TransitiveQuery (SuperTypeQuery (innerQuery)) => {
                    for (innerCompiledQuery <- getChachedQuery (innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath (
                            List (db.subTypes, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose (innerQuery)
                    }
                }
                case SuperTypeQuery (innerQuery) => {
                    for (innerCompiledQuery <- getChachedQuery (innerQuery)) {
                        // remove everything up to the inner query, we do not know what the ultimate roots of the inner query are
                        removeAllSingleObserversOnPath (
                            List (db.interfaceInheritance, db.classInheritance, innerCompiledQuery),
                            compiledQuery
                        )
                        dispose (innerQuery)
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
    private def removeAllSingleObserversOnPath(sources: List[Relation[_ <: AnyRef]], target: Relation[_ <: AnyRef]) {
        target.clearObserversForChildren (
            (o: Observable[_]) => !sources.contains (o)
        )
    }
}