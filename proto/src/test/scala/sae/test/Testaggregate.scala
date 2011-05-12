package sae.test

import junit.framework._
import org.junit.Assert._
import scala.collection.mutable.ListBuffer
import sae.operators._
import sae.functions._
import sae._
import sae.collections._
import sae.operators.CreateAggregationFunctionContainer._

class Testaggregate extends TestCase {
    class Line(iD : String, itemType : String, preis : Integer) {
        def iD() : String = { this.iD };
        def itemType() : String = { this.itemType };
        def preis() : Integer = { this.preis };
        override def toString : String = {
            "ID: " + iD + " Type: " + itemType + " Preis " + preis
        }
    }
    case class Edge(a : String, b : String, c : Int)
    case class Schuh(val art : String, val name : String, val hersteller : String, val preis : Int, val groesse : Int)
    var schuhe = new ObservableList[Schuh];
    //var aggOp : Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)] = new Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)](schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
    var aggOp : Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)] = Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)](schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
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
            foreach(f)
        }
        //   protected var data = List[V]();
        // var data = ListBuffer[V]();
        import com.google.common.collect.HashMultiset;
        val data : HashMultiset[V] = HashMultiset.create[V]()

        def add(k : V) {
            data.add(k) // += k
            element_added(k)
        }

        def remove(k : V) {
            //data = data.filterNot(_ eq k)
            data.remove(k) //data -= k
            element_removed(k)
        }

        def update(oldV : V, newV : V) {
            //data = newV +: (data.filterNot(_ eq oldV))
            data.remove(oldV)
            data.add(newV)
            //data -= oldV
            //data += newV
            element_updated(oldV, newV)
        }

        def foreach[T](f : (V) => T) {
            var x = data.iterator()
            while (x.hasNext()) {
                f(x.next)
            }
            //data.foreach(f)
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
        //aggOp = new Aggregation(schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
        aggOp = Aggregation(schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
        list = new ObserverList[(String, String, Int, Int, Int)]
        aggOp.addObserver(list);
    }
    //after every method
    override def tearDown() = {
        //  println("down!")
    }
    def testAggregationWithoutGrouping() = {
        var sum = Aggregation(schuhe, Sum((x : Schuh) => x.preis))
        val list = new ObserverList[Some[Int]]
        sum.addObserver(list);
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertTrue(list.size == 1)
        assertTrue(list.contains(Some(51)))

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
    def testAddMultyValue() = {
        val list = new ObserverList[(String, String, Int, Int, Int)]

        aggOp.addObserver(list);
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
        schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))

        assertTrue(list.size == 1)
        assertTrue(list.contains("herren", "Adidas", 66, 11, 11))

        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))

        assertTrue(list.contains("herren", "Adidas", 66, 11, 11))
        assertTrue(list.contains("damen", "Adidas", 120, 12, 12))

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

        // case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a : String, b : String, minCost : Int, avgCost : Double)
        val edges = new ObservableList[Edge]
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        //                                                                                                   aggragationConstructorFunc : (Key, AggregationValue) => Result)
        //val minAndAVGCost = new Aggregation(edges, (x : Edge) => { (x.a, x.b) }, (Min((x : Edge) => x.c), AVG((x : Edge) => x.c)), (x : (String, String), y : (Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2) })
        val minAndAVGCost = Aggregation(edges, (x : Edge) => { (x.a, x.b) }, (Min((x : Edge) => x.c), AVG((x : Edge) => x.c)), (x : (String, String), y : (Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2) })
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
        //        case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a : String, b : String, minCost : Int, count : Int, avgCost : Double)
        // case class Edge(a : String, b : String, c : Int)
        //case class EdgeGroup(a : String, b : String, minCost : Int, count : Int, avgCost : Double)
        val edges = new ObservableList[Edge]
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        val minAndAVGCost = Aggregation(edges, (e : Edge) => (e.a, e.b),
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
        //case class Edge(a : String, b : String, c : Int)
        case class EdgeGroup(a : String, b : String, minCost : Int, count : Int, avgCost : Double)
        val edges = new ObservableList[Edge]
        edges.add(new Edge("a", "b", 4))
        edges.add(new Edge("a", "b", 2))
        edges.add(new Edge("a", "b", 3))
        edges.add(new Edge("d", "b", 2))
        edges.add(new Edge("d", "b", 3))
        edges.add(new Edge("c", "b", 2))
        //[Domain <: AnyRef, Key <: AnyRef, AggregationValue <: AnyRef, Result <: AnyRef](val source : Observable[Domain], val groupFunction : Domain => Key, aggregationFuncFactory : AggregationFunktionFactory[Domain, AggregationValue],
        val minAndAVGCost = Aggregation(edges, (e : Edge) => (e.a, e.b),
            (Min((x : Edge) => x.c), Count[Edge](), AVG((x : Edge) => x.c)),
            (x : (String, String), y : (Int, Int, Double)) => { new EdgeGroup(x._1, x._2, y._1, y._2, y._3) })
        val ob = new ObserverList[EdgeGroup];
        minAndAVGCost.addObserver(ob)
        assertTrue(ob.size == 0)
        var minAndAVGCostAsList = List[EdgeGroup]()
        minAndAVGCost.asInstanceOf[MaterializedView[EdgeGroup]].foreach((x : EdgeGroup) => {
            minAndAVGCostAsList = x :: minAndAVGCostAsList
        }) //x => minAndAVGCostAsList = x :: minAndAVGCostAsList)
        assertTrue(minAndAVGCostAsList.contains(EdgeGroup("a", "b", 2, 3, 3.0)))
        assertTrue(minAndAVGCostAsList.contains(EdgeGroup("d", "b", 2, 2, 2.5)))
        assertTrue(minAndAVGCostAsList.contains(EdgeGroup("c", "b", 2, 1, 2.0)))
        assertTrue(minAndAVGCostAsList.size == 3)
        edges.add(new Edge("c", "b", 5))
        assertTrue(ob.contains(EdgeGroup("c", "b", 2, 2, 3.5)))
    }

    def testAggregationWithSelection() = {

        case class EdgeGroup(a : String, b : String, count : Int)
        val edges = new ObservableList[Edge]

        val selection = new LazySelection[Edge]((x : Edge) => { x.a == "a" || x.a == "b" }, edges)

        val minAndAVGCost = Aggregation(selection, (e : Edge) => (e.a, e.b),
            Count[Edge](),
            (x : (String, String), y : Int) => { new EdgeGroup(x._1, x._2, y) })
        val selection2 = new MaterializedSelection[EdgeGroup]((x : EdgeGroup) => { x.count > 2 }, minAndAVGCost)
        val op = new ObserverList[EdgeGroup]()
        minAndAVGCost.addObserver(op)
        edges.add(new Edge("a", "b", 4)) //ja
        assertTrue(selection2.materialized_size == 0)
        edges.add(new Edge("a", "b", 2)) //ja
        assertTrue(selection2.materialized_size == 0)
        edges.add(new Edge("a", "b", 3)) //ja
        assertTrue(selection2.materialized_size == 1)
        edges.add(new Edge("d", "b", 2))
        edges.add(new Edge("d", "b", 3))
        edges.add(new Edge("c", "b", 2))
        assertTrue(op.size == 1)
        assertTrue(op.contains(new EdgeGroup("a", "b", 3)))
        assertTrue(selection2.materialized_size == 1)
        edges.add(new Edge("b", "d", 2))
        assertTrue(op.size == 2)
        assertTrue(op.contains(new EdgeGroup("b", "d", 1)))
        edges.add(new Edge("b", "d", 2))
        edges.add(new Edge("b", "d", 2))
        assertTrue(selection2.materialized_size == 2)
    }
    private def readFile(fileName : String, size : Int) : Array[Line] = {
        val lines = new Array[Line](size);
        var i = 0;
        scala.io.Source.fromFile(fileName).getLines().foreach { line =>
            val lineArray = line.split(",")
            lines(i) = new Line(lineArray(0), lineArray(1), lineArray(2).toInt)
            i += 1
            //new Line(lineArray(0),lineArray(1),lineArray(2).toInt))    
        }
        lines
    }
    def testBigAdd100() = {

        val allLines = new LazyView[Line] {
            def add(k : Line) {
                element_added(k)
            }
            def remove(k : Line) {
                element_removed(k)
            }
            def update(oldV : Line, newV : Line) {
                element_updated(oldV, newV)
            }
            def lazy_foreach[T](f : (Line) => T) {
                //throw new Error
            }
        }
        val lines = readFile("Daten.txt", 1000000)
        val lines100 = readFile("100.txt", 100)
        val sumCost = Aggregation(allLines, (x : Line) => x.itemType, Sum((x : Line) => x.preis), (x : String, y : Int) => new Tuple2(x, y))
        val obs = new ObserverList[(String, Int)]()
        sumCost.addObserver(obs)

        lines.foreach(x => {
            allLines.add(x)
        })

        var t1 = java.lang.System.nanoTime;
        lines100.foreach(x => {
            allLines.add(x)
            //allLines.add(x)
        })
        var t2 = java.lang.System.nanoTime;
        println("add 100 lines in a 1000k Table: " + ((t2 - t1) / 1000000) + " milliseconds");
        //TODO add asserts
        /*  assertTrue(obs.contains(("TypeC", 498675626)))
        assertTrue(obs.contains(("TypeE", 502994963)))
        assertTrue(obs.contains(("TypeB", 497639747)))
        assertTrue(obs.contains(("TypeD", 498864034)))
        assertTrue(obs.contains(("TypeA", 501896644)))
        assertTrue(obs.size == 5)
*/

    }
    def testBigRemove100() = {

        val allLines = new LazyView[Line] {
            def add(k : Line) {
                element_added(k)
            }
            def remove(k : Line) {
                element_removed(k)
            }
            def update(oldV : Line, newV : Line) {
                element_updated(oldV, newV)
            }
            def lazy_foreach[T](f : (Line) => T) {
                //throw new Error
            }
        }
        val lines = readFile("Daten.txt", 1000000)
        val lines100 = readFile("100.txt", 100)
        val sumCost = Aggregation(allLines, (x : Line) => x.itemType, Sum((x : Line) => x.preis), (x : String, y : Int) => new Tuple2(x, y))
        val obs = new ObserverList[(String, Int)]()
        sumCost.addObserver(obs)

        lines.foreach(x => {
            allLines.add(x)
        })
        //obs.data.foreach(println _)
        var t1 = java.lang.System.nanoTime;
        lines100.foreach(x => {
            allLines.remove(x)
            //allLines.remove(x)
        })
        var t2 = java.lang.System.nanoTime;
        //obs.data.foreach(println _)
        println("remove 100 lines in a 1000k Table: " + ((t2 - t1) / 1000000) + " milliseconds");
        //TODO add asserts
        /*  assertTrue(obs.contains(("TypeC", 498675626)))
        assertTrue(obs.contains(("TypeE", 502994963)))
        assertTrue(obs.contains(("TypeB", 497639747)))
        assertTrue(obs.contains(("TypeD", 498864034)))
        assertTrue(obs.contains(("TypeA", 501896644)))
        assertTrue(obs.size == 5)
*/

    }
    def testBigUpdate100() = {

        val allLines = new LazyView[Line] {
            def add(k : Line) {
                element_added(k)
            }
            def remove(k : Line) {
                element_removed(k)
            }
            def update(oldV : Line, newV : Line) {
                element_updated(oldV, newV)
            }
            def lazy_foreach[T](f : (Line) => T) {
                //throw new Error
            }
        };
        val lines = readFile("Daten.txt", 1000000)
        val lines100 = readFile("100.txt", 100)
        val lines100Daten = readFile("100Daten.txt", 100)
        val sumCost = Aggregation(allLines, (x : Line) => x.itemType, Sum((x : Line) => x.preis), (x : String, y : Int) => new Tuple2(x, y))
        val obs = new ObserverList[(String, Int)]()
        sumCost.addObserver(obs)

        lines.foreach(x => {
            allLines.add(x)
        })

        var i = 0
        //obs.data.foreach(println _)
        var t1 = java.lang.System.nanoTime;
        lines100.foreach(x => {
            allLines.update(x, lines100Daten(i))
            //allLines.update(x, lines100Daten(i))
            i += 1
        })
        var t2 = java.lang.System.nanoTime;
        //obs.data.foreach(println _)
        println("updates 100 lines in a 1000k Table: " + ((t2 - t1) / 1000000) + " milliseconds");
        //TODO add asserts
        /*  assertTrue(obs.contains(("TypeC", 498675626)))
        assertTrue(obs.contains(("TypeE", 502994963)))
        assertTrue(obs.contains(("TypeB", 497639747)))
        assertTrue(obs.contains(("TypeD", 498864034)))
        assertTrue(obs.contains(("TypeA", 501896644)))
        assertTrue(obs.size == 5)
*/

    }

    def test100kAddsAndRemoves() = {

        val allLines = new LazyView[Line] {
            def add(k : Line) {
                element_added(k)
            }
            def remove(k : Line) {
                element_removed(k)
            }
            def update(oldV : Line, newV : Line) {
                element_updated(oldV, newV)
            }
            def lazy_foreach[T](f : (Line) => T) {
                //throw new Error
            }
        }
        val lines = readFile("Daten_100k.txt", 100000)
        val sumCost = Aggregation(allLines, (x : Line) => x.itemType, Sum((x : Line) => x.preis), (x : String, y : Int) => new Tuple2(x, y))
        val obs = new ObserverList[(String, Int)]()
        sumCost.addObserver(obs)
        //

        var t1 = java.lang.System.nanoTime;
        //println(t1);
        var idx = 0
        var count = 0

        lines.foreach(x => {
            allLines.add(x)
        })
        var t2 = java.lang.System.nanoTime;
        ////obs.data.foreach(println _)
        println("100k Adds Dif: " + ((t2 - t1) / 1000000) + " milliseconds");

        assertTrue(obs.contains(("TypeC", 49994539)))
        assertTrue(obs.contains(("TypeE", 50357309)))
        assertTrue(obs.contains(("TypeB", 50527562)))
        assertTrue(obs.contains(("TypeD", 49389381)))
        assertTrue(obs.contains(("TypeA", 49526049)))
        assertTrue(obs.size == 5)
        //
        t1 = java.lang.System.nanoTime;
        //println(t1);
        lines.foreach(x =>
            {
                allLines.remove(x)
            });

        t2 = java.lang.System.nanoTime;
        println("100k removes Dif: " + ((t2 - t1) / 1000000) + " milliseconds");
        assertTrue(obs.size == 0)

    }

    def test1000kAddsAndRemoves() = {

        val allLines = new LazyView[Line] {
            def add(k : Line) {
                element_added(k)
            }
            def remove(k : Line) {
                element_removed(k)
            }
            def update(oldV : Line, newV : Line) {
                element_updated(oldV, newV)
            }
            def lazy_foreach[T](f : (Line) => T) {
            }
        }
        val lines = readFile("Daten.txt", 1000000)
        val sumCost = Aggregation(allLines, (x : Line) => x.itemType, Sum((x : Line) => x.preis), (x : String, y : Int) => new Tuple2(x, y))
        val obs = new ObserverList[(String, Int)]()
        sumCost.addObserver(obs)
        var t1 = java.lang.System.nanoTime;
        lines.foreach(x => {
            allLines.add(x)
        })
        var t2 = java.lang.System.nanoTime;
        println("1000k adds Dif: " + ((t2 - t1) / 1000000) + " milliseconds");

        assertTrue(obs.contains(("TypeC", 498675626)))
        assertTrue(obs.contains(("TypeE", 502994963)))
        assertTrue(obs.contains(("TypeB", 497639747)))
        assertTrue(obs.contains(("TypeD", 498864034)))
        assertTrue(obs.contains(("TypeA", 501896644)))
        assertTrue(obs.size == 5)

        t1 = java.lang.System.nanoTime;
        lines.foreach(x =>
            {
                allLines.remove(x)
            });

        t2 = java.lang.System.nanoTime;
        println("1000k removes Dif: " + ((t2 - t1) / 1000000) + " milliseconds");
        assertTrue(obs.size == 0)

    }

}