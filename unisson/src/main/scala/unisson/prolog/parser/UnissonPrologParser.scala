package unisson.prolog.parser


import scala.util.parsing.combinator.JavaTokenParsers
;

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:39
 *
 */

class UnissonPrologParser
        extends JavaTokenParsers
{

    import unisson.ast._

    def ensemble: Parser[UnresolvedEnsemble] =
        "ensemble(" ~> atom ~ ("," ~> atom) ~ ("," ~> atomList) ~ ("," ~> query) ~ ("," ~> atomList <~ ").") ^^ {
            case (arch ~ name ~ params ~ query ~ subEnsembles) => UnresolvedEnsemble(name, query, subEnsembles)
        }

    def atom: Parser[String] = ident | "'" ~> """[^']*""".r <~ "'"

    def atomList: Parser[List[String]] =
        (
                ("[" ~> repsep(atom, ",") <~ "]") ^^ {
                    _.foldRight[List[String]](Nil) {
                            (e, l) => l.::(e)
                    }
                }
                        | failure("Illegal list")
                )


    def query: Parser[UnissonQuery] = unaryQuery ||| binaryQuery

    def unaryQuery: Parser[UnissonQuery] =
        parenthesis |
        `package` |
        class_with_members |
        class_with_members_short |
        transitive |
        supertype |
        classSelection |
        classFromSubQuery |
        derived |
        empty

    def binaryQuery : Parser[UnissonQuery] =
        or | without

    def or : Parser[UnissonQuery] =
        unaryQuery ~ ("or" ~> query) ^^
                {
                    case (left ~ right) => OrQuery(left, right)
                }

    def without : Parser[UnissonQuery] =
        unaryQuery ~ ("without" ~> query) ^^
                {
                    case (left ~ right) => WithoutQuery(left, right)
                }

    def parenthesis: Parser[UnissonQuery] = "(" ~> query <~ ")"

    def transitive: Parser[UnissonQuery] = ("transitive(" ~> query <~ ")") ^^ {TransitiveQuery(_:UnissonQuery)}

    def supertype: Parser[UnissonQuery] = ("supertype(" ~> query <~ ")") ^^ {SuperTypeQuery(_:UnissonQuery)}

    def class_with_members_short: Parser[ClassWithMembersQuery] =
        "class_with_members(" ~> atom ~ ("," ~> atom <~ ")") ^^
            {
                case (packageName ~ name) => ClassWithMembersQuery(ClassSelectionQuery(packageName, name))
            }

    def class_with_members: Parser[ClassWithMembersQuery] =
        ("class_with_members(" ~> query <~ ")") ^^ { ClassWithMembersQuery(_:UnissonQuery) }

    def classSelection : Parser[UnissonQuery] =
            "class(" ~> atom ~ ("," ~> atom <~ ")") ^^
            {
                case (packageName ~ name) => ClassSelectionQuery(packageName, name)
            }

    def classFromSubQuery : Parser[UnissonQuery] =
            "class(" ~> query <~ ")" ^^ { ClassQuery(_:UnissonQuery) }


    def `package` : Parser[PackageQuery] =
        "package(" ~> atom <~ ")" ^^
                {
                    case (name) => PackageQuery(name)
                }

    def derived : Parser[DerivedQuery] =
        "derived" ^^^ DerivedQuery()

    def empty : Parser[EmptyQuery] =
        "empty" ^^^ EmptyQuery()


    def dependencyConstraint: Parser[DependencyConstraintEdge] =
        incoming |
                outgoing |
                not_allowed |
                expected


    def incoming: Parser[DependencyConstraintEdge] =
        "incoming(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                IncomingConstraintEdge(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }

    def outgoing: Parser[DependencyConstraintEdge] =
        "outgoing(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                OutgoingConstraintEdge(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }

    def not_allowed: Parser[DependencyConstraintEdge] =
        "not_allowed(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                NotAllowedConstraintEdge(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }

    def expected: Parser[DependencyConstraintEdge] =
        "expected(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                ExpectedConstraintEdge(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }


    def dependency: Parser[(String, String, List[String], String, List[String], List[String])] =
        (atom <~ "," ~ wholeNumber) ~ ("," ~> atom) ~ (", " ~> atomList) ~ ("," ~> atom) ~ ("," ~> atomList) ~ ("," ~> atomList) ^^ {
            case (architecture ~ source ~ sourceParams ~ target ~ targetParams ~ kinds) =>
                (architecture, source, sourceParams, target, targetParams, kinds)
        }
}