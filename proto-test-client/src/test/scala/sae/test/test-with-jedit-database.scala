package sae

package test

import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert._
import scala.collection.mutable.ListBuffer
import sae.collections._
import sae.bytecode.model._
import de.tud.cs.st.bat._
import sae.syntax.RelationalAlgebraSyntax._
import sae.functions._
import sae.operators._
import sae.test.helpFunctions._
import scala.util.control._
import sae.bytecode._
import junit.framework.TestSuite


class JEditSuite {
    import sae.test.helpFunctions._

    private var database : MaterializedDatabase = null

    private val resourceName = "jedit-4.3.3-win.jar"

    def db = {
        if(database == null){
            database = new MaterializedDatabase()
            database.addArchiveAsResource(resourceName)
        }
        database
    }


    @Test
    def count_classfiles : Unit = {
        // the materialized db is already set up 
        // test that values were propagated to the results
        assertEquals(1132, db.classfiles.size);

    }

    @Test
    def count_classfiles_by_package : Unit =
        {
            val db = new BytecodeDatabase

            val groupByPackage = γ(
                db.classfiles,
                (_ : ObjectType).packageName,
                Count[ObjectType],
                (x : String, y : Int) => (x, y)
            )

            db.addArchiveAsResource(resourceName)

            val result = List( // verified in swi-prolog
                ("org/gjt/sp/jedit/bufferset", 13),
                ("org/gjt/sp/jedit/print", 5),
                ("org/gjt/sp/jedit/pluginmgr", 71),
                ("org/gjt/sp/jedit/syntax", 18),
                ("org/gjt/sp/jedit/options", 97),
                ("org/gjt/sp/jedit/bsh/org/objectweb/asm", 10),
                ("org/gjt/sp/jedit/bsh", 103),
                ("org/gjt/sp/jedit/menu", 29),
                ("com/microstar/xml", 4),
                ("org/gjt/sp/jedit/textarea", 68),
                ("org/gjt/sp/jedit/browser", 58),
                ("org/gjt/sp/jedit/bsh/commands", 1),
                ("org/gjt/sp/jedit/gui/statusbar", 51),
                ("org/gjt/sp/jedit/buffer", 28),
                ("org/gjt/sp/jedit/help", 35),
                ("org/gjt/sp/jedit/io", 30),
                ("org/gjt/sp/jedit/indent", 22),
                ("org/gjt/sp/jedit/gui", 213),
                ("org/gjt/sp/jedit/proto/jeditresource", 2),
                ("org/gjt/sp/jedit/visitors", 3),
                ("org/gjt/sp/jedit/bsh/collection", 2),
                ("org/gjt/sp/util", 26),
                ("org/gjt/sp/jedit/msg", 18),
                ("org/gjt/sp/jedit", 137),
                ("org/gjt/sp/jedit/bsh/classpath", 13),
                ("org/gjt/sp/jedit/search", 62),
                ("org/gjt/sp/jedit/input", 4),
                ("org/gjt/sp/jedit/bufferio", 8),
                ("org/gjt/sp/jedit/bsh/reflect", 1))
            assertEquals(result, groupByPackage.asList)
        }

    @Test
    def count_classfile_methods : Unit = {
        // the materialized db is already set up 
        // test that values were propagated to the results
        assertEquals(7999, db.classfile_methods.size);
    }

    @Test
    def count_method_calls : Unit = {
        // the materialized db is already set up 
        // test that values were propagated to the results
        val db = new BytecodeDatabase
        
        val query : QueryResult[MethodCall] = db.method_calls
        // reuse existing data without loading bytecode from filesystem again
        this.db.method_calls.foreach( db.method_calls.element_added )
        
        assertEquals(44776, query.size)
    }

    @Test
    def count_internal_method_calls : Unit = {
        val db = new BytecodeDatabase
        // the cross product would generate (7999 * 44776) ~ 350 million entries    
        // naive query // val query : QueryResult[MethodCall] = Π( (_:(MethodCall,Method))._1 )(db.method_calls ⋈( (_:(MethodCall,Method)) match {case (c:MethodCall, m:Method) => c.target == m} , db.classfile_methods));

        val query : QueryResult[MethodCall] = ((db.method_calls, (_ : MethodCall).target) ⋈ ((m : Method) => m, db.classfile_methods)) { (c : MethodCall, m : Method) => c }

        // reuse existing data without loading bytecode from filesystem again
        this.db.classfile_methods.foreach( db.classfile_methods.element_added )
        this.db.method_calls.foreach( db.method_calls.element_added )

        assertEquals(20358, query.size)
    }

