package sae.test.student

/**
 *
 * Author: Ralf Mitschke
 * Date: 14.09.12
 * Time: 11:57
 *
 */
class Student(val firstName: String,
              val lastName: String,
              val grades: List[Int])
{
    def gradeAverage: Float = grades.reduce(_ + _) / grades.length
}