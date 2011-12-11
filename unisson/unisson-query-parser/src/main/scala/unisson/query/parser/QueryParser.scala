package unisson.query.parser


import unisson.query.ast._
import unisson.query.UnissonQuery
import scala.util.parsing.combinator.JavaTokenParsers
;

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:39
 *
 *
 * A very first parser for the unisson query language
 *   Does not support the full spec yet
 *   No operator precedence
 */
class QueryParser
        extends JavaTokenParsers
{

    def parse(q : String) : ParseResult[UnissonQuery] = parseAll(query, q)

    protected[parser] def query: Parser[UnissonQuery] = unaryQuery ||| binaryQuery

    protected[parser] def atom: Parser[String] = ident | "'" ~> """[^']*""".r <~ "'"

    protected[parser] def unaryQuery: Parser[UnissonQuery] =
        parenthesis |
                `package` |
                class_with_members |
                class_with_members_short |
                transitive |
                supertype |
                classSelection |
                classFromSubQuery |
                fieldSelection |
                methodSelection |
                derived |
                empty |
                typeSelection

    protected[parser] def binaryQuery: Parser[UnissonQuery] =
        or | without

    protected[parser] def or: Parser[UnissonQuery] =
        unaryQuery ~ ("or" ~> query) ^^ {
            case (left ~ right) => OrQuery(left, right)
        }

    protected[parser] def without: Parser[UnissonQuery] =
        unaryQuery ~ ("without" ~> query) ^^ {
            case (left ~ right) => WithoutQuery(left, right)
        }

    protected[parser] def parenthesis: Parser[UnissonQuery] = "(" ~> query <~ ")"

    protected[parser] def list: Parser[Seq[UnissonQuery]] =
        ("[" ~> repsep(query, ",") <~ "]") ^^ {
            _.foldRight[List[UnissonQuery]](Nil) {
                (e, l) => l.::(e)
            }
        } |
                failure("failed to parse a list")

    protected[parser] def transitive: Parser[TransitiveQuery] = ("transitive(" ~> query <~ ")") ^^ {
        TransitiveQuery(_: UnissonQuery)
    }


    protected[parser] def supertype: Parser[SuperTypeQuery] = ("supertype(" ~> query <~ ")") ^^ {
        SuperTypeQuery(_: UnissonQuery)
    }


    protected[parser] def class_with_members_short: Parser[ClassWithMembersQuery] =
        "class_with_members(" ~> atom ~ ("," ~> atom <~ ")") ^^ {
            case (packageName ~ name) => ClassWithMembersQuery(ClassSelectionQuery(packageName, name))
        }

    protected[parser] def class_with_members: Parser[ClassWithMembersQuery] =
        ("class_with_members(" ~> query <~ ")") ^^ {
            ClassWithMembersQuery(_: UnissonQuery)
        }


    protected[parser] def classSelection: Parser[ClassSelectionQuery] =
        "class(" ~> atom ~ ("," ~> atom <~ ")") ^^ {
            case (packageName ~ name) => ClassSelectionQuery(packageName, name)
        }

    protected[parser] def classFromSubQuery: Parser[ClassQuery] =
        "class(" ~> query <~ ")" ^^ {
            ClassQuery(_: UnissonQuery)
        }


    protected[parser] def methodSelection: Parser[MethodQuery] =
        "method(" ~> query ~ ("," ~> atom) ~ ("," ~> query) ~ ("," ~> list <~ ")") ^^ {
            case (declaringClass ~ name ~ returnType ~ parameters) =>
                MethodQuery(
                    declaringClass,
                    name,
                    returnType,
                    parameters: _*
                )
        }

    protected[parser] def fieldSelection: Parser[FieldQuery] =
        "field(" ~> query ~ ("," ~> atom) ~ ("," ~> query <~ ")") ^^ {
            case (declaringClass ~ name ~ fieldType) =>
                FieldQuery(
                    declaringClass,
                    name,
                    fieldType
                )
        }

    protected[parser] def typeSelection: Parser[TypeQuery] =
        atom ^^ {
            TypeQuery(_)
        }


    protected[parser] def `package`: Parser[PackageQuery] =
        "package(" ~> atom <~ ")" ^^ {
            PackageQuery(_)
        }

    protected[parser] def derived: Parser[DerivedQuery] =
        "derived" ^^^ DerivedQuery()

    protected[parser] def empty: Parser[EmptyQuery] =
        "empty" ^^^ EmptyQuery()


}