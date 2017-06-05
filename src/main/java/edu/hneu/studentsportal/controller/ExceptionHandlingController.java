package edu.hneu.studentsportal.controller;


import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface ExceptionHandlingController {

    String baseUrl();

    Logger logger();

    @ExceptionHandler(RuntimeException.class)
    default String handleError(RuntimeException e, RedirectAttributes redirectAttributes) {
        return handleErrorInternal(e, e.getMessage(), redirectAttributes);
    }

    default String handleErrorInternal(Exception e, String errorMessage, RedirectAttributes redirectAttributes) {
        logger().warn(e.getMessage(), e);
        redirectAttributes.addFlashAttribute("error", errorMessage);
        return "redirect:" + baseUrl();
    }
}