package unisson.model.kinds.parser

import unisson.model.kinds.KindExpr
import unisson.model.kinds.primitive._
import unisson.model.kinds.group._
import unisson.model.kinds.operator._

import util.parsing.combinator._


/**
 *
 * Author: Ralf Mitschke
 * Date: 10.12.11
 * Time: 13:06
 *
 */
class KindParser
        extends JavaTokenParsers
{

    def parse(q: String): ParseResult[KindExpr] = parseAll(kindExpr, q)

    protected[parser] def kindExpr: Parser[KindExpr] = operators | constants

    protected[parser] def kinds: Parser[KindExpr] = create | throws | `extends` | implements | field_type |
            parameter | return_type | read_field | write_field | invoke_interface | invoke_special | invoke_static |
            invoke_virtual | instanceof | class_cast

    protected[parser] def groups: Parser[KindExpr] = all | calls | subtype | signature

    protected[parser] def constants: Parser[KindExpr] = kinds | groups

    protected[parser] def operators: Parser[KindExpr] = union | not | difference

    protected[parser] def union: Parser[KindExpr] = ((difference | constants | not) <~ ",") ~ kindExpr ^^ {
        case (left ~ right) => Union(left, right)
    }

    protected[parser] def not: Parser[KindExpr] = "!" ~> constants ^^ {
        Not(_: KindExpr)
    }

    protected[parser] def difference: Parser[KindExpr] = ((constants | not) <~ "\\") ~ (constants | not) ^^ {
        case (left ~ right) => Difference(left, right)
    }

    protected[parser] def all: Parser[AllKind.type] = "all" ^^^ AllKind

    protected[parser] def calls: Parser[CallsKind.type] = "calls" ^^^ CallsKind

    protected[parser] def subtype: Parser[SubtypeKind.type] = "subtype" ^^^ SubtypeKind

    protected[parser] def create: Parser[CreateKind.type] = "create" ^^^ CreateKind

    protected[parser] def throws: Parser[ThrowsKind.type] = "throws" ^^^ ThrowsKind

    protected[parser] def `extends`: Parser[ExtendsKind.type] = "extends" ^^^ ExtendsKind

    protected[parser] def implements: Parser[ImplementsKind.type] = "implements" ^^^ ImplementsKind

    protected[parser] def field_type: Parser[FieldTypeKind.type] = "field_type" ^^^ FieldTypeKind

    protected[parser] def signature: Parser[SignatureKind.type] = "signature" ^^^ SignatureKind

    protected[parser] def parameter: Parser[ParameterKind.type] = "parameter" ^^^ ParameterKind

    protected[parser] def return_type: Parser[ReturnTypeKind.type] = "return_type" ^^^ ReturnTypeKind

    protected[parser] def write_field: Parser[WriteFieldKind.type] = "write_field" ^^^ WriteFieldKind

    protected[parser] def read_field: Parser[ReadFieldKind.type] = "read_field" ^^^ ReadFieldKind

    protected[parser] def invoke_interface: Parser[InvokeInterfaceKind.type] = "invoke_interface" ^^^ InvokeInterfaceKind

    protected[parser] def invoke_special: Parser[InvokeSpecialKind.type] = "invoke_special" ^^^ InvokeSpecialKind

    protected[parser] def invoke_static: Parser[InvokeStaticKind.type] = "invoke_static" ^^^ InvokeStaticKind

    protected[parser] def invoke_virtual: Parser[InvokeVirtualKind.type] = "invoke_virtual" ^^^ InvokeVirtualKind

    protected[parser] def instanceof: Parser[InstanceOfKind.type] = "instanceof" ^^^ InstanceOfKind

    protected[parser] def class_cast: Parser[ClassCastKind.type] = "class_cast" ^^^ ClassCastKind

}