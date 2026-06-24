# Lernkarten-App – Flashcard Application

**Labor „Verteilte Systeme" – Hochschule für Technik und Wirtschaft Berlin (HTW) – Sommersemester 2026**

---

## Projektbeschreibung

Webbasierte Lernkarten-Anwendung zur Prüfungsvorbereitung. Das Projekt demonstriert eine vollständige, in Containern betriebene Mehrschicht-Architektur (Frontend, Backend, Datenbank) und zeigt Konzepte aus dem Bereich Verteilte Systeme: Containerisierung, Orchestrierung mit Kubernetes, persistente Datenhaltung, Skalierung und Selbstheilung.

---

## Funktionsumfang

| Funktion | Beschreibung |
|---|---|
| Karte erstellen | Neue Lernkarte mit Frage, Antwort und optionaler Kategorie anlegen |
| Karte anzeigen | Alle Karten in einer Rasteransicht; Karte umdrehen per Klick |
| Karte bearbeiten | Frage, Antwort und Kategorie nachträglich ändern |
| Karte löschen | Einzelne Karte dauerhaft entfernen |
| Lernstatus | Karte als „Gelernt" oder „Wiederholen" markieren |
| Filter | Anzeige nach: Alle / Offen / Gelernt |
| Fortschrittsanzeige | Zeigt `gelernt / gesamt` in der Kopfzeile |

---

## Technologieübersicht

| Komponente | Technologie |
|---|---|
| Frontend | React 19, Vite 8, nginx (Reverse Proxy) |
| Backend | Spring Boot 4, Spring Data JPA, Spring Actuator |
| Datenbank | PostgreSQL 16 |
| Containerisierung | Docker, Docker Compose |
| Orchestrierung | Kubernetes (Deployments, Services, PVC, Secrets) |
| CI | GitHub Actions (Test → Build → Push zu GHCR) |

---

## Architekturübersicht

```
Browser
  │  HTTP
  ▼
┌─────────────────────┐
│  nginx (Frontend)   │  Port 80 / NodePort 30000
│  React SPA          │
│  Reverse Proxy      │
└────────┬────────────┘
         │ /flashcards → :8080
         ▼
┌─────────────────────┐
│  Spring Boot        │  Port 8080 × 2 Replikas
│  REST API           │  /flashcards (CRUD)
│  JPA / Hibernate    │  /actuator/health
└────────┬────────────┘
         │ JDBC :5432
         ▼
┌─────────────────────┐
│  PostgreSQL 16      │  Port 5432
│  Persistente Daten  │◄── PersistentVolumeClaim
└─────────────────────┘
```

Detaillierte Architekturdokumentation: [docs/architecture.md](docs/architecture.md)

---

## Voraussetzungen

- Docker Desktop ≥ 24 oder Docker Engine + Docker Compose v2
- Kubernetes-Cluster (z. B. Minikube, kind, Docker Desktop k8s, oder ein externer Cluster)
- `kubectl` konfiguriert und auf den Cluster zeigend
- Java 17+ (nur für lokale Entwicklung ohne Docker)
- Maven 3.9+ (nur für lokale Entwicklung ohne Docker)

---

## Lokaler Start mit Docker Compose

```bash
# Repository klonen
git clone https://github.com/Mooh9876/hse-03-lernkarten.git
cd hse-03-lernkarten

# Alle Dienste starten (baut Images lokal)
docker compose up -d

# Logs verfolgen (optional)
docker compose logs -f

# Stoppen und aufräumen
docker compose down
```

Nach dem Start erreichbar unter:

| Dienst | URL |
|---|---|
| Frontend (Web-App) | http://localhost:5173 |
| Backend REST-API | http://localhost:8080/flashcards |
| Actuator Health | http://localhost:8080/actuator/health |
| PostgreSQL | localhost:5432 (Nutzer: postgres, DB: postgres) |

---

## Tests ausführen

### Backend-Tests (Spring Boot / JUnit 5)

Die Tests verwenden eine H2-In-Memory-Datenbank und benötigen keine laufende PostgreSQL-Instanz.

