package dev.ua.ikeepcalm.lumios.database.entities.records;

import dev.ua.ikeepcalm.lumios.database.entities.reverence.LumiosUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name = "messageRecords")
public class MessageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private Long messageId;

    @Column
    @Nullable
    private Long replyToMessageId;

    @Column
    @NotNull
    private Long chatId;

    @Column(columnDefinition = "LONGTEXT")
    private String text;

    @Column
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user")
    private LumiosUser user;
}
