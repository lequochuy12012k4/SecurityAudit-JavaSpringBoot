# JSA Core

JSA Core là backend API của hệ thống quản lý người dùng, xác thực và kiểm toán truy cập (audit logging) được xây dựng bằng Java Spring Boot. Dự án tích hợp nhiều thành phần hệ thống hiện đại như JWT, Redis, Kafka, Elasticsearch và PostgreSQL để phục vụ các tính năng bảo mật, phân quyền và tìm kiếm dữ liệu hiệu quả.

## Tóm tắt dự án

Dự án này cung cấp các chức năng chính sau:

- Xác thực người dùng bằng JWT (access token + refresh token)
- Phân quyền theo vai trò: `USER`, `ADMIN`
- Quản lý người dùng: tạo, cập nhật, xóa, lấy thông tin, đổi mật khẩu
- Quản lý hóa đơn: tạo, xem, cập nhật, xóa, tìm kiếm
- Chống brute-force login với cơ chế khóa tài khoản tạm thời
- Audit log cho các hoạt động bảo mật và nghiệp vụ quan trọng
- Blacklist token khi logout bằng Redis
- Đồng bộ dữ liệu qua Kafka và Elasticsearch cho tìm kiếm nâng cao
- Hỗ trợ đa ngôn ngữ tiếng Anh và tiếng Việt

## Công nghệ sử dụng

- Java 26
- Spring Boot 4.1.1
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- Elasticsearch
- Kafka
- JWT (Nimbus JOSE + JWT)
- Hibernate Validation
- Lombok, MapStruct
- Maven

## Kiến trúc dự án

```text
src/main/java/com/javasecurityaudit/jsa_core/
├── config/             # Cấu hình Spring, Security, Redis, Kafka, Elasticsearch, I18n
│   ├── annotation/
│   ├── aspect/
│   ├── audit/
│   ├── elasticsearch/
│   ├── kafka/
│   ├── redis/
│   └── security/
├── controller/         # REST API controllers
├── dto/                # Request/Response DTOs
├── entity/             # JPA entities
├── enums/              # Enum dùng trong hệ thống
├── exception/          # Exception và GlobalExceptionHandler
├── executor/           # Async consumer cho audit event
├── filter/             # JWT filter
├── mapper/             # MapStruct mapper
├── repository/         # Repository JPA và Elasticsearch
├── service/            # Business logic
├── util/               # Utility classes
├── document/           # Elasticsearch document model
├── base/               # Base response / audit trail
├── JsaCoreApplication.java
└── ...
```

## Cấu hình tài nguyên

```text
src/main/resources/
├── application.yaml                 # Cấu hình chính của ứng dụng
├── application.properties          # File cấu hình bổ sung
├── application-prod.yaml           # Cấu hình môi trường production
├── messages.properties             # Message tiếng Anh
├── messages_vi.properties         # Message tiếng Việt
├── elasticsearch/
│   ├── users-settings.json
│   └── invoices-settings.json
└── ...
```

## Yêu cầu môi trường

Trước khi chạy dự án, cần chuẩn bị các thành phần sau:

- JDK 26
- Maven 3.9+
- PostgreSQL
- Redis
- Elasticsearch
- Kafka

## Khởi chạy dự án

### 1. Clone repository

```bash
git clone https://github.com/lequochuy12012k4/SecurityAudit-JavaSpringBoot.git
cd SecurityAudit-JavaSpringBoot
```

### 2. Cấu hình biến môi trường

Sao chép file `.env.example` thành `.env` và điền các giá trị phù hợp:

```bash
cp .env.example .env
```


### 3. Khởi tạo services phụ trợ

Dự án có sẵn file `docker-compose.yml` để chạy các service cần thiết:

```bash
docker compose up -d
```

Các service bao gồm:

- PostgreSQL
- Redis
- Elasticsearch
- Kafka
- Kafka UI

### 4. Build và chạy ứng dụng

