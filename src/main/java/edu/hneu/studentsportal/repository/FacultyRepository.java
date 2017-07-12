package edu.hneu.studentsportal.repository;

import edu.hneu.studentsportal.domain.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long>, FacultyRepositoryCustom {

    Optional<Faculty> findByName(String name);

    Optional<Faculty> findById(Long id);

    Optional<Faculty> findByCode(String code);

    @Query(value = "SELECT * FROM Faculty LIMIT 1", nativeQuery = true)
    Faculty findFirst();

}
