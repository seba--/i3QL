package sae.test
import scala.collection.mutable.ListBuffer
import sae.operators._
import sae.functions._
import junit.framework._
import org.junit.Assert._
import sae._
import sae.collections._
import sae.syntax._
import sae.operators.CreateAggregationFunctionContainer._

class Testaggregate extends TestCase {
    case class Schuh(val art : String, val name : String, val hersteller : String, val preis : Int, val groesse : Int)
    var schuhe = new ObservableList[Schuh];
    var aggOp : Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)] = new Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)](schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
    var list = new ObserverList[(String, String, Int, Int, Int)]
    val grouping = (x : Schuh) => { (x.art, x.hersteller) }
    class ObserverList[Domain <: AnyRef] extends Observer[Domain] {
        val data = ListBuffer[Domain]()

        def contains(x : Any) : Boolean = {
            data.contains(x)
        }
        def size() : Int = {
            data.size
        }
        def updated(oldV : Domain, newV : Domain) {
            data -= oldV
            data += newV
        }

        def removed(v : Domain) {
            data -= v
        }

        def added(v : Domain) {
            data += v
        }
        override def toString() : String = {
            data.toString()
        }
    }
    class ObservableList[V <: AnyRef] extends LazyView[V] {
        def lazy_foreach[T](f : (V) => T) {
            data.foreach(f)
        }
        //   protected var data = List[V]();
        var data = ListBuffer[V]();

        def add(k : V) {
            data += k
            element_added(k)
        }

        def remove(k : V) {
            //data = data.filterNot(_ eq k)
            data -= k
            element_removed(k)
        }

        def update(oldV : V, newV : V) {
            //data = newV +: (data.filterNot(_ eq oldV))
            data -= oldV
            data += newV
            element_updated(oldV, newV)
        }

        def foreach[T](f : (V) => T) {
            data.foreach(f)
        }

    }
    val aggFuncsFac = CreateAggregationFunctionContainer[Schuh, Int, Int, Int](
        Sum((x : Schuh) => x.preis),
        Min((x : Schuh) => x.preis),
        Max((x : Schuh) => x.preis))
    //before every method
    override def setUp() = {
        schuhe = new ObservableList[Schuh];
        //aggOp = new Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)](schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
        aggOp = new Aggregation(schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
        list = new ObserverList[(String, String, Int, Int, Int)]
        aggOp.addObserver(list);
    }
    //after every method
    override def tearDown() = {
        //  println("down!")
    }
    def testAdd() = {
        val list = new ObserverList[(String, String, Int, Int, Int)]

        aggOp.addObserver(list);
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))

        assertTrue(list.size == 1)
        assertTrue(list.contains("herren", "Adidas", 11, 11, 11))

        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))

        assertTrue(list.contains("herren", "Adidas", 11, 11, 11))
        assertTrue(list.contains("damen", "Adidas", 12, 12, 12))

        schuhe.add(new Schuh("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        assertTrue(list.size == 3)
        assertTrue(list.contains("herren", "Adidas", 11, 11, 11))
        assertTrue(list.contains("damen", "Adidas", 12, 12, 12))
        assertTrue(list.contains("damen", "Puma", 13, 13, 13))

        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        assertTrue(list.size == 4)
        assertTrue(list.contains("herren", "Puma", 15, 15, 15))

        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue(list.size == 4)
        assertTrue(list.contains("herren", "Adidas", 11, 11, 11))
        assertTrue(list.contains("damen", "Adidas", 12, 12, 12))
        assertTrue(list.contains("damen", "Puma", 13, 13, 13))
        assertTrue(list.contains("herren", "Puma", 15, 0, 15))
        //assertTrue(result.size == 5)

    }
    def testDelet() = {
        val list = new ObserverList[(String, String, Int, Int, Int)]
        aggOp.addObserver(list);
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue(list.size == 4)
        schuhe.remove(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue(list.size == 4)
        assertTrue(list.contains("damen", "Puma", 13, 13, 13))
        schuhe.remove(new Schuh("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        assertTrue(list.size == 3)
        assertFalse(list.contains("damen", "Puma", 13, 13, 13))
        //assertTrue(result.size == 5)
    }
    //oldKey = newKey
    def testUpdateCase1() = {
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("herren", "New GOODYEAR STREET M", "Adidas", 24, 12))
        assertTrue(list.size == 1)
        assertTrue(list.contains("herren", "Adidas", 35, 11, 24))
        schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("herren", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue(list.size == 1)
        assertTrue(list.contains("herren", "Adidas", 29, 5, 24))
        schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 5, 12), new Schuh("herren", "GOODYEAR STREET M", "Adidas", 30, 12))
        assertTrue(list.size == 1)
        assertTrue(list.contains("herren", "Adidas", 54, 24, 30))
    }
    //oldKey <> newkey and remove oldKey and add newKey
    def testUpdateCase2() = {
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        assertTrue(list.size == 1)
        assertTrue(list.contains("herren", "Adidas", 11, 11, 11))
        schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue(list.size == 1)
        assertTrue(list.contains("damen", "Adidas", 5, 5, 5))
    }
    //oldKey <> newkey and remove oldKey and update newKey
    def testUpdateCase3() = {
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET M", "Adidas", 13, 12))
        assertTrue(list.size == 2)
        assertTrue(list.contains("herren", "Adidas", 11, 11, 11))
        schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue(list.size == 1)
        assertTrue(list.contains("damen", "Adidas", 18, 5, 13))
    }
    //oldKey <> newkey and update oldKey and update newKey
    def testUpdateCase4() = {
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET M", "Adidas", 13, 12))
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 18, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET M", "Adidas", 2, 12))
        assertTrue(list.size == 2)
        assertTrue(list.contains("herren", "Adidas", 29, 11, 18))
        assertTrue(list.contains("damen", "Adidas", 15, 2, 13))
        schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
        assertTrue(list.size == 2)
        assertTrue(list.contains("herren", "Adidas", 18, 18, 18))
        assertTrue(list.contains("damen", "Adidas", 20, 2, 13))
    }
    def testSmallEdgeTest = {
        case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a : String, b : String, minCost : Int, avgCost : Double)
        val edges = new ObservableList[Edge]
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        //                                                                                                   aggragationConstructorFunc : (Key, AggregationValue) => Result)
        val minAndAVGCost = new Aggregation(edges, (x : Edge) => { (x.a, x.b) }, (Min((x : Edge) => x.c), AVG((x : Edge) => x.c)), (x : (String, String), y : (Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2) })
        val ob = new ObserverList[EdgeGroup];
        minAndAVGCost.addObserver(ob)
        edges.add(new Edge("a", "b", 4))
        edges.add(new Edge("a", "b", 2))
        edges.add(new Edge("a", "b", 3))
        edges.add(new Edge("d", "b", 2))
        edges.add(new Edge("d", "b", 3))
        edges.add(new Edge("c", "b", 2))
        assertTrue(ob.size == 3)
        assertTrue(ob.contains(EdgeGroup("a", "b", 2, 3.0)))
        assertTrue(ob.contains(EdgeGroup("d", "b", 2, 2.5)))
        assertTrue(ob.contains(EdgeGroup("c", "b", 2, 2.0)))

        //assertContains(ob,EdgeGroup(a,b,2,3.0))
        //assertSize(ob,3)

    }
    def testSallEdgeTest2 = {
        case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a : String, b : String, minCost : Int, count : Int, avgCost : Double)
        val edges = new ObservableList[Edge]
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        val minAndAVGCost = new Aggregation(edges, (e : Edge) => (e.a, e.b),
            (Min((x : Edge) => x.c), Count[Edge](), AVG((x : Edge) => x.c)),
            (x : (String, String), y : (Int, Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2, y._3) })
        val ob = new ObserverList[EdgeGroup];
        minAndAVGCost.addObserver(ob)
        edges.add(new Edge("a", "b", 4))
        edges.add(new Edge("a", "b", 2))
        edges.add(new Edge("a", "b", 3))
        edges.add(new Edge("d", "b", 2))
        edges.add(new Edge("d", "b", 3))
        edges.add(new Edge("c", "b", 2))
        assertTrue(ob.contains(EdgeGroup("a", "b", 2, 3, 3.0)))
        assertTrue(ob.contains(EdgeGroup("d", "b", 2, 2, 2.5)))
        assertTrue(ob.contains(EdgeGroup("c", "b", 2, 1, 2.0)))
        assertTrue(ob.size == 3)
    }
    def testEdgeLazy = {
        case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a : String, b : String, minCost : Int, count : Int, avgCost : Double)
        val edges = new ObservableList[Edge]
        edges.add(new Edge("a", "b", 4))
        edges.add(new Edge("a", "b", 2))
        edges.add(new Edge("a", "b", 3))
        edges.add(new Edge("d", "b", 2))
        edges.add(new Edge("d", "b", 3))
        edges.add(new Edge("c", "b", 2))
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        val minAndAVGCost = new Aggregation(edges, (e : Edge) => (e.a, e.b),
            (Min((x : Edge) => x.c), Count[Edge](), AVG((x : Edge) => x.c)),
            (x : (String, String), y : (Int, Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2, y._3) })
        val ob = new ObserverList[EdgeGroup];
        minAndAVGCost.addObserver(ob)
        assertTrue(ob.size == 0)
        var minAndAVGCostAsList = List[EdgeGroup]()
        minAndAVGCost.foreach((x : EdgeGroup) => {
            minAndAVGCostAsList = x :: minAndAVGCostAsList
        }) //x => minAndAVGCostAsList = x :: minAndAVGCostAsList)
        assertTrue(minAndAVGCostAsList.contains(EdgeGroup("a", "b", 2, 3, 3.0)))
        assertTrue(minAndAVGCostAsList.contains(EdgeGroup("d", "b", 2, 2, 2.5)))
        assertTrue(minAndAVGCostAsList.contains(EdgeGroup("c", "b", 2, 1, 2.0)))
        assertTrue(minAndAVGCostAsList.size == 3)
        edges.add(new Edge("c", "b", 5))
        assertTrue(ob.contains(EdgeGroup("c", "b", 2, 2, 3.5)))
    }

}