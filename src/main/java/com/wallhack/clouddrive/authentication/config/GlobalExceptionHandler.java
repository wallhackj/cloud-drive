package com.wallhack.clouddrive.authentication.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(NoHandlerFoundException ex) {
        if (ex.getRequestURL().endsWith("/favicon.ico")) {
            return new ModelAndView("forward:/static/favicon.ico");
        }
        log.error(ex.getMessage(), ex);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("not_found");
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleInternalServerError(Exception ex) {
        log.error(ex.getMessage(), ex);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("server_error");
        return modelAndView;
    }
}
