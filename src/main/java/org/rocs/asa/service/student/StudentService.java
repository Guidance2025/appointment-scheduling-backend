package org.rocs.asa.service.student;

import org.rocs.asa.domain.person.student.Student;

public interface StudentService {
    /**
     * Find a student by their student number
     *
     * @param studentNumber the unique student number to search for
     * @return Student contains student information
     */
    Student findStudentByStudentNumber(String studentNumber);

}
