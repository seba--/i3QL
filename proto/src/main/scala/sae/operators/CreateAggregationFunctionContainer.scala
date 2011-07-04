package sae.operators
import sae.operators.intern._

/**
 * A collection of implicit methods to create an AggregationFunctionFactory out of a TupleX(AggregationFunctionFactory, AggregationFunctionFactory, ...)
 * currently supported:
 *       NotSelfMaintainalbeAggregationFunctionFactory and  SelfMaintainalbeAggregationFunctionFactory as AggregatonFunctionFactory types
 *       and
 *       x = 2, x = 3 as arity of TupelX
 *
 *
 * @author Malte V
 */
object CreateAggregationFunctionContainer {

  /**
   * the implicit methods call applyNotSelf/x if one of the parameters is a NotSelfMaintainalbeAggregationFunctionFactory
   * or applySelf/x if all parameters are SelfMaintainalbeAggregationFunctionFactories
   */


  implicit def AggregationFunctionsZipper3[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2)
  }
  implicit def AggregationFunctionsZipper4[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2)
  }
  implicit def AggregationFunctionsZipper5[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2)
  }
  implicit def AggregationFunctionsZipper6[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
      CreateAggregationFunctionContainer.applySelf(t._1, t._2)
  }
  implicit def AggregationFunctionsZipper7[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper8[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper9[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper10[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper11[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper12[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper13[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
  }
  implicit def AggregationFunctionsZipper14[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      CreateAggregationFunctionContainer.applySelf(t._1, t._2, t._3)
  }


  /**
   * the next methods get as parameters AggregationFunctions
   * and generate a "complex" XSelfMaintainalbeAggregationFunctionFactory
   */

  def applySelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any,Af1 <: AggregationFunction[Domain,Value1],Af2 <: AggregationFunction[Domain,Value2]](f1 : AggregationFunctionFactory[Domain, Value1, Af1], f2 : AggregationFunctionFactory[Domain, Value2,Af2 ]) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
      new SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] {
          def apply() : SelfMaintainalbeAggregationFunction[Domain, (Value1, Value2)] = {
              new SelfMaintainalbeAggregationFunction[Domain, (Value1, Value2)] {
                  val func1 = f1()
                  val func2 = f2()

                  def add(newD : Domain) = {
                      (func1.add(newD, null), func2.add(newD, null))
                  }
                  def remove(newD : Domain) = {
                      (func1.remove(newD, null), func2.remove(newD, null))
                  }
                  def update(oldD : Domain, newD : Domain) = {
                      (func1.update(oldD, newD, null), func2.update(oldD, newD, null))
                  }
              }

          }

      }
  }
  def applySelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any,Af1 <: AggregationFunction[Domain,Value1],Af2 <: AggregationFunction[Domain,Value2],Af3 <: AggregationFunction[Domain,Value3]](f1 : AggregationFunctionFactory[Domain, Value1, Af1], f2 : AggregationFunctionFactory[Domain, Value2, Af2], f3 : AggregationFunctionFactory[Domain, Value3, Af3]) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      new SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] {
          def apply() : SelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] = {
              new SelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] {
                  val func1 = f1()
                  val func2 = f2()
                  val func3 = f3()

                  def add(newD : Domain) = {
                      (func1.add(newD, null), func2.add(newD, null), func3.add(newD, null))
                  }
                  def remove(newD : Domain) = {
                      (func1.remove(newD, null), func2.remove(newD, null), func3.remove(newD, null))
                  }
                  def update(oldD : Domain, newD : Domain) = {
                      (func1.update(oldD, newD, null), func2.update(oldD, newD, null), func3.update(oldD, newD, null))
                  }
              }

          }

      }
  }

  def applyNotSelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any,Af1 <: AggregationFunction[Domain,Value1],Af2 <: AggregationFunction[Domain,Value2]](f1 : AggregationFunctionFactory[Domain, Value1, Af1], f2 : AggregationFunctionFactory[Domain, Value2,Af2]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {      new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] {
          def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2)] = {
              new NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2)] {
                  val func1 = f1()
                  val func2 = f2()

                  def add(newD : Domain, data : Iterable[Domain]) = {
                      (func1.add(newD, data), func2.add(newD, data))
                  }
                  def remove(newD : Domain, data : Iterable[Domain]) = {
                      (func1.remove(newD, data), func2.remove(newD, data))
                  }
                  def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
                      (func1.update(oldD, newD, data), func2.update(oldD, newD, data))
                  }
              }

          }

      }
  }

  def applyNotSelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any,Af1 <: AggregationFunction[Domain,Value1],Af2 <: AggregationFunction[Domain,Value2],Af3 <: AggregationFunction[Domain,Value3]](f1 : AggregationFunctionFactory[Domain, Value1,Af1], f2 : AggregationFunctionFactory[Domain, Value2,Af2], f3 : AggregationFunctionFactory[Domain, Value3,Af3]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
      new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] {
          def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] = {
              new NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] {
                  val func1 = f1()
                  val func2 = f2()
                  val func3 = f3()

                  def add(newD : Domain, data : Iterable[Domain]) = {
                      (func1.add(newD, data), func2.add(newD, data), func3.add(newD, data))
                  }
                  def remove(newD : Domain, data : Iterable[Domain]) = {
                      (func1.remove(newD, data), func2.remove(newD, data), func3.remove(newD, data))
                  }
                  def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
                      (func1.update(oldD, newD, data), func2.update(oldD, newD, data), func3.update(oldD, newD, data))
                  }
              }

          }

      }
  }

}