    @Test
    def count_distinct_internal_method_calls : Unit = {
        val db = new BytecodeDatabase
        // the cross product would generate (7999 * 44776) ~ 350 million entries    
        // naive query // val query : QueryResult[MethodCall] = Π( (_:(MethodCall,Method))._1 )(db.method_calls ⋈( (_:(MethodCall,Method)) match {case (c:MethodCall, m:Method) => c.target == m} , db.classfile_methods));

        val query : QueryResult[(Method, Method)] = δ(Π((c : MethodCall) => (c.source, c.target))(((db.method_calls, (_ : MethodCall).target) ⋈ ((m : Method) => m, db.classfile_methods)) { (c : MethodCall, m : Method) => c }))

        // reuse existing data without loading bytecode from filesystem again
        this.db.classfile_methods.foreach(db.classfile_methods.element_added)
        this.db.method_calls.foreach( db.method_calls.element_added )

        assertEquals(14847, query.size)
    }

    @Test
    def count_extends_relation : Unit = {
        assertEquals(1132, db.`extends`.size)
    }

    @Test
    def count_implements_relation : Unit = {
        assertEquals(513, db.implements.size)
    }

    @Test
    def count_field_type_relation : Unit = {
        assertEquals(2504, db.fieldType.size)
    }

    @Test
    def count_parameter_relation : Unit = {
        assertEquals(9248, db.parameter.size)
    }

    @Test
    def count_internal_parameter_relation : Unit = {
        assertEquals(6222, db.parameter.size)
    }

    @Test
    def count_return_type_relation : Unit = {
        assertEquals(7999, db.return_type.size)
    }

    @Test
    def count_internal_return_type_relation : Unit = {
        assertEquals(1999, db.return_type.size)
    }

    def find_classfile_with_max_methods_pair_package : Unit = {
        val db = new BytecodeDatabase

        val groupByClassesAndCountMethods = Aggregation(db.classfile_methods, (x : Method) => (x.declaringRef.packageName, x.declaringRef.simpleName), Count[Method], (x : (String, String), y : Int) => (x._1, x._2, y))

        val groupByPackageFindClassWithMaxMethods = Aggregation(groupByClassesAndCountMethods,
            (x : (String, String, Int)) => x._1,
            Max2[(String, String, Int), Option[(String, String, Int)]]((x : (String, String, Int)) => x._3, (y : Option[(String, String, Int)], x : Int) => y),
            (x : String, y : Option[(String, String, Int)]) => y)

        val result : QueryResult[Option[(String, String, Int)]] = groupByPackageFindClassWithMaxMethods

        // reuse existing data without loading bytecode from filesystem again
        this.db.classfile_methods.foreach(db.classfile_methods.element_added)

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
        val db = new BytecodeDatabase
        val groupByClassesAndCountMethods = Aggregation(db.classfile_methods, (x : Method) => (x.declaringRef.packageName, x.declaringRef.simpleName), Count[Method], (x : (String, String), y : Int) => (x._1, x._2, y))

        val pseudovarianzoveravg = Aggregation(groupByClassesAndCountMethods,
            (x : (String, String, Int)) => x._1,
            PseudoVarianz((x : (String, String, Int)) => x._3),
            (x : String, y : (Double, Double)) => (x, y))

        val result : QueryResult[(String, (Double, Double))] = pseudovarianzoveravg

        // reuse existing data without loading bytecode from filesystem again
        this.db.classfile_methods.foreach(db.classfile_methods.element_added)

        val list = result.asList

        //TODO add some asserts for update and remove events
        assertTrue(list.size == 29)
        assertTrue(list.contains(("org/gjt/sp/jedit/bsh/commands", (5.0, 0.0))))
        assertTrue(list.contains(("org/gjt/sp/jedit/visitors", (3.0, 0.6666666666666666))))
        assertTrue(list.contains(("org/gjt/sp/jedit/proto/jeditresource", (3.0, 1.0))))
        assertTrue(list.contains(("org/gjt/sp/jedit/print", (4.6, 5.44)))) // there are more methods and packages in JEditSuite.allMethods.data. then you see in the package explore

    }

