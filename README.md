# 🚀 My Control Server

A simple microservices-based file management system with service discovery.

## 🧩 Components

### 🔍 Eureka Server
Service registry for microservices discovery and registration. Allows services to find and communicate with each other without hardcoding hostname and port.

### 📁 Control Server
File management service that registers itself with Eureka. Provides functionality for:
- Uploading files
- Downloading files
- Listing files and directories

### 🖥️ Client Application
Client application that consumes the Control Server API (coming soon).

## 🛠️ Technology Stack

- Spring Boot 3.5.0
- Spring Cloud Netflix Eureka
- Maven

## 🚦 Getting Started

1. Start the Eureka Server:
   ```
   cd eureka
   mvn spring-boot:run
   ```

2. Start the Control Server:
   ```
   cd control-server
   mvn spring-boot:run
   ```

3. Access the Eureka dashboard at: http://localhost:8761

## 📝 License

This project is open source and available under the [MIT License](LICENSE).