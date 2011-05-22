package sae.operators

object CreateAggregationFunctionContainer {

    implicit def AggregationFunctionsZipper1[Domain <: AnyRef, Value1 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1)] = {
        CreateAggregationFunctionContainer.apply(t)
    }
    implicit def AggregationFunctionsZipper2[Domain <: AnyRef, Value1 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1])) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1)] = {
        CreateAggregationFunctionContainer.apply(t)
    }
    implicit def AggregationFunctionsZipper3[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2)
    }
    implicit def AggregationFunctionsZipper4[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2)
    }
    implicit def AggregationFunctionsZipper5[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2)
    }
    implicit def AggregationFunctionsZipper6[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2)
    }
    implicit def AggregationFunctionsZipper7[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper8[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper9[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper10[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper11[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper12[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper13[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }
    implicit def AggregationFunctionsZipper14[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](t : (SelfMaintainalbeAggregationFunctionFactory[Domain, Value1], SelfMaintainalbeAggregationFunctionFactory[Domain, Value2], SelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : SelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
        CreateAggregationFunctionContainer.apply(t._1, t._2, t._3)
    }

    def apply[Domain <: AnyRef, Value1 <: Any, ReturnFunction <: AggregationFunction[Domain, Value1], ReturnFactory <: AggregationFunctionFactory[Domain, Value1]](f1 : AggregationFunctionFactory[Domain, Value1]) : ReturnFactory = {
        new AggregationFunctionFactory[Domain, Value1] {
            def apply() : ReturnFunction = {
                new AggregationFunction[Domain, Value1] {
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
                }.asInstanceOf[ReturnFunction]
            }
        }.asInstanceOf[ReturnFactory]
    }

    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, ReturnFunction <: AggregationFunction[Domain, (Value1, Value2)], ReturnFactory <: AggregationFunctionFactory[Domain, (Value1, Value2)]](f1 : AggregationFunctionFactory[Domain, Value1], f2 : AggregationFunctionFactory[Domain, Value2]) : ReturnFactory = {
        new AggregationFunctionFactory[Domain, (Value1, Value2)] {
            def apply() : ReturnFunction = {
                new AggregationFunction[Domain, (Value1, Value2)] {
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
                }.asInstanceOf[ReturnFunction]

            }

        }.asInstanceOf[ReturnFactory]
    }
    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any, ReturnFunction <: AggregationFunction[Domain, (Value1, Value2, Value3)], ReturnFactory <: AggregationFunctionFactory[Domain, (Value1, Value2)]](f1 : AggregationFunctionFactory[Domain, Value1], f2 : AggregationFunctionFactory[Domain, Value2], f3 : AggregationFunctionFactory[Domain, Value3]) : ReturnFactory = {
        new AggregationFunctionFactory[Domain, (Value1, Value2, Value3)] {
            def apply() : ReturnFunction = {
                new AggregationFunction[Domain, (Value1, Value2, Value3)] {
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
                }.asInstanceOf[ReturnFunction]

            }

        }.asInstanceOf[ReturnFactory]
    }

    //   def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](f1 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1],
    //                                                                             f2 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], f3 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
    //        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] {
    //            def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] = {
    //                new NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] {
    //                    val func1 = f1()
    //                    val func2 = f2()
    //                    val func3 = f3()
    //                    def add(newD : Domain, data : Iterable[Domain]) = {
    //                        (func1.add(newD, data), func2.add(newD, data), func3.add(newD, data))
    //                    }
    //                    def remove(newD : Domain, data : Iterable[Domain]) = {
    //                        (func1.remove(newD, data), func2.remove(newD, data), func3.remove(newD, data))
    //                    }
    //                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
    //                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data), func3.update(oldD, newD, data))
    //                    }
    //                }
    //
    //            }
    //
    //        }
    //    }

}

// implicit def NotSelfMaintainalbeAggregationFunctionToAggregationFactory1[Domain <: AnyRef, Value1 <: Any]
//                                                         (t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1]
//                                                               )) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1)] = {
//         CreateAggregationFunctionContainer.apply(t)
//    }
//    implicit def NotSelfMaintainalbeAggregationFunctionToAggregationFactory2[Domain <: AnyRef, Value1 <: Any, Value2 <: Any]
//                                                         (t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1],
//                                                               NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] ={
//         CreateAggregationFunctionContainer.apply(t._1,t._2)
//    }
//    implicit def NotSelfMaintainalbeAggregationFunctionToAggregationFactory3[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any]
//                                                         (t : (NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1],
//                                                               NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3])) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] ={
//         CreateAggregationFunctionContainer.apply(t._1,t._2, t._3)
//    }
//    
//    
//    def apply[Domain <: AnyRef, Value1 <: Any](f1 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1)] = {
//        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1)] {
//            def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1)] = {
//                new NotSelfMaintainalbeAggregationFunction[Domain, (Value1)] {
//                    val func1 = f1()
//
//                    def add(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.add(newD, data))
//                    }
//                    def remove(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.remove(newD, data))
//                    }
//                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
//                        (func1.update(oldD, newD, data))
//                    }
//                }
//
//            }
//
//        }
//    }
//
//    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any](f1 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1],
//                                                              f2 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] = {
//        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2)] {
//            def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2)] = {
//                new NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2)] {
//                    val func1 = f1()
//                    val func2 = f2()
//
//                    def add(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.add(newD, data), func2.add(newD, data))
//                    }
//                    def remove(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.remove(newD, data), func2.remove(newD, data))
//                    }
//                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
//                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data))
//                    }
//                }
//
//            }
//
//        }
//    }
//
//    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any](f1 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1],
//                                                                             f2 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2], f3 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] = {
//        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3)] {
//            def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] = {
//                new NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3)] {
//                    val func1 = f1()
//                    val func2 = f2()
//                    val func3 = f3()
//                    def add(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.add(newD, data), func2.add(newD, data), func3.add(newD, data))
//                    }
//                    def remove(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.remove(newD, data), func2.remove(newD, data), func3.remove(newD, data))
//                    }
//                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
//                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data), func3.update(oldD, newD, data))
//                    }
//                }
//
//            }
//
//        }
//    }
//    def apply[Domain <: AnyRef, Value1 <: Any, Value2 <: Any, Value3 <: Any, Value4 <: Any](f1 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value1],
//                                                                                            f2 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value2],
//                                                                                            f3 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value3],
//                                                                                            f4 : NotSelfMaintainalbeAggregationFunctionFactory[Domain, Value4]) : NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3, Value4)] = {
//        new NotSelfMaintainalbeAggregationFunctionFactory[Domain, (Value1, Value2, Value3, Value4)] {
//            def apply() : NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3, Value4)] = {
//                new NotSelfMaintainalbeAggregationFunction[Domain, (Value1, Value2, Value3, Value4)] {
//                    val func1 = f1()
//                    val func2 = f2()
//                    val func3 = f3()
//                    val func4 = f4()
//                    def add(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.add(newD, data), func2.add(newD, data), func3.add(newD, data), func4.add(newD, data))
//                    }
//                    def remove(newD : Domain, data : Iterable[Domain]) = {
//                        (func1.remove(newD, data), func2.remove(newD, data), func3.remove(newD, data), func4.remove(newD, data))
//                    }
//                    def update(oldD : Domain, newD : Domain, data : Iterable[Domain]) = {
//                        (func1.update(oldD, newD, data), func2.update(oldD, newD, data), func3.update(oldD, newD, data), func4.update(oldD, newD, data))
//                    }
//                }
//
//            }
//
//        }
//    }