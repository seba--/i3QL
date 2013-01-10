package sae.operators

import sae._

/**
 * A operation that calculates the transitive closure for a given source relation
 * The Arity of TransitiveClosure is 2 even if the arity of the source relation is not 2
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
trait FixPointClosure[Edge, Vertex, Info]
    extends MaterializedRelation[Edge]
{
    def source: Relation[Edge]

    // naming after  "Network Flows: Theory, Algorithms, and Applications"
    // Edge e = (Vertex u , Vertx v)
    // u is tail of e
    // v is head of e
    def getTail: Edge => Vertex

    def getHead: Edge => Vertex

    def getTailInfo: Edge => Info

    def getHeadInfo: Edge => Info

    def createEdge: (Vertex, Info, Vertex, Info) => Edge

    def isSet = true

    override protected def children = List (source)
}


