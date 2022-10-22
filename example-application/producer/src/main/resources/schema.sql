drop table eventfeeds if exists;

create table eventfeeds
(
  id       varchar(64) primary key,
  type     varchar(128) not null,
  source   varchar(128) not null,
  time     timestamp not null,
  subject  varchar(64) not null,
  method   varchar(64),
  data     clob
);

create index eventfeeds_idx_id_source on eventfeeds
(
  id,
  source
);

create index eventfeeds_idx_id_source_subject on eventfeeds
(
  id,
  source,
  subject
);
