# 🚀 Control Server

A microservice for file management operations with secure API key authentication.

## 📋 Overview

Control Server is a Spring Boot application that provides a RESTful API for file operations. It's designed to be part of a microservices architecture and registers itself with Eureka service discovery.

## ✨ Features

- 📁 Upload files to specified directories
- 📥 Download files from specified paths
- 📋 List files in specified directories
- 🔐 Secure API with API key authentication
- 🔍 Service discovery with Eureka

## 🛠️ Technologies

- Java 17
- Spring Boot 3.5.0
- Spring Security
- Spring Cloud Netflix Eureka Client

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- Eureka Server (for service discovery)

### Configuration

The application can be configured using the following properties in `application.properties` or environment variables:

| Property | Environment Variable | Description | Default |
|----------|---------------------|-------------|---------|
| `api.key` | `API_KEY` | API key for authentication | `default-api-key-for-development-only` |
| `eureka.client.serviceUrl.defaultZone` | `EUREKA_URI` | Eureka server URL | `http://localhost:8761/eureka` |

Additional configuration options:
- File upload limits: 100MB (max file size and max request size)
- Actuator endpoints: health and info

### Building and Running

```bash
# Build the application
mvn clean package

# Run the application
java -jar target/controlserver-0.0.1-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

## 🔌 API Reference

All API endpoints require the `X-API-KEY` header with a valid API key.

### Upload a File

```
POST /api/files/upload
```

**Parameters:**
- `file` (multipart): The file to upload
- `directory` (query): The directory to upload to

**Response:**
- `200 OK`: File uploaded successfully
- `400 Bad Request`: Failed to upload file

### Download a File

```
GET /api/files/download
```

**Parameters:**
- `filePath` (query): The path of the file to download

**Response:**
- `200 OK`: File content with appropriate headers for download
- `404 Not Found`: File not found

### List Files

```
GET /api/files/list
```

**Parameters:**
- `directory` (query): The directory to list files from

**Response:**
- `200 OK`: JSON array of file information objects
- `400 Bad Request`: Failed to list files

**File Information Object:**
```json
{
  "name": "filename.ext",
  "directory": false,
  "size": 1024,
  "lastModified": 1623456789000
}
```

## 🔒 Security

The API is secured using API key authentication. All requests must include the `X-API-KEY` header with a valid API key.

## 🔄 Integration with Eureka

The Control Server registers itself with Eureka for service discovery. This allows other services to discover and communicate with the Control Server without hardcoding its address.

## 📝 License

This project is licensed under the MIT License.