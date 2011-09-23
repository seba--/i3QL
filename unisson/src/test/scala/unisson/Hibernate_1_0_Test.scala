package unisson

import ast._
import org.junit.Test
import org.junit.Assert._
import queries.QueryCompiler
import sae.bytecode.BytecodeDatabase
import sae.bytecode.model._
import de.tud.cs.st.bat._
import dependencies._

/**
 *
 * Author: Ralf Mitschke
 * Created: 22.09.11 20:26
 *
 */

class Hibernate_1_0_Test
{

    @Test
    def test_dependency_ensemble_membership()
    {
        val database = new BytecodeDatabase
        val queries = new Queries(database)

        import sae.syntax.RelationalAlgebraSyntax._


        val classMapping = Ensemble(
            "cirrus.hibernate.tools.codegen",
            ClassWithMembersQuery(ClassSelectionQuery("cirrus.hibernate.tools.codegen", "ClassMapping")),
            Nil,
            Nil
        )

        val types = Ensemble(
            "cirrus.hibernate.type",
            OrQuery(
                ClassWithMembersQuery(ClassSelectionQuery("cirrus.hibernate.type", "PrimitiveType")),
                OrQuery(
                    ClassWithMembersQuery(ClassSelectionQuery("cirrus.hibernate.type", "TypeFactory")),
                    ClassWithMembersQuery(ClassSelectionQuery("cirrus.hibernate.type", "Type"))
                )
            )
            , Nil, Nil
        )

        val checker = new ArchitectureChecker(database)


        val compiler = new QueryCompiler(checker)
        compiler.add(classMapping)
        compiler.add(types)

        val dependencies = lazyViewToResult(database.dependency)



        val firstView = checker.ensembleElements(classMapping)
        val secondView = checker.ensembleElements(types)

        val relation_threetimes = invoke_static(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            Method(
                ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                "basic",
                List(ObjectType(className = "java/lang/String")),
                ObjectType(className = "cirrus/hibernate/type/Type")
            )
        )

        val relation_twotimes = invoke_interface(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            Method(
                ObjectType(className = "cirrus/hibernate/type/Type"),
                "returnedClass",
                List(),
                ObjectType(className = "java/lang/Class")
            )
        )

        //val testSetBefore = CheckArchitectureFromProlog.createDependencyQuery(firstView, secondView, dependencies)

        val beforeInClassMapping = (dependencies, (_: Dependency[AnyRef, AnyRef]).source) ⋉ ((_: SourceElement[AnyRef]).element, firstView)

        val beforeInTypes = (dependencies, (_: Dependency[AnyRef, AnyRef]).target) ⋉ ((_: SourceElement[AnyRef]).element, secondView)

        val testSetBefore = beforeInClassMapping ∩ beforeInTypes

        // read the code
        database.addArchiveAsResource("hibernate-1.0.jar")

        // check that no double elements etc. are in the ensembles
        assertEquals(firstView.asList.sortBy(Utilities.ensembleElementsSortOrder), classMappingMembers)
        assertEquals(secondView.asList.sortBy(Utilities.ensembleElementsSortOrder), typesMembers)



        val afterInClassMapping = (dependencies, (_: Dependency[AnyRef, AnyRef]).source) ⋉ ((_: SourceElement[AnyRef]).element, firstView)

        val afterInTypes = (dependencies, (_: Dependency[AnyRef, AnyRef]).target) ⋉ ((_: SourceElement[AnyRef]).element, secondView)

        //val testSetAfter = CheckArchitectureFromProlog.createDependencyQuery(firstView, secondView, dependencies)
        val testSetAfter = afterInClassMapping ∩ afterInTypes

        assertEquals(3, beforeInClassMapping.asList.filter(_ == relation_threetimes).size)
        assertEquals(3, beforeInTypes.asList.filter(_ == relation_threetimes).size)
        assertEquals(3, afterInClassMapping.asList.filter(_ == relation_threetimes).size)
        assertEquals(3, afterInTypes.asList.filter(_ == relation_threetimes).size)

        assertEquals(2, beforeInClassMapping.asList.filter(_ == relation_twotimes).size)
        assertEquals(2, beforeInTypes.asList.filter(_ == relation_twotimes).size)
        assertEquals(2, afterInClassMapping.asList.filter(_ == relation_twotimes).size)
        assertEquals(2, afterInTypes.asList.filter(_ == relation_twotimes).size)

        assertEquals(classMappingToTypeDependencies, testSetBefore.asList.sortBy(dependencySortOrder))
        assertEquals(classMappingToTypeDependencies, testSetAfter.asList.sortBy(dependencySortOrder))
        assertEquals(8, testSetBefore.size)
        assertEquals(8, testSetAfter.size)
    }


