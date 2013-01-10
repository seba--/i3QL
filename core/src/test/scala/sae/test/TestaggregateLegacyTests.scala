package sae.test

import junit.framework._
import org.junit.Assert._
import sae.operators._
import sae.functions._
import sae._
import sae.operators.CreateAggregationFunctionContainer._


/**
 * Test suit for Aggregation
 * @author Malte V
 */
class TestaggregateLegacyTests extends TestCase
{

    case class Line(iD: String, itemType: String, preis: Integer)
    {

    }

    case class Edge(a: String, b: String, c: Int)

    case class Schuh(art: String, name: String, hersteller: String, preis: Int, groesse: Int)

    var schuhe = new ObservableList[Schuh]
    //: Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)]
    var aggOp  = relationToResult (Aggregation (schuhe, grouping, (Sum ((x: Schuh) => x.preis), Min ((x: Schuh) => x.preis), Max ((x: Schuh) => x.preis)), ((key: (String, String), aggV: (Int, Int, Int)) => (key._1, key._2, aggV._1, aggV._2, aggV._3))))

    val grouping = (x: Schuh) => {
        (x.art, x.hersteller)
    }


    class ObservableList[V <: AnyRef] extends Relation[V]
    {
        def lazy_foreach[T](f: (V) => T) {
            foreach (f)
        }

        //   protected var data = List[V]();
        // var data = ListBuffer[V]();

        import com.google.common.collect.HashMultiset

        val data: HashMultiset[V] = HashMultiset.create[V]()

        def add(k: V) {
            data.add (k) // += k
            element_added (k)
        }

        def remove(k: V) {
            //data = data.filterNot(_ eq k)
            data.remove (k) //data -= k
            element_removed (k)
        }

        def update(oldV: V, newV: V) {
            //data = newV +: (data.filterNot(_ eq oldV))
            data.remove (oldV)
            data.add (newV)
            //data -= oldV
            //data += newV
            element_updated (oldV, newV)
        }

        def foreach[T](f: (V) => T) {
            //var x = data.iterator()
            //while (x.hasNext()) {
            //  f(x.next)
            //}
            import scala.collection.JavaConversions._
            data.foreach (f)
        }

        def lazyInitialize() {}

        def isSet = false

        /**
         * Returns true if there is some intermediary storage, i.e., foreach is guaranteed to return a set of values.
         */
        def isStored = true
    }

    //before every method
    override def setUp() {
        schuhe = new ObservableList[Schuh]
        aggOp = relationToResult (Aggregation (schuhe, grouping, (Sum ((x: Schuh) => x.preis), Min ((x: Schuh) => x.preis), Max ((x: Schuh) => x.preis)), ((key: (String, String), aggV: (Int, Int, Int)) => (key._1, key._2, aggV._1, aggV._2, aggV._3))))
    }

    //after every method
    override def tearDown() {
        //  println("down!")
    }

