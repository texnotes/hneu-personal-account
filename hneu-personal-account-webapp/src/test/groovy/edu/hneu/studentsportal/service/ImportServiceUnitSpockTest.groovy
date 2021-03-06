package edu.hneu.studentsportal.service

import edu.hneu.studentsportal.domain.Discipline
import edu.hneu.studentsportal.domain.DisciplineMark
import edu.hneu.studentsportal.parser.factory.ParserFactory
import edu.hneu.studentsportal.parser.impl.DisciplinesExcelParser
import edu.hneu.studentsportal.parser.impl.StudentsChoiceExcelParser
import edu.hneu.studentsportal.repository.DisciplineRepository
import edu.hneu.studentsportal.repository.StudentRepository
import spock.lang.Specification

class ImportServiceUnitSpockTest extends Specification {

    def fileMock = Mock(File)
    def parseFactoryMock = Mock(ParserFactory)
    def disciplinesExcelParserMock = Mock(DisciplinesExcelParser)
    def studentsChoiceExcelParserMock = Mock(StudentsChoiceExcelParser)
    def disciplineRepositoryMock = Mock(DisciplineRepository)
    def disciplineMock1 = Mock(Discipline)
    def disciplineMock2 = Mock(Discipline)
    def disciplineMarkMock1 = Mock(DisciplineMark)
    def disciplineMarkMock2 = Mock(DisciplineMark)

    def disciplines = [disciplineMock1, disciplineMock2]

    def importService = new ImportService(parseFactoryMock, disciplineRepositoryMock)

//    def setup() {
//        parseFactoryMock.newDisciplinesParser() >> disciplinesExcelParserMock
//        parseFactoryMock.newStudentsChoiceParser() >> studentsChoiceExcelParserMock
//        disciplineMarkMock1.discipline >> disciplineMock1
//        disciplineMarkMock2.discipline >> disciplineMock2
//    }
//
//    def 'should parse disciplines when import disciplines'() {
//        given:
//        disciplinesExcelParserMock.parse(fileMock) >> disciplines
//        when:
//        def actual = importService.importDisciplines(fileMock)
//        then:
//        disciplines == actual
//    }
//
//    def 'should save all parsed disciplines when import disciplines'() {
//        given:
//        disciplinesExcelParserMock.parse(fileMock) >> disciplines
//        when:
//        importService.importDisciplines(fileMock)
//        then:
//        1 * disciplineRepositoryMock.save(disciplines)
//    }
}

