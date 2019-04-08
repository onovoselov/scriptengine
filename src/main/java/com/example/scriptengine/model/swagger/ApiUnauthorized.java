package com.example.scriptengine.model.swagger;

import io.swagger.annotations.ApiModelProperty;
import org.zalando.problem.spring.web.advice.ProblemHandling;

public interface ApiUnauthorized {
    @ApiModelProperty(example = "Unauthorized")
    String getTitle();
    @ApiModelProperty(example = "401")
    int getStatus();
    @ApiModelProperty(example = "Full authentication is required to access this resource")
    String getDetail();
}
