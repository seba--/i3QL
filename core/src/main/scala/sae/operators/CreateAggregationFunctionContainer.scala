package sae.operators

import sae.operators.intern._

/**
 * A collection of implicit methods to create an AggregateFunctionFactory out of a TupleX(AggregateFunctionFactory, AggregateFunctionFactory, ...)
 * currently supported:
 *       NotSelfMaintainalbeAggregateFunctionFactory and  SelfMaintainalbeAggregateFunctionFactory as AggregatonFunctionFactory types
 *       and
 *       x = 2, x = 3 as arity of TupelX
 *
 *
 * @author Malte V
 */
object CreateAggregationFunctionContainer
{

    /**
     * the implicit methods call applyNotSelf/x if one of the parameters is a NotSelfMaintainalbeAggregateFunctionFactory
     * or applySelf/x if all parameters are SelfMaintainalbeAggregationFunctionFactories
     */


    implicit def AggregationFunctionsZipper3[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t: (NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value2])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2)
    }

    implicit def AggregationFunctionsZipper4[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t: (SelfMaintainalbeAggregateFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value2])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2)
    }

    implicit def AggregationFunctionsZipper5[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t: (NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value1], SelfMaintainalbeAggregateFunctionFactory[Domain, Value2])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2)
    }

    implicit def AggregationFunctionsZipper6[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t: (SelfMaintainalbeAggregateFunctionFactory[Domain, Value1], SelfMaintainalbeAggregateFunctionFactory[Domain, Value2])): SelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.applySelf(t._1, t._2)
    }

    implicit def AggregationFunctionsZipper7[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper8[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (SelfMaintainalbeAggregateFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper9[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value1], SelfMaintainalbeAggregateFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper10[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value2], SelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper11[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (SelfMaintainalbeAggregateFunctionFactory[Domain, Value1], SelfMaintainalbeAggregateFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper12[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (SelfMaintainalbeAggregateFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value2], SelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper13[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (NotSelfMaintainalbeAggregateFunctionFactory[Domain, Value1], SelfMaintainalbeAggregateFunctionFactory[Domain, Value2], SelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applyNotSelf(t._1, t._2, t._3)
    }

    implicit def AggregationFunctionsZipper14[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t: (SelfMaintainalbeAggregateFunctionFactory[Domain, Value1], SelfMaintainalbeAggregateFunctionFactory[Domain, Value2], SelfMaintainalbeAggregateFunctionFactory[Domain, Value3])): SelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.applySelf(t._1, t._2, t._3)
    }


    /**
     * the next methods get as parameters AggregationFunctions
     * and generate a "complex" XSelfMaintainalbeAggregationFunctionFactory
     */

    def applySelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Af1 <: AggregateFunction[Domain, Value1], Af2 <: AggregateFunction[Domain, Value2]](f1: AggregateFunctionFactory[Domain, Value1, Af1],
                                                                                                                                                      f2: AggregateFunctionFactory[Domain, Value2, Af2]): SelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)] = {
        new SelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)]
        {
            def apply(): SelfMaintainalbeAggregateFunction[Domain, (Value1, Value2)] = {
                new SelfMaintainalbeAggregateFunction[Domain, (Value1, Value2)]
                {
                    val func1 = f1()
                    val func2 = f2()

                    def add(newD: Domain) = {
                        (func1.add(newD, null), func2.add(newD, null))
                    }

                    def remove(newD: Domain) = {
                        (func1.remove(newD, null), func2.remove(newD, null))
                    }

                    def update(oldD: Domain, newD: Domain) = {
                        (func1.update(oldD, newD, null), func2.update(oldD, newD, null))
                    }
                }

            }

        }
    }

    def applySelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any, Af1 <: AggregateFunction[Domain, Value1], Af2 <: AggregateFunction[Domain, Value2], Af3 <: AggregateFunction[Domain, Value3]](f1: AggregateFunctionFactory[Domain, Value1, Af1],
                                                                                                                                                                                                               f2: AggregateFunctionFactory[Domain, Value2, Af2],
                                                                                                                                                                                                               f3: AggregateFunctionFactory[Domain, Value3, Af3]): SelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        new SelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)]
        {
            def apply(): SelfMaintainalbeAggregateFunction[Domain, (Value1, Value2, Value3)] = {
                new SelfMaintainalbeAggregateFunction[Domain, (Value1, Value2, Value3)]
                {
                    val func1 = f1()
                    val func2 = f2()
                    val func3 = f3()

                    def add(newD: Domain) = {
                        (func1.add(newD, null), func2.add(newD, null), func3.add(newD, null))
                    }

                    def remove(newD: Domain) = {
                        (func1.remove(newD, null), func2.remove(newD, null), func3.remove(newD, null))
                    }

                    def update(oldD: Domain, newD: Domain) = {
                        (func1.update(oldD, newD, null), func2.update(oldD, newD, null), func3.update(oldD, newD, null))
                    }
                }

            }

        }
    }

    def applyNotSelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Af1 <: AggregateFunction[Domain, Value1], Af2 <: AggregateFunction[Domain, Value2]](f1: AggregateFunctionFactory[Domain, Value1, Af1],
                                                                                                                                                         f2: AggregateFunctionFactory[Domain, Value2, Af2]): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)] = {
        new NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2)]
        {
            def apply(): NotSelfMaintainalbeAggregateFunction[Domain, (Value1, Value2)] = {
                new NotSelfMaintainalbeAggregateFunction[Domain, (Value1, Value2)]
                {
                    val func1 = f1()
                    val func2 = f2()

                    def add(newD: Domain, data: Iterable[Domain]) = {
                        (func1.add(newD, data), func2.add(newD, data))
                    }

                    def remove(newD: Domain, data: Iterable[Domain]) = {
                        (func1.remove(newD, data), func2.remove(newD, data))
                    }

                    def update(oldD: Domain, newD: Domain, data: Iterable[Domain]) = {
                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data))
                    }
                }

            }

        }
    }

    def applyNotSelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any, Af1 <: AggregateFunction[Domain, Value1], Af2 <: AggregateFunction[Domain, Value2], Af3 <: AggregateFunction[Domain, Value3]](f1: AggregateFunctionFactory[Domain, Value1, Af1],
                                                                                                                                                                                                                  f2: AggregateFunctionFactory[Domain, Value2, Af2],
                                                                                                                                                                                                                  f3: AggregateFunctionFactory[Domain, Value3, Af3]): NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        new NotSelfMaintainalbeAggregateFunctionFactory[Domain, (Value1, Value2, Value3)]
        {
            def apply(): NotSelfMaintainalbeAggregateFunction[Domain, (Value1, Value2, Value3)] = {
                new NotSelfMaintainalbeAggregateFunction[Domain, (Value1, Value2, Value3)]
                {
                    val func1 = f1()
                    val func2 = f2()
                    val func3 = f3()

                    def add(newD: Domain, data: Iterable[Domain]) = {
                        (func1.add(newD, data), func2.add(newD, data), func3.add(newD, data))
                    }

                    def remove(newD: Domain, data: Iterable[Domain]) = {
                        (func1.remove(newD, data), func2.remove(newD, data), func3.remove(newD, data))
                    }

                    def update(oldD: Domain, newD: Domain, data: Iterable[Domain]) = {
                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data), func3.update(oldD, newD, data))
                    }
                }

            }

        }
    }

}