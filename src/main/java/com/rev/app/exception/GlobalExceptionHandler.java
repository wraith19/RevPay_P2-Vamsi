package com.rev.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException ex, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("error", "An unexpected error occurred. Please try again.");
        return "redirect:/dashboard";
    }
}
