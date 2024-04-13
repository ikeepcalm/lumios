package dev.ua.ikeepcalm.queueupnow.database.entities.tasks.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ua.ikeepcalm.queueupnow.database.entities.tasks.DueTask;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskWrapper {

    @JsonProperty("dueDate")
    private LocalDate dueDate;
    @JsonProperty("dueTime")
    private LocalTime dueTime;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("url")
    private String url;
    public static List<TaskWrapper> wrapTasks(List<DueTask> tasks){
        List<TaskWrapper> taskWrappers = new LinkedList<>();
        for (DueTask task : tasks) {
            TaskWrapper taskWrapper = new TaskWrapper();
            taskWrapper.setDueDate(task.getDueDate());
            taskWrapper.setDueTime(task.getDueTime());
            taskWrapper.setTaskName(task.getTaskName());
            taskWrapper.setUrl(task.getUrl());
            taskWrappers.add(taskWrapper);
        } return taskWrappers;
    }

    public static TaskWrapper wrapTask(DueTask task){
        TaskWrapper taskWrapper = new TaskWrapper();
        taskWrapper.setDueDate(task.getDueDate());
        taskWrapper.setDueTime(task.getDueTime());
        taskWrapper.setTaskName(task.getTaskName());
        taskWrapper.setUrl(task.getUrl());
        return taskWrapper;
    }

}