    @Test
    @Ignore
    def fanOut : Unit = {
        import scala.collection.mutable.Set

        val db = new BytecodeDatabase

        val groupByClassesAndCalcFanOut = Aggregation(db.classfile_methods, (x : Method) => (x.declaringRef.packageName, x.declaringRef.simpleName), sae.functions.FanOut((x : Method) => (x.parameters, x.returnType), y => true), (x : (String, String), y : Set[String]) => (x._1, x._2, y))

        val result : QueryResult[(String, String, Set[String])] = groupByClassesAndCalcFanOut

        // reuse existing data without loading bytecode from filesystem again
        this.db.classfile_methods.foreach(db.classfile_methods.element_added)

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
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit/gui", "ColorWellButton", "<init>"))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit/gui", "ColorWellButton", "setSelectedColor"))
        list = result.asList
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/gui" && x._2 == "ColorWellButton") {
                assertTrue(x._3.size == 1)
                assertTrue(x._3.contains("java.awt.Color"))
            }

        })
        list = result.asList
        assertTrue(list.size == 1108)
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit/gui", "ColorWellButton", "getSelectedColor"))
        list = result.asList
        assertTrue(list.size == 1107)
        list.foreach(x => {
            if (x._1 == "org/gjt/sp/jedit/gui" && x._2 == "ColorWellButton") // && x._3.size == 2 && x._3.contains("java.awt.Color") && x._3.contains("void")) i += 1
                fail()
        })
        db.classfile_methods.element_updated(getMethode("org/gjt/sp/jedit/bsh", "BSHTryStatement", "<init>"), getMethode("org/gjt/sp/jedit/bsh", "BSHType", "<init>"))
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
        db.classfile_methods.element_updated(getMethode("org/gjt/sp/jedit/bsh", "BSHTryStatement", "eval"), getMethode("org/gjt/sp/jedit/bsh", "BSHType", "classLoaderChanged"))
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
            this.db.classfile_methods.foreach(x => {
                if (x.declaringRef.packageName == pName && x.declaringRef.simpleName == className && x.name == mName) {
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
            this.db.classfile_methods.foreach(x => {
                if (x.declaringRef.packageName == pName && x.declaringRef.simpleName == className && x.name == mName) {
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
    @Ignore
    def fanIn() : Unit = {
        import scala.collection.mutable.Set
        val db = new BytecodeDatabase

        val groupByClassesAndCalcFanOut = Aggregation(db.classfile_methods, (x : Method) => (x.declaringRef.packageName, x.declaringRef.simpleName), sae.functions.FanOut((x : Method) => (x.parameters, x.returnType), y => true), (x : (String, String), y : Set[String]) => (x._1, x._2, y))
        //        var out_file = new java.io.FileOutputStream("testtttttttt.txt")
        //        var out_stream = new java.io.PrintStream(out_file)
        //        out_stream.print("\n" + x)
        //        out_stream.close

        def fanInFor(s : String) = {
            Aggregation(new MaterializedSelection((x : (String, String, Set[String])) => { x._3.contains(s) && (x._1.replace('/', '.') + "." + x._2) != s }, groupByClassesAndCalcFanOut), Count[(String, String, Set[String])])
        }
        val t : QueryResult[(String, String, Set[String])] = groupByClassesAndCalcFanOut

        val res1 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.jEdit")
        val res2 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.bsh.SimpleNode")
        val res3 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.Buffer")
        val res4 : QueryResult[Some[Int]] = fanInFor("org.gjt.sp.jedit.ActionSet")
        
        this.db.classfile_methods.foreach( db.classfile_methods.element_added )
        
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
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "jEdit", "addActionSet"))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "jEdit", "removeActionSet"))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "jEdit", "getBuiltInActionSet"))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "jEdit", "getActionSets"))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "jEdit", "getActionSetForAction"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(4)))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "jEdit", "getActionSetForAction", 2))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(3)))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "PluginJAR", "getActions"))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "PluginJAR", "getActionSet"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(3)))
        db.classfile_methods.element_removed(getMethode("org/gjt/sp/jedit", "PluginJAR", "getBrowserActionSet"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(2)))
        db.classfile_methods.element_updated(getMethode("org/gjt/sp/jedit", "Macros", "getMacroActionSet"), getMethode("org/gjt/sp/jedit", "PluginJAR", "activatePlugin"))
        assertTrue(res4.asList.size == 1 && res4.singletonValue == Some(Some(1)))
        db.classfile_methods.element_updated(getMethode("org/gjt/sp/jedit", "ActionContext", "getActionSetForAction"), getMethode("org/gjt/sp/jedit", "JEditKillRing", "<init>"))
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

