package sae.test.student

import sae.{Relation, SetExtent, BagExtent}
import sae.syntax.sql._

//
case class CourseDescription(title: String)


case class RecommendedBook(course: CourseDescription, author: String, title: String)

case class Lecturer(course: CourseDescription, name: String)

class Database()
{

    val descriptions: Relation[CourseDescription] =
        new BagExtent[CourseDescription]
    val lecturers: Relation[Lecturer] =
        new SetExtent[Lecturer]
    val books: Relation[RecommendedBook] =
        new SetExtent[RecommendedBook]

    def authorName: RecommendedBook => String = _.author

    def lecturerName: Lecturer => String = _.name

    /*
    val lecturersWithOwnBook: Relation[Lecturer] =
        SELECT (first) FROM(lecturers, books) WHERE
                (lecturerName === authorName)
     */
}