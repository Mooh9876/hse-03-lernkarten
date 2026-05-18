# Flashcard Starter App

Simple Spring Boot REST API for learning cards (flashcards).

## Features

- Create, read, update and delete flashcards
- Persistence with Spring Data JPA
- Test setup with in-memory H2
- Container build with Docker

## API Endpoints

- `GET /flashcards`
- `GET /flashcards/{id}`
- `POST /flashcards`
- `PUT /flashcards/{id}`
- `DELETE /flashcards/{id}`

Example request body for create/update:

```json
{
  "question": "What is CAP theorem?",
  "answer": "Consistency, Availability, Partition tolerance",
  "category": "Distributed Systems",
  "learned": false
}
```

## Local Run

```bash
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```

## Docker

Build image:

```bash
docker build -t flashcard-app .
```

Run container (with DB env vars as needed):

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/postgres \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=mysecretpassword \
  flashcard-app
```
