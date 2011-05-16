package sae.test
import org.junit.BeforeClass

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert._
import scala.collection.mutable.ListBuffer
import sae.collections._
import sae.bytecode.model._
import sae.syntax.RelationalAlgebraSyntax._
import sae.functions._
import sae.operators._
import sae._
import sae.test.helpFunctions._
import scala.util.control._
class JEditDatabase extends sae.bytecode.BytecodeDatabase {

    def readBytecode : Unit =
        {
            //addArchiveAsResource("jedit-4.3.3-win.jar")

            addArchiveAsFile("C:/Users/crypton/workspace_BA/SAE/proto-test-data/src/main/resources/jedit-4.3.3-win.jar")
        }

}
object JEditSuite {
    import sae.test.helpFunctions._
    val db = new JEditDatabase()
    var allClassfiles = new ObserverList[ClassFile]
    var allMethods = new ObserverList[Method]
    @BeforeClass
    def init() : Unit = {
        allClassfiles = new ObserverList[ClassFile]
        allMethods = new ObserverList[Method]
        db.classfiles.addObserver(allClassfiles)
        db.classfile_methods.addObserver(allMethods)
        db.readBytecode

    }
}
// import org.scalatest.junit.JUnitRunner// @RunWith(classOf[JUnitRunner]) 
class JEditSuite {
    import sae.test.helpFunctions._
    @Test
    def count_classfiles : Unit = {

        val classes : ObservableList[ClassFile] = new ObservableList[sae.bytecode.model.ClassFile] //JEditSuiteBefore.db.classfiles
        val test = JEditSuite.allClassfiles
        //val allMethods : ObservableList[Method]  = new ObservableList[Method]//JEditSuiteBefore.db.classfile_methods
        val groupByPackage = Aggregation(classes, (x : ClassFile) => x.packageName, Count[ClassFile], (x : String, y : Int) => (x, y))
        val query : QueryResult[ClassFile] = classes

        JEditSuite.allClassfiles.data.foreach(
            x => classes.add(x))
        //                

        assertEquals(1132, query.size);

    }

    @Test
    def count_classfile_methods : Unit = {
        val methods : ObservableList[Method] = new ObservableList[Method] //JEditSuiteBefore.db.classfile_methods
        val query : QueryResult[Method] = methods
        JEditSuite.allMethods.data.foreach(x => methods.add(x))
        assertEquals(7999, query.size);

    }

    @Test
    def find_classfile_with_max_methods_pair_package : Unit = {
        val methods : ObservableList[Method] = new ObservableList[Method] //JEditSuiteBefore.db.classfile_methods
        val groupByClassesAndCountMethods = Aggregation(methods, (x : Method) => (x.clazz.packageName, x.clazz.simpleName), Count[Method], (x : (String, String), y : Int) => (x._1, x._2, y))

        val groupByPackageFindClassWithMaxMethods = Aggregation(groupByClassesAndCountMethods,
            (x : (String, String, Int)) => x._1,
            Max2[(String, String, Int), Option[(String, String, Int)]]((x : (String, String, Int)) => x._3, (y : Option[(String, String, Int)], x : Int) => y),
            (x : String, y : Option[(String, String, Int)]) => y)
        JEditSuite.allMethods.data.foreach(x => methods.add(x))
        val result : QueryResult[Option[(String, String, Int)]] = groupByPackageFindClassWithMaxMethods

        val list : List[Option[(String, String, Int)]] = result.asList
        //TODO add some more asserts
        assertTrue(list.size == 29)
        assertTrue(list.contains(Some(("com/microstar/xml", "XmlParser", 118))))
        assertTrue(list.contains(Some(("org/gjt/sp/jedit/bsh/classpath", "BshClassPath", 49))))
        assertTrue(list.contains(Some(("org/gjt/sp/jedit/bsh/collection", "CollectionManagerImpl", 5))))
        assertTrue(list.contains(Some(("org/gjt/sp/jedit/bsh/commands", "dir", 5))))
        assertTrue(list.contains(Some(("org/gjt/sp/jedit/bufferset", "BufferSet", 18))))

        //val groupByPackage = Aggregation(methods, (x : Method) => x.clazz.packageName, Max(), (x : String, y : Int) => (x, y))
    }

