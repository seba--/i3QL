package sae.profiler

import sae._
import sae.operators._
import sae.functions._
import sae.util._
import sae.bytecode.model._
import sae.syntax.RelationalAlgebraSyntax._
import sae.test.helpFunctions._
import com.google.common.collect.HashMultiset
import scala.collection.mutable._
import de.tud.cs.st.bat._


class ScalaCompilerDatabase extends sae.bytecode.BytecodeDatabase {

    def readBytecode : Unit =
        {
            addArchiveAsResource("scala-compiler.jar")
            //addArchiveAsFile("C:/Users/crypton/workspace_BA/SAE/proto-test-data/src/main/resources/scala-compiler.jar")
        }

}

object ScalaCompilerProfiler {

    val allClassfiles = new ArrayBuffer[ObjectType]()
    val allMethods = new ArrayBuffer[Method]()
    val mostClassfiles = new ArrayBuffer[ObjectType]()
    val mostMethods = new ArrayBuffer[Method]()
    val someClassfiles = new ArrayBuffer[ObjectType]()
    val someMethods = new ArrayBuffer[Method]()
    val someMethodsInMostMethods = new ArrayBuffer[Method]()
    val ONE_OUT_OF_X = 100

    implicit val iterations = 1
    def main(args : Array[String]) : Unit = {
        init
        
        //
        write("read scala-compiler.jar", profile(read))
        
        profiling("Fan In for scala.Tupel2", initSelectiveFanIn)
        
        //-------------- group by  package find class with max methods   
        write("group by Package : find class with max methods", profile(classWithMaxMethodsPairPackage))
        write("mantaining s.o. (" + someMethods.size + " ADDs)", profile2(initClassWithMaxMethodsPairPackage,
            (x : sae.test.helpFunctions.ObservableList[Method]) => someMethods.foreach(z => x.add(z))))
        write("mantaining s.o. (" + someMethods.size + " UPDATES)", profile2(initClassWithMaxMethodsPairPackage,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethods.size - 1) {
                x.update(someMethodsInMostMethods(i), someMethods(i))
            }))
        write("mantaining s.o. (" + someMethodsInMostMethods.size + " REMOVES)", profile2(initClassWithMaxMethodsPairPackage,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethodsInMostMethods.size - 1) {
                x.remove(someMethodsInMostMethods(i))
            }))
        //-------------- groub by package calc fan out to not java.*
        write("group by Package : calc fan out", profile(selectiveFanOut))
        write("mantaining s.o. (" + someMethods.size + " ADDs)", profile2(initSelectiveFanOut,
            (x : sae.test.helpFunctions.ObservableList[Method]) => someMethods.foreach(z => x.add(z))))
        write("mantaining s.o. (" + someMethods.size + " UPDATES)", profile2(initSelectiveFanOut,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethods.size - 1) {
                x.update(someMethodsInMostMethods(i), someMethods(i))
            }))
        write("mantaining s.o. (" + someMethodsInMostMethods.size + " REMOVES)", profile2(initSelectiveFanOut,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethodsInMostMethods.size - 1) {
                x.remove(someMethodsInMostMethods(i))
            }))
            
        // fanin for all classes with sql like syntax
        //write("Fan In with Sql ", profile(fanInWithSQL))     
        //-------------- groub by class calc fan in
        write("group by class : calc fan in", profile(fanInAll))
        write("mantaining s.o. (" + someMethods.size + " ADDs)", profile2(initFanIn,
            (x : sae.test.helpFunctions.ObservableList[Method]) => someMethods.foreach(z => x.add(z))))
        write("mantaining s.o. (" + someMethods.size + " UPDATES)", profile2(initFanIn,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethods.size - 1) {
                //println(someMethodsInMostMethods(i))
                //x.remove(someMethodsInMostMethods(i))
                //x.add(someMethods(i))
                x.update(someMethodsInMostMethods(i), someMethods(i))
            }))
        write("mantaining s.o. (" + someMethodsInMostMethods.size + " REMOVES)", profile2(initFanIn,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethodsInMostMethods.size - 1) {
                x.remove(someMethodsInMostMethods(i))
            }))
    }

    private def profiling(text : String, f1 : => (sae.test.helpFunctions.ObservableList[Method], Any)){
        write(text, profile(f1))
        write("mantaining s.o. (" + someMethods.size + " ADDs)", profile2(f1,
            (x : sae.test.helpFunctions.ObservableList[Method]) => someMethods.foreach(z => x.add(z))))
        write("mantaining s.o. (" + someMethods.size + " UPDATES)", profile2(f1,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethods.size - 1) {
                x.update(someMethodsInMostMethods(i), someMethods(i))
            }))
        write("mantaining s.o. (" + someMethodsInMostMethods.size + " REMOVES)", profile2(f1,
            (x : sae.test.helpFunctions.ObservableList[Method]) => for (i <- 0 to someMethodsInMostMethods.size - 1) {
                x.remove(someMethodsInMostMethods(i))
            }))
    }
        

    def init : Unit = {
        val db = new ScalaCompilerDatabase()
        db.classfiles.addObserver(new Observer[ObjectType] {
            def updated(oldV : ObjectType, newV : ObjectType) : Unit = { throw new Error() }
            def removed(v : ObjectType) : Unit = { throw new Error() }
            def added(v : ObjectType) : Unit = { allClassfiles += v }
        })
        db.classfile_methods.addObserver(new Observer[Method] {
            def updated(oldV : Method, newV : Method) : Unit = { throw new Error() }
            def removed(v : Method) : Unit = { throw new Error() }
            def added(v : Method) : Unit = { allMethods += v }
        })
        db.readBytecode
        var i = 1
        allClassfiles.foreach(x => {
            if (i % ONE_OUT_OF_X == 0) { someClassfiles += x }
            else mostClassfiles += x
            i += 1
        })
        i = 1
        allMethods.foreach(x => {
            if (i % ONE_OUT_OF_X == 0) { someMethods += x; someMethodsInMostMethods += mostMethods(mostMethods.size - 1) }
            else mostMethods += x
            i += 1
        })
    }
    def read {
        val db = new ScalaCompilerDatabase()
        db.readBytecode
    }

    def classWithMaxMethodsPairPackage() : Unit = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        getGroupByPackageFindClassWithMaxMethods(source)
        allMethods.foreach(x => source.add(x))
    }

    def initClassWithMaxMethodsPairPackage : (sae.test.helpFunctions.ObservableList[Method], Any) = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        val res = getGroupByPackageFindClassWithMaxMethods(source)
        mostMethods.foreach(x => source.add(x))
        (source, res)
    }
    def selectiveFanOut : Unit = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        getFanOut(source, x => { !x.toJava.startsWith("java.", 0) })
        allMethods.foreach(x => source.add(x))
    }
    def initSelectiveFanOut : (sae.test.helpFunctions.ObservableList[Method], Any) = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        val res = getFanOut(source, x => { !x.toJava.startsWith("java.", 0) }) //FIXME fan out zï¿½hlt abhï¿½ngigkeiten zu sich selbst mit
        mostMethods.foreach(x => source.add(x))
        (source, res)
    }
    def initFanIn = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        val res = getFanOutByClass(source)
        var list = List[LazyView[Some[Int]]]()
        allClassfiles.foreach(x => {
            list = getFanIn(res, x.packageName.replace('/', '.') + "." + x.simpleName) :: list
        })
        mostMethods.foreach(x => source.add(x))

        
        
        (source, res)
    }
    def initSelectiveFanIn = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        val fanIn = getSelectivFanIn(source, "scala.Tuple2")
        mostMethods.foreach(x => source.add(x))
        (source, fanIn)
    }
    def fanInAll {
        initFanIn
    }
    def profile(f : => Unit)(implicit times : Int = 1) : Array[Timer] = {
        var i = 0;
        val timers = new Array[Timer](times)
        while (i < times) {
            val timer = new Timer()
            f
            timer.stop;
            timers(i) = timer
            i += 1
        }
        return timers
    }
    def profile2[T <: AnyRef](f1 : => (sae.test.helpFunctions.ObservableList[T], Any), f2 : sae.test.helpFunctions.ObservableList[T] => Unit)(implicit times : Int = 1) : Array[Timer] = {
        var i = 0;
        val timers = new Array[Timer](times)
        while (i < times) {
            val s = f1
            val timer = new Timer()
            f2(s._1)
            timer.stop;
            timers(i) = timer
            i += 1
        }
        return timers
    }
    def fanInWithSQL() : Unit = {
        val source = new sae.test.helpFunctions.ObservableList[Method]()
        val res = getFanOutByClass(source)
        case class ReducedMethod(className : String, name : String, dep : String)
        import scala.collection.mutable.Set
        val o2m = new DefaultOneToMany(source, (x : Method) => {
            var res = List[ReducedMethod]()
            res = new ReducedMethod((x.declaringRef.packageName +"."+ x.declaringRef.simpleName).replace('/', '.'), x.name, x.returnType.toJava) :: res
            if (x.parameters.size == 0) {

            } else {
                x.parameters.foreach((y : de.tud.cs.st.bat.Type) => {
                    res = new ReducedMethod((x.declaringRef.packageName +"."+ x.declaringRef.simpleName).replace('/', '.'), x.name, y.toJava) :: res
                })
            }
            res
        })
        var list = List[AnyRef]()
        val removeMethodWithDependencyToThereImplClass = new MaterializedSelection((x : ReducedMethod) => { x.className != x.dep }, o2m)
         allClassfiles.foreach(z => {   
            val filterDepEqFanInClass = new MaterializedSelection((x : ReducedMethod) => { x.dep == z.toJava}, removeMethodWithDependencyToThereImplClass)
            val groupByClass = Aggregation(filterDepEqFanInClass, (x : ReducedMethod) => x.className, Count[ReducedMethod](), (a :  String, b : (Int)) => a)
            val countClassesWithDepToFanInClass = Aggregation(groupByClass, Count[String])
            list = countClassesWithDepToFanInClass :: list
        })
        mostMethods.foreach(x => source.add(x))
    }
    
    def write(name : String, profile : Array[Timer]) : Unit = {
        print(name + " : ")
        val t = Timer.median(profile)
        println(t.elapsedSecondsWithUnit)
    }
    private def getGroupByPackageFindClassWithMaxMethods(source : LazyView[Method]) = {
        val groupByClassesAndCountMethods = Aggregation(source, (x : Method) => (x.declaringRef.packageName, x.declaringRef.simpleName), Count[Method], (x : (String, String), y : Int) => (x._1, x._2, y))
        val groupByPackageFindClassWithMaxMethods = Aggregation(groupByClassesAndCountMethods,
            (x : (String, String, Int)) => x._1,
            Max2[(String, String, Int), Option[(String, String, Int)]]((x : (String, String, Int)) => x._3, (y : Option[(String, String, Int)], x : Int) => y),
            (x : String, y : Option[(String, String, Int)]) => y)
        groupByPackageFindClassWithMaxMethods
    }
    // grouped by packages!
    private def getFanOut(source : LazyView[Method], f : de.tud.cs.st.bat.Type => Boolean) = {
        import sae.functions.FanOut
        import scala.collection.mutable._
        val groupByClassesAndCalcFanOut = Aggregation(source,
            (x : Method) => x.declaringRef.packageName /*, x.clazz.simpleName)*/ ,
            FanOut((x : Method) => (x.parameters, x.returnType), f),
            (x : (String), y : Set[String]) => (x, y))
        groupByClassesAndCalcFanOut
    }
    private def getFanOutByClass(source : LazyView[Method]) = {
        getFanOutByClassWithSelectFunction(source, y => true)
    }

    private def getFanOutByClassWithSelectFunction(source : LazyView[Method], select : de.tud.cs.st.bat.Type => Boolean) = {
        import sae.functions.FanOut
        import scala.collection.mutable._
        val groupByClassesAndCalcFanOut = Aggregation(source,
            (x : Method) => (x.declaringRef.packageName, x.declaringRef.simpleName),
            FanOut((x : Method) => (x.parameters, x.returnType), select),
            (x : (String, String), y : Set[String]) => (x._1, x._2, y))
        groupByClassesAndCalcFanOut
    }
    private def getFanIn(source : LazyView[(String, String, Set[String])], clazz : String) = {
//        val res = Aggregation(new MaterializedSelection((x : (String, String, Set[String])) => { x._3.contains(clazz) && (x._1.replace('/', '.') + "." + x._2) != clazz },
//            source), Count[(String, String, Set[String])])
//        res
         val res = Aggregation(new MaterializedSelection((x : (String, String, Set[String])) => { x._3.contains(clazz) && (x._1+"/" + x._2) != clazz.replace(".","/") },
            source), Count[(String, String, Set[String])])
        res
    }
    private def getSelectivFanIn(source : LazyView[Method], clazz : String) = {
        val groupByClassFanOutWithSelect = getFanOutByClassWithSelectFunction(source, (x : de.tud.cs.st.bat.Type) => {
            x.toJava == clazz
        })
        val fanIn = getFanIn(groupByClassFanOutWithSelect, clazz)
        fanIn
    }
}

