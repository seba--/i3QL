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

  class Line(iD: String, itemType: String, preis: Integer) {
    def iD(): String = { this.iD }
    def itemType(): String = { this.itemType }
    def preis(): Integer = { this.preis }
    override def toString: String = {
      "ID: " + iD + " Type: " + itemType + " Preis " + preis
    }
  }
  // create empty data structure
  val allfileNames = new ObservableList[String];
  val allLines = new ObservableList[Line];
  // create query
  val allClassFileNames = from(allfileNames).where(_ endsWith ".class").select((fileName) => "File: " + fileName).as_view

  // attach another query... (e.g. a simple metric that counts all class files and which is incrementally maintained)
  val classFilesCount = count(allClassFileNames)

  //SELECT count(*) FROM allfileNames GROUB BY fileEnd
  val groupedByFileTypeView = new SimplifyObservable[String, Integer]
  val groupedByFileType: Observable[(String, Observable[Integer])] =
    from(allfileNames).
      group_by((fn) => { val i = fn.lastIndexOf('.'); fn.substring(i) }).
      per_group((V) => { new count(V) })
  groupedByFileType.addObserver(groupedByFileTypeView)

  //SELECT itemType, sum(Preis) FROM allLines GROUP BY allLines.itemType
  val simpleSumView = new SimplifyObservable[String, Tuple2[String, Int]]
  val groupLinesByType =
    from(allLines).
      group_by((ln) => { ln.itemType }).per_group((V) =>
        { new Sum(V, { (ln: Line) => { ln.preis } }, { (ln: Line, i: Int) => { new Tuple2(ln.itemType, i) } }) })
  groupLinesByType.addObserver(simpleSumView)

  //add some changeobserver
  //simpleSumView.addObserver(ChangeObserver("Sum"))
  //groupedByFileTypeView.addObserver(ChangeObserver("Group by"))

  val view = new ViewList(groupedByFileTypeView)
  val viewSum = new ViewList(simpleSumView)

  allLines.add(new Line("1", "TypeA", 15))
  allLines.add(new Line("1", "TypeB", 14))
  allLines.add(new Line("1", "TypeA", 15))
  allLines.add(new Line("1", "TypeC", 15))
  allLines.add(new Line("1", "TypeA", 8))
  allLines.remove(new Line("1", "TypeA", 8))
  allLines.remove(new Line("1", "TypeA", 15))
  allLines.remove(new Line("1", "TypeC", 15))
  allLines.update(new Line("1", "TypeB", 14), new Line("1", "TypeB", 27))

  println("\n--------------------------\n")
  viewSum.foreach(println)
  println("\n--------------------------\n")

  // add some data to the base list...
  allfileNames.add("a.class");
  allfileNames.add("b.class");
  allfileNames.add("c.class");
  allfileNames.add("a.zip");
  allfileNames.add("index.html");
  allfileNames.add("d.class");

  // let's see if the view is populated correctly..

  allClassFileNames.foreach(println);

  // we want to observe how our result view changes when the base list is updated...
  // allClassFileNames.addObserver(ChangeObserver("allClassFileNames"))
  //classFilesCount.addObserver(ChangeObserver("classFilesCount"))
  allfileNames.remove("c.class")
  allfileNames.add("e.html")
  allfileNames.add("test.class")
  allfileNames.add("test.class")
  allfileNames.add("test.class")
  allfileNames.add("test.class")
  allfileNames.remove("test.class")
  allfileNames.add("f.class")
  allfileNames.update("a.class", "aha.class")
  allfileNames.update("b.class", "b.class.old")
  allfileNames.update("a.zip", "a_zip.class")
  allfileNames.add("g.html")
  println
  println("---")
  allClassFileNames.foreach(println)
  println("---")
  //classFilesCount.foreach(println)
  //allClassFileNames.foreach(println)
  if (true) {
    view.foreach(println)
    println("\n--------------SOME TIME MEASURMENT------------\n")
    val lines = new Array[Line](100000)
    var i = 0
    scala.io.Source.fromFile("Daten.txt").getLines().foreach { line =>
      val lineArray = line.split(",")
      lines(i) = new Line(lineArray(0), lineArray(1), lineArray(2).toInt)
      i += 1
      //new Line(lineArray(0),lineArray(1),lineArray(2).toInt))    
    }
    //add 30k lines
    var t1 = java.lang.System.nanoTime
    println(t1)
    var idx = 0;
    lines.foreach(x => allLines.add(x))
    //lines.foreach(x => allLines.add(x))
    var t2 = java.lang.System.nanoTime
    println(java.lang.System.currentTimeMillis + " Dif: " + ((t2 - t1) / 1000000) + " milliseconds")
    viewSum.foreach(println)

    //remove 30k lines
    t1 = java.lang.System.nanoTime
    println(t1)
    lines.foreach(x => allLines.remove(x))
    t2 = java.lang.System.nanoTime
    println(java.lang.System.currentTimeMillis + " Dif: " + ((t2 - t1) / 1000000) + " milliseconds")
    viewSum.foreach(println)
  }
}