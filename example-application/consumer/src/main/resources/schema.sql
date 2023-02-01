drop table eventfeeds_consummation if exists;

create table eventfeeds_consummation
(
  feed_consumer_name  varchar(128) primary key,
  last_processed_id   varchar(64)
);
