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

    def ensemble: Parser[Ensemble] =
        "ensemble(" ~> atom ~ ("," ~> atom) ~ ("," ~> atomList) ~ ("," ~> query) ~ ("," ~> atomList <~ ").") ^^ {
            case (arch ~ name ~ params ~ query ~ subEnsembles) => Ensemble(name, query, subEnsembles)
        }

    def atom: Parser[String] = ident | "'" ~> """[^']*""".r <~ "'"

    def atomList: Parser[List[String]] =
        (
                "[" ~> repsep(atom, ",") <~ "]" ^^ {
                    _.foldRight[List[String]](Nil) {
                            (e, l) => l.::(e)
                    }
                }
                        | failure("Illegal list")
                )


    def query: Parser[UnissonQuery] = unaryQuery ||| binaryQuery

    def parensQuery: Parser[UnissonQuery] = "(" ~> query <~ ")"

    def unaryQuery: Parser[UnissonQuery] =
        parensQuery |
        `package` |
        class_with_members

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


    def class_with_members: Parser[ClassWithMembersQuery] =
        "class_with_members(" ~> atom ~ ("," ~> atom <~ ")") ^^
            {
                case (packageName ~ name) => ClassWithMembersQuery(packageName, name)
            }

    def `package` : Parser[PackageQuery] =
        "package(" ~> atom <~ ")" ^^
                {
                    case (name) => PackageQuery(name)
                }


    def dependencyConstraint: Parser[DependencyConstraint] =
        incoming |
                outgoing |
                not_allowed |
                expected


    def incoming: Parser[DependencyConstraint] =
        "incoming(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                IncomingConstraint(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }

    def outgoing: Parser[DependencyConstraint] =
        "outgoing(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                OutgoingConstraint(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }

    def not_allowed: Parser[DependencyConstraint] =
        "not_allowed(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                NotAllowedConstraint(
                    architecture: String,
                    sourceName: String,
                    sourceParams: List[String],
                    targetName: String,
                    targetParams: List[String],
                    kinds: List[String]
                )
        }

    def expected: Parser[DependencyConstraint] =
        "expected(" ~> dependency <~ ")." ^^ {
            case (architecture, sourceName, sourceParams, targetName, targetParams, kinds) =>
                ExpectedConstraint(
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