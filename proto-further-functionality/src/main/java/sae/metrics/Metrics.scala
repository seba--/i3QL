package sae.metrics

import sae.bytecode._
import de.tud.cs.st.lyrebird.replayframework.Replay
import java.io.File
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

import sae.functions.Sum
import sae.functions.CalcSelfMaintable
import sun.font.TrueTypeFont

/**
 * Date: 16.06.11
 * Time: 00:16
 * @author Malte V
 */
//TODO add a fan in/out and lcom that igrnore constructors
object Metrics {
  def getFanOut(parameters: LazyView[parameter],
                methods: LazyView[Method],
                read_fields: LazyView[read_field],
                write_fields: LazyView[write_field],
                clase_fiels: LazyView[Field],
                calls: LazyView[calls] ,
                handled_exceptions: LazyView[ExceptionHandler]

                 ): LazyView[(ReferenceType, Int)] = {

    γ(getFanOutAsSet(parameters, methods, read_fields, write_fields, clase_fiels,
      calls,handled_exceptions), (x: (ReferenceType, Type)) => x._1, Count[(ReferenceType, Type)](),
      (a: ReferenceType, b: Int) => (a, b))

  }


  def getFanIn(parameters: LazyView[parameter],
               methods: LazyView[Method],
               read_fields: LazyView[read_field],
               write_fields: LazyView[write_field],
               clase_fiels: LazyView[Field],
               calls: LazyView[calls] ,
                handled_exceptions: LazyView[ExceptionHandler]
                ): LazyView[(Type, Int)] = {


    γ(getFanOutAsSet(parameters, methods, read_fields, write_fields, clase_fiels,
      calls,handled_exceptions), (x: (ReferenceType, Type)) => x._2, Count[(ReferenceType, Type)](),
      (a: Type, b: Int) => (a, b))
  }


  def getFanOutAsSet(parameters: LazyView[parameter],
                     methods: LazyView[Method],
                     read_fields: LazyView[read_field],
                     write_fields: LazyView[write_field],
                     clase_fiels: LazyView[Field],
                     calls: LazyView[calls],
                     handled_exceptions: LazyView[ExceptionHandler]
                      ): LazyView[(ReferenceType, Type)] = {

    val isNotSelfReferenceAndIsObjectType: ((ReferenceType, Type)) => Boolean =
      (x: (ReferenceType, Type)) => {
        x._2.isObjectType && x._1 != x._2
      }
    val union: LazyView[(ReferenceType, Type)] =
      δ(
        (
          (((((σ(isNotSelfReferenceAndIsObjectType)
            (Π[parameter, (ReferenceType, Type)]((x: parameter) => (x.source.declaringRef, x.target))(parameters))))
            ∪
            (σ(isNotSelfReferenceAndIsObjectType)
              (Π[Method, (ReferenceType, Type)]((x: Method) => (x.declaringRef, x.returnType))(methods)))
            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[read_field, (ReferenceType, Type)]((
                                                                                           x: read_field) => (x.source.declaringRef, x.target.declaringClass))(read_fields)))
            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[write_field, (ReferenceType, Type)]((
                                                                                            x: write_field) => (x.source.declaringRef, x.target.declaringClass))(write_fields))))
            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[calls, (ReferenceType, Type)]((
                                                                                      x: calls) => (x.source.declaringRef, x.target.declaringRef))(calls))))

            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[Field, (ReferenceType, Type)]((
                                                                                      x: Field) => (x.declaringClass, x.fieldType))(clase_fiels))))

            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[ExceptionHandler, (ReferenceType, Type)]((
                                                                                                 x: ExceptionHandler) => {
            (x.declaringMethod.declaringRef,
              x.catchType match {
                case Some(value) => value
              })
          })(handled_exceptions)))
          )
      )

    union
  }

  def getLCOMStar(readField: LazyView[read_field],
                  writeField: LazyView[write_field],
                  methods: LazyView[Method],
                  fiels: LazyView[Field]): LazyView[(ReferenceType, Option[Double])] = {
    // we count static final fields
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

    val join = new HashEquiJoin(lazyViewToIndexedView(sumRWtoFieldsInOneClass), lazyViewToIndexedView(countMethods), (x: (ReferenceType, Int)) => x._1, (x: (ReferenceType, Int)) => x._1, (a: (ReferenceType, Int), b: (ReferenceType, Int)) => (a._1, a._2, b._2))
    val join2 = new HashEquiJoin(lazyViewToIndexedView(join), lazyViewToIndexedView(countFields), (x: (ReferenceType, Int, Int)) => x._1, (x: (ReferenceType, Int)) => x._1, (a: (ReferenceType, Int, Int), b: (ReferenceType, Int)) => (a._1, a._2, a._3, b._2))

    val calc: ((ReferenceType, Int, Int, Int)) => Double =
      (x: (ReferenceType, Int, Int, Int)) => ({
        if (x._4.asInstanceOf[Double] == 0) 0
        val a: Double = 1.asInstanceOf[Double] / x._4.asInstanceOf[Double]
        val b: Double = a * x._2.asInstanceOf[Double] - x._3.asInstanceOf[Double]
        val c: Double = 1 - x._3
        if (c == 0) 0
        val d: Double = b / c
        d
      })
    val result = γ(join2, (x: (ReferenceType, Int, Int, Int)) => x._1, CalcSelfMaintable(calc), (a: ReferenceType, b: Option[Double]) => (a, b))
    result
  }


  def getInheritanceTransitiveClosure(extendz: LazyView[`extends`]) = {
    val view = new HashTransitiveClosure(extendz, (x: `extends`) => x.source, (x: `extends`) => x.target)
    view
  }

  def getDedthOfInheritanceTree(extendz: LazyView[`extends`]) = {
    val view: LazyView[(ObjectType, ObjectType)] = new HashTransitiveClosure(extendz, (x: `extends`) => x.source, (x: `extends`) => x.target)
    val res = Aggregation(view, (x: (ObjectType, ObjectType)) => x._1, Count[(ObjectType, ObjectType)](), (x: ObjectType, y: Int) => (x, y))
    res
  }
}