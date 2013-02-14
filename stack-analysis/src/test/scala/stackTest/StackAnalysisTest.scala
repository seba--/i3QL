package stackTest


import org.junit.{BeforeClass, Assert, Test}
import sae.QueryResult
import sandbox.stackAnalysis._
import codeInfo.CIStackAnalysis
import datastructure._
import datastructure.LocalVariables
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
    val analysis = CIStackAnalysis(database)

    methodSetAccessible = compile(SELECT(*) FROM analysis WHERE ((_: MethodResult[State]).declaringMethod.declaringClass.classType equals ObjectType("com/oracle/net/Sdp")) AND ((_: MethodResult[State]).declaringMethod.name equals "setAccessible"))
    methodIsLoggable = compile(SELECT(*) FROM analysis WHERE ((_: MethodResult[State]).declaringMethod.declaringClass.classType equals ObjectType("com/sun/activation/registries/LogSupport")) AND ((_: MethodResult[State]).declaringMethod.name equals "isLoggable"))

    database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    println("Finish analysis: " + new Date())


  }
}

class StackAnalysisTest {
  @Test
  def testSetAccessible() {
    //Get the newResult
    val results: Array[State] = StackAnalysisTest.methodSetAccessible.asList(0).result


    //Build the expected newResult
    val expected: Array[State] = Array.ofDim[State](13)
    var baseRes = State(Stacks(3, Nil).addStack(), LocalVariables(Array.fill[Item](1)(Item( -1, ItemType.None, Item.FLAG_IS_NOT_INITIALIZED))))
    baseRes = State(baseRes.stacks, baseRes.variables.setVar(0, Item( -1, ObjectType("java/lang/reflect/AccessibleObject"), Item.FLAG_IS_PARAMETER)))
    expected(0) = baseRes
    baseRes = State(baseRes.stacks.push(Item( 0, ObjectType("com/oracle/net/Sdp$1"), Item.FLAG_IS_CREATED_BY_NEW)), baseRes.variables)
    expected(3) = baseRes
    baseRes = State(baseRes.stacks.push(Item( 0, ObjectType("com/oracle/net/Sdp$1"), Item.FLAG_IS_CREATED_BY_NEW)), baseRes.variables)
    expected(4) = baseRes
    baseRes = State(baseRes.stacks.push(Item( -1, ItemType.fromType(ObjectType("java/lang/reflect/AccessibleObject")), Item.FLAG_IS_PARAMETER)), baseRes.variables)
    expected(5) = baseRes
    baseRes = State(baseRes.stacks.pop().pop(), baseRes.variables)
    expected(8) = baseRes
    baseRes = State(baseRes.stacks.pop().push(Item( 8, ObjectType("java/lang/Object"), Item.FLAG_IS_RETURN_VALUE)), baseRes.variables)
    expected(11) = baseRes
    baseRes = State(baseRes.stacks.pop(), baseRes.variables)
    expected(12) = baseRes

    //Test newResult with the expected newResult
    Assert.assertEquals(expected.length, results.length)
    Assert.assertArrayEquals(expected.asInstanceOf[Array[AnyRef]], results.asInstanceOf[Array[AnyRef]])

  }

  @Test
  def testIsLoggable() {
    //Get the newResult
    val results: Array[State] = StackAnalysisTest.methodIsLoggable.asList(0).result



    //Build the expected newResult
    val expected: Array[State] = Array.ofDim[State](24)
    var baseRes = State(Stacks(2, Nil).addStack(), LocalVariables(Array.fill[Item](0)(Item( -1, ItemType.None, Item.FLAG_IS_NOT_INITIALIZED))))
    baseRes = State(baseRes.stacks, baseRes.variables)
    expected(0) = baseRes
    baseRes = State(baseRes.stacks.push(new Item(0, ItemType.fromType(BooleanType), Item.FLAG_ORIGINATES_FROM_FIELD)), baseRes.variables)
    expected(3) = baseRes
    baseRes = State(baseRes.stacks.pop(), baseRes.variables)
    expected(6) = baseRes
    baseRes = State(baseRes.stacks.push(new Item(6, ItemType.fromType(ObjectType("java/util/logging/Logger")), Item.FLAG_ORIGINATES_FROM_FIELD)), baseRes.variables)
    expected(9) = baseRes
    baseRes = State(baseRes.stacks.push(new Item(9, ItemType.fromType(ObjectType("java/util/logging/Level")), Item.FLAG_ORIGINATES_FROM_FIELD)), baseRes.variables)
    expected(12) = baseRes
    baseRes = State(baseRes.stacks.pop().pop().push(Item(12, BooleanType,  Item.FLAG_IS_RETURN_VALUE)), baseRes.variables)
    expected(15) = baseRes
    baseRes = State(baseRes.stacks.pop(), baseRes.variables)
    expected(18) = baseRes
    baseRes = State(baseRes.stacks.push(IntegerType, 18), baseRes.variables)
    expected(19) = baseRes
    expected(22) = expected(18)
    expected(23) = expected(19).upperBound(State(expected(22).stacks.push( Item( 22, ItemType.fromType(IntegerType), Item.FLAG_COULD_BE_ZERO)), expected(22).variables))

    //Test newResult with the expected newResult
    Assert.assertEquals(expected.length, results.length)
    Assert.assertArrayEquals(expected.asInstanceOf[Array[AnyRef]], results.asInstanceOf[Array[AnyRef]])


  }
}
