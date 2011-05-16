package sae.operators

object CreateAggregationFunctionContainer {

    implicit def AggregationFunktionToAggregationFactory1[Domain <: AnyRef, Value1 <: Any]
                                                         (t : (AggregationFunktionFactory[Domain, Value1]
                                                               )) : AggregationFunktionFactory[Domain, (Value1)] = {
         CreateAggregationFunctionContainer.apply(t)
    }
    implicit def AggregationFunktionToAggregationFactory2[Domain <: AnyRef, Value1 <: Any, Value2 <: Any]
                                                         (t : (AggregationFunktionFactory[Domain, Value1],
                                                               AggregationFunktionFactory[Domain, Value2])) : AggregationFunktionFactory[Domain, (Value1, Value2)] ={
         CreateAggregationFunctionContainer.apply(t._1,t._2)
    }
    implicit def AggregationFunktionToAggregationFactory3[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any]
                                                         (t : (AggregationFunktionFactory[Domain, Value1],
                                                               AggregationFunktionFactory[Domain, Value2], AggregationFunktionFactory[Domain, Value3])) : AggregationFunktionFactory[Domain, (Value1, Value2, Value3)] ={
         CreateAggregationFunctionContainer.apply(t._1,t._2, t._3)
    }
    
    
    def apply[Domain <: AnyRef, Value1 <: Any](f1 : AggregationFunktionFactory[Domain, Value1]) : AggregationFunktionFactory[Domain, (Value1)] = {
        new AggregationFunktionFactory[Domain, (Value1)] {
            def apply() : AggregationFunktion[Domain, (Value1)] = {
                new AggregationFunktion[Domain, (Value1)] {
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

    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](f1 : AggregationFunktionFactory[Domain, Value1],
                                                              f2 : AggregationFunktionFactory[Domain, Value2]) : AggregationFunktionFactory[Domain, (Value1, Value2)] = {
        new AggregationFunktionFactory[Domain, (Value1, Value2)] {
            def apply() : AggregationFunktion[Domain, (Value1, Value2)] = {
                new AggregationFunktion[Domain, (Value1, Value2)] {
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

    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](f1 : AggregationFunktionFactory[Domain, Value1],
                                                                             f2 : AggregationFunktionFactory[Domain, Value2], f3 : AggregationFunktionFactory[Domain, Value3]) : AggregationFunktionFactory[Domain, (Value1, Value2, Value3)] = {
        new AggregationFunktionFactory[Domain, (Value1, Value2, Value3)] {
            def apply() : AggregationFunktion[Domain, (Value1, Value2, Value3)] = {
                new AggregationFunktion[Domain, (Value1, Value2, Value3)] {
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
    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any, Value4 <: Any](f1 : AggregationFunktionFactory[Domain, Value1],
                                                                                            f2 : AggregationFunktionFactory[Domain, Value2],
                                                                                            f3 : AggregationFunktionFactory[Domain, Value3],
                                                                                            f4 : AggregationFunktionFactory[Domain, Value4]) : AggregationFunktionFactory[Domain, (Value1, Value2, Value3, Value4)] = {
        new AggregationFunktionFactory[Domain, (Value1, Value2, Value3, Value4)] {
            def apply() : AggregationFunktion[Domain, (Value1, Value2, Value3, Value4)] = {
                new AggregationFunktion[Domain, (Value1, Value2, Value3, Value4)] {
                    val func1 = f1()
                    val func2 = f2()
                    val func3 = f3()
                    val func4 = f4()
                    def add(newD : Domain, data : Iterable[Domain]) = {
                        (func1.add(newD, data), func2.add(newD, data), func3.add(newD, data), func4.add(newD, data))
                    }
                    def remove(newD : Domain, data : Iterable[Domain]) = {
                        (func1.remove(newD, data), func2.remove(newD, data), func3.remove(newD, data), func4.remove(newD, data))
                    }
                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data), func3.update(oldD, newD, data), func4.update(oldD, newD, data))
                    }
                }

            }

        }
    }

}