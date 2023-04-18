drop table eventfeeds_processing if exists;
drop table eventfeeds_processing_dlq if exists;

create table eventfeeds_processing
(
  feed_consumer_name  varchar(128) primary key,
  last_processed_id   varchar(64) null
);

create table eventfeeds_dlq
(
  trace_id              varchar(64) primary key,
  feed_consumer_name    varchar(128) not null,
  shelved_time          timestamp not null,
  last_known_error      clob not null,
  -- process_attempts      int not null,
  attempt_reprocessing  bit not null
);

create table eventfeeds_dlq_event
(
  event_id  varchar(64) primary key,
  trace_id  varchar(64) not null,
  data      clob
);

create index eventfeeds_dlq_event_idx_trace_id on eventfeeds_dlq_event
(
  trace_id
);
