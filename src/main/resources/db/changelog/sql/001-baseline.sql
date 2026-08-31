-- Baseline schema for Lumios, generated from the JPA entity model (Hibernate 6.5, MariaDB dialect).
-- Applied only to an empty database. An existing deployment already has this schema, so the
-- changeset is marked as ran there instead (see the tableExists precondition in the changelog).


create table binds (
    id bigint not null auto_increment,
    chatId bigint,
    userId bigint,
    primary key (id)
) engine=InnoDB;

create table campus_bindings (
    id bigint not null auto_increment,
    accessToken varchar(2048) not null,
    externalId varchar(255),
    subscribedAt datetime(6) not null,
    telegramUserId bigint not null,
    primary key (id)
) engine=InnoDB;

create table chats (
    id bigint not null auto_increment,
    aiModel tinyint check (aiModel between 0 and 1),
    botNickname varchar(255),
    chatId bigint,
    communicationLimit integer default 10,
    description varchar(255),
    isAiEnabled boolean default false,
    isDiceEnabled boolean default false,
    isPlainTimetableEnabled boolean default false,
    isTimetableEnabled boolean default true,
    language varchar(5) default 'uk',
    lastWheelDate datetime(6),
    name varchar(255),
    summaryLimit integer default 2,
    primary key (id)
) engine=InnoDB;

create table chatShots (
    id bigint not null auto_increment,
    date date,
    chat_id bigint,
    primary key (id)
) engine=InnoDB;

create table chatShots_userShots (
    ChatShot_id bigint not null,
    userShots_id bigint not null
) engine=InnoDB;

create table classEntries (
    id bigint not null auto_increment,
    classType enum ('LAB','LECTURE','PRACTICE','UNKNOWN'),
    endTime time(6),
    name varchar(255),
    startTime time(6),
    url varchar(255),
    dayEntry_id bigint,
    primary key (id)
) engine=InnoDB;

create table dayEntries (
    id bigint not null auto_increment,
    dayName tinyint check (dayName between 0 and 6),
    timetableEntry_id bigint,
    days_id bigint,
    primary key (id)
) engine=InnoDB;

create table dayEntries_classEntries (
    dayEntries_id bigint not null,
    classEntries_id bigint not null
) engine=InnoDB;

create table dueTasks (
    id bigint not null auto_increment,
    attachment varchar(255),
    author bigint,
    description varchar(255),
    dueDate date,
    dueTime time(6),
    scope tinyint check (scope between 0 and 2),
    state tinyint check (state between 0 and 7),
    taskName varchar(255),
    url varchar(2048),
    chat_id bigint,
    primary key (id)
) engine=InnoDB;

create table messageRecords (
    id bigint not null auto_increment,
    chatId bigint,
    date datetime(6),
    messageId bigint,
    replyToMessageId bigint,
    text LONGTEXT,
    user integer,
    primary key (id)
) engine=InnoDB;

create table mixedQueues (
    id binary(16) not null,
    alias varchar(255),
    chatId bigint,
    messageId integer,
    shuffled bit,
    primary key (id)
) engine=InnoDB;

create table mixedUsers (
    id bigint not null auto_increment,
    accountId bigint,
    name varchar(255),
    username varchar(255),
    mixedQueue binary(16),
    primary key (id)
) engine=InnoDB;

create table simpleQueues (
    id binary(16) not null,
    alias varchar(255),
    chatId bigint,
    messageId integer,
    primary key (id)
) engine=InnoDB;

create table simpleUsers (
    id bigint not null auto_increment,
    accountId bigint,
    name varchar(255),
    username varchar(255),
    simpleQueue binary(16),
    primary key (id)
) engine=InnoDB;

create table timetableEntries (
    id bigint not null auto_increment,
    weekType enum ('UNKNOWN','WEEK_A','WEEK_B'),
    chat_id bigint,
    primary key (id)
) engine=InnoDB;

create table users (
    userEntityId integer not null auto_increment,
    balance integer default 0,
    credits integer default 100,
    fullName varchar(255),
    reverence integer default 0,
    sustainable integer default 100,
    userId bigint,
    username varchar(255),
    chat_id bigint,
    primary key (userEntityId)
) engine=InnoDB;

create table userShots (
    id bigint not null auto_increment,
    reverence integer,
    userId bigint,
    username varchar(255),
    primary key (id)
) engine=InnoDB;

alter table campus_bindings 
   add constraint UK8l3q1fqocgc3ruydle3n9yg36 unique (externalId);

alter table campus_bindings 
   add constraint UK8hhckw1ahf44tbq3vv8el3xni unique (telegramUserId);

alter table chatShots_userShots 
   add constraint UK8e118y6c0lxks3q7v8fdbam0p unique (userShots_id);

alter table dayEntries_classEntries 
   add constraint UKsbn7u1kl49re2q5cv0dfe0eku unique (classEntries_id);

alter table chatShots 
   add constraint FK118ko5iswy4rwr0xyjggjhpr3 
   foreign key (chat_id) 
   references chats (id);

alter table chatShots_userShots 
   add constraint FK7y0lfnv8745f39ahquyr9h5fg 
   foreign key (userShots_id) 
   references userShots (id);

alter table chatShots_userShots 
   add constraint FKafy54fqmqyussnab8ld97lx3x 
   foreign key (ChatShot_id) 
   references chatShots (id);

alter table classEntries 
   add constraint FKqoerr063pk2ovwf9xnob6n1vd 
   foreign key (dayEntry_id) 
   references dayEntries (id);

alter table dayEntries 
   add constraint FK4jkf4f6r3gnj99879n0pb2vn3 
   foreign key (timetableEntry_id) 
   references timetableEntries (id);

alter table dayEntries 
   add constraint FKqf7mkbsrcg5meyg8o3jm2p1mk 
   foreign key (days_id) 
   references timetableEntries (id);

alter table dayEntries_classEntries 
   add constraint FKrxcndd838gj8evjx9wro8j4yp 
   foreign key (classEntries_id) 
   references classEntries (id);

alter table dayEntries_classEntries 
   add constraint FK5500dpa9cma1cm8i5eyk1jiux 
   foreign key (dayEntries_id) 
   references dayEntries (id);

alter table dueTasks 
   add constraint FKqeutrhqnc135uwpydvhl86dgt 
   foreign key (chat_id) 
   references chats (id);

alter table messageRecords 
   add constraint FKrixr49c2fou620qidg2pvp1lm 
   foreign key (user) 
   references users (userEntityId);

alter table mixedUsers 
   add constraint FK8b26txsyl8iknn9lpdt2dvx6j 
   foreign key (mixedQueue) 
   references mixedQueues (id);

alter table simpleUsers 
   add constraint FKaaawufvopi5j0cg771vceb8lo 
   foreign key (simpleQueue) 
   references simpleQueues (id);

alter table timetableEntries 
   add constraint FK5rhhj7dxo05k2yn5aoc7up9xb 
   foreign key (chat_id) 
   references chats (id);

alter table users 
   add constraint FK7fq7mrgspmqpf2r2yaqlck5k4 
   foreign key (chat_id) 
   references chats (id);
