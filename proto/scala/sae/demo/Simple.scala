package sae
package demo

import sae.collections._
import sae.operators._
import sae.utils._
import scala.io.Source

/**
 * Assumptions:
 * - all input, output and intermediate data is immutable (only operators may contain internal mutable information)
 * - every function always produces the same output given the same input (sequence)
 */

object Simple extends App {
  case class Code(proj: String, pack: String, name: String, typ: String) {

  }
  class Line(iD: String, itemType: String, preis: Integer) {
    def iD(): String = { this.iD };
    def itemType(): String = { this.itemType };
    def preis(): Integer = { this.preis };
    override def toString: String = {
      "ID: " + iD + " Type: " + itemType + " Preis " + preis
    };
  };
  val allLines = new ObservableList[Line];

  //SELECT itemType, sum(Preis) FROM allLines GROUP BY allLines.itemType
  val simpleSumView = new SimplifyObservable[String, Tuple2[String, Int]];
  val groupLinesByType =
    from(allLines).
      group_by((ln) => { ln.itemType }).per_group((V) =>
        { new Sum(V, { (ln: Line) => { ln.preis } }, { (ln: Line, i: Int) => { new Tuple2(ln.itemType, i) } }) });
  groupLinesByType.addObserver(simpleSumView);

  //add some changeobserver
  //simpleSumView.addObserver(ChangeObserver("Sum"))
  //groupedByFileTypeView.addObserver(ChangeObserver("Group by"))

  val viewSum = new ViewList(simpleSumView);
  val testSumObs = new TestSumObserver[(String, Int)]
  viewSum.addObserver(testSumObs)
  allLines.add(new Line("1", "TypeA", 15));
  allLines.add(new Line("1", "TypeB", 14));
  allLines.add(new Line("1", "TypeA", 15));
  allLines.add(new Line("1", "TypeC", 15));
  allLines.add(new Line("1", "TypeA", 8));
  allLines.remove(new Line("1", "TypeA", 8));
  allLines.remove(new Line("1", "TypeA", 15));
  allLines.remove(new Line("1", "TypeC", 15));
  allLines.update(new Line("1", "TypeB", 14), new Line("1", "TypeB", 27));

  println("\n--------------------------\n");
  viewSum.foreach(println);
  println("\n--------------------------\n");

  //classFilesCount.foreach(println)
  //allClassFileNames.foreach(println)
  var a, b, c, d, e: Int = 0
  var count = 9
  b = 27
  a = 15
  if (true) {
    println("\n--------------SOME TIME MEASURMENT------------\n");
    val lines = new Array[Line](100000);
    var i = 0;
    scala.io.Source.fromFile("Daten_100k.txt").getLines().foreach { line =>
      val lineArray = line.split(",");
      lines(i) = new Line(lineArray(0), lineArray(1), lineArray(2).toInt);
      i += 1;
      //new Line(lineArray(0),lineArray(1),lineArray(2).toInt))    
    };
    //
    var t1 = java.lang.System.nanoTime;
    println(t1);
    var idx = 0;
    lines.foreach(x => {
      allLines.add(x)
/*
      if (x.preis > 0) {
        count += 1
        assert(testSumObs.getCount() == count)
        x.itemType match {
          case "TypeA" =>
            a += x.preis
            assert(testSumObs.getLast()._2 == a)
          case "TypeB" =>
            b += x.preis
            assert(testSumObs.getLast()._2 == b)
          case "TypeC" =>
            c += x.preis
            assert(testSumObs.getLast()._2 == c)
          case "TypeD" =>
            d += x.preis
            assert(testSumObs.getLast()._2 == d)
          case "TypeE" =>
            e += x.preis
            assert(testSumObs.getLast()._2 == e)
        }
      } else {
        assert(testSumObs.getCount == count)
      }
*/    });

    //lines.foreach(x => allLines.add(x))
    var t2 = java.lang.System.nanoTime;
    println(java.lang.System.currentTimeMillis + " Dif: " + ((t2 - t1) / 1000000) + " milliseconds");
    viewSum.foreach(println);

    //
    t1 = java.lang.System.nanoTime;
    println(t1);
    lines.foreach(x =>
      {
        allLines.remove(x)
        /*if (x.preis > 0) {
          count += 1
          assert(testSumObs.getCount() == count)
          x.itemType match {
            case "TypeA" =>
              a -= x.preis
              assert(testSumObs.getLast()._2 == a)
            case "TypeB" =>
              b -= x.preis
              assert(testSumObs.getLast()._2 == b)
            case "TypeC" =>
              c -= x.preis
              assert(testSumObs.getLast()._2 == c)
            case "TypeD" =>
              d -= x.preis
              assert(testSumObs.getLast()._2 == d)
            case "TypeE" =>
              e -= x.preis
              assert(testSumObs.getLast()._2 == e)
          }
        } else {
          assert(testSumObs.getCount == count)
        }
      }*/});

    t2 = java.lang.System.nanoTime;
    println(java.lang.System.currentTimeMillis + " Dif: " + ((t2 - t1) / 1000000) + " milliseconds");
    viewSum.foreach(println);
  }
}

