package sae.test.student

import sae.LocalIncrement

/**
 *
 * Author: Ralf Mitschke
 * Date: 14.09.12
 * Time: 11:57
 *
 */

//case class Department(name: String)

@LocalIncrement
case class ExaminationRegulation(department:String)

case class RegulationArticle(regulation: ExaminationRegulation, number:Int)

case class ArticleReference(regulation: ExaminationRegulation, number:Int)

case class CourseReference(regulation: ExaminationRegulation, name:String)

case class LabRegistration(student: Student,
                           groupMembers: List[Name])
{

}