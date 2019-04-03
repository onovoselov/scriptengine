package com.example.scriptengine.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionHandling implements ProblemHandling {
    @Override
    public boolean isCausalChainsEnabled() {
        return false;
    }

}
