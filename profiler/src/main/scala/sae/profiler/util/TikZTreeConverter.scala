package sae.profiler.util

import sae.LazyView
import sae.operators._
import sae.collections.BagResult
import sae.operators.Conversions.HashIndexedViewProxy

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.06.11 13:13
 *
 */

abstract class TikZTreeConverter
        extends QueryAnalyzer
{

    type T = String

    def apply[Domain <: AnyRef]( view : LazyView[Domain] ) =
    {
        analyze[Domain](view)
    }

    protected def makeEdgeLabel[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: LazyView[Parent]) : String

    private def child(s : String) : String = "child {\n" + s + "}\n"

    private def makeEdge[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: Option[LazyView[Parent]]) =
        if( parent != None)
        {
        "edge from parent\n" +
        makeEdgeLabel (view, parent.get) + "\n"
        }
        else ""

    private def makeNode(kind : String, name : String) = "node[" + kind + "] {" + name + "}\n"

    def baseView[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: Option[LazyView[Parent]]) =
        makeNode("base", "$Rel$") + makeEdge (view, parent)

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: HashIndexedViewProxy[Domain], parent: Option[LazyView[Parent]], childContinuation: => String) =
        makeNode("proxy", "$Idx$") + child(childContinuation) + makeEdge (view, parent)

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: BagResult[Domain], parent: Option[LazyView[Parent]], childContinuation: => String) =
        makeNode("proxy", "$Mat$") + child(childContinuation) + makeEdge (view, parent)

    def differenceView[Domain <: AnyRef, Parent <: AnyRef](view: Difference[Domain], parent: Option[LazyView[Parent]], leftContinuation: => String, rightContinuation: => String) =
    makeNode("operator", "$\\setminus$") + child(leftContinuation) + child(rightContinuation) + makeEdge (view, parent)

    def intersectionView[Domain <: AnyRef, Parent <: AnyRef](view: Intersection[Domain], parent: Option[LazyView[Parent]], leftContinuation: => String, rightContinuation: => String) =
        makeNode("operator", "$\\cap$") + child(leftContinuation) + child(rightContinuation) + makeEdge (view, parent)

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef](view: Union[Range, DomainA, DomainB], parent: Option[LazyView[Parent]], leftContinuation: => String, rightContinuation: => String) =
        makeNode("operator", "$\\cup$") + child(leftContinuation) + child(rightContinuation) + makeEdge (view, parent)

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](view: EquiJoin[DomainA, DomainB, Range, Key], parent: Option[LazyView[Parent]], leftContinuation: => String, rightContinuation: => String) =
        makeNode("operator", "$\\bowtie$") + child(leftContinuation) + child(rightContinuation) + makeEdge (view, parent)

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef](view: DuplicateElimination[Domain], parent: Option[LazyView[Parent]], childContinuation: => String) =
        makeNode("operator", "$\\delta$") + child(childContinuation) + makeEdge (view, parent)

    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef](view: Projection[Domain, Range], parent: Option[LazyView[Parent]], childContinuation: => String) =
        makeNode("operator", "$\\Pi$") + child(childContinuation) + makeEdge (view, parent)

    def selectionView[Domain <: AnyRef, Parent <: AnyRef](view: Selection[Domain], parent: Option[LazyView[Parent]], childContinuation: => String) =
        makeNode("operator", "$\\sigma$") + child(childContinuation) + makeEdge (view, parent)

    def transitiveClosureView[Domain <: AnyRef, Vertex <: AnyRef, Parent <: AnyRef](view: TransitiveClosure[Domain, Vertex], parent: Option[LazyView[Parent]], childContinuation: => String) =
        makeNode("operator", "$TC$") + child(childContinuation) + makeEdge (view, parent)
}