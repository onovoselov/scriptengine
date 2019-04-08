package com.example.scriptengine.controller;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptRuntimeException;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.ScriptResourceResult;
import com.example.scriptengine.model.swagger.ApiScriptError;
import com.example.scriptengine.model.swagger.ApiUnauthorized;
import com.example.scriptengine.security.AuthenticationFacade;
import com.example.scriptengine.service.ScriptExecutor;
import com.example.scriptengine.service.ScriptService;
import com.example.scriptengine.service.script.writer.ResponseBodyEmitterWriter;
import com.example.scriptengine.util.Converters;
// import io.swagger.annotations.*;
import io.swagger.annotations.*;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

import java.io.Writer;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Api(tags = {"script"})
@RestController
@RequestMapping(value = "script", produces = "application/hal+json")
public class EngineController {
    private ScriptService scriptService;

    public EngineController(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    @ApiOperation(
            value = "Execute JavaScript code in blocked mode",
            notes = "",
            authorizations = @Authorization(value = "basic"))
    @ApiResponses(
            value = {
                @ApiResponse(
                        code = 200,
                        message = "Javascript successfully completed",
                        response = String.class),
                @ApiResponse(
                        code = 401,
                        message = "You are not authorized to view the resource.",
                        response = ApiUnauthorized.class),
                @ApiResponse(
                        code = 400,
                        message = "Script execution error.",
                        response = ApiScriptError.class)
            })
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = "script",
                value = "JavaScript code.",
                required = true,
                paramType = "body",
                example = "print('Hello Script Executor!')")
    })
    @PostMapping("blocked")
    public ResponseEntity<ResponseBodyEmitter> newScriptBlocked(
            @ApiParam(value = "JavaScript code.", required = true) @RequestBody String script,
            @ApiIgnore AuthenticationFacade authenticationFacade)
            throws ScriptRuntimeException {
        User user = authenticationFacade.getUser();

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        Writer stdoutWriter = new ResponseBodyEmitterWriter(emitter);
        scriptService.runBlocked(script, user.getName(), stdoutWriter);
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }

    @ApiOperation(
            value = "Execute JavaScript code in unblocked mode",
            notes = "",
            authorizations = @Authorization(value = "basic"),
            response = void.class)
    @ApiResponses(
            value = {
                @ApiResponse(
                        code = 201,
                        message = "Javascript started successfully",
                        responseHeaders =
                                @ResponseHeader(
                                        name = "Location",
                                        description = "Result URI of the newly launched script",
                                        response = String.class),
                        response = String.class),
                @ApiResponse(
                        code = 401,
                        message = "You are not authorized to view the resource.",
                        response = ApiUnauthorized.class),
                @ApiResponse(
                        code = 400,
                        message = "Script compilation error.",
                        response = ApiScriptError.class)
            })
    @PostMapping("unblocked")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity newScriptUnblocked(
            @ApiParam(value = "JavaScript code.", required = true) @RequestBody String script,
            @ApiIgnore UriComponentsBuilder uriComponentsBuilder,
            @ApiIgnore AuthenticationFacade authenticationFacade)
            throws ScriptRuntimeException {

        User user = authenticationFacade.getUser();
        ScriptExecutor scriptExecutor = scriptService.runUnblocked(script, user.getName());
        UriComponents uriScript =
                uriComponentsBuilder
                        .path("/script/{id}")
                        .buildAndExpand(scriptExecutor.getScriptId());
        return ResponseEntity.created(uriScript.toUri()).build();
    }

    /**
     * Returns a script list for a given stage. <i>curl -X GET
     * http://localhost:8080/script?satge=DoneOk</i>
     *
     * @param stage One of: Pending, InProgress, DoneOk, DoneError, Interrupted
     * @return Script list
     */
    @GetMapping()
    public ResponseEntity<Resources<ScriptResourceResult>> scripts(
            @RequestParam("stage") Optional<String> stage) {
        Collection<ScriptResourceResult> collection;
        if (stage.isPresent()) {
            collection = scriptService.getScripts(Converters.stringToScriptStage(stage.get()));
        } else {
            collection = scriptService.getScripts();
        }

        final Resources<ScriptResourceResult> resources = new Resources<>(collection);
        final String uriString =
                ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
        resources.add(new Link(uriString, "self"));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                .body(resources);
    }

    /**
     * Returns the script body for the script. <i>curl -X GET
     * http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078/body</>
     *
     * @param id Script id
     * @return Script body
     */
    @GetMapping("{id}/body")
    public String scriptBody(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return scriptService.getScriptBody(id, authenticationFacade.getUser());
    }

    /**
     * Returns the script output for the script. <i>curl -X GET
     * http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078/output</>
     *
     * @param id Script id
     * @return Script output
     */
    @GetMapping("{id}/output")
    public String scriptOutput(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return scriptService.getScriptOutput(id, authenticationFacade.getUser());
    }

    /**
     * Returns script info by id <i>curl -X GET
     * http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Script id
     * @return ScriptResourceResultWidthLog
     */
    @GetMapping("{id}")
    public ScriptResourceResult script(
            @PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return scriptService.getScriptResult(id, authenticationFacade.getUser());
    }

    /**
     * Terminates script by ID <i>curl -X DELETE
     * http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Script id
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException, NotFoundException {
        scriptService.interrupt(id, authenticationFacade.getUser());
    }
}
