# NovaCine - Movie Theater Ticketing Platform

NovaCine là một hệ thống backend bán vé xem phim phân tán (Distributed Movie Theater Ticketing System) được thiết kế theo kiến trúc **Microservices** hướng sự kiện (Event-Driven Architecture). Hệ thống hỗ trợ luồng đặt vé hoàn chỉnh, xử lý thanh toán bất đồng bộ, quản lý ghế ngồi thời gian thực chống trùng lặp, và tích hợp hệ thống giám sát hiệu năng chuẩn doanh nghiệp.

---

## 🚀 Công Nghệ Sử Dụng (Tech Stack)

* **Ngôn ngữ & Framework:** Java 21, Spring Boot 3
* **Hệ thống Microservices (Spring Cloud):**
  * **Netflix Eureka Server:** Đăng ký và phát hiện dịch vụ (Service Discovery & Registry).
  * **Spring Cloud Gateway:** API Gateway tập trung, định tuyến request và phân tải.
  * **Spring Cloud OpenFeign:** Gọi API đồng bộ, trực tiếp giữa các service nội bộ.
* **Hệ thống hàng đợi thông điệp (Message Broker):** Apache Kafka (Event-driven Saga Pattern).
* **Caching & Concurrency:**
  * **Redis Cache:** Lưu trữ bộ nhớ đệm cho dữ liệu danh sách phim & lịch chiếu để giảm tải DB.
  * **Redisson (Redis Distributed Lock):** Khóa phân tán chống race condition (đặt trùng ghế) khi lượng truy cập cao.
* **Cơ sở dữ liệu:** PostgreSQL, Spring Data JPA / Hibernate.
* **Giám sát hệ thống (Observability):**
  * **OpenTelemetry (OTel) Java Agent:** Tự động thu thập Traces và Metrics.
  * **Grafana Cloud APM:** Trực quan hóa bản đồ dịch vụ (Service Map) và theo dõi luồng request xuyên suốt (Distributed Tracing).
* **Containerization:** Docker & Docker Compose.

---

## 📁 Danh Sách Các Services

| Service Name | Port | Chức Năng |
| :--- | :--- | :--- |
| `discovery-server` | `8761` | Eureka Server - Đăng ký & quản lý địa chỉ của các service. |
| `api-gateway` | `8080` | API Gateway - Cổng kết nối duy nhất của client với các dịch vụ ngầm. |
| `event-service` | `8081` | Quản lý Phim, Lịch chiếu, Ghế ngồi và trạng thái ghế trong phòng chiếu. |
| `booking-service` | `8082` | Quản lý quy trình đặt vé, khóa ghế tạm thời (Redis Lock) và điều phối giao dịch. |
| `payment-service` | `8083` | Mô phỏng xử lý thanh toán bất đồng bộ qua Kafka. |
| `notification-service` | `8084` | Lắng nghe sự kiện để gửi email/tin nhắn thông báo (giả lập qua Log). |
| `common` | *(Library)* | Thư viện dùng chung chứa DTOs, Event models, và cấu trúc xử lý lỗi chuẩn. |

---

## 🛠️ Hướng Dẫn Khởi Chạy

### 1. Khởi động Hạ Tầng (Docker)
Khởi chạy Redis và Kafka bằng Docker Compose:
```bash
docker-compose up -d
```

### 2. Khởi chạy các Spring Boot Services
Chạy lệnh `mvn spring-boot:run` tại từng thư mục service theo thứ tự:
1. `discovery-server` (Đợi 5-10 giây để Eureka sẵn sàng)
2. `api-gateway`
3. `event-service`
4. `booking-service`
5. `payment-service`
6. `notification-service`

Truy cập Eureka Dashboard tại: [http://localhost:8761](http://localhost:8761) để kiểm tra trạng thái hoạt động của các dịch vụ.
