package sae
package utils

case class ChangeObserver(val viewName: String = null) extends Observer[AnyRef] {

   def updated(oldV: AnyRef, newV: AnyRef) {
      if (!(viewName eq null))
         print(viewName + " - ");
      println("Updated: " + oldV + " -> " + newV)
   }

   def removed(v: AnyRef) {
      if (!(viewName eq null))
         print(viewName + " - ");
      println("Removed: " + v)
   }

   def added(v: AnyRef) {
      if (!(viewName eq null))
         print(viewName + " - ");
      println("Added:" + v)
   }

}

