package com.example.scriptengine.model.swagger;

import io.swagger.annotations.ApiModelProperty;

public interface ApiScriptNotFound {
    @ApiModelProperty(example = "Not found")
    String getTitle();
    @ApiModelProperty(example = "404")
    int getStatus();
    @ApiModelProperty(example = "Script not found")
    String getDetail();
}