```bash
cd starterapp

# Mit Maven (systemweit installiert)
mvn test

# Mit Maven Wrapper (falls Maven nicht installiert)
./mvnw test
```

Erwartetes Ergebnis: `Tests run: 5, Failures: 0, Errors: 0`

Getestete Szenarien:
- `contextLoads` – Spring-Kontext startet korrekt
- `flashcardsEndpointReturnsJson` – GET /flashcards liefert JSON
- `createFlashcardReturnsCreatedResource` – POST /flashcards erstellt Ressource
- `updateFlashcardUsesPathIdAndReturnsNotFoundForMissingFlashcard` – PUT mit falscher ID → 404
- `deleteFlashcardReturnsNoContentOrNotFound` – DELETE → 204, zweiter Versuch → 404

---

## API-Übersicht

Basis-URL: `http://localhost:8080` (lokal) oder über nginx auf Port 5173 / 30000

| Methode | Pfad | Beschreibung | Request-Body |
|---|---|---|---|
| `GET` | `/flashcards` | Alle Karten abrufen | – |
| `GET` | `/flashcards/{id}` | Eine Karte per ID | – |
| `POST` | `/flashcards` | Neue Karte erstellen | JSON (s. u.) |
| `PUT` | `/flashcards/{id}` | Karte aktualisieren | JSON (s. u.) |
| `DELETE` | `/flashcards/{id}` | Karte löschen | – |
| `GET` | `/actuator/health` | Health-Status | – |
| `GET` | `/actuator/health/readiness` | Readiness-Status | – |
| `GET` | `/actuator/health/liveness` | Liveness-Status | – |

**Flashcard JSON-Struktur:**
```json
{
  "question": "Was ist der CAP-Satz?",
  "answer": "Consistency, Availability, Partition Tolerance – maximal zwei der drei sind gleichzeitig erreichbar.",
  "category": "Verteilte Systeme",
  "learned": false
}
```

**Beispiel-Anfragen mit curl:**
```bash
# Alle Karten abrufen
curl http://localhost:8080/flashcards

# Karte erstellen
curl -X POST http://localhost:8080/flashcards \
  -H "Content-Type: application/json" \
  -d '{"question":"Was ist Docker?","answer":"Containerisierungsplattform","category":"DevOps","learned":false}'

# Karte als gelernt markieren (ID anpassen)
curl -X PUT http://localhost:8080/flashcards/1 \
  -H "Content-Type: application/json" \
  -d '{"question":"Was ist Docker?","answer":"Containerisierungsplattform","category":"DevOps","learned":true}'

# Karte löschen
curl -X DELETE http://localhost:8080/flashcards/1
```

---

## Kubernetes-Deployment

### Schritt 1 – Container-Images bauen und in Registry pushen

Die Kubernetes-Manifeste referenzieren Images in der **GitHub Container Registry (GHCR)**. Bevor du deployst, musst du die Images bauen und pushen.

```bash
# Bei GHCR anmelden (einmalig)
echo $GITHUB_TOKEN | docker login ghcr.io -u mooh9876 --password-stdin

# Backend-Image bauen und pushen
docker build -t ghcr.io/mooh9876/flashcard-backend:latest ./starterapp
docker push ghcr.io/mooh9876/flashcard-backend:latest

# Frontend-Image bauen und pushen
docker build -t ghcr.io/mooh9876/flashcard-frontend:latest ./frontend
docker push ghcr.io/mooh9876/flashcard-frontend:latest
```

> **Hinweis:** Die GHCR-Packages müssen auf „Public" gesetzt sein, damit Kubernetes sie ohne imagePullSecret ziehen kann.
> Einstellung unter: GitHub → Profile → Packages → Package-Einstellungen → Change visibility → Public

Alternativ kann nach dem ersten CI-Run (push auf `main`) der GitHub Actions Workflow die Images automatisch pushen.

### Schritt 2 – Kubernetes-Manifeste anwenden

