package dev.ua.ikeepcalm.lumios.database.entities.timetable.types;

/**
 * Where a member wants to be told about their upcoming classes.
 * <p>
 * {@link #GROUP_TAG} does not send a second message: it attaches the member's mention to the class
 * line of the announcement the group already gets. Because that is one shared message at one shared
 * time, a per-member lead time cannot apply to it - only {@link #DM} honours
 * {@code TimetableMember#getLeadMinutes()}.
 */
public enum ReminderChannel {

    /**
     * A private message listing only their classes.
     */
    DM,

    /**
     * A mention on the group announcement, next to the elective they attend.
     */
    GROUP_TAG,

    /**
     * Both: a private message and a mention in the group.
     */
    BOTH,

    /**
     * No reminders at all. The member keeps their elective choices, so the personal views of
     * {@code /today} and friends still work.
     */
    OFF;

    public boolean sendsDm() {
        return this == DM || this == BOTH;
    }

    public boolean tagsInGroup() {
        return this == GROUP_TAG || this == BOTH;
    }
}
