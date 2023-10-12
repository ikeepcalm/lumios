/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.dal.interfaces;

import dev.ua.ikeepcalm.merged.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.entities.tasks.DueTask;

import java.util.List;

public interface TaskService {

    public void save(DueTask dueTask);

    public List<DueTask> getTasksForCurrentChat(ReverenceChat chat);

    DueTask findTaskById(Long id);
}

