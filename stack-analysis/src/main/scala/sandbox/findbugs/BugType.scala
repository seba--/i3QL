package sandbox.findbugs

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 29.11.12
 * Time: 13:04
 * To change this template use File | Settings | File Templates.
 */
object BugType extends Enumeration {
  val
    ES_COMPARING_STRINGS_WITH_EQ,
    ES_COMPARING_PARAMETER_STRING_WITH_EQ,
    RC_REF_COMPARISON,
    RC_REF_COMPARISON_BAD_PRACTICE,
    RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN,
    SA_FIELD_SELF_ASSIGNMENT,
    SA_LOCAL_SELF_ASSIGNMENT,
    SA_LOCAL_SELF_ASSIGNMENT_INSTEAD_OF_FIELD
    = Value
}
