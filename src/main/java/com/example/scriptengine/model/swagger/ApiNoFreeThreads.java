package com.example.scriptengine.model.swagger;

import io.swagger.annotations.ApiModelProperty;

public interface ApiNoFreeThreads {
    @ApiModelProperty(example = "Not found")
    String getTitle();
    @ApiModelProperty(example = "404")
    int getStatus();
    @ApiModelProperty(example = "There are no free threads to execute the script. Try later.")
    String getDetail();
}