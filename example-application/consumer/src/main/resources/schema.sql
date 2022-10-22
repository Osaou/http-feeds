drop table eventfeeds_consummation if exists;

create table eventfeeds_consummation
(
  feedName         varchar(128) primary key,
  lastProcessedId  varchar(64)
);
