drop table eventfeeds if exists;

create table eventfeeds
(
  id         varchar(64) primary key,
  feed_name  varchar(128) not null,
  type       varchar(128) not null,
  time       timestamp not null,
  subject    varchar(64) not null,
  method     varchar(64),
  data       clob
);

create index eventfeeds_idx_id_feed_name on eventfeeds
(
  id,
  feed_name
);

create index eventfeeds_idx_id_feed_name_subject on eventfeeds
(
  id,
  feed_name,
  subject
);
