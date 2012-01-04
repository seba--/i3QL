package unisson.model.kinds

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

    protected def kindExpr: Parser[KindExpr] = operators | constants

    protected def kinds: Parser[KindExpr] = create | throws | `extends` | implements | field_type |
            parameter | return_type | read_field | write_field | invoke_interface | invoke_special | invoke_static |
            invoke_virtual | instanceof | class_cast

    protected def groups: Parser[KindExpr] = all | calls | subtype | signature

    protected def constants: Parser[KindExpr] = kinds | groups

    protected def operators: Parser[KindExpr] = union | not | difference

    protected def union: Parser[KindExpr] = ((difference | constants | not) <~ ",") ~ kindExpr ^^ {
        case (left ~ right) => Union(left, right)
    }

    protected def not: Parser[KindExpr] = "!" ~> constants ^^ {
        Not(_: KindExpr)
    }

    protected def difference: Parser[KindExpr] = ((constants | not) <~ "\\") ~ (constants | not) ^^ {
        case (left ~ right) => Difference(left, right)
    }

    protected def all: Parser[AllKind.type] = "all" ^^^ AllKind

    protected def calls: Parser[CallsKind.type] = "calls" ^^^ CallsKind

    protected def subtype: Parser[SubtypeKind.type] = "subtype" ^^^ SubtypeKind

    protected def create: Parser[CreateKind.type] = "create" ^^^ CreateKind

    protected def throws: Parser[ThrowsKind.type] = "throws" ^^^ ThrowsKind

    protected def `extends`: Parser[ExtendsKind.type] = "extends" ^^^ ExtendsKind

    protected def implements: Parser[ImplementsKind.type] = "implements" ^^^ ImplementsKind

    protected def field_type: Parser[FieldTypeKind.type] = "field_type" ^^^ FieldTypeKind

    protected def signature: Parser[SignatureKind.type] = "signature" ^^^ SignatureKind

    protected def parameter: Parser[ParameterKind.type] = "parameter" ^^^ ParameterKind

    protected def return_type: Parser[ReturnTypeKind.type] = "return_type" ^^^ ReturnTypeKind

    protected def write_field: Parser[WriteFieldKind.type] = "write_field" ^^^ WriteFieldKind

    protected def read_field: Parser[ReadFieldKind.type] = "read_field" ^^^ ReadFieldKind

    protected def invoke_interface: Parser[InvokeInterfaceKind.type] = "invoke_interface" ^^^ InvokeInterfaceKind

    protected def invoke_special: Parser[InvokeSpecialKind.type] = "invoke_special" ^^^ InvokeSpecialKind

    protected def invoke_static: Parser[InvokeStaticKind.type] = "invoke_static" ^^^ InvokeStaticKind

    protected def invoke_virtual: Parser[InvokeVirtualKind.type] = "invoke_virtual" ^^^ InvokeVirtualKind

    protected def instanceof: Parser[InstanceOfKind.type] = "instanceof" ^^^ InstanceOfKind

    protected def class_cast: Parser[ClassCastKind.type] = "class_cast" ^^^ ClassCastKind

}