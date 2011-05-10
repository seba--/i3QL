package sae.test
import scala.collection.mutable.ListBuffer
import sae.operators._
import sae.functions._
import junit.framework._
import org.junit.Assert._
import sae._
import sae.collections._
import sae.syntax._

class Testaggregate extends TestCase {
    case class Schuh(val art : String, val name : String, val hersteller : String, val preis : Int, val groesse : Int)
    var schuhe = new ObservableList[Schuh];
    var aggOp : Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)] = new Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)](schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
    var list = new ObserverList[(String, String, Int, Int, Int)]
       
    val assertContainsNot = (x : ObserverList[(String, String, Int, Int, Int)], y : Any) => {
        if(x.data.contains(y)){
            println(x + " -- " +y)
            fail
        }     
    }
    val assertSize = (x : ObserverList[(String, String, Int, Int, Int)], y : Int) => {
        if(x.data.size == y){
            
        }else{
            println(x+ " -- " +y)
            fail
        }
    }
    val assertContains = (x : ObserverList[(String, String, Int, Int, Int)], y : Any) => {
        if(!(x.data.contains(y))){
            println(x+ " -- " +y)
            fail
        }
    }
    val grouping = (x : Schuh) => { (x.art, x.hersteller) }
    class ObserverList[Domain <: AnyRef] extends Observer[Domain] {
        val data = ListBuffer[Domain]()

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
    val aggFuncsFac = CreateAggregationFunctionContainer[Schuh, Int, Int, Int](
            Sum( (x : Schuh) => x.preis),
            Min( (x : Schuh) => x.preis),
            Max( (x : Schuh) => x.preis))
    //before every method
    override def setUp() = {
        schuhe = new ObservableList[Schuh];
        aggOp = new Aggregation[Schuh, (String, String), (Int, Int, Int), (String, String, Int, Int, Int)](schuhe, grouping, aggFuncsFac, ((key, aggV) => (key._1, key._2, aggV._1, aggV._2, aggV._3)))
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

        assertSize(list, 1)
        assertContains(list, ("herren", "Adidas", 11, 11, 11))

        schuhe.add(new Schuh("damen", "GOODYEAR STREET W", "Adidas", 12, 12))
        assertSize(list, 2)
        assertContains(list, ("herren", "Adidas", 11, 11, 11))
        assertContains(list, ("damen", "Adidas", 12, 12, 12))

        schuhe.add(new Schuh("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        assertSize(list, 3)
        assertContains(list, ("herren", "Adidas", 11, 11, 11))
        assertContains(list, ("damen", "Adidas", 12, 12, 12))
        assertContains(list, ("damen", "Puma", 13, 13, 13))

        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 15, 12))
        assertSize(list, 4)
        assertContains(list, ("herren", "Puma", 15, 15, 15))

        schuhe.add(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertSize(list, 4)
        assertContains(list, ("herren", "Adidas", 11, 11, 11))
        assertContains(list, ("damen", "Adidas", 12, 12, 12))
        assertContains(list, ("damen", "Puma", 13, 13, 13))
        assertContains(list, ("herren", "Puma", 15, 0, 15))
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
        assertSize(list, 4)
        schuhe.remove(new Schuh("herren", "Speed Cat Gloss  M", "Puma", 0, 0))
        assertSize(list, 4)
        assertContains(list, ("damen", "Puma", 13, 13, 13))
        schuhe.remove(new Schuh("damen", "Speed Cat Gloss  W", "Puma", 13, 12))
        assertSize(list, 3)
        assertContainsNot(list, ("damen", "Puma", 13, 13, 13))
        //assertTrue(result.size == 5)
    }
    //oldKey = newKey
    def testUpdateCase1() = {
    	schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
    	schuhe.add(new Schuh("herren", "New GOODYEAR STREET M", "Adidas", 24, 12))
    	assertSize(list, 1)
    	assertContains(list, ("herren", "Adidas", 35, 11, 24))
    	schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("herren", "GOODYEAR STREET M", "Adidas", 5, 12))
    	assertSize(list, 1)
    	assertContains(list, ("herren", "Adidas", 29, 5, 24))
    	schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 5, 12), new Schuh("herren", "GOODYEAR STREET M", "Adidas", 30, 12))
    	assertSize(list, 1)
    	assertContains(list, ("herren", "Adidas", 54, 24, 30))
    }
    //oldKey <> newkey and remove oldKey and add newKey
    def testUpdateCase2() = {
    	schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
    	assertSize(list, 1)
    	assertContains(list, ("herren", "Adidas", 11, 11, 11))
    	schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
    	assertSize(list, 1)
    	assertContains(list, ("damen", "Adidas", 5, 5, 5))
    }
    //oldKey <> newkey and remove oldKey and update newKey
    def testUpdateCase3() = {
    	schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
    	schuhe.add(new Schuh("damen", "GOODYEAR STREET M", "Adidas", 13, 12))
    	assertSize(list, 2)
    	assertContains(list, ("herren", "Adidas", 11, 11, 11))
    	schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
    	assertSize(list, 1)
    	assertContains(list, ("damen", "Adidas", 18, 5, 13))
    }
    //oldKey <> newkey and update oldKey and update newKey
    def testUpdateCase4() = {
    	schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12))
    	schuhe.add(new Schuh("damen", "GOODYEAR STREET M", "Adidas", 13, 12))
    	schuhe.add(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 18, 12))
    	schuhe.add(new Schuh("damen", "GOODYEAR STREET M", "Adidas", 2, 12))
    	assertSize(list, 2)
    	assertContains(list, ("herren", "Adidas", 29, 11, 18))
    	assertContains(list, ("damen", "Adidas", 15, 2, 13))
    	schuhe.update(new Schuh("herren", "GOODYEAR STREET M", "Adidas", 11, 12), new Schuh("damen", "GOODYEAR STREET M", "Adidas", 5, 12))
    	assertSize(list, 2)
    	assertContains(list, ("herren", "Adidas", 18, 18, 18))
    	assertContains(list, ("damen", "Adidas", 20, 2, 13))
    }
    def test12 = {

    }

}