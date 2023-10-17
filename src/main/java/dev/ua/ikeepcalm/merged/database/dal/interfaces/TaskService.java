/*
 * Decompiled with CFR 0.150.
 */
package dev.ua.ikeepcalm.merged.database.dal.interfaces;

import dev.ua.ikeepcalm.merged.database.entities.reverence.ReverenceChat;
import dev.ua.ikeepcalm.merged.database.entities.tasks.DueTask;

import java.util.List;

public interface TaskService {

    void save(DueTask dueTask);

    List<DueTask> getTasksForCurrentChat(ReverenceChat chat);

    DueTask findTaskById(Long id);
}