```bash
# Namespace anlegen (zuerst!)
kubectl apply -f k8s/namespace.yaml

# Secret mit Datenbankzugangsdaten anlegen
kubectl apply -f k8s/secret.yaml

# PostgreSQL mit PersistentVolumeClaim deployen
kubectl apply -f k8s/postgres.yaml

# Backend deployen (2 Replikas)
kubectl apply -f k8s/backend.yaml

# Frontend deployen
kubectl apply -f k8s/frontend.yaml

# Optional: Ingress anlegen (erfordert nginx Ingress Controller)
# kubectl apply -f k8s/ingress.yaml
```

Oder alle auf einmal:
```bash
kubectl apply -f k8s/namespace.yaml -f k8s/secret.yaml -f k8s/postgres.yaml -f k8s/backend.yaml -f k8s/frontend.yaml
```

### Schritt 3 – Status prüfen

```bash
# Alle Ressourcen im Namespace anzeigen
kubectl get all -n flashcards

# Pods beobachten bis alle Ready sind (Strg+C zum Beenden)
kubectl get pods -n flashcards -w

# Backend-Logs anzeigen
kubectl logs -l app=backend -n flashcards

# Pod-Details anzeigen (inkl. Probe-Status)
kubectl describe pod -l app=backend -n flashcards
```

### Schritt 4 – Anwendung aufrufen

**Mit NodePort (Standard):**
```bash
# Minikube
minikube service frontend -n flashcards

# Oder direkt über Node-IP
kubectl get nodes -o wide   # Node-IP ermitteln
# Anwendung unter http://<Node-IP>:30000 erreichbar
```

**Mit kubectl port-forward (für lokalen Zugriff auf jeden Cluster):**
```bash
kubectl port-forward svc/frontend 8080:80 -n flashcards
# Anwendung unter http://localhost:8080 erreichbar
```

### Aufräumen

```bash
kubectl delete namespace flashcards
```

---

## Secrets und Konfiguration

Datenbankzugangsdaten werden als **Kubernetes Secret** gespeichert (`k8s/secret.yaml`).

Die Werte sind base64-kodiert (kein Klartext in YAML-Dateien):

```bash
# Eigene Werte kodieren
echo -n "meinpasswort" | base64
```

Das Secret enthält:
- `POSTGRES_DB` – Datenbankname
- `POSTGRES_USER` – Datenbankbenutzer
- `POSTGRES_PASSWORD` – Datenbankpasswort

Backend und PostgreSQL-Pod lesen diese Werte als Umgebungsvariablen. Spring Boot übersetzt `SPRING_DATASOURCE_PASSWORD` automatisch in die entsprechende Property.

> Für Produktivumgebungen: Secrets in einer Secret-Management-Lösung (z. B. HashiCorp Vault, Kubernetes External Secrets) verwalten, nicht in Git einchecken.

---

## Persistente PostgreSQL-Datenhaltung

Daten werden in einem **PersistentVolumeClaim** (`postgres-pvc`, 1 Gi) gespeichert:

```yaml
volumeMounts:
  - mountPath: /var/lib/postgresql/data
    name: postgres-storage
    subPath: pgdata
```

- Der PVC überlebt Pod-Neustarts und Node-Wechsel
- Auch nach `kubectl delete pod -l app=postgres -n flashcards` sind alle Flashcards nach dem Neustart noch vorhanden
- Der PVC wird erst gelöscht, wenn der gesamte Namespace gelöscht wird

---

## Backend-Skalierung

Das Backend läuft standardmäßig mit **2 Replikas**. Kubernetes verteilt Anfragen über den ClusterIP-Service automatisch:

```bash
# Auf 3 Replikas skalieren
kubectl scale deployment backend --replicas=3 -n flashcards

# Aktuelle Skalierung anzeigen
kubectl get deployment backend -n flashcards
```

Beide Pods greifen auf dieselbe PostgreSQL-Instanz zu – die Anwendungsschicht ist zustandslos und beliebig horizontal skalierbar.

---

## Kubernetes Self-Healing demonstrieren

Kubernetes stellt automatisch sicher, dass die gewünschte Anzahl Pods (`replicas: 2`) immer läuft.

