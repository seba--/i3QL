package sae.operators

import util.control.Breaks
import collection.mutable.{HashSet, HashMap}
import _root_.java.lang.Error
import sae._

/**
 * A operation that calculates the transitive closure for a given source relation
 * The Arity of TransitiveClosure is 2 even if the arity of the source relation is not 2
 *
 * @author Malte V
 * @author Ralf Mitschke
 */
trait TransitiveClosure[Edge, Vertex, Range]
    extends MaterializedRelation[(Vertex, Vertex)]
{
    def source: Relation[Edge]
    // naming after  "Network Flows: Theory, Algorithms, and Applications"
    // Edge e = (Vertex u , Vertx v)
    // u is tail of e
    // v is head of e
    def getTail: Edge => Vertex
    def getHead: Edge => Vertex
}


