# HDFC Life Policy Ledger

Spring Boot 3 + Java 17 REST API using Spring Data JPA and Flyway. H2 is used in the `dev` profile and PostgreSQL in the `prod` profile.

## 1. How to run

Requirements:
- Java 17
- Maven 3.9+

Run with the default `dev` profile:

```bash
mvn spring-boot:run
```

Or package and run:

```bash
mvn clean package
java -jar target/hdfc-life-policy-ledger-1.0.0.jar
```

The default database is an in-memory H2 database:

```text
jdbc:h2:mem:hdfclife;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
```

H2 console:

```text
http://localhost:8080/h2-console
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

For `prod`, provide:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/hdfclife'
export DB_USER='postgres'
export DB_PASSWORD='change-me'
java -jar target/hdfc-life-policy-ledger-1.0.0.jar --spring.profiles.active=prod
```

Do not commit real production credentials.

## 2. Endpoint table

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/policies` | All policies, policy number ascending |
| GET | `/api/policies/{policyNo}` | Policy detail |
| GET | `/api/policies?status=Active` | Filter by status |
| GET | `/api/policies?type=TERM` | Filter by product type |
| GET | `/api/policies?customer=Anita%20Sharma` | Filter by customer |
| GET | `/api/policies/search?minPremium=20000` | Premium search using JPQL |
| POST | `/api/policies` | Create policy |
| DELETE | `/api/policies/{policyNo}` | Delete policy |
| GET | `/api/policies/{policyNo}/claims` | Claims for policy |
| POST | `/api/claims` | Create claim |
| GET | `/swagger-ui/index.html` | Swagger UI |

## 3. Which side owns `policy_riders`?

`Policy` is the owning side of the many-to-many relationship. Its `@ManyToMany` declares `@JoinTable(name = "policy_riders")`. `Rider` uses `mappedBy = "riders"` and therefore does not own the join table.

## 4. Why DTOs when `open-in-view` is false?

Controllers return DTOs so the HTTP contract is independent of the JPA entity model. With `spring.jpa.open-in-view=false`, the persistence context is not kept open while the web layer serializes the response. If a controller returned a `Policy` entity and Jackson reached a lazy `riders` collection after the service transaction ended, serialization could trigger a `LazyInitializationException`. DTO mapping inside a service transaction reads the required data safely and avoids exposing entity graphs, bidirectional relationships, or persistence details.

## Architecture

- `domain/` — JPA entities and enums
- `repo/` — Spring Data JPA repositories
- `dto/` — request/response API models
- `validation/` — custom `@PolicyType` constraint
- `service/` — transaction boundaries and business logic
- `web/` — REST controllers
- `exception/` — application exceptions
- `config/` — seeding and uniform REST exception handling
- `db/migration/` — Flyway schema and reference rider data

The database is the source of truth. Policies are persisted through `JpaRepository`; there is no in-memory policy list used as the data store.