    def testAggregationWithoutGrouping() {
        var sum = relationToResult (Aggregation (schuhe, Sum ((x: Schuh) => x.preis)))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        schuhe.add (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        schuhe.add (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue (sum.size == 1)
        assertTrue (sum.asList.contains (Some (51)))

    }

    def testAdd() {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))

        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 11, 11, 11))

        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))

        assertTrue (aggOp.asList.contains ("herren", "Adidas", 11, 11, 11))
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 12, 12, 12))

        schuhe.add (new Schuh ("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        assertTrue (aggOp.size == 3)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 11, 11, 11))
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 12, 12, 12))
        assertTrue (aggOp.asList.contains ("damen", "Puma", 13, 13, 13))

        schuhe.add (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        assertTrue (aggOp.size == 4)
        assertTrue (aggOp.asList.contains ("herren", "Puma", 15, 15, 15))

        schuhe.add (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue (aggOp.size == 4)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 11, 11, 11))
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 12, 12, 12))
        assertTrue (aggOp.asList.contains ("damen", "Puma", 13, 13, 13))
        assertTrue (aggOp.asList.contains ("herren", "Puma", 15, 0, 15))
        //assertTrue(result.size == 5)

    }

    def testAddMultiValue() {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))

        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 66, 11, 11))

        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))

        assertTrue (aggOp.asList.contains ("herren", "Adidas", 66, 11, 11))
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 120, 12, 12))

    }

    def testDelete() {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add (new Schuh ("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        schuhe.add (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        schuhe.add (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue (aggOp.size == 4)
        schuhe.remove (new Schuh ("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue (aggOp.size == 4)
        assertTrue (aggOp.asList.contains ("damen", "Puma", 13, 13, 13))
        schuhe.remove (new Schuh ("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        assertTrue (aggOp.size == 3)
        assertFalse (aggOp.asList.contains ("damen", "Puma", 13, 13, 13))
        //assertTrue(result.size == 5)
    }

    //oldKey = newKey
    def testUpdateCase1() {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("herren", "New GOODYEAR STREET M", "Adidas", 24, 12))
        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 35, 11, 24))
        schuhe.update (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12),
            new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 29, 5, 24))
        schuhe.update (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 5, 12),
            new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 30, 12))
        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 54, 24, 30))
    }

    //oldKey <> newkey and remove oldKey and add newKey
    def testUpdateCase2()
    {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 11, 11, 11))
        schuhe.update (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh ("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 5, 5, 5))
    }

    //oldKey <> newkey and remove oldKey and update newKey
    def testUpdateCase3() {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET M", "Adidas", 13, 12))
        assertTrue (aggOp.size == 2)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 11, 11, 11))
        schuhe.update (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh ("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue (aggOp.size == 1)
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 18, 5, 13))
    }

    //oldKey <> newkey and update oldKey and update newKey
    def testUpdateCase4() {
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET M", "Adidas", 13, 12))
        schuhe.add (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 18, 12))
        schuhe.add (new Schuh ("damen", "GOODYEAR STREET M", "Adidas", 2, 12))
        assertTrue (aggOp.size == 2)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 29, 11, 18))
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 15, 2, 13))
        schuhe.update (new Schuh ("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh ("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue (aggOp.size == 2)
        assertTrue (aggOp.asList.contains ("herren", "Adidas", 18, 18, 18))
        assertTrue (aggOp.asList.contains ("damen", "Adidas", 20, 2, 13))
    }

    def testSmallEdgeTest() {

        // case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a: String, b: String, minCost: Int, avgCost: Double)
        val edges = new ObservableList[Edge]
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        //                                                                                                   aggragationConstructorFunc : (Key, AggregationValue) => Result)
        //val minAndAVGCost = new Aggregation(edges, (x : Edge) => { (x.a, x.b) }, (Min((x : Edge) => x.c), AVG((x : Edge) => x.c)), (x : (String, String), y : (Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2) })
        val minAndAVGCost = relationToResult (Aggregation (edges, (x: Edge) => {
            (x.a, x.b)
        }, (Min ((x: Edge) => x.c), AVG ((x: Edge) => x.c)), (x: (String, String), y: (Int, Double)) => {
            new EdgeGroup (x._1, x._2, y._1, y._2)
        }))

        edges.add (new Edge ("a", "b", 4))
        edges.add (new Edge ("a", "b", 2))
        edges.add (new Edge ("a", "b", 3))
        edges.add (new Edge ("d", "b", 2))
        edges.add (new Edge ("d", "b", 3))
        edges.add (new Edge ("c", "b", 2))
        assertTrue (minAndAVGCost.size == 3)
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("a", "b", 2, 3.0)))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("d", "b", 2, 2.5)))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("c", "b", 2, 2.0)))

    }

    def testSallEdgeTest2() {
        case class EdgeGroup(a: String, b: String, minCost: Int, count: Int, avgCost: Double)
        val edges = new ObservableList[Edge]
        val minAndAVGCost =
            relationToResult (
                Aggregation (edges, (e: Edge) => (e.a, e.b),
                    (Min ((x: Edge) => x.c), Count[Edge](), AVG ((x: Edge) => x.c)),
                    (x: (String, String), y: (Int, Int, Double)) => {
                        new EdgeGroup (x._1, x._2, y._1, y._2, y._3)
                    })
            )
        edges.add (new Edge ("a", "b", 4))
        edges.add (new Edge ("a", "b", 2))
        edges.add (new Edge ("a", "b", 3))
        edges.add (new Edge ("d", "b", 2))
        edges.add (new Edge ("d", "b", 3))
        edges.add (new Edge ("c", "b", 2))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("a", "b", 2, 3, 3.0)))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("d", "b", 2, 2, 2.5)))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("c", "b", 2, 1, 2.0)))
        assertTrue (minAndAVGCost.size == 3)
    }

    def testEdgeLazy() {
        //case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a: String, b: String, minCost: Int, count: Int, avgCost: Double)

        val edges = new ObservableList[Edge]

        val minAndAVGCost =
            relationToResult (
                Aggregation (edges, (e: Edge) => (e.a, e.b),
                    (Min ((x: Edge) => x.c), Count[Edge](), AVG ((x: Edge) => x.c)),
                    (x: (String, String), y: (Int, Int, Double)) => {
                        new EdgeGroup (x._1, x._2, y._1, y._2, y._3)
                    })
            )



        assertTrue (minAndAVGCost.size == 0)

        edges.add (new Edge ("a", "b", 4))
        edges.add (new Edge ("a", "b", 2))
        edges.add (new Edge ("a", "b", 3))
        edges.add (new Edge ("d", "b", 2))
        edges.add (new Edge ("d", "b", 3))
        edges.add (new Edge ("c", "b", 2))


        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("a", "b", 2, 3, 3.0)))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("d", "b", 2, 2, 2.5)))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("c", "b", 2, 1, 2.0)))
        assertTrue (minAndAVGCost.size == 3)
        edges.add (new Edge ("c", "b", 5))
        assertTrue (minAndAVGCost.asList.contains (EdgeGroup ("c", "b", 2, 2, 3.5)))
    }

    def testAggregationWithSelection() {
        import sae.syntax.RelationalAlgebraSyntax._
        case class EdgeGroup(a: String, b: String, count: Int)
        val edges = new ObservableList[Edge]

        val selection = σ ((x: Edge) => {
            x.a == "a" || x.a == "b"
        })(edges)

        val edgeStartingWithAOrB =
            relationToResult (
                Aggregation (selection, (e: Edge) => (e.a, e.b),
                    Count[Edge](),
                    (x: (String, String), y: Int) => {
                        new EdgeGroup (x._1, x._2, y)
                    })
            )
        val selection2 = σ ((x: EdgeGroup) => {
            x.count > 2
        })(edgeStartingWithAOrB)


        edges.add (new Edge ("a", "b", 4)) //ja
        assertTrue (selection2.size == 0)
        edges.add (new Edge ("a", "b", 2)) //ja
        assertTrue (selection2.size == 0)
        edges.add (new Edge ("a", "b", 3)) //ja
        assertTrue (selection2.size == 1)
        edges.add (new Edge ("d", "b", 2))
        edges.add (new Edge ("d", "b", 3))
        edges.add (new Edge ("c", "b", 2))
        assertTrue (edgeStartingWithAOrB.size == 1)
        assertTrue (edgeStartingWithAOrB.asList.contains (new EdgeGroup ("a", "b", 3)))
        assertTrue (selection2.size == 1)
        edges.add (new Edge ("b", "d", 2))
        assertTrue (edgeStartingWithAOrB.size == 2)
        assertTrue (edgeStartingWithAOrB.asList.contains (new EdgeGroup ("b", "d", 1)))
        edges.add (new Edge ("b", "d", 2))
        edges.add (new Edge ("b", "d", 2))
        assertTrue (selection2.size == 2)
    }


    def testDistinct() {
        case class EdgeGroup(a: String, b: String, count: Int)
        val edges = new ObservableList[Edge]

        import sae.syntax.RelationalAlgebraSyntax._
        val GroupAndCount = Aggregation (δ (Π ((e: Edge) => (e.a, e.b))(edges)),
            identity (_: (String, String)),
            Count[(String, String)](),
            (a: (String, String), b: Int) => (a, b))
        val res: QueryResult[((String, String), Int)] = GroupAndCount

        edges.add (new Edge ("a", "b", 4))
        assertEquals (1, res.asList.size)
        assertTrue (res.asList.contains (("a", "b"), 1))
        edges.add (new Edge ("a", "b", 2))
        assertEquals (1, res.asList.size)
        assertTrue (res.asList.contains (("a", "b"), 1))

        edges.add (new Edge ("a", "b", 3))
        assertEquals (1, res.asList.size)
        assertTrue (res.asList.contains (("a", "b"), 1))

        edges.add (new Edge ("d", "b", 2))
        assertEquals (2, res.asList.size)
        assertTrue (res.asList.contains (("d", "b"), 1))

        edges.add (new Edge ("d", "b", 3))
        assertEquals (2, res.asList.size)
        assertTrue (res.asList.contains (("d", "b"), 1))

        edges.add (new Edge ("c", "b", 2))
        assertEquals (3, res.asList.size)
        assertTrue (res.asList.contains (("c", "b"), 1))

        edges.add (new Edge ("b", "d", 2))
        assertEquals (4, res.asList.size)
        assertTrue (res.asList.contains (("b", "d"), 1))

        edges.add (new Edge ("b", "d", 2))
        assertEquals (4, res.asList.size)
        assertTrue (res.asList.contains (("b", "d"), 1))

        edges.add (new Edge ("b", "d", 2))
        assertEquals (4, res.asList.size)
        assertTrue (res.asList.contains (("b", "d"), 1))

    }

}