```bash
# Aktuellen Zustand zeigen
kubectl get pods -n flashcards -l app=backend

# Einen Backend-Pod löschen
kubectl delete pod $(kubectl get pod -n flashcards -l app=backend -o name | head -1) -n flashcards

# Sofort beobachten: Kubernetes startet neuen Pod
kubectl get pods -n flashcards -w
```

**Erwartetes Verhalten:**
1. Pod-Status wechselt auf `Terminating`
2. Neuer Pod erscheint sofort mit Status `Pending` → `ContainerCreating` → `Running`
3. Startup Probe → Readiness Probe erfolgreich → Pod nimmt Traffic an
4. Während des Neustarts läuft der zweite Pod weiter → kein Totalausfall

---

## Continuous Integration (GitHub Actions)

Der Workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) läuft automatisch bei jedem Push auf `main` und bei Pull Requests:

| Job | Beschreibung |
|---|---|
| `test-backend` | Maven-Tests mit Java 17, H2 In-Memory |
| `build-frontend` | `npm ci` + `npm run build` |
| `build-and-push-images` | Docker-Images bauen und zu GHCR pushen (nur bei Push auf `main`) |

Images werden automatisch nach `ghcr.io/mooh9876/flashcard-backend:latest` und `ghcr.io/mooh9876/flashcard-frontend:latest` gepusht – keine manuellen Credentials nötig (verwendet `GITHUB_TOKEN`).

---

## Bekannte Einschränkungen und Hinweise vor der Abnahme

- GHCR-Packages müssen manuell auf „Public" gesetzt werden (oder imagePullSecret konfigurieren)
- `spring.jpa.show-sql=true` ist aktiviert (für Debugging); in Produktivumgebungen deaktivieren
- PostgreSQL läuft mit einer Replica – für echte HA wäre ein PostgreSQL-Operator nötig
- Das Secret `k8s/secret.yaml` liegt im Repository – für Produktion in ein Secret-Management-System auslagern

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


## Containerisation

Introduction into packaging applications as containers:

- Problem with running applications directly on a host
  - different machines may have different Java versions, tools or system libraries
  - manual setup is hard to reproduce
  - deployments become dependent on the local environment
  - scaling and replacing instances becomes harder
- Container image
  - packaged application plus runtime dependencies
  - built once and started many times
  - usually created from a `Dockerfile` or build tool plugin
  - consists of layers that can be cached and reused
- Container
  - running instance of an image
  - isolated process with its own filesystem, network view and environment
  - can be started, stopped, removed and recreated
- Dockerfile
  - describes how to build an image
  - chooses a base image
  - copies the application artifact
  - defines the command used to start the application
- Ports and networking
  - Spring Boot usually listens on port `8080` inside the container
  - host-to-container port mapping makes the application reachable from outside
  - containers can communicate through container networks
- Configuration
  - environment variables are commonly used for runtime settings
  - datasource URLs, active profiles and secrets should not be hardcoded into the image
  - same image can be used in different environments with different configuration
- Container registry
  - storage location for container images
  - examples are Docker Hub, GitHub Container Registry or a private registry
  - deployment platforms pull images from registries
- Cloud native perspective
  - containers support reproducible deployment
  - application instances should be stateless where possible
  - persistent state belongs in external databases or managed storage
  - containers are the basis for orchestration platforms like Kubernetes

Basic example workflow:

```bash
docker build -t starterapp .
docker run --rm -p 8080:8080 starterapp
```

### Questions for Exam Preparation

- What problem does containerisation solve compared to installing an application directly on a server?
- What is the difference between a container image and a running container?
- What is the purpose of a `Dockerfile`?
- Why should a container image not contain environment-specific secrets or database URLs?
- What does port mapping do when running a web application in a container?
- Why are containers often described as disposable?
- What happens to data stored only inside a container when the container is removed?
- Why should persistent state usually be stored outside the application container?
- What is a container registry, and why is it useful in deployment pipelines?
- How does containerisation support cloud native principles such as scalability and reproducibility?

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
