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
    type T = Unit

    val profile = new DataQueryProfile

    private def leafFunc() {}

    private def joinResults : (Unit, Unit) => Unit = (_, _) => {}

    def apply[Domain <: AnyRef]( view : LazyView[Domain] )
    {
        analyze[Domain](view)(leafFunc, joinResults)
    }

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: HashIndexedViewProxy[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: BagResult[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](view: EquiJoin[DomainA, DomainB, Range, Key], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef](view: DuplicateElimination[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }


    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef](view: Projection[Domain, Range], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def selectionView[Domain <: AnyRef, Parent <: AnyRef](view: Selection[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef](view: Union[Range, DomainA, DomainB], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def differenceView[Domain <: AnyRef, Parent <: AnyRef](view: Difference[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def intersectionView[Domain <: AnyRef, Parent <: AnyRef](view: Intersection[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }

    def baseView[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: Option[LazyView[Parent]])
    {
        profile.addOperator(view, parent)
    }
}

