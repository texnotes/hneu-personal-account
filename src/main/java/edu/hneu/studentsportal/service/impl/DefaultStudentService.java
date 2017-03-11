package edu.hneu.studentsportal.service.impl;

import edu.hneu.studentsportal.entity.*;
import edu.hneu.studentsportal.enums.UserRole;
import edu.hneu.studentsportal.parser.PointsExcelParser;
import edu.hneu.studentsportal.parser.StudentProfileExcelParser;
import edu.hneu.studentsportal.parser.dto.PointsDto;
import edu.hneu.studentsportal.repository.GroupDao;
import edu.hneu.studentsportal.repository.StudentDao;
import edu.hneu.studentsportal.repository.StudentRepository;
import edu.hneu.studentsportal.service.StudentService;
import edu.hneu.studentsportal.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang.BooleanUtils.isFalse;

@Service
public class DefaultStudentService implements StudentService {

    private static final Logger LOG = Logger.getLogger(DefaultStudentService.class);

    private static final int PREFIX_LENGTH = 5;
    private static final double MIN_SIMILARITY_COEFFICIENT = 0.6;
    private static final Map<String, String> semesters = new HashMap<>();
    private static final String GET_STUDENT_EMAIL_URL = "/EmailToOutController?name=%s&surname=%s&groupId=%s";
    private static final String SEND_PASSWORD_VM_TEMPLATE = "velocity/sendProfileWasCreated.vm";
    private static final String SEND_PROFILE_WAS_CHANGED_VM_TEMPLATE = "velocity/sendProfileWasChangedMessage.vm";

    static {
        semesters.put("1", "І СЕМЕСТР");
        semesters.put("2", "ІІ СЕМЕСТР");
        semesters.put("3", "ІІІ СЕМЕСТР");
        semesters.put("4", "IV СЕМЕСТР");
        semesters.put("5", "V СЕМЕСТР");
        semesters.put("6", "VІ СЕМЕСТР");
        semesters.put("7", "VІI СЕМЕСТР");
    }

    @Autowired
    private StudentDao studentDao;
    @Autowired
    private UserService userService;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private GroupDao groupDao;
    @Autowired
    private DefaultEmailService emailService;
    @Autowired
    private GmailService gmailService;
    @Autowired
    private StudentRepository studentRepository;

    @Value("${support.mail}")
    public String supportMail;
    @Value("${emails.integration.service.url}")
    public String emailsIntegrationServiceUrl;

    @Override
    public StudentProfile readStudentProfilesFromFile(final File file) {
        LOG.info("Create new profile from : " + file.getAbsolutePath());
        return ((StudentProfileExcelParser) context.getBean("studentProfileExcelParser")).parse(file);
    }

    @Override
    public void save(final StudentProfile studentProfile) {
        studentRepository.save(studentProfile);
    }

    @Override
    public StudentProfile findStudentProfileById(final String id) {
        return studentDao.findById(id);
    }

    @Override
    public Optional<StudentProfile> findStudentProfileByEmail(final String email) {
        return studentRepository.findByEmail(email);
    }

    public void updateStudentsScoresFromFile(final File file) {
        final PointsDto studentsPoints = new PointsExcelParser().parse(file);
        for (final Map.Entry<String, Map<String, String>> studentScore : studentsPoints.getMap().entrySet()) {
            final StudentProfile studentProfile = getStudentProfile(studentScore);
            if (nonNull(studentProfile)) {
                final String semesterId = studentsPoints.getSemester();
                updateStudentProfileSemester(studentProfile, semesterId, studentScore.getValue());
                save(studentProfile);
                sendEmailAfterProfileUpdating(studentProfile);
            }
        }
    }

    private StudentProfile getStudentProfile(final Map.Entry<String, Map<String, String>> studentScore) {
        final String[] keys = studentScore.getKey().split("\\$");
        if (keys.length == 2) {
            final String subKey = keys[0];
            final String groupCode = keys[1];
            return findStudentProfile(subKey, groupCode);
        }
        return null;
    }

    private StudentProfile findStudentProfile(final String subKey, final String groupCode) {
        return studentDao.find(subKey, groupCode);
    }

    private void updateStudentProfileSemester(final StudentProfile studentProfile, final String semesterId, final Map<String, String> studentScore) {
        final Optional<Semester> semester = findSemesterForLabel(studentProfile, semesterId);
        if (semester.isPresent()) {
            final List<Discipline> disciplines = semester.get().getDisciplines();
            for (final Discipline discipline : disciplines)
                discipline.setMark(getDisciplineScore(studentScore, discipline.getLabel()));
            if (!studentScore.isEmpty() && studentScore.values().stream().noneMatch("-"::equals))
                throw new RuntimeException("Some disciplines were not found");
        }
    }

    private Discipline getEmptyDiscipline(final List<Discipline> disciplines) {
        return disciplines.stream().filter(discipline -> StringUtils.isEmpty(discipline.getLabel())).findFirst().orElse(new Discipline());
    }

    private Optional<Semester> findSemesterForLabel(final StudentProfile studentProfile, final String semesterId) {
        for (final Course course : studentProfile.getCourses())
            for (final Semester semester : course.getSemesters())
                if (Objects.equals(semesters.get(semesterId), semester.getLabel()))
                    return Optional.of(semester);
        return Optional.empty();
    }

    private String getDisciplineScore(final Map<String, String> studentScore, final String discipline) {
        for (final String disciplineName : studentScore.keySet()) {
            if (isAppropriateDisciplineName(discipline, disciplineName)) {
                final String score = studentScore.get(disciplineName);
                studentScore.remove(disciplineName);
                return score;
            }
        }
        return null; // should be empty in case when discipline mark wasn't found.
    }

