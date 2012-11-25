package stackTest


import org.scalatest.junit.AssertionsForJUnit
import org.junit.{BeforeClass, Before, Assert, Test}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSuite
import sae.{Relation, QueryResult}
import sandbox.stackAnalysis.Types._
import sandbox.stackAnalysis._
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.CodeInfo
import sae.syntax.sql._
import sae.bytecode.structure.CodeInfo
import sae.bytecode.structure.CodeInfo
import sandbox.dataflowAnalysis.MethodResult
import java.io.FileInputStream
import de.tud.cs.st.bat.resolved._
import sandbox.stackAnalysis.Result
import sandbox.stackAnalysis.CodeInfoTransformer
import sandbox.stackAnalysis.StackAnalysis
import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo
import java.util.Date


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 21.11.12
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */

object StackAnalysisTest extends org.scalatest.junit.JUnitSuite {
  var methodSetAccessible : QueryResult[MethodResult[StackResult]] = null
  var methodIsLoggable : QueryResult[MethodResult[StackResult]] = null

  @BeforeClass
  def start() {
    println("Start analysis: " + new Date())
    //Setup the database
    val database = BATDatabaseFactory.create()
    val infos: Relation[CodeInfo] = compile(SELECT(*) FROM database.code)

    //Setup the analysis
    val cfg = new CodeInfoCFG(infos)
    val funs = new CodeInfoTransformer(infos)
    val analysis : StackAnalysis = new StackAnalysis(infos, cfg, funs)

    methodSetAccessible = compile(SELECT (*) FROM analysis.result WHERE ((_ : MethodResult[StackResult]).declaringMethod.declaringClass.classType equals ObjectType("com/oracle/net/Sdp")) AND ((_ : MethodResult[StackResult]).declaringMethod.name == "setAccessible"))
    methodIsLoggable = compile(SELECT (*) FROM analysis.result WHERE ((_ : MethodResult[StackResult]).declaringMethod.declaringClass.classType equals ObjectType("com/sun/activation/registries/LogSupport")) AND ((_ : MethodResult[StackResult]).declaringMethod.name == "isLoggable"))

    database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    println("Finish analysis: " + new Date())
  }
}

class StackAnalysisTest {
  @Test
  def testSetAccessible() {
    //Get the result
    val results : Array[StackResult] = StackAnalysisTest.methodSetAccessible.asList(0).resultArray

    println("setAccessibleResults:")
    results.foreach(println)

    //Build the expected result
    val expected : Array[StackResult] = Array.ofDim[StackResult](13)
    var baseRes = Result[Type,Int](Stacks[Type,Int](3,Nil,Nil,Nil :: Nil),new LocalVars[Type, Int](1, None, None :: Nil))
    baseRes = Result[Type,Int](baseRes.s,baseRes.l.setVar(0,1,Some(ObjectType("java/lang/reflect/AccessibleObject")),Some(-1)))
    expected(0) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,ObjectType("com/oracle/net/Sdp$1"),0),baseRes.l)
    expected(3) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,ObjectType("com/oracle/net/Sdp$1"),0),baseRes.l)
    expected(4) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,ObjectType("java/lang/reflect/AccessibleObject"),4),baseRes.l)
    expected(5) = baseRes
    baseRes = Result[Type,Int](baseRes.s.pop().pop(),baseRes.l)
    expected(8) = baseRes
    baseRes = Result[Type,Int](baseRes.s.pop().push(1, ObjectType("java/lang/Object"),8),baseRes.l)
    expected(11) = baseRes
    baseRes = Result[Type,Int](baseRes.s.pop(),baseRes.l)
    expected(12) = baseRes

    //Test result with the expected result
    Assert.assertEquals(expected.length, results.length)

    for(i <- 0 until expected.length) {
      if(expected(i) != null)
        Assert.assertEquals("PC" + i,expected(i),results(i))
    }

  }

  @Test
  def testIsLoggable() {
    //Get the result
    val results : Array[StackResult] = StackAnalysisTest.methodIsLoggable.asList(0).resultArray

    println("isLoggableResults:")
    results.foreach(println)

    //Build the expected result
    val expected : Array[StackResult] = Array.ofDim[StackResult](24)
    var baseRes = Result[Type,Int](Stacks[Type,Int](2,Nil,Nil,Nil :: Nil),new LocalVars[Type, Int](0, None, None :: Nil))
    baseRes = Result[Type,Int](baseRes.s,baseRes.l)
    expected(0) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,BooleanType,0),baseRes.l)
    expected(3) = baseRes
    baseRes = Result[Type,Int](baseRes.s.pop(),baseRes.l)
    expected(6) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,ObjectType("java/util/logging/Logger"),6),baseRes.l)
    expected(9) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,ObjectType("java/util/logging/Level"),9),baseRes.l)
    expected(12) = baseRes
    baseRes = Result[Type,Int](baseRes.s.pop().pop().push(1,BooleanType,12),baseRes.l)
    expected(15) = baseRes
    baseRes = Result[Type,Int](baseRes.s.pop(),baseRes.l)
    expected(18) = baseRes
    baseRes = Result[Type,Int](baseRes.s.push(1,IntegerType,18),baseRes.l)
    expected(19) = baseRes
    expected(22) = expected(18)
    expected(23) = expected(19).combineWith(Result[Type,Int](expected(22).s.push(1,IntegerType,22),expected(22).l))


    //Test result with the expected result
    Assert.assertEquals(expected.length, results.length)

    for(i <- 0 until expected.length) {
      if(expected(i) != null)
        Assert.assertEquals("PC" + i, expected(i),results(i))
    }

  }
}
