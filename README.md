# 🚀 FlyChat

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Build](https://img.shields.io/badge/Build-Maven-red.svg)](https://maven.apache.org/)

**FlyChat** is a high-performance, secure, and scalable real-time chat platform built with **Spring Boot 3**. It is designed to provide a seamless and secure messaging experience with end-to-end encryption support and a robust dual-database architecture.

---

## 👤 Author
**Santanu Sau**
[GitHub Profile](https://github.com/Santanu-Sau) <!-- Replace with your actual link -->

---

## ✨ Key Features

- **⚡ Real-time Messaging**: Powered by WebSockets (STOMP/SockJS) for instantaneous, low-latency communication.
- **🔐 End-to-End Encryption (E2EE)**: Built-in support for secure message exchange with advanced cryptographic key management (Signal Protocol inspired).
- **🗄️ Dual Database Architecture**:
    - **MySQL**: Handles critical relational data such as user profiles, groups, and contacts with high integrity.
    - **MongoDB**: Used for high-speed, schema-less storage of chat messages and history, ensuring scalability.
- **🛡️ Secure Authentication**: Stateless JWT-based authentication integrated with Spring Security.
- **👥 Group & Private Chats**: Seamlessly transition between personal 1-to-1 conversations and large group discussions.
- **📁 Media Sharing**: Integrated support for uploading and sharing images (JPEG, PNG, WebP) and documents (PDF).
- **📇 Contact Management**: Easily manage your circle of contacts for quick access and secure messaging.

---

## 🛠️ Tech Stack

- **Backend**: Java 25, Spring Boot 3.5.13
- **Security**: Spring Security, JJWT (JSON Web Token)
- **Databases**: 
    - **Relational**: MySQL (via Spring Data JPA / Hibernate)
    - **NoSQL**: MongoDB (via Spring Data MongoDB)
- **Messaging**: Spring WebSocket (STOMP)
- **Utilities**: Project Lombok, Hibernate Validator, Maven

---

## 📂 Project Structure

The project follows a standard Spring Boot package-by-feature architecture:

```text
com.chat.fly.app
├── auth        # Authentication, JWT, and Security configurations
├── chat        # Messaging logic, WebSocket handlers, and E2E key management
├── config      # Core application and database configurations
└── user        # User profile and contact management services
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 25** or higher
- **MySQL** 8.0+
- **MongoDB** 5.0+
- **Maven** 3.9+

### Configuration

The application uses an `application.yaml` file for configuration. You can override settings using environment variables:

- `MYSQL_URL`: MySQL connection URL
- `MYSQL_USERNAME` / `MYSQL_PASSWORD`: Database credentials
- `MONGODB_URI`: MongoDB connection string
- `JWT_SECRET`: Secret key for JWT signing (Minimum 32 characters)
- `CHAT_MEDIA_UPLOAD_DIR`: Path for uploaded files (Default: `uploads`)

### Running Locally

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Santanu-Sau/fly-chat.git
   cd fly-chat
   ```

2. **Install dependencies**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

The server will start on `http://localhost:8080` (default).


---

*Made with ❤️ by [Santanu Sau](https://github.com/Program2Builder)*
