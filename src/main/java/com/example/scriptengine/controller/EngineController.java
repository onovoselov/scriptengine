package com.example.scriptengine.controller;

import com.example.scriptengine.exceptions.NotFoundException;
import com.example.scriptengine.model.Task;
import com.example.scriptengine.model.TaskLog;
import com.example.scriptengine.model.dto.TaskResult;
import com.example.scriptengine.model.dto.TaskResultWidthLog;
import com.example.scriptengine.model.dto.TaskStart;
import com.example.scriptengine.service.TaskService;
import com.example.scriptengine.util.Converters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

@RestController
@RequestMapping("task")
public class EngineController {

    @Autowired
    TaskService taskService;

    /**
     * Script execution:
     * <i>curl -X POST -H "Content-Type: application/json" -d @FILE_TASK_START_JSON http://localhost:8080/task</i>
     * FILE_TASK_START_JSON - File with json string. Example: {"script":"print()","blocked":false}
     *
     * @param taskStart TaskStart
     * @param uriComponentsBuilder UriComponentsBuilder
     * @return DeferredResult
     * Blocked mode: return TaskResult
     * Unblocked mode:
     * HTTP/1.1 201 Created
     * Location: /task/f9d4092f-a614-4c58-96f7-8a1e0b564078
     */
    @PostMapping
    public DeferredResult<ResponseEntity<?>> newTask(@RequestBody TaskStart taskStart,
                                                     UriComponentsBuilder uriComponentsBuilder) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        if(taskStart.isBlocked()) {   // Blocked mode
            ForkJoinPool.commonPool().submit(() -> {
                TaskResult result = taskService.run(taskStart.getScript(), true);
                ResponseEntity<TaskResult> responseEntity =
                        new ResponseEntity<>(result, HttpStatus.OK);
                deferredResult.setResult(responseEntity);
            });
        } else {  // Unblocked mode
            TaskResult result = taskService.run(taskStart.getScript(), false);
            HttpHeaders headers = new HttpHeaders();
            UriComponents uriComponents = uriComponentsBuilder.path("/task/{id}").buildAndExpand(result.getId());
            headers.setLocation(uriComponents.toUri());
            ResponseEntity<Void> responseEntity = new ResponseEntity<>(headers, HttpStatus.CREATED);
            deferredResult.setResult(responseEntity);
        }

        return deferredResult;
    }

    /**
     * Returns a task list for a given stage.
     * <i>curl -X GET http://localhost:8080/task?satge=DoneOk</i>
     *
     * @param stage One of: Pending, InProgress, DoneOk, DoneError, Interrupted
     * @return List<TaskResult>
     */
    @GetMapping()
    public List<TaskResult> tasks(@RequestParam("stage") Optional<String> stage) {
        return taskService.getTasks(Converters.stringToTaskStage(stage.orElse("InProgress")));
    }

    /**
     * Returns task info by id
     * <i>curl -X GET http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Task id
     * @return TaskResultWidthLog
     */
    @GetMapping("{id}")
    public TaskResultWidthLog task(@PathVariable String id) {
        return taskService.getTaskResult(id);
    }

    /**
     * Terminates task by ID
     * <i>curl -X DELETE http://localhost:8080/task/f9d4092f-a614-4c58-96f7-8a1e0b564078</>
     *
     * @param id Task id
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        taskService.interrupt(id);
    }
}

