package edu.hneu.studentsportal.conditions;

import edu.hneu.studentsportal.domain.Discipline;
import edu.hneu.studentsportal.enums.DisciplineType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DisciplineConditions {

    public static boolean isMasterDiscipline(Discipline discipline) {
        Validate.notNull(discipline, "The discipline must not be null!");
        return DisciplineType.MAGMAYNOR.equals(discipline.getType());
    }

    public static boolean isMasterDisciplineLabel(String label) {
        Validate.notEmpty(label, "The discipline label must not be empty!");
        return label.toLowerCase().contains("маголего");
    }
}
