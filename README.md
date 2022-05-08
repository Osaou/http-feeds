# http-feeds

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.aourell.httpfeeds/http-feeds-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.aourell.httpfeeds/http-feeds-spring-boot-starter)

Spring Boot starter for an [HTTP Feed](http://www.http-feeds.org/).


## Getting started

Go to [start.spring.io](https://start.spring.io/#!type=maven-project&language=java&packaging=jar&groupId=com.example&artifactId=httpfeeds-example&name=httpfeeds-example&description=Demo%20project%20for%20Spring%20Boot&packageName=com.example.httpfeeds-example&dependencies=web,jdbc,h2) and create a new application. Select these dependencies:

- Spring Web (to provide an HTTP endpoint)
- JDBC API (for database connectivity)

for testing, you might also want to add

- H2 Database

Then add this library to your `pom.xml`:

```xml
    <dependency>
      <groupId>se.aourell.httpfeeds</groupId>
      <artifactId>http-feeds-spring-boot-starter</artifactId>
      <version>0.0.1</version>
    </dependency>
```

The [`HttpFeedsAutoConfiguration`](http-feeds-spring-boot-starter/src/main/java/se/aourell/httpfeeds/infrastructure/spring/autoconfigure/HttpFeedsAutoConfiguration.java) adds all relevant Spring beans.

Check the [`example-application`](example-application) folder for a full example of using the "-spring-boot-starter" version, mentioned above, of this library. This is a sample application that uses the `spring-boot-starter-parent` as a parent pom, as is a common strategy.

### "But I don't want to use Spring"

Of course, you can also use the "-core" dependency version, which does not depend on Spring. However, you must then [wire up](http-feeds-spring-boot-starter/src/main/java/se/aourell/httpfeeds/infrastructure/spring/autoconfigure/HttpFeedsAutoConfiguration.java) the [necessary infrastructure](http-feeds-spring-boot-starter/src/main/java/se/aourell/httpfeeds/infrastructure/spring/HttpFeedsBeanFactoryPostProcessor.java) manually.


## Feed definition

Define your HTTP Feed using standard Java class hierarchy, for example like the following [ADT](https://en.wikipedia.org/wiki/Algebraic_data_type):

```java
@HttpFeed(path = "/feed/patient", feedName = "patient", persistenceName = "httpfeed_patient")
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

Next, make sure to have a valid schema for your database set up (use [Flyway](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-use-a-higher-level-database-migration-tool) or a [schema.sql](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-initialize-a-database-using-spring-jdbc) file):

```sql
create table httpfeed_patient
(
  id       varchar(1024) primary key,
  type     varchar(1024),
  source   varchar(1024),
  time     timestamp,
  subject  varchar(1024),
  method   varchar(1024),
  data     clob
);
```

And make sure your database is connected in your `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
```


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


## Consuming events

When you start the application (`./mvnw spring-boot:run` etc), you can consume the HTTP Feed by GET:ing e.g. http://localhost:8080/feed/patient.


## Acknowledgements

Thank you to http://www.http-feeds.org and [their example project](https://github.com/http-feeds/http-feeds-server-spring-boot-starter) for excellent ideas and a sample reference implementation.
This project is heavily inspired by that repository. <3
