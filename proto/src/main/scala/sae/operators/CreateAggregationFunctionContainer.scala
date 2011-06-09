package sae.operators
import sae.operators.intern._

// TODO comment on the functions
object CreateAggregationFunctionContainer {

    implicit def AggregationFunctionsZipper1[Domain <: AnyRef, Value1 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1] = {
        CreateAggregationFunctionContainer.applyNotSelf(t)
    }
    implicit def AggregationFunctionsZipper2[Domain <: AnyRef, Value1 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1])) : SelfMaintainalbeAggregationFunctionFactory[Domain, Value1] = {
        CreateAggregationFunctionContainer.applySelf(t)
    }
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

    def applySelf[Domain <: AnyRef, Value1 <: Any](f1 : AggregationFunctionFactory[Domain, Value1]) : SelfMaintainalbeAggregationFunctionFactory[Domain, Value1] = {
        new SelfMaintainalbeAggregationFunctionFactory[Domain, Value1] {
            def apply() : SelfMaintainalbeAggregationFunction[Domain, Value1] = {
                new SelfMaintainalbeAggregationFunction[Domain, Value1] {
                    val func1 = f1()

                    def add(newD : Domain) = {
                        func1.add(newD, null)
                    }
                    def remove(newD : Domain) = {
                        func1.remove(newD, null)
                    }
                    def update(oldD : Domain, newD : Domain) = {
                        func1.update(oldD, newD, null)
                    }
                }
            }
        }
    }

    def applySelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](f1 : AggregationFunctionFactory[Domain, Value1], f2 : AggregationFunctionFactory[Domain, Value2]) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
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
    def applySelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](f1 : AggregationFunctionFactory[Domain, Value1], f2 : AggregationFunctionFactory[Domain, Value2], f3 : AggregationFunctionFactory[Domain, Value3]) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
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

    def applyNotSelf[Domain <: AnyRef, Value1 <: Any](f1 : AggregationFunctionFactory[Domain, Value1]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1] = {
        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1] {
            def apply() : NotSelfMaintainalbeAggregationFunction[Domain, Value1] = {
                new NotSelfMaintainalbeAggregationFunction[Domain, Value1] {
                    val func1 = f1()

                    def add(newD : Domain, data : Iterable[Domain]) = {
                        (func1.add(newD, data))
                    }
                    def remove(newD : Domain, data : Iterable[Domain]) = {
                        (func1.remove(newD, data))
                    }
                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
                        (func1.update(oldD, newD, data))
                    }
                }
            }
        }
    }

    def applyNotSelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](f1 : AggregationFunctionFactory[Domain, Value1], f2 : AggregationFunctionFactory[Domain, Value2]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] {
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

    def applyNotSelf[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](f1 : AggregationFunctionFactory[Domain, Value1], f2 : AggregationFunctionFactory[Domain, Value2], f3 : AggregationFunctionFactory[Domain, Value3]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
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