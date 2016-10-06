package idb.operators

import idb.{View, MaterializedView, Relation}


/**
 * A operation that calculates the transitive closure for a given source relation
 * The Arity of TransitiveClosure is 2 even if the arity of the source relation is not 2
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
trait TransitiveClosure[Edge, Vertex]
    extends View[(Vertex, Vertex)]
{
    def source: Relation[Edge]
    // naming after  "Network Flows: Theory, Algorithms, and Applications"
    // Edge e = (Vertex u , Vertx v)
    // u is tail of e
    // v is head of e
    def getTail: Edge => Vertex
    def getHead: Edge => Vertex

    override def children() = List (source)

    override def prettyprint(implicit prefix: String) = prefix +
      s"TransitiveClosure(${nested(source)})"
}


