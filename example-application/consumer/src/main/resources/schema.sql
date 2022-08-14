drop table httpfeeds_consummation if exists;

create table httpfeeds_consummation
(
  feedName         varchar(1024) primary key,
  lastProcessedId  varchar(1024)
);