    private val classMappingMembers: List[SourceElement[AnyRef]] =
        List[SourceElement[AnyRef]](
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "<init>",
                    List(ObjectType(className = "org/jdom/Element")),
                    VoidType()
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getPackageName",
                    List(),
                    ObjectType(className = "java/lang/String")
                )
            ),
            SourceElement(ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping")),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getFields",
                    List(),
                    ObjectType(className = "java/util/List")
                )
            ),
            SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "superClass",
                    ObjectType(className = "java/lang/String")
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "doCollection",
                    List(
                        ObjectType(className = "org/jdom/Element"),
                        ObjectType(className = "java/lang/String"),
                        ObjectType(className = "java/lang/String"),
                        ObjectType(className = "java/lang/String")
                    ),
                    VoidType()
                )
            ),
            SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "subclasses",
                    ObjectType(className = "java/util/List")
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getName",
                    List(),
                    ObjectType(className = "java/lang/String")
                )
            ),
            SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "imports",
                    ObjectType(className = "java/util/TreeSet")
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "<init>",
                    List(
                        ObjectType(className = "cirrus/hibernate/tools/codegen/ClassName"),
                        ObjectType(className = "org/jdom/Element")
                    ),
                    VoidType()
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "addImport",
                    List(ObjectType(className = "cirrus/hibernate/tools/codegen/ClassName")),
                    VoidType()
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getSuperClass",
                    List(),
                    ObjectType(className = "java/lang/String")
                )
            ),
            SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "fields",
                    ObjectType(className = "java/util/List")
                )
            ),
            SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "name",
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassName")
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getImports",
                    List(),
                    ObjectType(className = "java/util/TreeSet")
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getCanonicalName",
                    List(),
                    ObjectType(className = "java/lang/String")
                )
            ),
            SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                    "getSubclasses",
                    List(),
                    ObjectType(className = "java/util/List")
                )
            )
        ).sortBy(Utilities.ensembleElementsSortOrder)

    private val typesMembers =
        List[SourceElement[AnyRef]](
            SourceElement(ObjectType(className = "cirrus/hibernate/type/TypeFactory"))
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/PrimitiveType"),
                    "<init>",
                    List(),
                    VoidType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "set",
                    List(ObjectType(className = "java/lang/String"), BooleanType()),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "list",
                    List(ObjectType(className = "java/lang/String"), BooleanType()),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "isPersistentCollectionType",
                    List(),
                    BooleanType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/PrimitiveType"),
                    "primitiveClass",
                    List(),
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "deepCopy",
                    List(
                        ObjectType(className = "java/lang/Object"),
                        ObjectType(className = "cirrus/hibernate/impl/SessionImplementor")
                    ),
                    ObjectType(className = "java/lang/Object")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "returnedClass",
                    List(),
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "nullSafeSet",
                    List(
                        ObjectType(className = "java/sql/PreparedStatement"),
                        ObjectType(className = "java/lang/Object"),
                        IntegerType(),
                        ObjectType(className = "cirrus/hibernate/impl/SessionImplementor")
                    ),
                    VoidType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "sqlTypes",
                    List(ObjectType(className = "cirrus/hibernate/impl/PersistenceImplementor")),
                    ArrayType(IntegerType())
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "equals",
                    List(ObjectType(className = "java/lang/Object"), ObjectType(className = "java/lang/Object")),
                    BooleanType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "sortedMap",
                    List(
                        ObjectType(className = "java/lang/String"),
                        BooleanType(),
                        ObjectType(className = "java/util/Comparator")
                    ),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "<init>",
                    List(),
                    VoidType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "basic",
                    List(ObjectType(className = "java/lang/String")),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(ObjectType(className = "cirrus/hibernate/type/PrimitiveType"))
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "nullSafeGet",
                    List(
                        ObjectType(className = "java/sql/ResultSet"),
                        ArrayType(ObjectType(className = "java/lang/String")),
                        ObjectType(className = "cirrus/hibernate/impl/SessionImplementor"),
                        ObjectType(className = "java/lang/Object")
                    ),
                    ObjectType(className = "java/lang/Object")
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$5",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$3",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "nullSafeGet",
                    List(
                        ObjectType(className = "java/sql/ResultSet"),
                        ObjectType(className = "java/lang/String"),
                        ObjectType(className = "cirrus/hibernate/impl/SessionImplementor"),
                        ObjectType(className = "java/lang/Object")
                    ),
                    ObjectType(className = "java/lang/Object")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "association",
                    List(ObjectType(className = "java/lang/Class"), BooleanType()),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "toXML",
                    List(
                        ObjectType(className = "java/lang/Object"),
                        ObjectType(className = "cirrus/hibernate/impl/PersisterCache")
                    ),
                    ObjectType(className = "java/lang/String")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "array",
                    List(
                        ObjectType(className = "java/lang/String"),
                        BooleanType(),
                        ObjectType(className = "java/lang/Class")
                    ),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "isComponentType",
                    List(),
                    BooleanType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "value",
                    List(ObjectType(className = "java/lang/String")),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "sortedSet",
                    List(
                        ObjectType(className = "java/lang/String"),
                        BooleanType(),
                        ObjectType(className = "java/util/Comparator")
                    ),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "map",
                    List(ObjectType(className = "java/lang/String"), BooleanType()),
                    ObjectType(className = "cirrus/hibernate/type/Type")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "getColumnSpan",
                    List(ObjectType(className = "cirrus/hibernate/impl/PersistenceImplementor")),
                    IntegerType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "getName",
                    List(),
                    ObjectType(className = "java/lang/String")
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$1",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/PrimitiveType"),
                    "toXML",
                    List(ObjectType(className = "java/lang/Object")),
                    ObjectType(className = "java/lang/String")
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$0",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$6",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "<clinit>",
                    List(),
                    VoidType()
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "isPersistentObjectType",
                    List(),
                    BooleanType()
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "basics",
                    ObjectType(className = "java/util/HashMap")
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$2",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/Type"),
                    "isMutable",
                    List(),
                    BooleanType()
                )
            )
            , SourceElement(
                Field(
                    ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                    "class$4",
                    ObjectType(className = "java/lang/Class")
                )
            )
            , SourceElement(
                Method(
                    ObjectType(className = "cirrus/hibernate/type/PrimitiveType"),
                    "equals",
                    List(ObjectType(className = "java/lang/Object"), ObjectType(className = "java/lang/Object")),
                    BooleanType()
                )
            )
            , SourceElement(ObjectType(className = "cirrus/hibernate/type/Type"))

        ).sortBy(Utilities.ensembleElementsSortOrder)

    private val classMappingToTypeDependencies = List(
        invoke_interface(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ), Method(
                ObjectType(className = "cirrus/hibernate/type/Type"),
                "returnedClass",
                List(),
                ObjectType(className = "java/lang/Class")
            )
        ),
        invoke_interface(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ), Method(
                ObjectType(className = "cirrus/hibernate/type/Type"),
                "returnedClass",
                List(),
                ObjectType(className = "java/lang/Class")
            )
        ),
        class_cast(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            ObjectType(className = "cirrus/hibernate/type/PrimitiveType")
        ),
        instanceof(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            ObjectType(className = "cirrus/hibernate/type/PrimitiveType")
        ),
        invoke_virtual(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            Method(
                ObjectType(className = "cirrus/hibernate/type/PrimitiveType"),
                "primitiveClass",
                List(),
                ObjectType(className = "java/lang/Class")
            )
        ),
        invoke_static(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            Method(
                ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                "basic",
                List(ObjectType(className = "java/lang/String")),
                ObjectType(className = "cirrus/hibernate/type/Type")
            )
        ),
        invoke_static(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ),
            Method(
                ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                "basic",
                List(ObjectType(className = "java/lang/String")),
                ObjectType(className = "cirrus/hibernate/type/Type")
            )
        ),
        invoke_static(
            Method(
                ObjectType(className = "cirrus/hibernate/tools/codegen/ClassMapping"),
                "<init>",
                List(ObjectType(className = "org/jdom/Element")),
                VoidType()
            ), Method(
                ObjectType(className = "cirrus/hibernate/type/TypeFactory"),
                "basic",
                List(ObjectType(className = "java/lang/String")),
                ObjectType(className = "cirrus/hibernate/type/Type")
            )
        )
    ).sortBy(dependencySortOrder)


    def dependencySortOrder(d: Dependency[AnyRef, AnyRef]) = (Utilities.dependencyAsKind(d),
            Utilities.ensembleElementsSortOrder(SourceElement(d.source)),
            Utilities.ensembleElementsSortOrder(SourceElement(d.target))
            )
}