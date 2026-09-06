# JSA Core

A Java Spring Boot application for JWT-based authentication and authorization with audit logging.

## Features

- User authentication (login, logout, token refresh)
- Role-based access control (ADMIN, USER)
- Audit logging for security events
- Redis-based token blacklisting for logout functionality
- Refresh token mechanism
- Password encoding with BCrypt
- Input validation with Hibernate Validator
- Global exception handling
- **Internationalization (i18n) support** - English and Vietnamese
- API documentation with Swagger/OpenAPI (if configured)

## Project Structure

```
src/main/java/com/javasecurityaudit/jsa_core/
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── enums/               # Enumerations
├── exception/           # Custom exceptions and handlers
├── executor/            # Async task executors
├── filter/              # Servlet filters
├── mapper/              # Object mappers
├── repository/          # Spring Data repositories
├── service/             # Business logic services
├── util/                # Utility classes
└── config/              # Configuration classes
    ├── annotation/      # Custom annotations
    ├── aspect/          # AOP aspects
    ├── audit/           # Audit configuration
    ├── redis/           # Redis configuration
    ├── security/        # Security configuration
    └── I18nConfig.java  # Internationalization configuration
```

### Resource Files

```
src/main/resources/
├── application.properties          # Main configuration
├── application-dev.properties      # Development profile
├── application-prod.properties     # Production profile
├── application-redis.properties    # Redis configuration
├── messages.properties             # English messages (default)
└── messages_vi.properties          # Vietnamese messages
```

## Getting Started

### Prerequisites

- Java 26
- Maven 3.6+
- MySQL or H2 database (for development)
- Redis server (for token blacklisting)

### Installation

1. Clone the repository
2. Configure the database in `src/main/resources/application.properties` or `application.yaml`
3. Configure Redis in `application-redis.properties` or similar
4. Build the project:
   ```bash
   ./mvnw clean install
   ```
5. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Configuration

The application uses Spring Boot's external configuration. Key configuration files:

- `application.properties` - Main configuration
- `application-dev.properties` - Development profile
- `application-prod.properties` - Production profile
- `application-redis.properties` - Redis configuration

### Internationalization (i18n)

The application supports multiple languages through Spring's `MessageSource`:

- **Supported languages**: English (default) and Vietnamese
- **Message files**: 
  - `src/main/resources/messages.properties` - English (default)
  - `src/main/resources/messages_vi.properties` - Vietnamese
- **Locale resolution**: Via `Accept-Language` header or `?lang=` query parameter
- **Usage**: Use `?lang=en` or `?lang=vi` in API requests, or set `Accept-Language` header

Example:
```bash
# Get Vietnamese messages
curl -H "Accept-Language: vi" http://localhost:8080/api/v1/auth/login

# Or using query parameter
curl "http://localhost:8080/api/v1/auth/login?lang=vi"
```

### API Endpoints

#### Elasticsearch search
- `GET /javasecurityaudit/api/v1/users/search?keyword=nguyen&page=0&size=20` - Admin tìm người dùng theo username, email hoặc họ tên; hỗ trợ tiếng Việt có dấu/không dấu và trả về metadata phân trang.
- `GET /javasecurityaudit/api/v1/invoices/search?keyword=khach hang&page=0&size=20` - User/Admin tìm hóa đơn theo mã, khách hàng, email, số điện thoại hoặc mô tả; hỗ trợ metadata phân trang.

`page` bắt đầu từ `0`, `size` mặc định là `20` và được giới hạn tối đa `100`. Response có dạng `content`, `page`, `size`, `totalElements` và `totalPages`.  `totalElements` có luôn luôn = 10000 do giới hạn elasticsearch có 10000

Các index `users` và `invoices` được tạo với analyzer `vi_analyzer` (lowercase + asciifolding). Nếu index đã tồn tại trước khi cập nhật mapping, cần xóa/tạo lại index hoặc reindex dữ liệu để mapping mới có hiệu lực.

#### Authentication
- `POST /api/v1/auth/login` - Authenticate user and return JWT tokens
- `POST /api/v1/auth/logout` - Invalidate refresh token
- `POST /api/v1/auth/refresh-token` - Refresh access token using refresh token

### Security

- Passwords are encoded using BCrypt
- JWT tokens are signed with HMAC using a secret key
- Access tokens have short expiration (configurable)
- Refresh tokens have longer expiration and are stored in Redis
- Logout invalidates the refresh token by storing it in a Redis blacklist

### Audit Logging

The application uses AOP to automatically log security-related events:
- User login/logout
- Token refresh
- Access denied events
- Account lock/unlock events

Audit records are stored in the `user_activity_log` table.

## Building for Production

```bash
./mvnw clean package -DskipTests
```

The resulting JAR file will be in the `target` directory.

## Running Tests

```bash
./mvnw test
```

## License

This project is licensed under the MIT License.