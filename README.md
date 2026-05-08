# hse-26-summer

Distributed Systems lecture collected material, summaries and questions.
Also this repository will hold example code.

## Distributed Systems - 19.03.26

General introduction into

- what a distributed system is
- advantages and disadvantages
- how it relates to cloud computing
- Service Models: IaaS, PaaS, SaaS

![MindMap](https://github.com/user-attachments/assets/40db9f01-5fc7-4701-9ec7-7ba523dc384a)

![Provisioning](https://github.com/user-attachments/assets/e2139d80-21c5-48b2-ac28-0daf6fbebe25)

## Cloud Native Development - 27.03.26

Introduction into the developers perspective of the cloud world

- Pillars of Cloud Native Development
- Microservices
- Staging
- Scaling
- CAP theorem
- Conways law
- 12 Factor Apps

![Staging](https://github.com/user-attachments/assets/6466ae7d-31cb-4993-a5ab-b2b771044906)

## Cloud Native Development in Practice - 10.04.26

Introduction into the practical side of cloud native development:

- Frameworks
  - General Idea
  - Benefits
  - Spring Boot
  - Spring ecosystem
- Interservice Communication
  - Synchronous vs Asynchronous Communication
  - REST
  - Resources, Verbs and Representations
  - Richardson Maturity Model

### Questions for Exam Preparation

- With the basic Rest Controller having a local `ArrayList` as storage of `TodoItem`s: what are potential issues in the long run, where does this conflict with concepts we learned about?

![service communication](images/service-communication.png)

## Persistence - 08.05.26

Introduction into moving the TODO application from local memory towards persistence:

- Problem with local `ArrayList` storage
  - data is lost after restart
  - state is tied to one application instance
  - multiple replicas would not share the same data
  - controller mixes HTTP handling and storage logic
- Persistence
  - storing data beyond the lifetime of one process
  - keeping application instances mostly stateless
  - moving durable state into a database
- Spring Boot application structure
  - `TodoController` handles HTTP requests and REST status codes
  - `TodoService` contains application logic
  - `TodoRepository` handles database access
  - `TodoItem` represents persistent data as a JPA entity
- JDBC
  - low-level Java API for database connections, SQL statements and result sets
  - powerful but often verbose when used directly
- JPA and Hibernate
  - JPA maps Java objects to relational database tables
  - Hibernate is the JPA implementation used by Spring Boot
  - `@Entity`, `@Id` and `@GeneratedValue` describe how `TodoItem` is stored
- Spring Data JPA
  - repository abstraction on top of JPA
  - provides methods like `findAll`, `findById`, `save`, `deleteById` and `existsById`
  - reduces boilerplate persistence code
- H2 database
  - lightweight database for local development and tests
  - file-based H2 can keep local application data
  - in-memory H2 is useful for isolated automated tests
- Production perspective
  - external databases such as PostgreSQL or MySQL are more realistic for deployed systems
  - datasource configuration can be changed without rewriting the controller or service layer

Basic repository example:

```java
public interface TodoRepository extends JpaRepository<TodoItem, Integer> {
}
```

### Questions for Exam Preparation

- Why is local in-memory state problematic when an application is restarted, redeployed, or scaled to multiple instances?
- What is the responsibility of a controller, service, repository, and entity in a typical Spring Boot application?
- What problem does persistence solve compared to storing data in a local Java collection?
- What does `JpaRepository<TodoItem, Integer>` provide automatically?
- Why should a database usually be external to application instances in a cloud native or distributed system?
- What is the difference between using H2 for local development or tests and using a production database such as PostgreSQL?
- Why is it useful to keep tests on an in-memory database instead of the application's file-based local database?
