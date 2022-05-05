drop table httpfeed_patient if exists;

create table httpfeed_patient
(
  id       varchar(1024) primary key,
  type     varchar(1024),
  time     timestamp,
  subject  varchar(1024),
  method   varchar(1024),
  data     clob
);
