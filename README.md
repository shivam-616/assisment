# Global Class Offering Booking System

A robust, highly concurrent backend service for a global live-learning platform. This system allows teachers to schedule classes in their local timezones and enables parents to book those classes, with mathematical guarantees against double-booking and schedule overlaps.

## 🚀 Tech Stack
* **Framework:** Java 17, Spring Boot 3.4.0
* **Database:** PostgreSQL 16 (Relational/ACID) & Redis 7 (Distributed Caching/Locking)
* **Migrations:** Flyway
* **Security:** Stateless JWT Auth with Role-Based Access Control (RBAC)
* **Infrastructure:** Docker & Docker Compose

---

## 🧠 Key Architectural Decisions

### 1. The Timezone Engine (Absolute UTC)
Handling global timezones is notoriously error-prone due to Daylight Saving Time (DST) shifts. 
* **Database Layer:** All time columns (`start_time_utc`, `end_time_utc`) are stored as pure `TIMESTAMP` (without timezone) epochs. The database acts as a dumb storage layer for absolute time.
* **Application Layer:** Java's `Instant` is used internally. A dedicated `TimezoneService` translates local times (e.g., `America/New_York`) to UTC upon entry, and translates UTC back to local `ZonedDateTime` at the API boundary based on the requesting user's profile.

### 2. Dual-Layer Concurrency Control (Double-Booking Prevention)
To satisfy strict conflict resolution requirements under high load, the booking engine utilizes a two-tiered defense:
* **Layer 1: Redis Distributed Locks:** Prevents the "thundering herd" problem. If a parent clicks "Book" 10 times in 100ms, Redis `SETNX` short-lived locks ensure only the first request enters the database transaction pool.
* **Layer 2: Serializable Transactions & Interval Queries:** The core booking method operates under `@Transactional(isolation = Isolation.SERIALIZABLE)`. It executes a native-style overlapping interval query `(Start A < Target End AND End A > Target Start)` to mathematically guarantee no schedule conflicts exist before confirming the booking.

### 3. Schema Versioning
Hibernate `ddl-auto` is strictly disabled. All schema changes, indexes, and constraints are managed via version-controlled Flyway SQL scripts (`V1__init_schema.sql`). Strategic composite indexes exist on `sessions(start_time_utc, end_time_utc)` to ensure the overlap queries execute in sub-millisecond time.

---

## ⚙️ Local Development Setup

### Prerequisites
* Docker & Docker Compose
* Java 17+
* Maven (or use the provided `mvnw`)

### 1. Start the Infrastructure
Spin up the isolated PostgreSQL and Redis containers from the `src` directory:
```bash
cd assisment/src
docker-compose up -d
```

### 2. Build and Run the Application
Navigate back to the project root and use the Maven wrapper:
```bash
cd ..
./mvnw clean install
./mvnw spring-boot:run
```

### 3. Running Unit Tests
Verify the core logic, timezone conversions, and concurrency controls:
```bash
./mvnw test
```

---

## 🛠 API Endpoints (Quick Reference)

### Authentication
* `POST /api/v1/auth/register` - Create a new user (TEACHER/PARENT)
* `POST /api/v1/auth/login` - Obtain a JWT token

### Teacher Flow (Requires ROLE_TEACHER)
* `POST /api/v1/teachers/courses` - Create a new Course
* `POST /api/v1/teachers/offerings` - Create an Offering for a Course
* `POST /api/v1/teachers/offerings/{id}/sessions` - Add sessions (localized) to an offering
* `GET /api/v1/teachers/offerings` - View my offerings (localized to teacher's timezone)

### Parent Flow (Requires ROLE_PARENT)
* `GET /api/v1/parents/offerings` - View available published offerings (localized to parent's timezone)
* `POST /api/v1/parents/bookings` - Book an offering (concurrency-protected)
* `GET /api/v1/parents/bookings` - View my confirmed bookings (localized)

---

## 🧪 Robustness & Reliability
The system includes:
* **Global Exception Handler**: Standardized JSON error responses for 400, 409, and 500 status codes.
* **Unit Testing**: 100% coverage of critical path logic in `BookingService` and `TimezoneService`.
* **Database Integrity**: Serializable isolation levels ensure that even under extreme concurrency, the "double-booking" invariant is never violated.
