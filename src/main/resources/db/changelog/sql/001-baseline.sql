-- Baseline schema for Lumios, generated from the JPA entity model with the naming strategies
-- Spring Boot actually applies (CamelCaseToUnderscoresNamingStrategy + SpringImplicitNamingStrategy),
-- so the identifiers here are the snake_case ones the running application uses.
--
-- Applied only to an empty database. An existing deployment already has this schema, so the
-- changeset is marked as ran there instead (see the tableExists precondition in the changelog).


create table binds (
    id bigint not null auto_increment,
    chat_id bigint,
    user_id bigint,
    primary key (id)
) engine=InnoDB;

create table campus_bindings (
    id bigint not null auto_increment,
    access_token varchar(2048) not null,
    external_id varchar(255),
    subscribed_at datetime(6) not null,
    telegram_user_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table chats (
    id bigint not null auto_increment,
    ai_model tinyint check (ai_model between 0 and 1),
    bot_nickname varchar(255),
    chat_id bigint,
    communication_limit integer default 10,
    description varchar(255),
    is_ai_enabled boolean default false,
    is_dice_enabled boolean default false,
    is_plain_timetable_enabled boolean default false,
    is_timetable_enabled boolean default true,
    language varchar(5) default 'uk',
    last_wheel_date datetime(6),
    name varchar(255),
    reminder_lead_minutes integer,
    summary_limit integer default 2,
    primary key (id)
) engine=InnoDB;

create table chat_shots (
    id bigint not null auto_increment,
    date date,
    chat_id bigint,
    primary key (id)
) engine=InnoDB;

create table chat_shots_user_shots (
    chat_shot_id bigint not null,
    user_shots_id bigint not null
) engine=InnoDB;

create table class_entries (
    id bigint not null auto_increment,
    class_type enum ('LAB','LECTURE','PRACTICE','UNKNOWN'),
    end_time time(6),
    location varchar(255),
    name varchar(255),
    start_time time(6),
    teacher_name varchar(255),
    url varchar(255),
    day_entry_id bigint,
    primary key (id)
) engine=InnoDB;

create table day_entries (
    id bigint not null auto_increment,
    day_name tinyint check (day_name between 0 and 6),
    timetable_entry_id bigint,
    days_id bigint,
    primary key (id)
) engine=InnoDB;

create table day_entries_class_entries (
    day_entries_id bigint not null,
    class_entries_id bigint not null
) engine=InnoDB;

create table due_tasks (
    id bigint not null auto_increment,
    attachment varchar(255),
    author bigint,
    description varchar(255),
    due_date date,
    due_time time(6),
    scope tinyint check (scope between 0 and 2),
    state tinyint check (state between 0 and 7),
    task_name varchar(255),
    url varchar(2048),
    chat_id bigint,
    primary key (id)
) engine=InnoDB;

create table elective_choices (
    id bigint not null auto_increment,
    chat_id bigint not null,
    subject_key varchar(255) not null,
    telegram_user_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table message_records (
    id bigint not null auto_increment,
    chat_id bigint,
    date datetime(6),
    message_id bigint,
    reply_to_message_id bigint,
    text LONGTEXT,
    user integer,
    primary key (id)
) engine=InnoDB;

create table mixed_queues (
    id binary(16) not null,
    alias varchar(255),
    chat_id bigint,
    message_id integer,
    shuffled bit,
    primary key (id)
) engine=InnoDB;

create table mixed_users (
    id bigint not null auto_increment,
    account_id bigint,
    name varchar(255),
    username varchar(255),
    mixed_queue binary(16),
    primary key (id)
) engine=InnoDB;

create table simple_queues (
    id binary(16) not null,
    alias varchar(255),
    chat_id bigint,
    message_id integer,
    primary key (id)
) engine=InnoDB;

create table simple_users (
    id bigint not null auto_increment,
    account_id bigint,
    name varchar(255),
    username varchar(255),
    simple_queue binary(16),
    primary key (id)
) engine=InnoDB;

create table timetable_members (
    id bigint not null auto_increment,
    chat_id bigint not null,
    dm_reminders_enabled bit not null,
    dm_unavailable bit not null,
    lead_minutes integer,
    reviewed_at datetime(6),
    telegram_user_id bigint not null,
    primary key (id)
) engine=InnoDB;

create table timetable_entries (
    id bigint not null auto_increment,
    week_type enum ('UNKNOWN','WEEK_A','WEEK_B'),
    chat_id bigint,
    primary key (id)
) engine=InnoDB;

create table users (
    user_entity_id integer not null auto_increment,
    balance integer default 0,
    credits integer default 100,
    full_name varchar(255),
    reverence integer default 0,
    sustainable integer default 100,
    user_id bigint,
    username varchar(255),
    chat_id bigint,
    primary key (user_entity_id)
) engine=InnoDB;

create table user_shots (
    id bigint not null auto_increment,
    reverence integer,
    user_id bigint,
    username varchar(255),
    primary key (id)
) engine=InnoDB;

alter table campus_bindings 
   add constraint UK34omgfx3a8iqa59vc5r9eedeq unique (external_id);

alter table campus_bindings 
   add constraint UKmwqealh8t0b39sph0sw1tlem4 unique (telegram_user_id);

alter table chat_shots_user_shots 
   add constraint UKc418u1xb08trnnoluwhg92f2b unique (user_shots_id);

alter table day_entries_class_entries 
   add constraint UKck0n56it0xnvsei94jyw53nxs unique (class_entries_id);

create index idx_elective_choices_chat_user 
   on elective_choices (chat_id, telegram_user_id);

alter table elective_choices 
   add constraint uk_elective_choices_chat_user_subject unique (chat_id, telegram_user_id, subject_key);

alter table timetable_members 
   add constraint uk_timetable_members_chat_user unique (chat_id, telegram_user_id);

alter table chat_shots 
   add constraint FKd2qelk2iq26yi9k76jhgtjxv9 
   foreign key (chat_id) 
   references chats (id);

alter table chat_shots_user_shots 
   add constraint FKine2w2n2vkq5kegmlq3vyb5eg 
   foreign key (user_shots_id) 
   references user_shots (id);

alter table chat_shots_user_shots 
   add constraint FK2arci1n6c66gyj9xk7alv4ol8 
   foreign key (chat_shot_id) 
   references chat_shots (id);

alter table class_entries 
   add constraint FK5rkmilud0l7vn50h4rfn1nel6 
   foreign key (day_entry_id) 
   references day_entries (id);

alter table day_entries 
   add constraint FKb8fa8al2yxlwivee12k0vqgq7 
   foreign key (timetable_entry_id) 
   references timetable_entries (id);

alter table day_entries 
   add constraint FKhssb9naspl9xwp9xrc91b2sor 
   foreign key (days_id) 
   references timetable_entries (id);

alter table day_entries_class_entries 
   add constraint FK6pv50oc2i9v7wbslux2bypi4o 
   foreign key (class_entries_id) 
   references class_entries (id);

alter table day_entries_class_entries 
   add constraint FKt801rhegcsajmeo3tqjh6us9m 
   foreign key (day_entries_id) 
   references day_entries (id);

alter table due_tasks 
   add constraint FK3iy8odrvwq3jkcrgu35g0q2tf 
   foreign key (chat_id) 
   references chats (id);

alter table message_records 
   add constraint FKqmr17moo8m0j49deucyua4k9w 
   foreign key (user) 
   references users (user_entity_id);

alter table mixed_users 
   add constraint FKnc970b58y3oktmb5tn7w4gc34 
   foreign key (mixed_queue) 
   references mixed_queues (id);

alter table simple_users 
   add constraint FKt10mlkukdu05mg6htqbmycnua 
   foreign key (simple_queue) 
   references simple_queues (id);

alter table timetable_entries 
   add constraint FKed6ka20y1sr7ubp17e8ydkcc0 
   foreign key (chat_id) 
   references chats (id);

alter table users 
   add constraint FK7fq7mrgspmqpf2r2yaqlck5k4 
   foreign key (chat_id) 
   references chats (id);
