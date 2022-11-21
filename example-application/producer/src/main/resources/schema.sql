drop table eventfeeds if exists;

create table eventfeeds
(
  id       varchar(64) primary key,
  feedName varchar(128) not null,
  type     varchar(128) not null,
  time     timestamp not null,
  subject  varchar(64) not null,
  method   varchar(64),
  data     clob
);

create index eventfeeds_idx_id_feedName on eventfeeds
(
  id,
  feedName
);

create index eventfeeds_idx_id_feedName_subject on eventfeeds
(
  id,
  feedName,
  subject
);
