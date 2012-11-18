package sandbox.stackAnalysis


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 13.11.12
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */
case class Stacks[T, V](maxSize: Int, size: List[Int], types: List[T], stacks: List[List[V]]) {

  def getSize: Int = {
    var res = 0

    for (i <- size)
      res = res + i

    return res
  }

  def jDup(offset: Int): Stacks[T, V] = {
    if (size.length <= offset)
      return this
    if (getSize + size.head > maxSize)
      return this

    var res: List[List[V]] = Nil
    for (s <- stacks) {
      res = (s.take(offset) ++ (s.head :: s.drop(offset))) :: res
    }

    return Stacks(maxSize, size.take(offset) ++ (size.head :: size.drop(offset)), types.take(offset) ++ (types.head :: types.drop(offset)), res)
  }

  def jPop(amount: Int): Stacks[T, V] = {
    if (size == Nil)
      return this
    else if (amount <= 0)
      return this
    else {
      var res: List[List[V]] = Nil
      for (s <- stacks) {
        res = (s.tail) :: res
      }

      return Stacks[T, V](maxSize, size.tail, types.tail, res).jPop(amount - size.head)
    }
  }

  def jSwap() : Stacks[T,V] = {
    if (getSize < 2)
      return this
    else {
      var res: List[List[V]] = Nil
      for (s <- stacks) {
        res = (s(1) :: s.head :: s.drop(2)) :: res
      }

      return Stacks[T, V](maxSize, size(1) :: size.head :: size.drop(2), types(1) :: types.head :: types.drop(2), res)
    }
  }

  def push(tSize: Int, t: T, value: V): Stacks[T, V] = {
    if (getSize + tSize > maxSize)
      return this

    var res: List[List[V]] = Nil
    for (s <- stacks) {
      res = (value :: s) :: res
    }

    return Stacks[T, V](maxSize, (tSize :: size), (t :: types), res)
  }

  def push(t: T, value: V): Stacks[T, V] =
    push(1, t, value)

  def pop(): Stacks[T, V] = {
    if (size == Nil)
      return this

    var res: List[List[V]] = Nil
    for (s <- stacks) {
      res = (s.tail) :: res
    }

    return Stacks[T, V](maxSize, size.tail, types.tail, res)
  }


  def combineWith(other: Stacks[T, V]): Stacks[T, V] = {
    if (other.maxSize != maxSize)
      throw new IllegalArgumentException("The attribute maxSize needs to be the same.")
    else if (other.size != size)
      throw new IllegalArgumentException("The attribute size needs to be the same.")
    else if (other.types != types)
      throw new IllegalArgumentException("The attribute types needs to be the same.")

    Stacks[T, V](maxSize, size, types, CodeInfoTools.distinctAppend(stacks, other.stacks))
  }

  override def toString(): String =
    stacks.mkString("{", ";", "}")

}
