package sae.java

import members._
import types._
import java.lang.{IllegalArgumentException, UnsupportedOperationException}
import java.io.{FileInputStream, InputStream}
import sae.reader.BytecodeReader

class BytecodeDatabase
{
    var packages: Seq[Package] = Nil

    /**
     * category may be unknown, e.g. if we have a reference to class as a parameter or field_type.
     *
     */
    private def addObjectType(o: de.tud.cs.st.bat.ObjectType, category : Option[de.tud.cs.st.bat.ClassFileCategory.ClassFileCategory], isCompiled : Boolean) : ObjectType =
    {
        import de.tud.cs.st.bat.ClassFileCategory._

        var P = Package(o.packageName.replaceAll("/", "."))
        if (!packages.contains(P))
        {
            packages = packages :+ P
        }
        else
        {
            P = packages.find( _ == P).get
        }
        // check that this was not added as an external class earlier
        val C = category match
        {
            case Some(AnnotationDeclaration) => Annotation(P, o.simpleName)
            case Some(ClassDeclaration) => Class(P, o.simpleName)
            case Some(EnumDeclaration) => Enumeration(P, o.simpleName)
            case Some(InterfaceDeclaration) => Interface(P, o.simpleName)
            case None => new ObjectType { // TODO should we create a type for unresolved classes?
                val Package = P
                val Name = o.simpleName
            }
        }
        C.IsSourceAvailable = isCompiled
        val maybeClass: Option[ObjectType] = P.Members.find(_ == C)
        if (maybeClass == None) {
            P.Members = P.Members :+ C
            C
        }
        else {
            // TODO setting the SourceAvailable Property mutable means we need to update views pertaining on this state, i.e. all packages that have implemented classes
            maybeClass.get.IsSourceAvailable = true
            maybeClass.get
        }
    }

    private def addMethod(declaringRef: de.tud.cs.st.bat.ReferenceType, name: String, parameters: Seq[de.tud.cs.st.bat.Type], returnType: de.tud.cs.st.bat.Type): Method =
    {
        if (declaringRef.isObjectType) {
            val C = findObjectType(declaringRef.asObjectType)
            var M = Method(C, name, parameters.map(BAT_To_SAE_Type), BAT_To_SAE_Type(returnType))
            val maybeMethod = C.Methods.find(_ == M)
            if (maybeMethod == None) {
                C.Methods = C.Methods :+ M
            }
            else {
                M = maybeMethod.get
            }
            M
        }
        else {
            throw new UnsupportedOperationException("SAE can not resolve methods of arrays yet")
        }
    }

    private def addField(declaringClass: de.tud.cs.st.bat.ObjectType, name: String, fieldType: de.tud.cs.st.bat.FieldType, isStatic: Boolean): Field =
    {
        val C = findObjectType(declaringClass)
        var F = Field(C, name, BAT_To_SAE_Type(fieldType), isStatic)
        val maybeField = C.Fields.find(_ == F)
        if (maybeField == None) {
            C.Fields = C.Fields :+ F
        }
        else {
            F = maybeField.get
        }
        F
    }

    private def findObjectType(o: de.tud.cs.st.bat.ObjectType): ObjectType =
    {
        // TODO is a variant with collectFirst quicker?
        val res = (for{ p <- packages
                        if( p.Name == o.packageName.replaceAll("/", "."))
                        c <- p.Members
                        if( c.Name == o.simpleName)
                } yield(c)
                ).headOption
        if( res != None ) {
            res.get
        }
        else {
            addObjectType(o, None, false)
        }
    }

    private def findClassType(o: de.tud.cs.st.bat.ObjectType): Class =
    {
        val res = (for{ p <- packages
                        if( p.Name == o.packageName.replaceAll("/", "."))
                        c @ Class(_, n) <- p.Members
                        if( n == o.simpleName)
                } yield(c)
                ).headOption
        if( res != None ) {
            res.get
        }
        else {
            addObjectType(o, Some(de.tud.cs.st.bat.ClassFileCategory.ClassDeclaration), false).asInstanceOf[Class]
        }
    }

    private def findInterfaceType(o: de.tud.cs.st.bat.ObjectType): Interface =
    {
        val res = (for{ p <- packages
                        if( p.Name == o.packageName.replaceAll("/", "."))
                        i @ Interface(_, n) <- p.Members
                        if( n == o.simpleName)
                } yield(i)
                ).headOption
        if( res != None ) {
            res.get
        }
        else {
            addObjectType(o, Some(de.tud.cs.st.bat.ClassFileCategory.InterfaceDeclaration), false).asInstanceOf[Interface]
        }
    }

