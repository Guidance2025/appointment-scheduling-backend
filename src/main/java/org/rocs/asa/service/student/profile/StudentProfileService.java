package org.rocs.asa.service.student.profile;

import org.rocs.asa.domain.student.Student;

public interface StudentProfileService {
    /**
     * Find a student by their student number
     *
     * @param studentNumber the unique student number to search for
     * @return Student contains student information
     */
    Student findStudentByStudentNumber(String studentNumber);


    Student saveStudentProfile(Student student);



}
