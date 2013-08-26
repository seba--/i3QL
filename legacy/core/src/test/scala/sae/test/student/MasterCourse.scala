package sae.test.student

/**
 *
 * Author: Ralf Mitschke
 * Date: 14.09.12
 * Time: 13:43
 *
 */
class MasterCourse(val prerequisite: Course,
                   title: String)
        extends Course(title)