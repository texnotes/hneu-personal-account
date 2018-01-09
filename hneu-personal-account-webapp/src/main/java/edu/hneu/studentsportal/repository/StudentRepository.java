package edu.hneu.studentsportal.repository;

import edu.hneu.studentsportal.domain.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    Student findByEmail(String email);

    List<Student> findByGroup(Group group, Sort sort);

    Optional<Student> findByNameAndSurnameAndGroup(String name, String surname, Group group);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    class StudentSpecifications {

        public static Specifications<Student> hasSpecialityAndEducationProgram(Speciality speciality, EducationProgram educationProgram) {
            return Specifications.where((root, query, cb) -> cb.and(
                    cb.equal(root.get("speciality"), speciality),
                    cb.equal(root.get("educationProgram"), educationProgram))
            );
        }

        public static Specifications<Student> hasIncomeYear(Integer incomeYear) {
            return Specifications.where((root, query, cb) -> cb.and(cb.equal(root.get("incomeYear"), incomeYear)));
        }

    }
}
