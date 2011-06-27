package sae.profiler.util

import sae.LazyView
import sae.collections.BagResult
import sae.operators.Conversions.HashIndexedViewProxy
import sae.operators._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 17.06.11 11:17
 *
 * The query that registers a counter for each constituent of a query.
 * The resulting profil thus tracks data that flows through the query tree
 */
class DataQueryAnalyzer
    extends QueryAnalyzer
{
    val profile = new DataQueryProfile

    type T = Unit

    // profile.addOperator(view, parent)

    def apply[Domain <: AnyRef]( view : LazyView[Domain] )
    {
        analyze[Domain](view)
    }

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: HashIndexedViewProxy[Domain], parent: Option[LazyView[Parent]], childContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        childContinuation
    }

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: BagResult[Domain], parent: Option[LazyView[Parent]], childContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        childContinuation
    }

    def differenceView[Domain <: AnyRef, Parent <: AnyRef](view: Difference[Domain], parent: Option[LazyView[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        leftContinuation
        rightContinuation
    }

    def intersectionView[Domain <: AnyRef, Parent <: AnyRef](view: Intersection[Domain], parent: Option[LazyView[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        leftContinuation
        rightContinuation
    }

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef](view: Union[Range, DomainA, DomainB], parent: Option[LazyView[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        leftContinuation
        rightContinuation
    }

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](view: EquiJoin[DomainA, DomainB, Range, Key], parent: Option[LazyView[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        leftContinuation
        rightContinuation
    }

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef](view: DuplicateElimination[Domain], parent: Option[LazyView[Parent]], childContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        childContinuation
    }

    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef](view: Projection[Domain, Range], parent: Option[LazyView[Parent]], childContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        childContinuation
    }

    def selectionView[Domain <: AnyRef, Parent <: AnyRef](view: Selection[Domain], parent: Option[LazyView[Parent]], childContinuation: => Unit)
    {
        profile.addOperator(view, parent)
        childContinuation
    }

    def baseView[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }
}

