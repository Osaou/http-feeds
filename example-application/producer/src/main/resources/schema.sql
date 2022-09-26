drop table httpfeeds if exists;

create table httpfeeds
(
  id       varchar(256) primary key,
  type     varchar(256) not null,
  source   varchar(256) not null,
  time     timestamp not null,
  subject  varchar(256) not null,
  method   varchar(256),
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
