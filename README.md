# Employee Management System - GraphQL API

A complete GraphQL API built with Spring Boot for managing employees with role-based access control, pagination, filtering, and performance optimizations.

## 🚀 Features

- **GraphQL API** with queries and mutations
- **Authentication & Authorization** using JWT and Spring Security
- **Role-Based Access Control** (Admin & Employee roles)
- **Pagination & Sorting** for all list queries
- **Advanced Filtering** by name, age, class, and subjects
- **Attendance Tracking** system
- **Performance Optimizations**:
    - Caching with Caffeine
    - N+1 query prevention with EntityGraph
    - Database indexing
    - Connection pooling with HikariCP
    - Batch insert/update operations
- **Docker Compose** for easy deployment
- **PostgreSQL** database

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Postman (for API testing)

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.2
- **GraphQL**: Spring GraphQL
- **Database**: PostgreSQL 15
- **Security**: Spring Security + JWT
- **Cache**: Caffeine
- **ORM**: Spring Data JPA + Hibernate
- **Build Tool**: Maven
- **Containerization**: Docker

## 📁 Project Structure

```
employee-graphql-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/employee/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── model/               # Entity classes
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── resolver/            # GraphQL resolvers
│   │   │   ├── security/            # Security components
│   │   │   ├── service/             # Business logic
│   │   │   └── EmployeeApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Application configuration
│   │       └── graphql/
│   │           └── schema.graphqls  # GraphQL schema
│   └── test/                        # Test classes
├── docker-compose.yml               # Docker services
├── Dockerfile                       # Application container
├── pom.xml                          # Maven dependencies
└── README.md
```

## 🏃 Running the Application

### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
```bash
git clone <repository-url>
cd employee-graphql-api
```

2. **Build and run with Docker Compose**
```bash
docker-compose up --build
```

The application will be available at:
- GraphQL API: http://localhost:8080/graphql
- GraphiQL Interface: http://localhost:8080/graphiql
- PostgreSQL: localhost:5432

### Option 2: Running Locally

1. **Start PostgreSQL**
```bash
docker-compose up postgres
```

2. **Run the Spring Boot application**
```bash
mvn clean install
mvn spring-boot:run
```

## 🔑 Authentication

### Register a New User

**GraphQL Mutation:**
```graphql
mutation {
  register(username: "admin", password: "admin123", role: ADMIN) {
    token
    user {
      id
      username
      role
    }
  }
}
```

### Login

**GraphQL Mutation:**
```graphql
mutation {
  login(username: "admin", password: "admin123") {
    token
    user {
      id
      username
      role
    }
  }
}
```

**Use the returned token in subsequent requests:**
- Header: `Authorization: Bearer <your-token>`

## 📊 API Examples

### 1. Add Employee (Admin Only)

```graphql
mutation {
  addEmployee(input: {
    name: "John Doe"
    age: 30
    class: "Engineering"
    subjects: ["Java", "Spring Boot", "GraphQL"]
    email: "john@example.com"
    phone: "1234567890"
  }) {
    id
    name
    age
    class
    subjects
    email
    phone
    createdAt
  }
}
```

### 2. Get All Employees with Pagination

```graphql
query {
  employees(page: 0, size: 10, sortBy: "name", sortDir: "ASC") {
    content {
      id
      name
      age
      class
      subjects
      email
    }
    pageInfo {
      pageNumber
      pageSize
      totalElements
      totalPages
      hasNext
      hasPrevious
    }
  }
}
```

### 3. Get Employees with Filters

```graphql
query {
  employees(
    filter: {
      name: "John"
      minAge: 25
      maxAge: 40
      class: "Engineering"
    }
    page: 0
    size: 10
  ) {
    content {
      id
      name
      age
      class
    }
    pageInfo {
      totalElements
    }
  }
}
```

### 4. Get Single Employee

```graphql
query {
  employee(id: 1) {
    id
    name
    age
    class
    subjects
    attendance {
      date
      present
    }
  }
}
```

### 5. Update Employee

```graphql
mutation {
  updateEmployee(
    id: 1
    input: {
      name: "John Updated"
      age: 31
      class: "Senior Engineering"
      subjects: ["Java", "Spring Boot", "GraphQL", "Microservices"]
      email: "john.updated@example.com"
      phone: "9876543210"
    }
  ) {
    id
    name
    age
    updatedAt
  }
}
```

### 6. Mark Attendance

```graphql
mutation {
  markAttendance(employeeId: 1, date: "2024-01-15", present: true) {
    id
    name
    attendance {
      date
      present
    }
  }
}
```

### 7. Delete Employee (Admin Only)

```graphql
mutation {
  deleteEmployee(id: 1)
}
```

### 8. Get Current User Info

```graphql
query {
  me {
    id
    username
    role
    employeeId
  }
}
```

## 🔐 Role-Based Access Control

| Operation | Admin | Employee |
|-----------|-------|----------|
| View all employees | ✅ | ✅ |
| View employee details | ✅ | ✅ (own only) |
| Add employee | ✅ | ❌ |
| Update employee | ✅ | ✅ (own only) |
| Delete employee | ✅ | ❌ |
| Mark attendance | ✅ | ✅ (own only) |

## ⚡ Performance Optimizations

1. **Caching**: Caffeine cache for frequently accessed employees
2. **N+1 Prevention**: EntityGraph to fetch related data in single query
3. **Database Indexing**: Indexes on name and class columns
4. **Connection Pooling**: HikariCP with optimized settings
5. **Batch Operations**: Hibernate batch processing for inserts/updates
6. **Pagination**: Efficient data retrieval with Spring Data
7. **Lazy Loading**: Attendance data loaded on-demand

## 🧪 Testing with Postman

### Setup Postman

1. **Import the provided Postman collection** (see below)
2. **Set up environment variables**:
    - `baseUrl`: http://localhost:8080
    - `token`: (will be set automatically after login)

### Postman Collection Structure

1. **Auth**
    - Register Admin
    - Register Employee
    - Login

2. **Employee Operations**
    - Add Employee
    - Get All Employees
    - Get Employee by ID
    - Update Employee
    - Delete Employee

3. **Attendance**
    - Mark Attendance

4. **User**
    - Get Current User

### Sample Postman Request (REST-style for GraphQL)

**Endpoint**: POST http://localhost:8080/graphql

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <your-token>
```

**Body** (JSON):
```json
{
  "query": "mutation { login(username: \"admin\", password: \"admin123\") { token user { id username role } } }"
}
```

## 🐛 Troubleshooting

### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker-compose ps

# View logs
docker-compose logs postgres
```

### Application Won't Start
```bash
# Check application logs
docker-compose logs app

# Rebuild containers
docker-compose down
docker-compose up --build
```

### JWT Token Issues
- Ensure the token is valid and not expired
- Check that the Authorization header format is: `Bearer <token>`
- Token expiration is set to 24 hours by default

## 📝 Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_DATASOURCE_URL | jdbc:postgresql://localhost:5432/employee_db | Database URL |
| SPRING_DATASOURCE_USERNAME | postgres | Database username |
| SPRING_DATASOURCE_PASSWORD | postgres123 | Database password |
| JWT_SECRET | (auto-generated) | JWT signing secret |
| JWT_EXPIRATION | 86400000 | Token expiration (24h in ms) |

## 📚 Additional Resources

- [Spring GraphQL Documentation](https://spring.io/projects/spring-graphql)
- [GraphQL Specification](https://graphql.org/)
- [Spring Security Reference](https://spring.io/projects/spring-security)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👤 Author

Mrugesh Limbachiya - mrugeshrlimbachiya@gmail.com

---

**Happy Coding! 🚀**