    @Test
    def calc_pseudo_varianz_over_avg : Unit = {
        val methods : ObservableList[Method] = new ObservableList[Method] //JEditSuiteBefore.db.classfile_methods
        val groupByClassesAndCountMethods = Aggregation(methods, (x : Method) => (x.clazz.packageName, x.clazz.simpleName), Count[Method], (x : (String, String), y : Int) => (x._1, x._2, y))

        val pseudovarianzoveravg = Aggregation(groupByClassesAndCountMethods,
            (x : (String, String, Int)) => x._1,
            PseudoVarianz((x : (String, String, Int)) => x._3),
            (x : String, y : (Double, Double)) => (x, y))
        JEditSuite.allMethods.data.foreach(x => methods.add(x))
        val result : QueryResult[(String, (Double, Double))] = pseudovarianzoveravg
        val list = result.asList

        //TODO add some asserts for update and remove events
        assertTrue(list.size == 29)
        assertTrue(list.contains(("org/gjt/sp/jedit/bsh/commands", (5.0, 0.0))))
        assertTrue(list.contains(("org/gjt/sp/jedit/visitors", (3.0, 0.6666666666666666))))
        assertTrue(list.contains(("org/gjt/sp/jedit/proto/jeditresource", (3.0, 1.0))))
        assertTrue(list.contains(("org/gjt/sp/jedit/print", (4.6, 5.44)))) // there are more methods and packages in JEditSuite.allMethods.data. then you see in the package explore

    }
    @Test
    def fanOut : Unit = {
        import scala.collection.mutable.Set
        val methods : ObservableList[Method] = new ObservableList[Method] //JEditSuiteBefore.db.classfile_methods

        val groupByClassesAndCalcFanOut = Aggregation(methods, (x : Method) => (x.clazz.packageName, x.clazz.simpleName), sae.functions.FanOut((x : Method) => (x.parameters, x.returnType), y => true), (x : (String, String), y : Set[String]) => (x._1, x._2, y))
        val ob = new ObserverList[(String, String, Set[String])]()
        groupByClassesAndCalcFanOut.addObserver(ob)
        JEditSuite.allMethods.data.foreach(x => {
            methods.add(x)
        })

        val result : QueryResult[(String, String, Set[String])] = groupByClassesAndCalcFanOut
        var list = result.asList
        //val list = ob.data
        // list.foreach(println _)
        assertTrue(list.size == 1108) //there a 24 classes without methods
        var i = 0
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/gui" && x._2 == "ColorWellButton" && x._3.size == 2 && x._3.contains("java.awt.Color") && x._3.contains("void")) i += 1
        })
        assertTrue(i == 1)
        i = 0
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/gui/statusbar" && x._2 == "ToolTipLabel" && x._3.size == 3 && x._3.contains("java.awt.Point") && x._3.contains("void")) i += 1
        })
        assertTrue(i == 1)
        i = 0
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/bsh" && x._2 == "BSHTryStatement" && x._3.size == 5 && x._3.contains("int") && x._3.contains("void") && x._3.contains("java.lang.Object") && x._3.contains("org.gjt.sp.jedit.bsh.Interpreter") && x._3.contains("org.gjt.sp.jedit.bsh.CallStack")) i += 1
        })
        assertTrue(i == 1)
        i = 0
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/bsh/org/objectweb/asm" && x._2 == "ClassVisitor" && x._3.size == 6 && x._3.contains("java.lang.Object") && x._3.contains("int") && x._3.contains("java.lang.String[]") && x._3.contains("java.lang.String") && x._3.contains("org.gjt.sp.jedit.bsh.org.objectweb.asm.CodeVisitor")) { i += 1 }
        })
        assertTrue(i == 1)
        methods.remove(getMethode("org/gjt/sp/jedit/gui", "ColorWellButton", "<init>"))
        methods.remove(getMethode("org/gjt/sp/jedit/gui", "ColorWellButton", "setSelectedColor"))
        list = result.asList
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/gui" && x._2 == "ColorWellButton") {
                assertTrue(x._3.size == 1)
                assertTrue(x._3.contains("java.awt.Color"))
            }

        })
        list = result.asList
        assertTrue(list.size == 1108)
        methods.remove(getMethode("org/gjt/sp/jedit/gui", "ColorWellButton", "getSelectedColor"))
        list = result.asList
        assertTrue(list.size == 1107)
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/gui" && x._2 == "ColorWellButton") // && x._3.size == 2 && x._3.contains("java.awt.Color") && x._3.contains("void")) i += 1
                fail()
        })
        methods.update(getMethode("org/gjt/sp/jedit/bsh", "BSHTryStatement", "<init>"), getMethode("org/gjt/sp/jedit/bsh", "BSHType", "<init>"))
        list = result.asList
        i = 0
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/bsh" && x._2 == "BSHTryStatement") {
                assertTrue(x._3.size == 3)
                i += 1
                //x._3.contains("int") 
                //&& x._3.contains("void")
                assertTrue(x._3.contains("java.lang.Object") && x._3.contains("org.gjt.sp.jedit.bsh.Interpreter") && x._3.contains("org.gjt.sp.jedit.bsh.CallStack"))
            }

        })
        assertTrue(i == 1)
        methods.update(getMethode("org/gjt/sp/jedit/bsh", "BSHTryStatement", "eval"), getMethode("org/gjt/sp/jedit/bsh", "BSHType", "classLoaderChanged"))
        list = result.asList
        assertTrue(list.size == 1106)
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/bsh" && x._2 == "BSHTryStatement") {
                fail()
            }

        })

    }
    private def getMethode(pName : String, className : String, mName : String) = {
        val mybreaks = new Breaks
        import mybreaks.{ break, breakable }
        var res : Method = null
        breakable {
            JEditSuite.allMethods.data.foreach(x => {
                if (x.clazz.packageName == pName && x.clazz.simpleName == className && x.name == mName) {
                    res = x
                    break
                }
            })
        }
        res
    }
    private def getMethode(pName : String, className : String, mName : String, count : Int) = {
        val mybreaks = new Breaks
        import mybreaks.{ break, breakable }
        var res : Method = null
        var i = 1
        breakable {
            JEditSuite.allMethods.data.foreach(x => {
                if (x.clazz.packageName == pName && x.clazz.simpleName == className && x.name == mName) {
                    if (i == count) {
                        res = x
                        break
                    } else {
                        i += 1
                    }
                }
            })
        }
        res
    }
    @Test
    def fanIn() : Unit = {
        import scala.collection.mutable.Set
        val methods : ObservableList[Method] = new ObservableList[Method] //JEditSuiteBefore.db.classfile_methods
        val groupByClassesAndCalcFanOut = Aggregation(methods, (x : Method) => (x.clazz.packageName, x.clazz.simpleName), sae.functions.FanOut((x : Method) => (x.parameters, x.returnType), y => true), (x : (String, String), y : Set[String]) => (x._1, x._2, y))
        //        var out_file = new java.io.FileOutputStream("testtttttttt.txt")
        //        var out_stream = new java.io.PrintStream(out_file)
        //        out_stream.print("\n" + x)
        //        out_stream.close
        JEditSuite.allMethods.data.foreach(x => {
            methods.add(x)
        })

        def fanInFor(s : String) = {
            Aggregation(new MaterializedSelection((x : (String, String, Set[String])) => { x._3.contains(s) && (x._1.replace('/', '.') + "." + x._2) != s }, groupByClassesAndCalcFanOut), Count[(String, String, Set[String])])
        }
        val t : QueryResult[(String, String, Set[String])] = groupByClassesAndCalcFanOut
        val classes : ObservableList[ClassFile] = new ObservableList[sae.bytecode.model.ClassFile] //JEditSuiteBefore.db.classfiles
        val res1 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.jEdit")
        val res2 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.bsh.SimpleNode")
        val res3 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.Buffer")
        val res4 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.ActionSet")
        assertTrue(res1.asList.size == 0 && res1.singletonValue == None)
        assertTrue(res2.asList.size == 1 && res2.singletonValue == Some(Some(19)))
        assertTrue(res3.asList.size == 1 && res3.singletonValue == Some(Some(51)))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(4)))
        //Method(ClassFile(org/gjt/sp/jedit,ActionContext),getActionSetForAction,List(ObjectType(className="java/lang/String")),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //
        //Method(ClassFile(org/gjt/sp/jedit,Macros),getMacroActionSet,List(),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //
        //Method(ClassFile(org/gjt/sp/jedit,PluginJAR),getActions,List(),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //Method(ClassFile(org/gjt/sp/jedit,PluginJAR),getActionSet,List(),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //Method(ClassFile(org/gjt/sp/jedit,PluginJAR),getBrowserActionSet,List(),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //
        //Method(ClassFile(org/gjt/sp/jedit,jEdit),addActionSet,List(ObjectType(className="org/gjt/sp/jedit/ActionSet")),VoidType)
        //Method(ClassFile(org/gjt/sp/jedit,jEdit),removeActionSet,List(ObjectType(className="org/gjt/sp/jedit/ActionSet")),VoidType)
        //Method(ClassFile(org/gjt/sp/jedit,jEdit),getBuiltInActionSet,List(),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //Method(ClassFile(org/gjt/sp/jedit,jEdit),getActionSets,List(),ArrayType(ObjectType(className="org/gjt/sp/jedit/ActionSet")))
        //Method(ClassFile(org/gjt/sp/jedit,jEdit),getActionSetForAction,List(ObjectType(className="java/lang/String")),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        //Method(ClassFile(org/gjt/sp/jedit,jEdit),getActionSetForAction,List(ObjectType(className="org/gjt/sp/jedit/EditAction")),ObjectType(className="org/gjt/sp/jedit/ActionSet"))
        methods.remove(getMethode("org/gjt/sp/jedit", "jEdit", "addActionSet"))
        methods.remove(getMethode("org/gjt/sp/jedit", "jEdit", "removeActionSet"))
        methods.remove(getMethode("org/gjt/sp/jedit", "jEdit", "getBuiltInActionSet"))
        methods.remove(getMethode("org/gjt/sp/jedit", "jEdit", "getActionSets"))
        methods.remove(getMethode("org/gjt/sp/jedit", "jEdit", "getActionSetForAction"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(4)))
        methods.remove(getMethode("org/gjt/sp/jedit", "jEdit", "getActionSetForAction",2))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(3)))
        methods.remove(getMethode("org/gjt/sp/jedit", "PluginJAR", "getActions"))
        methods.remove(getMethode("org/gjt/sp/jedit", "PluginJAR", "getActionSet"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(3)))
        methods.remove(getMethode("org/gjt/sp/jedit", "PluginJAR", "getBrowserActionSet"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(2)))
        methods.update(getMethode("org/gjt/sp/jedit", "Macros", "getMacroActionSet"), getMethode("org/gjt/sp/jedit", "PluginJAR","activatePlugin" ))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(1)))
        methods.update(getMethode("org/gjt/sp/jedit", "ActionContext", "getActionSetForAction"),getMethode("org/gjt/sp/jedit", "JEditKillRing","<init>" ))
        assertTrue(res4.asList.size == 0 && res4.singletonValue == None)
        /*
 org.gjt.sp.jedit.jEdit 0
 org.gjt.sp.jedit.bsh.SimpleNode 19 
  org.gjt.sp.jedit.Buffer 51
  org.gjt.sp.jedit.ActionSet 4
 */
        //                var i = 0
        //                var j = 0
        //                JEditSuite.allClassfiles.data.foreach(z => {
        //                    j += 1
        //                    fanInFor(z.packageName.replace('/', '.') + "." + z.simpleName).foreach(y => {
        //                        i += 1
        //                        println(z.simpleName + ": " + y)
        //                    })
        //                })
        //                println(j)
        //                println(i)
        //            	fanInFor("java.lang.String").foreach(x => println("String: " + x))
        //            	fanInFor("org.gjt.sp.jedit.bsh.Interpreter").foreach(x => println("org.gjt.sp.jedit.bsh.Interpreter: " + x))
        //            	fanInFor("javax.swing.ListModel").foreach(x => println("javax.swing.ListModel: " + x))
        //            	fanInFor("void").foreach(x => println("void: " + x))
        //            	fanInFor("org.gjt.sp.jedit.textarea.TextAreaPainter").foreach(x => println("org.gjt.sp.jedit.textarea.TextAreaPainter: " + x))

    }

}




