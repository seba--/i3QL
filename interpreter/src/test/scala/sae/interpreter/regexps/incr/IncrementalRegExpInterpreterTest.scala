//package sae.interpreter.regexps.incr
//
//import sae.interpreter.regexps._
//import org.junit.Assert._
//import org.junit.{BeforeClass, Ignore, Test}
//import org.hamcrest.CoreMatchers._
//
///**
// * @author Mirko Köhler
// */
//object IncrementalRegExpInterpreterTest {
//	val interpreter = new InterpreterImpl()
//
//	@BeforeClass
//	def setUp() {}
//
//	import IncrementalRegExpInterpreterTest.interpreter._
//	val s0 = defineString("")
//	val s1 = defineString("aaabc")
//	val s2 = defineString("b")
//	val s3 = defineString("accbabcabbbc")
//	val s4 = defineString("accb")
//	val s5 = defineString("ca")
//	val s6 = defineString("bb")
//
//	val l0 = defineList(Nil)
//	val l1 = defineList(3 :: 2 :: 5 :: Nil)
//	val l2 = defineList(1 :: 4 :: Nil)
//	val l3 = defineList("ab" :: "bc" :: "ab" :: Nil)
//
//	val e0 = defineRegExp(Terminal("a"))
//	val e1 = defineRegExp(Alt(Terminal("a"), Terminal("b")))
//	val e2 = defineRegExp(Sequence(Terminal("a"), Terminal("c")))
//	val e3 = defineRegExp(Asterisk(Terminal("b")))
//	val e4 = defineRegExp(
//		Sequence(
//			Asterisk(Terminal("a")),
//			Sequence(
//				Terminal("b"),
//				Alt(
//					Terminal("b"),
//					Terminal("c")
//				)
//			)
//		)
//	)
//	val e5 = defineRegExp(
//		Sequence(
//			Asterisk(
//				Alt(
//					Terminal("a"),
//					Alt(
//						Terminal("b"),
//					    Sequence(
//							Terminal("c"),
//							Terminal("c")
//						)
//					)
//				)
//			),
//			Sequence(
//				Terminal("c"),
//				Sequence(
//					Terminal("a"),
//					Sequence(
//						Asterisk(Terminal("b")),
//						Terminal("c")
//					)
//				)
//			)
//		)
//	)
//
//}
//
//class IncrementalRegExpInterpreterTest {
//
//	import IncrementalRegExpInterpreterTest._
//	import IncrementalRegExpInterpreterTest.interpreter._
//
//
//
//	@Test
//	def testStringRead () {
//		assertThat (keyToString(s0), is (""))
//		assertThat (keyToString(s1), is ("aaabc"))
//		assertThat (keyToString(s2), is ("b"))
//		assertThat (keyToString(s3), is ("accbabcabbbc"))
//	}
//
//
//	@Test
//	def testStringSize () {
//		val v0 = incrStringSize(s1)
//		val v1 = incrStringSize(s0)
//		val v2 = incrStringSize(s2)
//
//		assertThat (valToInt(v0), is (5))
//		assertThat (valToInt(v1), is (0))
//		assertThat (valToInt(v2), is (1))
//	}
//
//	@Test
//	def testStringStartsWith () {
//		val v0 = incrStartsWith(s3, s4)
//		val v1 = incrStartsWith(s4, s3)
//		val v2 = incrStartsWith(s3, s0)
//		val v3 = incrStartsWith(s0, s3)
//		val v4 = incrStartsWith(s0, s0)
//
//		assertThat (valToBoolean(v0), is (true))
//		assertThat (valToBoolean(v1), is (false))
//		assertThat (valToBoolean(v2), is (true))
//		assertThat (valToBoolean(v3), is (false))
//		assertThat (valToBoolean(v4), is (true))
//	}
//
//	@Test
//	def testSubString () {
//		val v0 = incrSubString(s1,3)
//		val v1 = incrSubString(s1,0)
//		val v2 = incrSubString(s1,5)
//		val v3 = incrSubString(s0,0)
//		val v4 = incrSubString(s0,2)
//		val v5 = incrSubString(s1,10)
//
//		assertThat (valToString(v0), is ("bc"))
//		assertThat (valToString(v1), is ("aaabc"))
//		assertThat (valToString(v2), is (""))
//		assertThat (valToString(v3), is (""))
//		assertThat (valToString(v4), is (""))
//		assertThat (valToString(v5), is ("")) //TODO is this how it should be?
//	}
//
//	@Test
//	def testAppend () {
//		val v0 = incrAppend(l1, l2)
//		val v1 = incrAppend(l1, l0)
//		val v2 = incrAppend(l0, l1)
//		val v3 = incrAppend(l0, l0)
//		val v4 = incrAppend(l3, l2)
//		val v5 = incrAppend(l3, l3)
//
//		assertThat (valToList(v0), is (Seq[Any](3,2,5,1,4)))
//		assertThat (valToList(v1), is (Seq[Any](3,2,5)))
//		assertThat (valToList(v2), is (Seq[Any](3,2,5)))
//		assertThat (valToList(v3), is (Seq[Any]()))
//		assertThat (valToList(v4), is (Seq[Any]("ab", "bc", "ab", 1, 4)))
//		assertThat (valToList(v5), is (Seq[Any]("ab", "bc", "ab", "ab", "bc", "ab")))
//	}
//
//	@Test
//	def testInterpTerminal () {
//		val v0 = incrInterpret(e0, s4)
//		val v1 = incrInterpret(e0, s5)
//		val v2 = incrInterpret(e0, s0)
//
//		assertThat (valToStringSet(v0), is (Set("ccb")))
//		assertThat (valToStringSet(v1), is (Set[String]()))
//		assertThat (valToStringSet(v2), is (Set[String]()))
//	}
//
//	@Test
//	def testInterpAlt () {
//		val v0 = incrInterpret(e1, s4)
//		val v1 = incrInterpret(e1, s5)
//		val v2 = incrInterpret(e1, s0)
//
//		assertThat (valToStringSet(v0), is (Set("ccb")))
//		assertThat (valToStringSet(v1), is (Set[String]()))
//		assertThat (valToStringSet(v2), is (Set[String]()))
//	}
//
//	@Test
//	def testInterpSeq () {
//		val v0 = incrInterpret(e2, s4)
//		val v1 = incrInterpret(e2, s1)
//		val v2 = incrInterpret(e2, s0)
//
//		assertThat (valToStringSet(v0), is (Set("cb")))
//		assertThat (valToStringSet(v1), is (Set[String]()))
//		assertThat (valToStringSet(v2), is (Set[String]()))
//	}
//
//	@Test
//	def testInterpAsterisk () {
//		val v0 = incrInterpret(e3, s6)
//		val v1 = incrInterpret(e3, s4)
//		val v2 = incrInterpret(e3, s0)
//
//		assertThat (valToStringSet(v0), is (Set("bb", "b", "")))
//		assertThat (valToStringSet(v1), is (Set("accb")))
//		assertThat (valToStringSet(v2), is (Set[String]("")))
//	}
//
//	@Test
//	def testInterpAll1 () {
//		val v0 = incrInterpret(e4, s1)
//		val v1 = incrInterpret(e4, s3)
//		val v2 = incrInterpret(e4, s0)
//
//
//		assertThat (valToStringSet(v0), is (Set("")))
//		assertThat (valToStringSet(v1), is (Set[String]()))
//		assertThat (valToStringSet(v2), is (Set[String]()))
//	}
//
//	@Test
//	def testInterpAll2 () {
//		val v0 = incrInterpret(e5, s1)
//		val v1 = incrInterpret(e5, s3)
//		val v2 = incrInterpret(e5, s0)
//
//
//		assertThat (valToStringSet(v0), is (Set[String]()))
//		assertThat (valToStringSet(v1), is (Set[String]("")))
//		assertThat (valToStringSet(v2), is (Set[String]()))
//	}
//
//
//
//
//
//
//
//
//
//
//}
//
//import sae.interpreter.regexps.incr.Interpreter._
//class InterpreterImpl extends RegExpInterpreter[ListKey, ListKey, ExpKey, TaskKey] {
//
//	private val tab = createITable
//	private val values = getValues(tab)
//
//	override def defineString(s: String): ListKey = insertString(tab, s)
//	override def defineRegExp(e: RegExp): ExpKey = insertExp(tab,e)
//	override def defineList(l: Seq[Any]): ListKey = insertList(tab, l)
//
//
//	override def incrInterpret(e: ExpKey, s: ListKey): TaskKey = insertTask(tab,interpTag,Seq(e,s))
//	override def incrStringSize(s: ListKey): TaskKey = insertTask(tab, sizeTag, Seq(s))
//	override def incrStartsWith(s0: ListKey, s1: ListKey): TaskKey = insertTask(tab, startsWithTag, Seq(s0,s1))
//	override def incrSubString(s: ListKey, i: Int): TaskKey = insertTask(tab, sublistTag, Seq(s,i))
//	override def incrAppend(s0: ListKey, s1: ListKey): ListKey = insertTask(tab, appendTag, Seq(s0, s1))
//
//
//
//	override def keyToString(k: ListKey): String = string(tab,k)
//
//	override def valToStringSet(k: TaskKey): Set[String] = Set(list(tab,values(k).asInstanceOf[ListKey]).map((e) => string(tab,e.asInstanceOf[ListKey])) : _*)
//	override def valToBoolean(k: TaskKey): Boolean = values(k).asInstanceOf[Boolean]
//	override def valToString(k: TaskKey): String = keyToString(values(k).asInstanceOf[ListKey])
//	override def valToInt(k: TaskKey): Int = values(k).asInstanceOf[Int]
//	override def valToList(k: TaskKey): Seq[Any] = list(tab, values(k).asInstanceOf[ListKey])
//}
