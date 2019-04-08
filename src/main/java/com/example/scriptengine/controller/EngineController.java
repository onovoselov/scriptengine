package com.example.scriptengine.controller;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.ScriptResource;
import com.example.scriptengine.security.AuthenticationFacade;
import com.example.scriptengine.service.ScriptExecutor;
import com.example.scriptengine.service.ScriptService;
import com.example.scriptengine.service.script.writer.ResponseBodyEmitterWriter;
import com.example.scriptengine.util.Converters;
//import io.swagger.annotations.*;
import io.swagger.annotations.Api;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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

    /**
     * Script execution:
     * <i>curl -X POST -H "Content-Type: text/plain" -d @SCRIPT_FILE http://localhost:8080/script?blocked=1</i>
     *
     * @param script Javascript content
     * @return Blocked mode: Returns script output Unblocked mode: HTTP/1.1 201 Created Location:
     *     /script/f9d4092f-a614-4c58-96f7-8a1e0b564078
     */

//    @ApiOperation(value = "View a list of available products",response = ResponseEntity.class)
//    @ApiResponses(value = {
//        @ApiResponse(code = 200, message = "Successfully retrieved list"),
//        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
//        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
//        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
//    }
//    )
    @PostMapping("/blocked")
    @PreAuthorize("authenticated")
    public ResponseEntity newScriptBlocked(
            @RequestBody String script,
            AuthenticationFacade authenticationFacade)
            throws ScriptCompileException {
        User user = authenticationFacade.getUser();

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        Writer stdoutWriter = new ResponseBodyEmitterWriter(emitter);
        scriptService.runBlocked(script, user.getName(), stdoutWriter);
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }

    @PostMapping("/unblocked")
    @PreAuthorize("authenticated")
    public ResponseEntity<ResponseBodyEmitter> newScriptUnblocked(
        @RequestBody String script,
        UriComponentsBuilder uriComponentsBuilder,
        AuthenticationFacade authenticationFacade)
        throws ScriptCompileException {

        User user = authenticationFacade.getUser();
        ScriptExecutor scriptExecutor = scriptService.runUnblocked(script, user.getName());
        UriComponents uriScript =
            uriComponentsBuilder
                .path("/script/{id}")
                .buildAndExpand(scriptExecutor.getScriptId());
        return ResponseEntity.created(uriScript.toUri()).build();
    }

    /**
     * Returns a script list for a given stage.
     * <i>curl -X GET  http://localhost:8080/script?satge=DoneOk</i>
     *
     * @param stage One of: Pending, InProgress, DoneOk, DoneError, Interrupted
     * @return Script list
     */
    @GetMapping()
    public ResponseEntity<Resources<ScriptResource>> scripts(@RequestParam("stage") Optional<String> stage) {
        Collection<ScriptResource> collection;
        if (stage.isPresent()) {
            collection = scriptService.getScripts(Converters.stringToScriptStage(stage.get()));
        } else {
            collection = scriptService.getScripts();
        }

        final Resources<ScriptResource> resources = new Resources<>(collection);
        final String uriString = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
        resources.add(new Link(uriString, "self"));
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
            .body(resources);
    }

    /**
     * Returns the script body for the script.
     * <i>curl -X GET http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078/body</>
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
     * Returns the script output for the script.
     * <i>curl -X GET http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078/output</>
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
     * Returns script info by id
     * <i>curl -X GET http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Script id
     * @return ScriptResourceWidthLog
     */
    @GetMapping("{id}")
    public ScriptResource script(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return scriptService.getScriptResult(id, authenticationFacade.getUser());
    }

    /**
     * Terminates script by ID
     * <i>curl -X DELETE http://localhost:8080/script/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Script id
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException, NotFoundException {
        scriptService.interrupt(id, authenticationFacade.getUser());
    }
}
