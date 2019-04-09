package com.example.scriptengine.model.swagger;

import io.swagger.annotations.ApiModelProperty;

public interface ApiScriptNotActive {
    @ApiModelProperty(example = "Not Acceptable")
    String getTitle();
    @ApiModelProperty(example = "406")
    int getStatus();
    @ApiModelProperty(example = "Script is not active")
    String getDetail();
}
