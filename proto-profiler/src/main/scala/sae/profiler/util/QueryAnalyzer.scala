package sae.profiler.util

import sae.{IndexedView, MaterializedView, LazyView}
import sae.operators.{DuplicateElimination, EquiJoin, Projection, Selection, Union, Intersection, Difference}
import sae.collections.BagResult
import sae.operators.Conversions.HashIndexedViewProxy

/**
 *
 * Author: Ralf Mitschke
 * Created: 17.06.11 12:19
 *
 * The query analyzer traverses all relations in a query calls appropriate functions for each one, giving information
 * on the relation itself and the parents. Children may be accessed by knowing the layout of individual relations.
 */
trait QueryAnalyzer {

    type T

    import sae.syntax.RelationalAlgebraSyntax._

    def analyze[Domain <: AnyRef]( view : LazyView[Domain] )(implicit leafFunc : () => T, joinResults : (T,T) => T) : T =
        analyze(view, None)


    private def analyze[Domain <: AnyRef, Parent <: AnyRef]( view : LazyView[Domain], parent : Option[LazyView[Parent]] )(implicit leafFunc : () => T, joinResults : (T,T) => T) : T = view match {
        case selection @ σ(_, relation : LazyView[Domain]) =>
        {
            selectionView(selection, parent)
            analyze(relation, Some(view))
        }
        case projection @ Π(_, relation : LazyView[Domain]) =>
        {
            projectionView(projection, parent)
            analyze(relation, Some(view))
        }
        // TODO at least for case classes there is a way to do infix operators, check with ⋈ unapply method
        case equiJoin @ ⋈(relationA, _, relationB, _) =>
        {
            equiJoinView(equiJoin, parent)
            val ta = analyze(relationA, Some(view))
            val tb = analyze(relationB, Some(view))
            joinResults(ta, tb)
        }
        case delta @ δ(relation) =>
        {
            duplicateEliminationView(delta, parent)
            analyze(relation, Some(view))
        }
        // TODO the pattern matching is not type safe here
        case union : Union[_,_,_] =>
        {
            unionView(union, parent)
            val ta = analyze( union.left, Some(view) )
            val tb = analyze( union.right, Some(view) )
            joinResults(ta, tb)

        }
        case intersection : Intersection[_] =>
        {
            intersectionView(intersection, parent)
            val ta = analyze( intersection.left, Some(view) )
            val tb = analyze( intersection.right, Some(view) )
            joinResults(ta, tb)

        }
        case difference : Difference[_] =>
        {
            differenceView(difference, parent)
            val ta = analyze( difference.left, Some(view) )
            val tb = analyze( difference.right, Some(view) )
            joinResults(ta, tb)

        }
        case proxy @ MaterializedViewProxy(relation) =>
        {
            materializedProxyView(proxy, parent)
            analyze(relation, Some(view))
        }
        case proxy @ IndexedViewProxy(relation) =>
        {
            indexedProxyView(proxy, parent)
            analyze(relation, Some(view))
        }
        case _ =>
        {
            baseView(view, parent)
            leafFunc()
        }
    }

    def selectionView[Domain <: AnyRef, Parent <: AnyRef]( view : Selection[Domain], parent : Option[LazyView[Parent]] )

    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef]( view : Projection[Domain, Range], parent : Option[LazyView[Parent]] )

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef]( view : DuplicateElimination[Domain], parent : Option[LazyView[Parent]] )

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](
                                                                                                                view : EquiJoin[DomainA, DomainB, Range, Key], parent : Option[LazyView[Parent]] )

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef]( view : Union[Range, DomainA, DomainB], parent : Option[LazyView[Parent]] )

    def intersectionView[Domain <: AnyRef, Parent <: AnyRef]( view : Intersection[Domain], parent : Option[LazyView[Parent]] )

    def differenceView[Domain <: AnyRef, Parent <: AnyRef]( view : Difference[Domain], parent : Option[LazyView[Parent]] )

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef]( view : BagResult[Domain], parent : Option[LazyView[Parent]] )

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef]( view : HashIndexedViewProxy[Domain], parent : Option[LazyView[Parent]] )

    def baseView[Domain <: AnyRef, Parent <: AnyRef]( view : LazyView[Domain], parent : Option[LazyView[Parent]])

    def isMaterializedView[Domain <: AnyRef]( view : LazyView[Domain] ) = view.isInstanceOf[MaterializedView[Domain]]

    def isIndexedView[Domain <: AnyRef]( view : LazyView[Domain] ) = view.isInstanceOf[IndexedView[Domain]]


    private object MaterializedViewProxy
    {
        def unapply[Domain <: AnyRef](proxy : BagResult[Domain]) : Option[LazyView[Domain]]= Some(proxy.relation)
    }

    private object IndexedViewProxy
    {
        def unapply[Domain <: AnyRef](proxy : HashIndexedViewProxy[Domain]) : Option[LazyView[Domain]]= Some(proxy.relation)
    }
}