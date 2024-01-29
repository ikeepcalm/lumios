package dev.ua.ikeepcalm.queueupnow.database.entities.history;

import dev.ua.ikeepcalm.queueupnow.database.entities.reverence.ReverenceUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "message_records")
public class MessageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private Long messageId;

    @Column
    @NotNull
    private Long chatId;

    @Column
    private String text;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ReverenceUser user;

}
