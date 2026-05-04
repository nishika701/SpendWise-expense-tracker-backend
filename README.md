# SpendWise Expense Tracker

SpendWise Expense Tracker is a Spring Boot REST API for tracking personal expenses. It supports user registration and login with JWT authentication, expense CRUD operations, filtering, sorting, searching, and basic expense totals.

## Features

- User registration and login
- JWT-based stateless authentication
- BCrypt password hashing
- Create, read, update, and delete expenses
- Filter expenses by category or date range
- Sort expenses by amount, date, id, title, or category
- Search expenses by title
- Monthly expense total
- Category-wise expense totals
- H2 in-memory database for local development
- Swagger UI dependency included through SpringDoc

## Tech Stack

- Java 17
- Spring Boot 4.0.5
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database
- JWT with `jjwt`
- Lombok
- Maven

## Project Structure

```text
ExpenseTracker/
  ExpenseTracker/
    pom.xml
    src/
      main/
        java/com/SpendWise/ExpenseTracker/
          controller/
          dto/
          exception/
          model/
          repository/
          security/
          service/
        resources/
          application.properties
      test/
```

## Requirements

- Java 17 or newer
- Maven, or use the included Maven wrapper

## Getting Started

From the repository root, move into the Spring Boot project:

```bash
cd ExpenseTracker
```

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

## Database

The app uses an H2 in-memory database by default.

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

H2 Console:

```text
http://localhost:8080/h2-console
```

Use this JDBC URL:

```text
jdbc:h2:mem:testdb
```

## Authentication

Register a user:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

The login response is a JWT token. Send it with protected requests:

```text
Authorization: Bearer <token>
```

## Expense Request Format

```json
{
  "title": "Groceries",
  "amount": 1500.0,
  "category": "Food",
  "date": "2026-04-10"
}
```

## API Endpoints

### Auth

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a new user | No |
| POST | `/api/v1/auth/login` | Login and receive a JWT | No |

### Expenses

| Method | Endpoint | Description | Auth Required |
| --- | --- | --- | --- |
| POST | `/api/v1/expenses` | Create an expense | Yes |
| GET | `/api/v1/expenses` | Get all expenses for the logged-in user | Yes |
| GET | `/api/v1/expenses/{id}` | Get an expense by id | Yes |
| PUT | `/api/v1/expenses/{id}` | Replace expense fields | Yes |
| PATCH | `/api/v1/expenses/{id}/{fieldName}` | Update one field | Yes |
| DELETE | `/api/v1/expenses/{id}` | Delete an expense | Yes |
| GET | `/api/v1/expenses/category` | Filter by category | Yes |
| GET | `/api/v1/expenses?startDate=2026-04-01&endDate=2026-04-30` | Filter by date range | Yes |
| GET | `/api/v1/expenses?sortBy=amount` | Sort expenses | Yes |
| GET | `/api/v1/expenses/total/monthly?month=4&year=2026` | Get monthly total | Yes |
| GET | `/api/v1/expenses/total/category` | Get totals grouped by category | Yes |
| GET | `/api/v1/expenses/search?title=grocery` | Search by title | Yes |

## Example Protected Request

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"Groceries","amount":1500.0,"category":"Food","date":"2026-04-10"}'
```

## Validation Rules

- `email` must be a valid email address
- `password` is required
- `title` is required
- `amount` must be greater than `0`
- `category` is required
- `date` is required and should use `yyyy-MM-dd`
