package edu.hneu.studentsportal.repository;

import edu.hneu.studentsportal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByEmail(String email);

}