package dev.ua.ikeepcalm.lumios.database.dal.interfaces;

import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
     * Members of this chat who want a reminder in any shape - private message, mention in the group,
     * or both. Callers filter further by {@code getReminderChannel()}, and by {@code isDmUnavailable()}
     * before sending anything privately.
     */
    List<TimetableMember> remindableMembers(Long chatId);

    /**
     * Everyone in the chat due a digest at this time, including those who never changed the setting
     * and so match {@code TimetableMember#DEFAULT_DIGEST_TIME}.
     */
    List<TimetableMember> digestMembersAt(LocalTime time);

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
     * Every member's choices in one query, keyed by Telegram user id. The reminder scheduler runs once
     * a minute over every slot, so asking per member does not scale.
     *
     * @return members with no choices recorded are absent from the map
     */
    Map<Long, Set<String>> chosenSubjectsByMember(Long chatId);

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