```bash
./mvnw clean install
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy với context-path:

```text
http://localhost:8080/javasecurityaudit
```

## Cấu hình ứng dụng

Các cấu hình quan trọng được định nghĩa trong file:

- `src/main/resources/application.yaml`
- `src/main/resources/application-prod.yaml`
- `.env` hoặc biến môi trường của hệ thống

## API chính

### 1) Xác thực và phân quyền

- `POST /javasecurityaudit/api/v1/auth/login`
  - Đăng nhập, trả về `accessToken` và `refreshToken`
- `POST /javasecurityaudit/api/v1/auth/logout`
  - Đăng xuất, vô hiệu hóa refresh token
- `POST /javasecurityaudit/api/v1/auth/refresh-token`
  - Làm mới access token

### 2) Quản lý người dùng

- `POST /javasecurityaudit/api/v1/users/create`
  - Tạo người dùng mới
- `GET /javasecurityaudit/api/v1/users/my-info`
  - Xem thông tin cá nhân
- `PUT /javasecurityaudit/api/v1/users/update-my-info`
  - Cập nhật thông tin cá nhân
- `PUT /javasecurityaudit/api/v1/users/change-password`
  - Đổi mật khẩu
- `GET /javasecurityaudit/api/v1/users`
  - Admin xem danh sách người dùng
- `GET /javasecurityaudit/api/v1/users/{userId}`
  - Admin xem chi tiết người dùng
- `PUT /javasecurityaudit/api/v1/users/{userId}`
  - Admin cập nhật thông tin user
- `DELETE /javasecurityaudit/api/v1/users/{userId}`
  - Admin xóa user
- `PATCH /javasecurityaudit/api/v1/users/{userId}/status`
  - Admin thay đổi trạng thái tài khoản
- `GET /javasecurityaudit/api/v1/users/search?keyword=...&page=0&size=20`
  - Tìm kiếm người dùng trên Elasticsearch

### 3) Quản lý hóa đơn

- `POST /javasecurityaudit/api/v1/invoices/create`
  - Tạo hóa đơn
- `GET /javasecurityaudit/api/v1/invoices/{invoiceId}`
  - Xem hóa đơn
- `PUT /javasecurityaudit/api/v1/invoices/{invoiceId}`
  - Cập nhật hóa đơn
- `DELETE /javasecurityaudit/api/v1/invoices/{invoiceId}`
  - Xóa hóa đơn
- `GET /javasecurityaudit/api/v1/invoices/search?keyword=...&page=0&size=20`
  - Tìm kiếm hóa đơn theo từ khóa

## Bảo mật và audit log

Dự án có các tính năng bảo mật quan trọng như:

- Mã hóa mật khẩu bằng BCrypt
- Xác thực bằng JWT
- Refresh token được lưu và kiểm soát bằng Redis
- Người dùng bị logout sẽ được lưu vào blacklist token
- Giới hạn login attempts để chống brute-force
- Lưu audit logs cho các hành động như đăng nhập, đăng xuất, refresh token, tạo/sửa/xóa user / invoice
- Tất cả sự kiện quan trọng đều được ghi vào bảng `user_activity_log`

## Hỗ trợ đa ngôn ngữ

Dự án hỗ trợ tiếng Anh và tiếng Việt thông qua `MessageSource`:

- `src/main/resources/messages.properties` : tiếng Anh
- `src/main/resources/messages_vi.properties` : tiếng Việt

Có thể chuyển đổi ngôn ngữ bằng header `Accept-Language` hoặc query param `?lang=en` / `?lang=vi`.

## Chạy test

```bash
./mvnw test
```

## Build production

```bash
./mvnw clean package -DskipTests
```

File JAR sẽ được sinh ra trong thư mục `target/`.

## Ghi chú

- Nếu cập nhật mapping Elasticsearch, cần xóa index cũ hoặc reindex dữ liệu để mapping mới được áp dụng.
- Dự án đang dùng PostgreSQL cho dữ liệu nghiệp vụ, Redis cho token và audit stream, Elasticsearch cho tìm kiếm, Kafka cho đồng bộ dữ liệu.

## License

Dự án hiện đang được phân phối theo giấy phép MIT.