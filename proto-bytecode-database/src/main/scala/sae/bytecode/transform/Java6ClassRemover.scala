package sae.bytecode.transform
import sae.Observable
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.Method
import sae.bytecode.model.Field
import sae.collections.Table


class Java6ClassRemover(
                        classfiles: Observable[ObjectType],
                        classfile_methods: Observable[Method],
                        classfile_fields: Observable[Field],
                        classes: Observable[ObjectType],
                        methods: Observable[Method],
                        fields: Observable[Field]/*,
                        instructions: Observable[Instr[_]],
                        `extends` : Observable[`extends`],
                        implements: Observable[implements],
                        parameter: Observable[parameter]*/)
{
        def remove( classFile : ObjectType) {
            //methods.element_removed({case Method(classFile, _,_,_)})
            //classFiles -= classFile
    }

}