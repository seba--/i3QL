package idb.macrosimpl

import language.experimental.macros

import reflect.macros.Context
import scala.collection.mutable.{ListBuffer, Stack}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.06.13
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
object Macros {

	/*
	Hello World!
	 */
	def hello(any : Any): Unit = macro hello_impl

	def hello_impl(c: Context)(any : c.Expr[Any]): c.Expr[Unit] = {
		import c.universe._
		reify { println( any.splice ) }
	}

	/*
	Apply function to integer
	 */
	def checkInt(a : Int, f : Int => Boolean): Boolean = macro checkInt_impl

	def checkInt_impl(c: Context)(a : c.Expr[Int], f : c.Expr[Int => Boolean]): c.Expr[Boolean] = {
		import c.universe._
		reify {f.splice.apply(a.splice)}
	}

	/*
	Map
	 */
	def map[A,B](list : List[A], f : A => B) : List[B] = macro map_impl[A,B]


	def map_impl[A : c.WeakTypeTag, B : c.WeakTypeTag]
				(c : Context)
				(list : c.Expr[List[A]], f : c.Expr[A => B]) : c.Expr[List[B]] = {
		c.universe.reify{ list.splice.map(f.splice) }

	}

	/*
	printf
	 */
	def printf(format: String, params: Any*): Unit = macro printf_impl

	def printf_impl(c: Context)(format: c.Expr[String], params: c.Expr[Any]*): c.Expr[Unit] =


		{
			import c.universe._
			val Literal(Constant(s_format: String)) = format.tree

			val evals = ListBuffer[ValDef]()
			def precompute(value: Tree, tpe: Type): Ident = {
				val freshName = newTermName(c.fresh("eval$"))
				evals += ValDef(Modifiers(), freshName, TypeTree(tpe), value)
				Ident(freshName)
			}

			val paramsStack = Stack[Tree]((params map (_.tree)): _*)
			val refs = s_format.split("(?<=%[\\w%])|(?=%[\\w%])") map {
				case "%d" => precompute(paramsStack.pop, typeOf[Int])
				case "%s" => precompute(paramsStack.pop, typeOf[String])
				case "%%" => Literal(Constant("%"))
				case part => Literal(Constant(part))
			}

			val stats = evals ++ refs.map(ref => reify(print(c.Expr[Any](ref).splice)).tree)
			c.Expr[Unit](Block(stats.toList, Literal(Constant(()))))
		}




}
