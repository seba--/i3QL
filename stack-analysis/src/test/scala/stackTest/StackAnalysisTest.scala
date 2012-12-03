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
import de.tud.cs.st.bat.resolved.{BooleanType, IntegerType, ObjectType}
import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo
import java.util.Date

import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo
import scala.Some


import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo
import scala.Some
import sandbox.stackAnalysis.TypeOption.NoneType
import sandbox.stackAnalysis.StackAnalysis
import sandbox.dataflowAnalysis.MethodResult
import sae.bytecode.structure.CodeInfo
import sandbox.stackAnalysis.LocVariables
import sandbox.stackAnalysis.Configuration
import sandbox.stackAnalysis.Stacks
import sandbox.stackAnalysis.CodeInfoTransformer
import sandbox.stackAnalysis.LocVariable


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 21.11.12
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */

object StackAnalysisTest extends org.scalatest.junit.JUnitSuite {
  var methodSetAccessible : QueryResult[MethodResult[Configuration]] = null
  var methodIsLoggable : QueryResult[MethodResult[Configuration]] = null

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
    //analysis.printResults = true
    methodSetAccessible = compile(SELECT (*) FROM analysis.result WHERE ((_ : MethodResult[Configuration]).declaringMethod.declaringClass.classType equals ObjectType("com/oracle/net/Sdp")) AND ((_ : MethodResult[Configuration]).declaringMethod.name == "setAccessible"))
    methodIsLoggable = compile(SELECT (*) FROM analysis.result WHERE ((_ : MethodResult[Configuration]).declaringMethod.declaringClass.classType equals ObjectType("com/sun/activation/registries/LogSupport")) AND ((_ : MethodResult[Configuration]).declaringMethod.name == "isLoggable"))

    database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    println("Finish analysis: " + new Date())
  }
}

class StackAnalysisTest {
  @Test
  def testSetAccessible() {
    //Get the result
    val results : Array[Configuration] = StackAnalysisTest.methodSetAccessible.asList(0).resultArray

    //Build the expected result
    val expected : Array[Configuration] = Array.ofDim[Configuration](13)
    var baseRes = Configuration(Stacks(3,Nil).addStack(),LocVariables(Array.fill[LocVariable](1)(LocVariable(NoneType :: Nil))))
    baseRes = Configuration(baseRes.s,baseRes.l.setVar(0,ObjectType("java/lang/reflect/AccessibleObject"),-1))
    expected(0) = baseRes
    baseRes = Configuration(baseRes.s.push(ObjectType("com/oracle/net/Sdp$1"),0),baseRes.l)
    expected(3) = baseRes
    baseRes = Configuration(baseRes.s.push(ObjectType("com/oracle/net/Sdp$1"),0),baseRes.l)
    expected(4) = baseRes
    baseRes = Configuration(baseRes.s.push(ObjectType("java/lang/reflect/AccessibleObject"),4),baseRes.l)
    expected(5) = baseRes
    baseRes = Configuration(baseRes.s.pop().pop(),baseRes.l)
    expected(8) = baseRes
    baseRes = Configuration(baseRes.s.pop().push(ObjectType("java/lang/Object"),8),baseRes.l)
    expected(11) = baseRes
    baseRes = Configuration(baseRes.s.pop(),baseRes.l)
    expected(12) = baseRes

    //Test result with the expected result
    Assert.assertEquals(expected.length, results.length)

    for(i <- 0 until expected.length)
      Assert.assertEquals(expected(i),results(i))

  }

  @Test
  def testIsLoggable() {
    //Get the result
    val results : Array[Configuration] = StackAnalysisTest.methodIsLoggable.asList(0).resultArray



    //Build the expected result
    val expected : Array[Configuration] = Array.ofDim[Configuration](24)
    var baseRes = Configuration(Stacks(2,Nil).addStack(),LocVariables(Array.fill[LocVariable](0)(LocVariable(NoneType :: Nil))))
    baseRes = Configuration(baseRes.s,baseRes.l)
    expected(0) = baseRes
    baseRes = Configuration(baseRes.s.push(BooleanType,0),baseRes.l)
    expected(3) = baseRes
    baseRes = Configuration(baseRes.s.pop(),baseRes.l)
    expected(6) = baseRes
    baseRes = Configuration(baseRes.s.push(ObjectType("java/util/logging/Logger"),6),baseRes.l)
    expected(9) = baseRes
    baseRes = Configuration(baseRes.s.push(ObjectType("java/util/logging/Level"),9),baseRes.l)
    expected(12) = baseRes
    baseRes = Configuration(baseRes.s.pop().pop().push(BooleanType,12),baseRes.l)
    expected(15) = baseRes
    baseRes = Configuration(baseRes.s.pop(),baseRes.l)
    expected(18) = baseRes
    baseRes = Configuration(baseRes.s.push(IntegerType,18),baseRes.l)
    expected(19) = baseRes
    expected(22) = expected(18)
    expected(23) = expected(19).combineWith(Configuration(expected(22).s.push(IntegerType,22),expected(22).l))

    //Test result with the expected result
    Assert.assertEquals(expected.length, results.length)

    for(i <- 0 until expected.length)
      Assert.assertEquals(expected(i),results(i))


  }
}