    private def findArray(a: de.tud.cs.st.bat.ArrayType): Array =
    {
        Array(BAT_To_SAE_Type(a.componentType), a.dimensions)
    }

    /**
     * Note that inheritance information is reliable to detect the kind of object type we are dealing with.
     * Enumeration and Annotation can not serve as super classes.
     */
    private def addExtendsRelation(source : ObjectType, target : de.tud.cs.st.bat.ObjectType)
    {
        source.SuperType = Some(findClassType(target))
    }

    /**
     * Note that inheritance information is reliable to detect the kind of object type we are dealing with.
     * Enumeration and Annotation can not serve as super classes.
     */
    private def addImplementsRelation(source : ObjectType, target : de.tud.cs.st.bat.ObjectType)
    {
        // TODO the first lookup could be spared, since we know this class already
        source.Interfaces = source.Interfaces :+ findInterfaceType(target)
    }

    private def BAT_To_SAE_Type(Type: de.tud.cs.st.bat.Type): Type =
    {
        if (Type.isVoidType) {
            return sae.java.types.primitives.Void()
        }
        if (Type.isByteType) {
            return sae.java.types.primitives.Byte()
        }
        if (Type.isCharType) {
            return sae.java.types.primitives.Char()
        }
        if (Type.isShortType) {
            return sae.java.types.primitives.Short()
        }
        if (Type.isIntegerType) {
            return sae.java.types.primitives.Int()
        }
        if (Type.isLongType) {
            return sae.java.types.primitives.Long()
        }
        if (Type.isFloatType) {
            return sae.java.types.primitives.Float()
        }
        if (Type.isDoubleType) {
            return sae.java.types.primitives.Double()
        }
        if (Type.isBooleanType) {
            return sae.java.types.primitives.Boolean()
        }
        if (Type.isArrayType) {
            return findArray(Type.asArrayType)
        }
        if (Type.isObjectType) {
            return findObjectType(Type.asObjectType)
        }
        throw new IllegalArgumentException(Type.toJava + "is not a understood by sae")
    }

    private def classAdder = new Java6ClassTransformer(
        addObjectType,
        addMethod,
        addField,
        addExtendsRelation,
        addImplementsRelation
        /*,
        instructions.element_added,
        exception_handlers.element_added,
        thrown_exceptions.element_added,
        internal_inner_classes.element_added,
        internal_enclosing_methods.element_added*/
    )

/*
    private def classRemover = new Java6ClassTransformer(
        classfiles.element_removed,
        classfile_methods.element_removed,
        classfile_fields.element_removed,
        classes.element_removed,
        methods.element_removed,
        fields.element_removed,
        instructions.element_removed,
        `extends`.element_removed,
        implements.element_removed,
        parameter.element_removed,
        exception_handlers.element_removed,
        thrown_exceptions.element_removed,
        internal_inner_classes.element_removed,
        internal_enclosing_methods.element_removed
    )
*/


    def addArchivesTransformer[T]( inputs : T* )( asStream : T => InputStream ) =
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        inputs.foreach((t:T) => reader.readArchive(asStream(t)))
        transformer
    }

    def addArchives[T]( inputs : T* )( asStream : T => InputStream )
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        inputs.foreach((t:T) => reader.readArchive(asStream(t)))
        transformer.processAllFacts()
    }

    def addClassesTransformer[T]( inputs : T* )( asStream : T => InputStream ) =
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        inputs.foreach((t:T) => reader.readClassFile(asStream(t)))
        transformer
    }

    def addClasses[T]( inputs : T* )( asStream : T => InputStream )
    {
        val transformer = classAdder
        val reader = new BytecodeReader(transformer)
        inputs.foreach((t:T) => reader.readClassFile(asStream(t)))
        transformer.processAllFacts()
    }

    // ----------------
    // some predefined functions for the asStream parameter
    // ----------------

    /**
     * Convenience method that opens a stream from a given resource in the classpath
     */
    def resourceAsStream( resource : String ) : InputStream = this.getClass.getClassLoader.getResourceAsStream(resource)


    /**
     * Convenience method that opens a stream from a given loaded class
     */
    def classAsStream( clazz: java.lang.Class[_] ) : InputStream =
    {
        val resource = clazz.getName.replace(".", "/") + ".class"
        this.getClass.getClassLoader.getResourceAsStream(resource)
    }


    /**
     * Convenience method that opens a stream from a given file name
     */
    def fileAsStream( fileName : String ) : InputStream =
    {
        val file = new java.io.File(fileName)
        new FileInputStream(file)
    }

}
