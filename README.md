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
- PostgreSQL database configuration with environment variables
- Expense responses use DTOs and do not expose user password data
- Expense access is scoped to the logged-in user
- Swagger UI dependency included through SpringDoc

## Tech Stack

- Java 17
- Spring Boot 4.0.5
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
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
- PostgreSQL running locally

## Getting Started

From the repository root, move into the Spring Boot project:

```bash
cd ExpenseTracker
```

Set the database environment variables.

On Windows PowerShell:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
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

The app uses PostgreSQL. Create a database named `expense_tracker`:

```sql
CREATE DATABASE expense_tracker;
```

The application reads the database username and password from environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

To inspect the database in pgAdmin, connect to `expense_tracker` and run:

```sql
SELECT * FROM users;
SELECT * FROM expense;
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
| GET | `/api/v1/expenses/category?categoryName=Food` | Filter by category | Yes |
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

## Expense Response Format

Expense APIs return `ExpenseResponseDTO` values, so user password data is not included in responses:

```json
{
  "id": 1,
  "title": "Groceries",
  "amount": 1500.0,
  "category": "Food",
  "date": "2026-04-10"
}
```

ID-based expense operations check both the expense id and the logged-in user, so users cannot read, update, or delete another user's expenses by guessing an id.

## Validation Rules

- `email` must be a valid email address
- `password` is required
- `title` is required
- `amount` is required and must be greater than `0`
- `category` is required
- `date` is required and should use `yyyy-MM-dd`
