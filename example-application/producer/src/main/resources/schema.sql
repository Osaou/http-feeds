drop table httpfeeds if exists;

create table httpfeeds
(
  id       varchar(64) primary key,
  type     varchar(128) not null,
  source   varchar(128) not null,
  time     timestamp not null,
  subject  varchar(64) not null,
  method   varchar(64),
  data     clob
);

create index httpfeeds_idx_id_source on httpfeeds
(
  id,
  source
);

create index httpfeeds_idx_id_source_subject on httpfeeds
(
  id,
  source,
  subject
);