    private boolean isAppropriateDisciplineName(final String discipline, final String total) {
        final int levenshteinDistance = StringUtils.getLevenshteinDistance(discipline, total);
        final double antiSimilarityCoefficient = levenshteinDistance * 1.0 / Math.max(discipline.length(), total.length());
        return antiSimilarityCoefficient < MIN_SIMILARITY_COEFFICIENT && total.startsWith(discipline.substring(0, PREFIX_LENGTH)) ||
                total.startsWith(discipline.substring(0, discipline.length() / 3));
    }

    @Override
    public List<StudentProfile> find() {
        return studentDao.findAll();
    }

    @Override
    public void setCredentials(final StudentProfile studentProfile) {
        final String studentEmail = getStudentEmail(studentProfile);
        if (StringUtils.isNotBlank(studentEmail)) {
            final User user = new User();
            user.setId(studentEmail.toLowerCase());
            studentProfile.setEmail(studentEmail.toLowerCase());
            user.setRole(UserRole.STUDENT);
            userService.save(user);
        } else {
            throw new RuntimeException("Cannot found email for the user!");
        }
    }

    @Override
    public void setGroupId(final StudentProfile studentProfile) {
        //final Group group = groupDao.findOne(studentProfile.getStudentGroup());
        //studentProfile.setStudentGroupId(group.getId());
    }

    @Override
    public void sendEmailAfterProfileCreation(final StudentProfile studentProfile) {
        /*try {
            final Map<String, Object> modelForVelocity = ImmutableMap.of("name", studentProfile.getName());
            //@formatter:off
            final MimeMessage mimeMessage = emailService.new MimeMessageBuilder(
                    supportMail, studentProfile.getEmail())
                    .setSubject("Кабінет студента | Вхід")
                    .setText(emailService.createHtmlFromVelocityTemplate(SEND_PASSWORD_VM_TEMPLATE, modelForVelocity), true)
                    .build();
            //@formatter:on
            gmailService.api().users().messages().send(supportMail, gmailService.convertToGmailMessage(mimeMessage)).execute();
        } catch (final Exception e) {
            LOG.warn(e);
        }*/
    }

    @Override
    public void sendEmailAfterProfileUpdating(final StudentProfile studentProfile) {
        /*try {
            final Map<String, Object> modelForVelocity = ImmutableMap.of("message", "Ваш профіль був оновлений. Перейдійть у кабінет для перегляду.");
            final MimeMessage mimeMessage = emailService.new MimeMessageBuilder(supportMail, studentProfile.getEmail())
                    .setSubject("Кабінет студента | Зміни в профілі")
                    .setText(emailService.createHtmlFromVelocityTemplate(SEND_PROFILE_WAS_CHANGED_VM_TEMPLATE, modelForVelocity), true).build();
            gmailService.api().users().messages().send(supportMail, gmailService.convertToGmailMessage(mimeMessage)).execute();
        } catch (final Exception e) {
            LOG.warn(e);
        }*/
    }

    @Override
    public List<StudentProfile> find(final String searchCriteria, final Integer page) {
        return studentDao.find(searchCriteria, page);
    }

    @Override
    public long getPagesCountForSearchCriteria(final String searchCriteria) {
        return studentDao.getPagesCountForSearchCriteria(searchCriteria);
    }

    @Override
    public void remove(final String id) {
        studentDao.remove(id);
    }


    private String getStudentEmail(final StudentProfile studentProfile) {
        try {
            final String name = studentProfile.getName().toLowerCase().split(" ")[0];
            final String surname = studentProfile.getSurname().toLowerCase();
            final RestTemplate restTemplate = new RestTemplate();
            return restTemplate
                    .getForEntity(String.format(emailsIntegrationServiceUrl + GET_STUDENT_EMAIL_URL, name, surname, studentProfile.getStudentGroup().getName()), String.class)
                    .getBody();
        } catch (final RuntimeException e) {
            LOG.warn("Cannot receive the email!", e);
            return StringUtils.EMPTY;
        }
    }

    @Override
    public void refreshMarks() {
        final Map<String, List<StudentProfile>> studentsPerSpecialityAndCourse = studentDao.findAll().stream()
                .collect(Collectors.groupingBy(s -> s.getIncomeYear() + "$" + s.getSpeciality(), Collectors.mapping(s -> s, Collectors.toList())));
        studentsPerSpecialityAndCourse.values().forEach(students -> {
            students.forEach(student -> {
                final List<Discipline> allStudentDisciplines = extractDisciplinesFunction.apply(student);
                final Double studentAverage = calculateAverageFunction.apply(allStudentDisciplines);
                if(isFalse(studentAverage.isNaN())) {
                    student.setAverage(new BigDecimal(studentAverage).setScale(2, RoundingMode.HALF_UP).doubleValue());
                } else {
                    student.setAverage(0.0);
                }
            });
            Collections.sort(students, (s1, s2) -> NumberUtils.compare(s2.getAverage(), s1.getAverage()));
            IntStream.range(0, students.size()).forEach(i -> students.get(i).setRate(i + 1));
            students.forEach(studentDao::save);
        } );
    }

    Function<List<Discipline>, Double> calculateAverageFunction = disciplines -> {
        double total = disciplines.stream().map(Discipline::getMark).mapToDouble(Double::valueOf).sum();
        return total / disciplines.size();
    };

    Function<StudentProfile, List<Discipline>> extractDisciplinesFunction = student -> student
            .getCourses().stream()
            .flatMap(c -> c.getSemesters().stream())
            .flatMap(s -> s.getDisciplines().stream())
            .filter(d -> NumberUtils.isNumber(d.getMark()))
            .collect(Collectors.toList());
}
