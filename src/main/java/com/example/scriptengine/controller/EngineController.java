package com.example.scriptengine.controller;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.exceptions.PermissionException;
import com.example.scriptengine.exceptions.ScriptCompileException;
import com.example.scriptengine.model.User;
import com.example.scriptengine.model.dto.TaskResult;
import com.example.scriptengine.security.AuthenticationFacade;
import com.example.scriptengine.service.TaskExecutor;
import com.example.scriptengine.service.TaskService;
import com.example.scriptengine.service.script.writer.ResponseBodyEmitterWriter;
import com.example.scriptengine.util.Converters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.Writer;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("task")
public class EngineController {
    private TaskService taskService;

    public EngineController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Script execution: <i>curl -X POST -H "Content-Type: text/plain" -d @SCRIPT_FILE
     * http://localhost:8080/task?blocked=1</i>
     *
     * @param script Javascript content
     * @param blocked Blocked mode = 1 (default), Unblocked mode = 0
     * @param uriComponentsBuilder UriComponentsBuilder
     * @return Blocked mode: Returns script output Unblocked mode: HTTP/1.1 201 Created Location:
     *     /task/f9d4092f-a614-4c58-96f7-8a1e0b564078
     */
    @PostMapping()
    @PreAuthorize("authenticated")
    public ResponseEntity<ResponseBodyEmitter> newTask(
            @RequestBody String script,
            @RequestParam("blocked") Optional<Integer> blocked,
            UriComponentsBuilder uriComponentsBuilder,
            AuthenticationFacade authenticationFacade)
            throws ScriptCompileException {

        User user = authenticationFacade.getUser();
        if (blocked.orElse(1) == 1) {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();
            Writer stdoutWriter = new ResponseBodyEmitterWriter(emitter);
            taskService.runBlocked(script, user.getName(), stdoutWriter);
            return new ResponseEntity<>(emitter, HttpStatus.OK);
        } else {
            TaskExecutor taskExecutor = taskService.runUnblocked(script, user.getName());
            UriComponents uriTask =
                    uriComponentsBuilder
                            .path("/task/{id}")
                            .buildAndExpand(taskExecutor.getTaskId());
            return ResponseEntity.created(uriTask.toUri()).build();
        }
    }

    /**
     * Returns a task list for a given stage. <i>curl -X GET
     * http://localhost:8080/task?satge=DoneOk</i>
     *
     * @param stage One of: Pending, InProgress, DoneOk, DoneError, Interrupted
     * @return List<TaskResult>
     */
    @GetMapping()
    public List<TaskResult> tasks(@RequestParam("stage") Optional<String> stage) {
        if (stage.isPresent()) {
            return taskService.getTasks(Converters.stringToTaskStage(stage.get()));
        } else {
            return taskService.getTasks();
        }
    }

    /**
     * Returns the script body for the task. <i>curl -X GET
     * http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078/body</>
     *
     * @param id Task id
     * @return Script body
     */
    @GetMapping("{id}/body")
    public String scriptBody(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return taskService.getTaskScriptBody(id, authenticationFacade.getUser());
    }

    /**
     * Returns the script output for the task. <i>curl -X GET
     * http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078/output</>
     *
     * @param id Task id
     * @return Script output
     */
    @GetMapping("{id}/output")
    public String scriptOutput(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return taskService.getTaskScriptOutput(id, authenticationFacade.getUser());
    }

    /**
     * Returns task info by id <i>curl -X GET
     * http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Task id
     * @return TaskResultWidthLog
     */
    @GetMapping("{id}")
    public TaskResult task(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException {
        return taskService.getTaskResult(id, authenticationFacade.getUser());
    }

    /**
     * Terminates task by ID <i>curl -X DELETE
     * http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Task id
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id, AuthenticationFacade authenticationFacade)
            throws PermissionException, NotFoundException {
        taskService.interrupt(id, authenticationFacade.getUser());
    }
}
