package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 10:12
 *
 */

trait UnissonQuery {

}


object UnissonQuery {


    def asString(query:UnissonQuery)(implicit packageSubstitution : (String, String) = ("", "")) : String =
    // NOTE: declaring packageSubstitution as two strings is not feasible because the implicits are ambiguous
        query match
    {
        case OrQuery(left,right) => asString(left) + " or " + asString(right)
        case WithoutQuery(left,right) => asString(left) + " without " + asString(right)
        case ClassQuery(inner) => "class(" + asString(inner) + ")"
        case ClassSelectionQuery(packageName,className) => "class(" + substitutePackagePrefix(packageName) + "," + className+ ")"
        case ClassWithMembersQuery(inner) => "class_with_members(" + asString(inner) + ")"
        case DerivedQuery() => "derived"
        case EmptyQuery() => "empty"
        case PackageQuery(packageName) => "package(" + substitutePackagePrefix(packageName) + ")"
        case RestQuery() => "rest"
        case SuperTypeQuery(inner) => "supertype(" + asString(inner) + ")"
        case TransitiveQuery(inner) => "transitive(" + asString(inner) + ")"
    }

    private def substitutePackagePrefix(packageName:String)(implicit packageSubstitution : (String, String)) : String =
    {
        val oldPackagePrefix : String = packageSubstitution._1
        val newPackagePrefix : String = packageSubstitution._2
        if( packageName.startsWith(oldPackagePrefix) ){
            newPackagePrefix +
                    packageName.substring(
                        oldPackagePrefix.length(),
                        packageName.length() - oldPackagePrefix.length() - 1
                    )
        }
        else{
            packageName
        }
    }
}