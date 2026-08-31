package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;

import java.util.List;
import java.util.Set;

/**
 * Which classes each member of a chat actually attends, and how they want to be reminded.
 */
public interface PersonalTimetableService {

    /**
     * The member's row for this chat, created on first use.
     */
    TimetableMember member(Long chatId, Long telegramUserId);

    /**
     * Members of this chat who should receive personal reminders.
     */
    List<TimetableMember> remindableMembers(Long chatId);

    /**
     * Chats where this member has been through the picker.
     */
    List<TimetableMember> membershipsOf(Long telegramUserId);

    void save(TimetableMember member);

    /**
     * Marks the member unreachable in private, after Telegram refused a direct message.
     */
    void markDmUnavailable(Long chatId, Long telegramUserId);

    Set<String> chosenSubjects(Long chatId, Long telegramUserId);

    /**
     * Adds the elective if absent, removes it if present.
     *
     * @return true when the member now attends it
     */
    boolean toggleChoice(Long chatId, Long telegramUserId, String subjectKey);

    /**
     * Drops choices whose subject is no longer an elective of the chat - for instance after a
     * re-import replaced the curriculum.
     */
    int pruneChoices(Long chatId, Set<String> validSubjectKeys);

}
