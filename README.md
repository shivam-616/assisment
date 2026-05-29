# Global Class Offering Booking System

A highly concurrent backend platform for a global live-learning ecosystem. The system enables teachers to create and manage classes in their local time zones while allowing parents to securely book available sessions. The architecture is designed to guarantee consistency, prevent double-booking, and handle timezone complexities at a global scale.

---

## 🚀 Technology Stack

| Category                    | Technology                       |
| --------------------------- | -------------------------------- |
| Language                    | Java 17                          |
| Framework                   | Spring Boot 3.4                  |
| Database                    | PostgreSQL 16                    |
| Cache & Distributed Locking | Redis 7                          |
| Database Migration          | Flyway                           |
| Authentication              | JWT (Stateless)                  |
| Authorization               | Role-Based Access Control (RBAC) |
| Containerization            | Docker & Docker Compose          |

---

## 🏗️ System Architecture & Design Decisions

### 1. Timezone Management Engine

Handling global time zones accurately is one of the most challenging aspects of scheduling systems, especially when Daylight Saving Time (DST) changes occur.

#### Database Layer

All session timestamps are stored in UTC using:

* `start_time_utc`
* `end_time_utc`

The database acts purely as a storage layer for absolute timestamps.

#### Application Layer

Java's `Instant` serves as the internal representation of time.

A dedicated `TimezoneService` is responsible for:

* Converting teacher-provided local times to UTC before persistence.
* Converting stored UTC timestamps back into localized `ZonedDateTime` values when responding to API requests.
* Automatically handling DST transitions without manual intervention.

This approach guarantees consistent scheduling across all geographical regions.

---

### 2. Dual-Layer Concurrency Protection

To prevent race conditions and booking conflicts under heavy traffic, the booking workflow uses a two-tier protection mechanism.

#### Layer 1: Redis Distributed Locks

Redis short-lived locks (`SETNX`) ensure that duplicate booking requests from the same user are filtered before reaching the database.

Benefits:

* Prevents request storms.
* Reduces database contention.
* Handles rapid repeated clicks gracefully.

#### Layer 2: Serializable Database Transactions

The booking process executes within:

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
```

Before confirming a booking, the system performs an interval-overlap validation:

```sql
(Start_A < End_B)
AND
(End_A > Start_B)
```

This mathematical condition guarantees that conflicting schedules cannot exist simultaneously.

As a result, the platform maintains strict consistency even under extreme concurrent booking loads.

---

### 3. Database Versioning & Schema Management

Database schema changes are fully version-controlled using Flyway.

#### Key Principles

* Hibernate `ddl-auto` is disabled.
* All schema modifications are maintained through migration scripts.
* Database structure remains reproducible across environments.

Example migration:

```text
V1__init_schema.sql
```

#### Performance Optimization

Strategic composite indexes are created on:

```sql
(start_time_utc, end_time_utc)
```

These indexes enable overlap checks to execute with minimal latency, even as data volume grows.

---

## ⚙️ Local Development Setup

### Prerequisites

* Java 17+
* Docker
* Docker Compose
* Maven (or Maven Wrapper)

---

### Step 1: Start Infrastructure Services

From the infrastructure directory:

```bash
cd assisment/src
docker-compose up -d
```

This launches:

* PostgreSQL
* Redis

---

### Step 2: Build and Run the Application

Navigate to the project root:

```bash
cd ..
./mvnw clean install
./mvnw spring-boot:run
```

---

### Step 3: Execute Unit Tests

```bash
./mvnw test
```

The test suite validates:

* Booking logic
* Timezone conversions
* Concurrency safeguards
* Business rules

---

## 🔐 Authentication APIs

### Register User

```http
POST /api/v1/auth/register
```

Creates a new user with either:

* TEACHER role
* PARENT role

---

### Login

```http
POST /api/v1/auth/login
```

Returns a JWT token used to access protected endpoints.

---

## 👨‍🏫 Teacher APIs

Requires `ROLE_TEACHER`.

| Method | Endpoint                                 | Description            |
| ------ | ---------------------------------------- | ---------------------- |
| POST   | /api/v1/teachers/courses                 | Create a course        |
| POST   | /api/v1/teachers/offerings               | Create an offering     |
| POST   | /api/v1/teachers/offerings/{id}/sessions | Add localized sessions |
| GET    | /api/v1/teachers/offerings               | View teacher offerings |

---

## 👨‍👩‍👧 Parent APIs

Requires `ROLE_PARENT`.

| Method | Endpoint                  | Description              |
| ------ | ------------------------- | ------------------------ |
| GET    | /api/v1/parents/offerings | View published offerings |
| POST   | /api/v1/parents/bookings  | Book an offering         |
| GET    | /api/v1/parents/bookings  | View confirmed bookings  |

---

## 📚 Swagger Documentation

Access Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

### Generate JWT Token

1. Open `POST /api/v1/auth/register`.
2. Click **Try it Out**.
3. Submit a request such as:

```json
{
  "email": "teacher@test.com",
  "role": "TEACHER",
  "timezone": "America/New_York"
}
```

4. Copy the JWT token from the response.

### Authorize Swagger

1. Click the **Authorize** button.
2. Paste the token value.
3. Click **Authorize**.

Swagger will automatically include the token in future requests.

### Test Protected APIs

You can now directly invoke:

```text
/api/v1/teachers/*
```

and

```text
/api/v1/parents/*
```

without manually adding authorization headers.

---

## 🧪 Reliability & Quality Assurance

### Global Exception Handling

Standardized API responses are provided for:

* 400 Bad Request
* 401 Unauthorized
* 403 Forbidden
* 409 Conflict
* 500 Internal Server Error

---

### Unit Testing

Comprehensive test coverage is implemented for:

* BookingService
* TimezoneService
* Validation logic
* Conflict detection mechanisms

---

### Strong Consistency Guarantees

The platform ensures:

✅ No double bookings

✅ No overlapping schedules

✅ Timezone-safe scheduling

✅ ACID-compliant transactions

✅ Concurrency-safe booking operations

---

## 🎯 Key Features

* Global timezone support with DST handling
* Secure JWT authentication
* Role-based access control (Teacher/Parent)
* Distributed locking with Redis
* Serializable transaction isolation
* Flyway-managed database migrations
* Dockerized development environment
* Swagger/OpenAPI integration
* High-concurrency booking protection
* Comprehensive unit testing

This architecture provides a scalable, production-ready foundation for a global live-learning platform while guaranteeing scheduling correctness and booking consistency under heavy load.
