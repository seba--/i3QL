package unisson.prolog.parser


import scala.util.parsing.combinator.JavaTokenParsers
import unisson.ast.UnissionQuery
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

    def ensemble : Parser[Ensemble] =
        "ensemble(" ~> atom ~ ("," ~> atom) ~ ("," ~> atomList) ~ ("," ~> query) ~ ("," ~> atomList <~ ").")  ^^ {
            //(arch:String, name:String, params:List[String], query:UnissionQuery, subEnsembles:List[String]) =>
            case (arch ~ name ~ params ~ query ~ subEnsembles) => Ensemble(name, query)
        }

    def atom: Parser[String] = ident | "'" ~> ident <~ "'"

    def atomList: Parser[List[String]] =
        (
            "[" ~> repsep(atom, ",") <~ "]" ^^ { _.foldRight[List[String]](Nil){ (e,l) => l :+ e } }
            | failure("Illegal list")
        )

    def query : Parser[UnissionQuery] = "(" ~> singleQuery <~ ")"

    def singleQuery : Parser[UnissionQuery] =
        class_with_members |
        `package`


    def class_with_members : Parser[ClassWithMembersQuery] = "class_with_members(" ~> atom ~ ("," ~> atom <~ ")") ^^
            {
                case (packageName ~ name) => ClassWithMembersQuery(packageName, name)
            }

    def `package` : Parser[PackageQuery] = "package(" ~> atom <~ ")" ^^
            {
                case (name) => PackageQuery(name)
            }

/*
            def theory:Parser[Theory] = rep(clause) ^^ {list => new Theory(list)}

            def clause: Parser[Clause] = (
                            predicate~opt(":-"~>formula)<~"."
                                    ^^ {
                                            case head~None => new Clause(head,True)
                                            case head~Some(body) => new Clause(head,body)
                            }
                            | failure("Illegal clause")
            )

            def formula: Parser[Formula] = (
                            "true" ^^ {_ => True}
                            | "false" ^^ {_ => False}
                            | predicate~","~formula
                                    ^^ {case first~_~second => new And(first, second)}
                            | predicate
                            | failure("Illegal formula")
            )


            def predicate: Parser[Predicate] = (
                            """[a-z]\w*""".r ~ opt("("~>repsep(term,",")<~")")
                                    ^^ {
                                            case name ~ None => new Predicate(Symbol(name),0,List())
                                            case name ~ Some(arglist) => new Predicate(Symbol(name),arglist.length,arglist)
                            }
                            | failure("Illegal predicate")
            )

            def term: Parser[Term] = (
                            atom
                            | variable
                            | wholeNumber ^^ {n => new Integer(n.toInt)}
                            //| decimalNumber ^^ {n => new Number(n.toDouble)}
                            | list
                            | failure("Illegal term")
            )

            def atom: Parser[Atom] = """[a-z]\w*""".r ^^ {name => new Atom(Symbol(name))}

            def variable: Parser[Variable] = (
                    """[A-Z]\w*""".r ^^ {name => new NamedVar(Symbol(name))}
                    | """_\w*""".r ^^ {_ => DontCare}
            )

            def list: Parser[ScalaLogicList] = (
                    "["~>repsep(term,",")<~"]"
                            ^^ { _.foldRight(EmptyList:ScalaLogicList){ (e,l) => new ListNode(e,l) } }
                    | "["~rep1sep(term,",")~"|"~variable~"]"
                            ^^ {  case "["~(e::r)~"|"~end~"]" => new ListNode(e, r.foldRight(end:Term){ ListNode(_,_) } )}
                    | failure("Illegal list")
            )

*/
}