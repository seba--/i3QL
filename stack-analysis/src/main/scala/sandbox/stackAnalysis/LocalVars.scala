package sandbox.stackAnalysis

import sandbox.dataflowAnalysis.Combinable

/**
 * This class implements the store for local variables for the s analysis.
 *
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 25.10.12
 * Time: 14:10
 */

case class LocalVars[T,V](typeStore : Array[Option[T]] , varStore: Array[List[Option[V]]])(implicit m: Manifest[T]) extends Combinable[LocalVars[T,V]] {
  require(typeStore.length == varStore.length)

  def this(size : Int, t : Option[T], value : List[Option[V]])(implicit m: Manifest[T]) = this(Array.fill[Option[T]](size)(t), Array.fill[List[Option[V]]](size)(value))

  def setVar(index: Int, vSize: Int,  t : Option[T], value: Option[V]): LocalVars[T,V] = {
    var resV: Array[List[Option[V]]] = Array.ofDim[List[Option[V]]](varStore.length)
    var resT: Array[Option[T]] = Array.ofDim[Option[T]](varStore.length)
    System.arraycopy(varStore, 0, resV, 0, varStore.length)
    System.arraycopy(typeStore, 0, resT, 0, varStore.length)

    for (i <- 0 until vSize){
      resV.update(index + i, value :: Nil)
      resT.update(index + i, t)
    }

    new LocalVars[T,V](resT,resV)
  }

  def setVar(index: Int, vSize: Int, t : T, value: V): LocalVars[T,V] =
    setVar(index,vSize,Some(t),Some(value))

  def setVar(index: Int, t : T, value: V): LocalVars[T,V] =
    setVar(index,1,t,value)

  def length(): Int = {
    varStore.length
  }

  def combineWith(other: LocalVars[T,V]): LocalVars[T,V] = {
    if (other == null)
      this
    else {
      if(length != other.length)
        throw new IllegalArgumentException("Varstores need to have the same length")

      val resT: Array[Option[T]] = Array.ofDim[Option[T]](length)
      val resV: Array[List[Option[V]]] = Array.ofDim[List[Option[V]]](length)

      for (i <- 0 until length) {
        resT(i) = if(typeStore(i) == None) other.typeStore(i) else typeStore(i)
        resV(i) = CodeInfoTools.distinctAppend(varStore(i), other.varStore(i))
      }

      LocalVars[T,V](resT,resV)

    }


  }


  override def toString = {
    var stringList: List[String] = Nil
    for (l <- varStore) {
      var stringList2: List[String] = Nil
      for (o <- l)
        o match {
          case None => stringList2 = "None" :: stringList2
          case Some(x) => stringList2 = x.toString :: stringList2
        }
      stringList = stringList2.mkString("{", ",", "}") :: stringList
    }



    stringList.reverse.mkString("LV: [", "; ", "][" + typeStore.mkString("; ") + "]")
  }

  def equals(other: LocalVars[T,V]): Boolean = {
    if (length() != other.length())
      return false

    for (i <- 0 until length()) {
      if (!varStore(i).forall(other.varStore(i).contains))
        return false
      if (!(typeStore(i) equals other.typeStore(i)))
        return false
    }

    return true
  }

  private def listEquals[A](as : List[A] , bs : List[A]) : Boolean = {
    if(as.length != bs.length)
      return false

    for(a <- as) {
      if(!bs.contains(a))
        return false
    }

    return true

  }

}
