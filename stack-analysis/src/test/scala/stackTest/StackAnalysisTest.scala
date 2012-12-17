package stackTest


import org.junit.{BeforeClass, Assert, Test}
import sae.QueryResult
import sandbox.stackAnalysis._
import codeInfo.StackAnalysis
import datastructure._
import datastructure.LocVariables
import datastructure.Stacks
import datastructure.State
import sae.bytecode.bat.BATDatabaseFactory
import sae.syntax.sql._
import java.io.FileInputStream
import de.tud.cs.st.bat.resolved.{BooleanType, IntegerType, ObjectType}
import java.util.Date
import sandbox.dataflowAnalysis.MethodResult


/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 21.11.12
 * Time: 18:10
 * To change this template use File | Settings | File Templates.
 */

object StackAnalysisTest extends org.scalatest.junit.JUnitSuite {
  var methodSetAccessible: QueryResult[MethodResult[State]] = null
  var methodIsLoggable: QueryResult[MethodResult[State]] = null

  @BeforeClass
  def start() {
    println("Start analysis: " + new Date())
    //Setup the database
    val database = BATDatabaseFactory.create()
    val analysis = StackAnalysis(database)

    methodSetAccessible = compile(SELECT(*) FROM analysis WHERE ((_: MethodResult[State]).declaringMethod.declaringClass.classType equals ObjectType("com/oracle/net/Sdp")) AND ((_: MethodResult[State]).declaringMethod.name equals "setAccessible"))
    methodIsLoggable = compile(SELECT(*) FROM analysis WHERE ((_: MethodResult[State]).declaringMethod.declaringClass.classType equals ObjectType("com/sun/activation/registries/LogSupport")) AND ((_: MethodResult[State]).declaringMethod.name equals "isLoggable"))

    database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    println("Finish analysis: " + new Date())


  }
}

class StackAnalysisTest {
  @Test
  def testSetAccessible() {
    //Get the result
    val results: Array[State] = StackAnalysisTest.methodSetAccessible.asList(0).resultArray


    //Build the expected result
    val expected: Array[State] = Array.ofDim[State](13)
    var baseRes = State(Stacks(3, Nil).addStack(), LocVariables(Array.fill[Item](1)(Item(ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED))))
    baseRes = State(baseRes.s, baseRes.l.setVar(0, Item(ObjectType("java/lang/reflect/AccessibleObject"), -1, Item.FLAG_IS_PARAMETER)))
    expected(0) = baseRes
    baseRes = State(baseRes.s.push(Item(ObjectType("com/oracle/net/Sdp$1"), 0, Item.FLAG_IS_CREATED_BY_NEW)), baseRes.l)
    expected(3) = baseRes
    baseRes = State(baseRes.s.push(Item(ObjectType("com/oracle/net/Sdp$1"), 0, Item.FLAG_IS_CREATED_BY_NEW)), baseRes.l)
    expected(4) = baseRes
    baseRes = State(baseRes.s.push(Item(ItemType.fromType(ObjectType("java/lang/reflect/AccessibleObject")), -1, Item.FLAG_IS_PARAMETER)), baseRes.l)
    expected(5) = baseRes
    baseRes = State(baseRes.s.pop().pop(), baseRes.l)
    expected(8) = baseRes
    baseRes = State(baseRes.s.pop().push(Item(ObjectType("java/lang/Object"), 8, Item.FLAG_IS_RETURN_VALUE)), baseRes.l)
    expected(11) = baseRes
    baseRes = State(baseRes.s.pop(), baseRes.l)
    expected(12) = baseRes

    //Test result with the expected result
    Assert.assertEquals(expected.length, results.length)
    Assert.assertArrayEquals(expected.asInstanceOf[Array[AnyRef]], results.asInstanceOf[Array[AnyRef]])

  }

  @Test
  def testIsLoggable() {
    //Get the result
    val results: Array[State] = StackAnalysisTest.methodIsLoggable.asList(0).resultArray



    //Build the expected result
    val expected: Array[State] = Array.ofDim[State](24)
    var baseRes = State(Stacks(2, Nil).addStack(), LocVariables(Array.fill[Item](0)(Item(ItemType.None, -1, Item.FLAG_IS_NOT_INITIALIZED))))
    baseRes = State(baseRes.s, baseRes.l)
    expected(0) = baseRes
    baseRes = State(baseRes.s.push(BooleanType, 0), baseRes.l)
    expected(3) = baseRes
    baseRes = State(baseRes.s.pop(), baseRes.l)
    expected(6) = baseRes
    baseRes = State(baseRes.s.push(ObjectType("java/util/logging/Logger"), 6), baseRes.l)
    expected(9) = baseRes
    baseRes = State(baseRes.s.push(ObjectType("java/util/logging/Level"), 9), baseRes.l)
    expected(12) = baseRes
    baseRes = State(baseRes.s.pop().pop().push(Item(BooleanType, 12, Item.FLAG_IS_RETURN_VALUE)), baseRes.l)
    expected(15) = baseRes
    baseRes = State(baseRes.s.pop(), baseRes.l)
    expected(18) = baseRes
    baseRes = State(baseRes.s.push(IntegerType, 18), baseRes.l)
    expected(19) = baseRes
    expected(22) = expected(18)
    expected(23) = expected(19).combineWith(State(expected(22).s.push(Item(ItemType.fromType(IntegerType), 22, Item.FLAG_COULD_BE_ZERO)), expected(22).l))

    //Test result with the expected result
    Assert.assertEquals(expected.length, results.length)
    Assert.assertArrayEquals(expected.asInstanceOf[Array[AnyRef]], results.asInstanceOf[Array[AnyRef]])


  }
}
