package com.example.scriptengine.model.swagger;

import io.swagger.annotations.ApiModelProperty;

public interface ApiScriptError {
    @ApiModelProperty(example = "Bad Request")
    String getTitle();
    @ApiModelProperty(example = "400")
    int getStatus();
    @ApiModelProperty(example = "detail\": \"<eval>:1:12 Missing close quote\\nprint('Hello\\n            ^ in <eval> at line number 1 at column number 12")
    String getDetail();
}
