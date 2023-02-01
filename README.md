# http-feeds

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.aourell.httpfeeds/http-feeds-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.aourell.httpfeeds/http-feeds-spring-boot-starter)

Spring Boot starter for implementing [HTTP Feed](http://www.http-feeds.org/) servers and/or clients.

Compiled against, and requires at least, Java 17 LTS.


## Getting started

Go to [start.spring.io](https://start.spring.io/#!type=maven-project&language=java&packaging=jar&groupId=com.example&artifactId=httpfeeds-example&name=httpfeeds-example&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.httpfeeds-example&dependencies=web,jdbc,h2) and create a new application. Select these dependencies:

- Spring Web (to provide an HTTP endpoint)
- JDBC API (for database connectivity)

for testing, you might also want to add

- H2 Database (or equivalent)

Then add this library to your `pom.xml`:

```xml
<dependency>
  <groupId>se.aourell.httpfeeds</groupId>
  <artifactId>http-feeds-spring-boot-starter</artifactId>
  <version>0.6.2</version>
</dependency>
```

The [`HttpFeedsAutoConfiguration`](http-feeds-spring-boot-starter/src/main/java/se/aourell/httpfeeds/infrastructure/spring/autoconfigure) adds all relevant Spring beans.

Check the [`example-application`](example-application) folder for a full example of using the "-spring-boot-starter" version, mentioned above, of this library. This is a sample application that uses the `spring-boot-starter-parent` as a parent pom, as is a common strategy.

### "But I don't use Spring"

Not a problem. You can simply use the core library, which does not depend on Spring. However, you must then [wire up the necessary infrastructure](http-feeds-spring-boot-starter/src/main/java/se/aourell/httpfeeds/infrastructure/spring/autoconfigure) manually.


## Feed definition

Define your HTTP Feed using standard Java class hierarchy, for example like the following [ADT](https://en.wikipedia.org/wiki/Algebraic_data_type):

```java
@EventFeed("patient")
public sealed interface PatientEvent
  permits AssessmentEnded, AssessmentStarted, PatientAdded, PatientDeleted {

  String id();
}

public record PatientAdded(String id, String firstName, String lastName) implements PatientEvent { }
public record AssessmentStarted(String id, String deviceId, OffsetDateTime startDate, OffsetDateTime endDate) implements PatientEvent { }
public record AssessmentEnded(String id) implements PatientEvent { }
@DeletionEvent
public record PatientDeleted(String id) implements PatientEvent { }
```


## DB schema definition

Next, make sure to have a valid schema configured for your database (use [Flyway](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-use-a-higher-level-database-migration-tool) or a [schema.sql](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-initialize-a-database-using-spring-jdbc) file):

```sql
drop table eventfeeds if exists;

create table eventfeeds
(
  id       varchar(256) primary key,
  type     varchar(256) not null,
  source   varchar(256) not null,
  time     timestamp not null,
  subject  varchar(256) not null,
  method   varchar(256),
  data     clob
);

create index eventfeeds_idx_id_source on eventfeeds
(
  id,
  source
);
```

Also make sure the server portion of httpfeeds is enabled, and e.g. that your database is running, in your `application.properties`:

```properties
eventfeeds.producer.enabled=true
eventfeeds.producer.publish.patient=true

spring.datasource.url=jdbc:h2:mem:testdb
```

`eventfeeds.producer.publish.patient` tells the framework to indeed publish the feed named `patient` over HTTP.
If you _only_ need in-process consuming support, you can skip this config for any feeds you define. See the section named "Consuming events: in-process" below for more info.


## Publishing events

Finally, make sure that your application adds new feed items by calling the `EventBus<T>::publish()` method.

```java
@Autowired // ...or preferably, use constructor injection
private final EventBus<PatientEvent> eventBus;

...

var subject = UUID.randomUUID().toString();
var event = new PatientAdded(subject, "Scooby", "Doe");
eventBus.publish(subject, event);
```


## Consuming events: HTTP

When you start the application (`./mvnw spring-boot:run` etc), you can consume the HTTP Feed by GET:ing e.g. http://localhost:8080/feed/patient.

```json
Content-Type: application/cloudevents-batch+json
...

[
  {
    "specversion": "1.0",
    "id": "1eccf0ce-699e-6e99-b86d-532a9b59bc9c",
    "type": "PatientAdded",
    "source": "patient",
    "time": "2022-05-08T20:24:45.695+00:00",
    "subject": "1eccf0ce-6161-6897-b86d-df21df0bb0e2",
    "datacontenttype": "application/json",
    "data": {
      "id": "1eccf0ce-6161-6897-b86d-df21df0bb0e2",
      "firstName": "Scooby",
      "lastName": "Doe"
    }
  },
  {
    "specversion": "1.0",
    "id": "1eccf0ce-718e-629b-b86d-2b881a64c97e",
    "type": "PatientDeleted",
    "source": "patient",
    "time": "2022-05-08T20:24:46.514+00:00",
    "subject": "1eccf0ce-6161-6897-b86d-df21df0bb0e2",
    "method": "delete"
  }
]
```


## Consuming events: Java

There is also a Java API for consuming events, mainly designed to be used from _other_ applications.

Simply define a consumer like so:

```java
@Service
@EventFeedConsumer("patient")
public class PatientFeedConsumer {

  @EventHandler
  public void on(PatientAdded event) {
    ...
  }

  @EventHandler
  public void on(AssessmentEnded event, EventMetaData meta) {
    ...
  }
}
```

`PatientFeedConsumer` is defined here as a regular Spring bean, so all the normal injection mechanics (@Autowired, @Inject, constructor injection, etc) work just as expected.
Any and all "magic" setup via the http-feeds-spring-boot-starter dependency is done via `BeanFactoryPostProcessor`, and should not interfere with other dependencies.

Event types accepted here for the handlers must have class names that match the cloud events' `type` properties, from the (producer) HTTP Feed, and be serializable from their data portion (setter with matching name or constructor with matching parameter list).
So e.g. any of the following:
```java
record PatientAdded(String id) { }
record PatientAdded(String id, String firstName) { }
record PatientAdded(String id, String firstName, String lastName) { }
class PatientAdded {
  ...
  void setId(String id) { ... }
}
...
etc
```

Finally, make sure the correct settings are applied in `application.properties`:

```properties
eventfeeds.consumer.enabled=true
eventfeeds.consumer.sources.patient=http://localhost:8080
```

`eventfeeds.consumer.sources` is a map where the consumer names are mapped to base urls. In this case, we say that events for the consumer named `patient` can be found at url `http://localhost:8080/feed/patient`.


## Consuming events: in-process

There is optimized support for when we want to process events from the same JVM process as where we publish them.
This can be helpful in monolithic architectures, for example.

The Spring implementation ensures events are persisted before they are processed, and additionally no communication is sent over HTTP.

You don't need any additional configuration to benefit from this optimization - the same API as described above is used.
The framework will detect that we have any `EventFeed` defined in the same class path loader (with matching feed name), and then use this implementation automatically for that consumer.


## Acknowledgements

Thank you to http://www.http-feeds.org and [their example project](https://github.com/http-feeds/http-feeds-server-spring-boot-starter) for excellent ideas and a sample reference implementation.
This project is heavily inspired by that repository. <3
