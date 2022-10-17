drop table httpfeeds_consummation if exists;

create table httpfeeds_consummation
(
  feedName         varchar(128) primary key,
  lastProcessedId  varchar(64)
);
