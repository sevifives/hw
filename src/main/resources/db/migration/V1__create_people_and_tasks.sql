create table PERSON (
    ID bigint not null auto_increment,
    FIRST_NAME varchar(128) not null,
    LAST_NAME varchar(128) not null
);

create table TASKS(
	ID bigint not null auto_increment,
	TITLE varchar(128) not null,
	DESCRIPTION varchar(512) not null,
	PERSON_ID bigint not null
);