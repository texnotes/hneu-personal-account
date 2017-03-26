package edu.hneu.studentsportal.controller.management;

import edu.hneu.studentsportal.entity.Student;
import edu.hneu.studentsportal.service.FileService;
import edu.hneu.studentsportal.service.ImportService;
import edu.hneu.studentsportal.service.StudentService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;

@Log4j
@Controller
@RequestMapping("/management/import/student")
public class ImportStudentController {

    @Resource
    private ImportService importService;
    @Resource
    private StudentService studentService;
    @Resource
    private FileService fileService;

    @GetMapping
    public String importStudent() {
        return "management/uploadUserProfilesFromExcel";
    }

    @PostMapping
    @SneakyThrows
    public String importStudent(@RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes) {
        File file = fileService.getFile(multipartFile);
        try {
            Student student = importService.importStudent(file);
            studentService.sendEmailAfterProfileCreation(student);
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/management/students/" + student.getId();
        } finally {
            Files.deleteIfExists(file.toPath());
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public String importStudentException(RuntimeException e, RedirectAttributes redirectAttributes) {
        log.warn(e.getMessage(), e);
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "management/uploadUserProfilesFromExcel";
    }

}