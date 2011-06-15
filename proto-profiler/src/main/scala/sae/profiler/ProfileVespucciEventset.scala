package sae.profiler

import sae.bytecode._
import de.tud.cs.st.lyrebird.replayframework.Replay
import java.io.File
import sae.profiler.util._
import sae.LazyView
import sae.bytecode.model._
import sae.bytecode.model.dependencies._
import sae.operators._
import sae.syntax._
import sae._
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.Type
import de.tud.cs.st.bat.ObjectType
import de.tud.cs.st.bat.ReferenceType
import sae.functions.Count
import sae.collections.QueryResult
import sae.test.helpFunctions.ObserverList
import sae.functions.Sum
import sae.functions.CalcSelfMaintable

object ProfileVespucciEventset {
  var tmp: QueryResult[(ObjectType, ObjectType)] = null
  var tmp2 : QueryResult[(ObjectType, Int)] = null
  //var tmp3 : ObserverList[(ReferenceType,Int,Int,Int)] = new ObserverList()
  //    var tmp3 : ObserverList[(ReferenceType, Option[Double])] = new ObserverList()
  //private val replay = new Replay(new File("./src/main/resources/VespucciEventSet"))
  def main(args: Array[String]): Unit = {
    //initial settings
    //val replayHelper = new ReplayHelper(new File("./src/main/resources/VespucciEventSet"))
    //val replayHelper = new ReplayHelper(new File("./src/main/resources/Flashcards 0.3/bin"))
    //val replayHelper = new ReplayHelper(new File("./src/main/resources/testSet/bin"))
     val replayHelper = new ReplayHelper(new File("./src/main/resources/extendsSet/bin"))
    register(replayHelper.buffer)

    replayHelper.applyAll
     tmp.foreach(println _)
     println("--")
    tmp2.foreach(println _)
    //        println("--")
    //        tmp3.data.foreach(println _)
    //        println("--")
    //        tmp.foreach(println _)
  }

  /**
   *
   */
  def register(dbBuffer: DatabaseBuffer) {
    registerFanOut(dbBuffer.parameter, dbBuffer.classfile_methods, dbBuffer.classfile_fields)
    registerFanIn(dbBuffer.parameter, dbBuffer.classfile_methods, dbBuffer.classfile_fields)
    registerLCOMStar(dbBuffer.read_field, dbBuffer.write_field, dbBuffer.classfile_methods, dbBuffer.classfile_fields)
    tmp = registerExtends(dbBuffer.`extends`)
    tmp2 = registerExtendsCount(dbBuffer.`extends`)
    //registerExtendsCount(dbBuffer.`extends`)
  }

  private def registerFanOut(parameters: LazyView[parameter],
                             methods: LazyView[Method],
                             fiels: LazyView[Field]): LazyView[(ReferenceType, Int)] = {
    val func: ((ReferenceType, Type)) => Boolean =
      (x: (ReferenceType, Type)) => {
        x._1 != x._2 && x._2.isObjectType
      }
    val union: LazyView[(ReferenceType, Type)] =
      δ(((σ(func)
        (Π[parameter, (ReferenceType, Type)]((x: parameter) => (x.source.declaringRef, x.target))(parameters))))
        ∪
        (σ(func)
          (Π[Method, (ReferenceType, Type)]((x: Method) => (x.declaringRef, x.returnType))(methods)))
        ∪ (σ(func)(Π[Field, (ReferenceType, Type)]((
                                                     x: Field) => (x.declaringClass, x.fieldType))(fiels))))
    γ(union, (x: (ReferenceType, Type)) => x._1, Count[(ReferenceType, Type)](),
      (a: ReferenceType, b: Int) => (a, b))
  }

  private def registerFanIn(parameters: LazyView[parameter], methods: LazyView[Method], fiels: LazyView[Field]): LazyView[(Type, Int)] = {
    val func: ((ReferenceType, Type)) => Boolean = (x: (ReferenceType, Type)) => {
      x._1 != x._2 && x._2.isObjectType
    }
    val union: LazyView[(ReferenceType, Type)] = δ(((σ(func)(Π[parameter, (ReferenceType, Type)]((x: parameter) => (x.source.declaringRef, x.target))(parameters)))) ∪ (σ(func)(Π[Method, (ReferenceType, Type)]((x: Method) => (x.declaringRef, x.returnType))(methods))) ∪ (σ(func)(Π[Field, (ReferenceType, Type)]((x: Field) => (x.declaringClass, x.fieldType))(fiels))))

    γ(union, (x: (ReferenceType, Type)) => x._2, Count[(ReferenceType, Type)](), (a: Type, b: Int) => (a, b))
  }

