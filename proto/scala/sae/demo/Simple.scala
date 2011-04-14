package sae
package demo

import sae.collections._
import sae.operators._
import sae.utils._

/**
 * Assumptions:
 * - all input, output and intermediate data is immutable (only operators may contain internal mutable information)
 * - every function always produces the same output given the same input (sequence)
 */

object Simple extends App {

   // create empty data structure
   val allfileNames = new ObservableList[String];

   // create query
   val allClassFileNames = from(allfileNames).where(_ endsWith ".class").select((fileName) => "File: "+fileName).as_view

   // attach another query... (e.g. a simple metric that counts all class files and which is incrementally maintained)
   val classFilesCount = count(allClassFileNames)

//   val groupedByFileType : Observable[(String,Observable[java.lang.Integer])] = 
//		   from(allfileNames).
//		   group_by((fn) => { val i = fn.lastIndexOf('.'); fn.substring(i) }).
//		   per_group((V) => { new count(V) })
		 
//	val groupedByFileTypeView = new ObservableList[(String,java.lang.Integer)](){
//
//	   
//	   groupedByFileType addObserver (new Observer[(String,Observable[java.lang.Integer])] {
//	  	   def added(v : (String,Observable[java.lang.Integer])) {
//	  		   v._2.addObserver( new Observer[java.lang.Integer] {
//	  		  	   def added(v : java.lang.Integer) { throw new Error }
//	  		  	   def removed(v : java.lang.Integer) { throw new Error }
//	  		  	    def updated(oldV: java.lang.Integer, newV:java.lang.Integer) {
//	  		  	    	
//	  		  	    }
//	  		   })
//	  		   
//	  	   }
//	  	     
//	  	   def updated() {
//	  	  	   throw new Error
//	  	   }
//	   })
//	   
//	     
//   }

   // add some data to the base list...
   allfileNames.add("a.class");
   allfileNames.add("b.class");
   allfileNames.add("c.class");
   allfileNames.add("a.zip");
   allfileNames.add("index.html");
   allfileNames.add("d.class");

   // let's see if the view is populated correctly..
   allClassFileNames.foreach(println);
   println

   // we want to observe how our result view changes when the base list is updated...
   allClassFileNames.addObserver(ChangeObserver("allClassFileNames"))
   classFilesCount.addObserver(ChangeObserver("classFilesCount"))
   allfileNames.remove("c.class")
   allfileNames.add("e.html")
   allfileNames.add("f.class")
   allfileNames.update("a.class", "aha.class")
   allfileNames.update("b.class", "b.class.old")
   allfileNames.update("a.zip", "a_zip.class")   
   allfileNames.add("g.html")
   println

   classFilesCount.foreach(println)
   allClassFileNames.foreach(println)

}