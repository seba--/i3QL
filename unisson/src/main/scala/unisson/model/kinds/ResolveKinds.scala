package unisson.model.kinds


/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 10:44
 *
 */

object ResolveKinds
{
    def apply(s:String) : DependencyKind = s match {
            case "all" => AllKind
            case "calls" => CallsKind
            case "subtype" => SubtypeKind
            case "create" => CreateKind
            case "throws" => ThrowsKind
            case "extends" => ExtendsKind
            case "implements" => ImplementsKind
            case "field_type" => FieldTypeKind
            case "signature" => SignatureKind
            case "parameter" => ParameterKind
            case "return_type" => ReturnTypeKind
            case "write_field" => WriteFieldKind
            case "read_field" => ReadFieldKind
            case "invoke_interface" => InvokeInterfaceKind
            case "invoke_special" => InvokeSpecialKind
            case "invoke_static" => InvokeStaticKind
            case "invoke_virtual" => InvokeVirtualKind
            case "instanceof" => InstanceOfKind
            case "class_cast" => ClassCastKind
                /* TODO enble rest of kinds
            case "create_class_array" =>

                */
    }

    def unapply(d:DependencyKind) : Option[String] = Some(d.designator)
}