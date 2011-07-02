package sae.metrics

import sae.bytecode._
import sae.bytecode.model._
import sae.bytecode.model.dependencies._
import sae.operators._
import sae._
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.Type
import de.tud.cs.st.bat.ObjectType
import de.tud.cs.st.bat.ReferenceType
import sae.functions.Count
import sae.operators.Conversions._


/**
 * Collection of software metrics
 *
 *
 * author: Malte V
 */
object Metrics {

  /**
   * Calculates the fan out for all classes in db
   * Calculation after:  Predicting Class Testability using Object-Oriented Metrics
   */
  def numberOfFanOutPerClass(db: Database): LazyView[(ReferenceType, Int)] = {
    //count all outgoing dependencies
    γ(fanOutAsSet(db), (x: (ReferenceType, Type)) => x._1, Count[(ReferenceType, Type)]())
  }

  /**
   * Calculates the fan in for all classes in db
   */
  def numberOfFanInPerClass(db: Database): LazyView[(Type, Int)] = {
    //count all incoming depedencies
    γ(fanOutAsSet(db), (x: (ReferenceType, Type)) => x._2, Count[(ReferenceType, Type)]())
  }

  /**
   * helper method for fan in/out
   */
  def fanOutAsSet(db : Database
                      ): LazyView[(ReferenceType, Type)] = {


    val isNotSelfReferenceAndIsObjectType: ((ReferenceType, Type)) => Boolean =
      (x: (ReferenceType, Type)) => {
        x._2.isObjectType && x._1 != x._2
      }

   // calc set = { (classA,classB) | a uses b }
   // classA use classB if:
   // - classA calls a method of classB   (calls)
   // - classA references a field of classB (    classfile_methods,   parameter,   read_field,   write_field,   classfile_fields,  ExceptionHandler)
    val allDependencies: LazyView[(ReferenceType, Type)] =
      δ(
        (
          (((((σ(isNotSelfReferenceAndIsObjectType)
            (Π[parameter, (ReferenceType, Type)]((x: parameter) => (x.source.declaringRef, x.target))(db.parameter))))
            ∪
            (σ(isNotSelfReferenceAndIsObjectType)
              (Π[Method, (ReferenceType, Type)]((x: Method) => (x.declaringRef, x.returnType))(db.classfile_methods)))
            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[read_field, (ReferenceType, Type)]((
                                                                                           x: read_field) => (x.source.declaringRef, x.target.declaringClass))(db.read_field)))
            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[write_field, (ReferenceType, Type)]((
                                                                                            x: write_field) => (x.source.declaringRef, x.target.declaringClass))(db.write_field))))
            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[calls, (ReferenceType, Type)]((
                                                                                      x: calls) => (x.source.declaringRef, x.target.declaringRef))(db.calls))))

            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[Field, (ReferenceType, Type)]((
                                                                                      x: Field) => (x.declaringClass, x.fieldType))(db.classfile_fields))))

            ∪ (σ(isNotSelfReferenceAndIsObjectType)(Π[ExceptionHandler, (ReferenceType, Type)]((
                                                                                                 x: ExceptionHandler) => {
            (x.declaringMethod.declaringRef,
              x.catchType match {
                case Some(value) => value
              })
          })(db.handled_exceptions)))
          )
      )

    allDependencies
  }

  /**
   * Calculates the lcom* metric for all classes in db
   * Calculation after Predicting Class Testability using Object-Oriented Metrics
   */
  def LCOMStar(db: Database): LazyView[(ReferenceType, Option[Double])] = {

    //number of fields per class
    val numberOfFields =γ(db.classfile_fields, (x: Field) => x.declaringClass, Count[Field]())


    //number of methods per class
    val numberOfMethods: LazyView[(ReferenceType, Int)] =
      γ(db.classfile_methods, (x: Method) => x.declaringRef, Count[Method]())

    //set = { (Method, Field) | method use field}
    //Method use field if
    //  - field is a global class field
    //  - method reads/writes the field
    val methodUseField: LazyView[(Method, Field)] = δ(
      (Π[read_field, (Method, Field)]((x: read_field) => (x.source, x.target))(σ((x: read_field) => (x.source.declaringRef == x.target.declaringClass))(db.read_field))
        ∪
        Π[write_field, (Method, Field)]((x: write_field) => (x.source, x.target))(σ((x: write_field) => (x.source.declaringRef == x.target.declaringClass))(db.write_field))))

    // Sum_allMethods( distinct class field access)
    val countRWtoOneField: LazyView[(ReferenceType, Int)] =
      γ(methodUseField, (x: (Method, Field)) => (x._1.declaringRef), Count[(Method, Field)]())



    //create one relation over  numberOfFields, numberOfMethods countRWtoOneField
    val join = new HashEquiJoin(lazyViewToIndexedView(countRWtoOneField), lazyViewToIndexedView(numberOfMethods), (x: (ReferenceType, Int)) => x._1, (x: (ReferenceType, Int)) => x._1, (a: (ReferenceType, Int), b: (ReferenceType, Int)) => (a._1, a._2, b._2))
    val join2 = new HashEquiJoin(lazyViewToIndexedView(join), lazyViewToIndexedView(numberOfFields), (x: (ReferenceType, Int, Int)) => x._1, (x: (ReferenceType, Int)) => x._1, (a: (ReferenceType, Int, Int), b: (ReferenceType, Int)) => (a._1, a._2, a._3, b._2))


    // calc lcom* for all classes  (class, lcom value)
    // the lcom value can be None. eg the class has no methods
    val result = Π[(ReferenceType, Int, Int, Int), (ReferenceType, Option[Double])]((x: (ReferenceType, Int, Int, Int)) => {
      if (x._4 == 0 || x._4 == 1 || x._3 == 0) (x._1, None)
      val a: Double = 1.asInstanceOf[Double] / x._4.asInstanceOf[Double]
      val b: Double = a * x._2.asInstanceOf[Double] - x._3.asInstanceOf[Double]
      val c: Double = 1 - x._3
      if (c == 0) (x._1, None)
      val d: Double = b / c
      if (d == 0 || d == -0) (x._1, 0)
      if (d < 0) (x._1, None)
      (x._1, Some(d))
    })(join2)

    result


  }

  /**
   * Calculates the transitive closure over the inheritance relation
   */
  def getInheritanceTransitiveClosure(db : Database) : LazyView[(ObjectType,ObjectType)]= {
    new HashTransitiveClosure(db.`extends`, (x: `extends`) => x.source, (x: `extends`) => x.target)
  }

  /**
   * Calculates the depth of the inheritance tree for all classes in db
   */
  def depthOfInheritanceTree(db: Database): LazyView[(ObjectType, Int)] = {
    γ(getInheritanceTransitiveClosure(db), (x: (ObjectType, ObjectType)) => x._1, Count[(ObjectType, ObjectType)](), (x: ObjectType, y: Int) => (x, y))
  }

  /**
   * Calculates the number of classes for all packages
   */
  def numberOfClassesPerPackage(db: Database): LazyView[(String, Int)] = {
    γ(db.classfiles, (x: ObjectType) => x.packageName, Count[ObjectType]())
  }

  /**
   * Calculates the number of methods for all classes in db
   */
  def numberOfMethodsPerClass(db: Database): LazyView[(ReferenceType, Int)] = {
    γ(db.classfile_methods, (x: Method) => x.declaringRef, Count[Method]())
  }

  /**
   * Calculates the number of methods for all packageds
   */
  def numberOfMethodsPerPackage(db: Database): LazyView[(String, Int)] = {
    γ(db.classfile_methods, (x: Method) => x.declaringRef.packageName, Count[Method]())
  }


}