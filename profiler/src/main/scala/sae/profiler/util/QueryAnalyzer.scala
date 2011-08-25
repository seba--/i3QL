package sae.profiler.util

import sae.{IndexedView, MaterializedView, LazyView}
import sae.collections.BagResult
import sae.operators.Conversions.HashIndexedViewProxy
import sae.operators._

/**
 *
 * Author: Ralf Mitschke
 * Created: 17.06.11 12:19
 *
 * The query analyzer traverses all relations in a query calls appropriate functions for each one, giving information
 * on the relation itself and the parents. Children may be accessed by knowing the layout of individual relations.
 */
trait QueryAnalyzer {

    import sae.syntax.RelationalAlgebraSyntax._

    def analyze[Domain <: AnyRef]( view : LazyView[Domain] ) : T =
        analyze(view, None)

    type T

    private def analyze[Domain <: AnyRef, Parent <: AnyRef]( view : LazyView[Domain], parent : Option[LazyView[Parent]] ) : T = view match {
        case selection @ σ(_, relation : LazyView[Domain]) =>
        {
            selectionView(selection, parent, analyze(relation, Some(view)))
        }
        case projection @ Π(_, relation : LazyView[Domain]) =>
        {
            projectionView(projection, parent, analyze(relation, Some(view)))
        }
        // TODO at least for case classes there is a way to do infix operators, check with ⋈ unapply method
        case equiJoin @ ⋈(relationA, _, relationB, _, _) =>
        {
            equiJoinView(equiJoin, parent, analyze(relationA, Some(view)), analyze(relationB, Some(view)))
        }
        case delta @ δ(relation) =>
        {
            duplicateEliminationView(delta, parent, analyze(relation, Some(view)))
        }
        // TODO the pattern matching is not type safe here
        case union : Union[_,_,_] =>
        {
            unionView(union, parent, analyze(union.left, Some(view)), analyze(union.right, Some(view)))
        }
        case intersection : Intersection[_] =>
        {
            intersectionView(intersection, parent, analyze(intersection.left, Some(view)), analyze(intersection.right, Some(view)))
        }
        case difference : Difference[_] =>
        {
            differenceView(difference, parent, analyze(difference.left, Some(view)), analyze(difference.right, Some(view)))

        }
        case proxy @ MaterializedViewProxy(relation) =>
        {
            materializedProxyView(proxy, parent, analyze(relation, Some(view)))
        }
        case proxy @ IndexedViewProxy(relation) =>
        {
            indexedProxyView(proxy, parent, analyze(relation, Some(view)))
        }
        case tc @ TC(relation, _, _) =>
        {
            transitiveClosureView(tc, parent, analyze(relation, Some(view)))
        }
        case _ =>
        {
            baseView(view, parent)
        }
    }

    def selectionView[Domain <: AnyRef, Parent <: AnyRef]( view : Selection[Domain], parent : Option[LazyView[Parent]], childContinuation : => T) : T

    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef]( view : Projection[Domain, Range], parent : Option[LazyView[Parent]], childContinuation : => T) : T

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef]( view : DuplicateElimination[Domain], parent : Option[LazyView[Parent]], childContinuation : => T) : T

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](
                                                                                                                view : EquiJoin[DomainA, DomainB, Range, Key], parent : Option[LazyView[Parent]], leftContinuation : => T, rightContinuation : => T) : T

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef]( view : Union[Range, DomainA, DomainB], parent : Option[LazyView[Parent]], leftContinuation : => T, rightContinuation : => T) : T

    def intersectionView[Domain <: AnyRef, Parent <: AnyRef]( view : Intersection[Domain], parent : Option[LazyView[Parent]], leftContinuation : => T, rightContinuation : => T) : T

    def differenceView[Domain <: AnyRef, Parent <: AnyRef]( view : Difference[Domain], parent : Option[LazyView[Parent]], leftContinuation : => T, rightContinuation : => T) : T

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef]( view : BagResult[Domain], parent : Option[LazyView[Parent]], childContinuation : => T) : T

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef]( view : HashIndexedViewProxy[Domain], parent : Option[LazyView[Parent]], childContinuation : => T) : T

    def baseView[Domain <: AnyRef, Parent <: AnyRef]( view : LazyView[Domain], parent : Option[LazyView[Parent]]) : T

    def transitiveClosureView[Domain <: AnyRef, Vertex <: AnyRef, Parent <: AnyRef]( view : TransitiveClosure[Domain, Vertex], parent : Option[LazyView[Parent]], childContinuation : => T) : T

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