  private def registerLCOMStar(readField: LazyView[read_field],
                               writeField: LazyView[write_field],
                               methods: LazyView[Method],
                               fiels: LazyView[Field]): LazyView[(ReferenceType, Option[Double])] = {
    val countFields =
      γ(fiels, (x: Field) => x.declaringClass, Count[Field](), (a: ObjectType, b: Int) => (a, b))

    val countMethods: LazyView[(ReferenceType, Int)] =
      γ(methods, (x: Method) => x.declaringRef, Count[Method](), (a: ReferenceType, b: Int) => (a, b))
    val methodsAndFiels: LazyView[(Method, Field)] =
      δ(Π[read_field, (Method, Field)]((x: read_field) => (x.source, x.target))(readField) ∪ Π[write_field, (Method, Field)]((x: write_field) => (x.source, x.target))(writeField))
    val countRWtoOneField: LazyView[((ReferenceType, Field), Int)] =
      γ(methodsAndFiels, (x: (Method, Field)) => (x._1.declaringRef, x._2), Count[(Method, Field)](), (a: (ReferenceType, Field), b: Int) => (a, b))
    val sumRWtoFieldsInOneClass: LazyView[(ReferenceType, Int)] =
      γ(countRWtoOneField,
        (x: ((ReferenceType, Field), Int)) => x._1._1,
        Sum((x: ((ReferenceType, Field), Int)) => x._2),
        (a: ReferenceType, b: Int) => (a, b))

    import sae.operators.Conversions._
    //TODO use RelationalAlgebraSyntax
    //val joinREl : LazyView[(ReferenceType, Int, Int)]= ((sumRWtoFieldsInOneClass, (x : (ReferenceType,Int)) => x._1) ⋈ (countMethods, (x : (ReferenceType,Int)) => x._1)) {(a : (ReferenceType,Int) , b : (ReferenceType,Int) ) => (a._1, a._2, b._2)}
    val join = new HashEquiJoin(lazyViewToIndexedView(sumRWtoFieldsInOneClass), lazyViewToIndexedView(countMethods), (x: (ReferenceType, Int)) => x._1, (x: (ReferenceType, Int)) => x._1, (a: (ReferenceType, Int), b: (ReferenceType, Int)) => (a._1, a._2, b._2))
    val join2 = new HashEquiJoin(lazyViewToIndexedView(join), lazyViewToIndexedView(countFields), (x: (ReferenceType, Int, Int)) => x._1, (x: (ReferenceType, Int)) => x._1, (a: (ReferenceType, Int, Int), b: (ReferenceType, Int)) => (a._1, a._2, a._3, b._2))
    //: ((ReferenceType,Int,Int,Int) => Double)
    val calc: ((ReferenceType, Int, Int, Int)) => Double =
      (x: (ReferenceType, Int, Int, Int)) => ({
        val a: Double = 1.asInstanceOf[Double] / x._4.asInstanceOf[Double]
        val b: Double = a * x._2.asInstanceOf[Double] - x._3.asInstanceOf[Double]
        val c: Double = 1 - x._3
        val d: Double = b / c
        d
      })
    val result = γ(join2, (x: (ReferenceType, Int, Int, Int)) => x._1, CalcSelfMaintable(calc), (a: ReferenceType, b: Option[Double]) => (a, b))
    //join2 = (ClassName, SumRW, SumMethods, SumFiels)
    //result addObserver tmp3
    // join2 addObserver tmp3
    result
  }


  private def registerExtends(extendz: LazyView[`extends`]) = {
    val view = new HashTransitiveClosure(extendz, (x: `extends`) => x.source, (x: `extends`) => x.target)
    view
  }
  private def registerExtendsCount(extendz: LazyView[`extends`]) = {
    val view : LazyView[(ObjectType,ObjectType)] = new HashTransitiveClosure(extendz, (x: `extends`) => x.source, (x: `extends`) => x.target)
    val res = Aggregation(view,(x : (ObjectType,ObjectType)) => x._1, Count[(ObjectType,ObjectType)](), (x : ObjectType, y : Int) => (x,y))
    res
  }

}