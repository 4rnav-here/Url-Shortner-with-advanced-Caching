# URL Shortener — Scalable Distributed URL Shortening Service

A production-grade URL Shortener backend built using Java, Spring Boot, MongoDB, and Redis with distributed caching, analytics, rate limiting, async processing, and horizontal scaling support. This project demonstrates real-world backend engineering concepts and scalable system design patterns.

---

# Features

* URL shortening with Base62 encoding
* Custom aliases support
* URL expiration support
* Redis cache-aside strategy
* Redis trending URLs leaderboard
* Analytics tracking
* Async event processing
* Per-IP rate limiting
* Health check APIs
* Dockerized deployment
* Horizontal scaling ready
* Swagger/OpenAPI documentation
* Global exception handling

---

# Tech Stack

| Technology          | Purpose                          |
| ------------------- | -------------------------------- |
| Java 21             | Core language                    |
| Spring Boot         | Backend framework                |
| MongoDB             | Persistent database              |
| Redis               | Cache + rate limiting + trending |
| Docker              | Containerization                 |
| Spring Data MongoDB | Mongo integration                |
| Spring Data Redis   | Redis integration                |
| Lombok              | Boilerplate reduction            |
| Swagger/OpenAPI     | API documentation                |

---

# System Architecture

```text
                ┌──────────────────────┐
                │       Client         │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │   NGINX (Optional)   │
                │    Load Balancer     │
                └──────────┬───────────┘
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
 ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
 │ Spring Boot #1 │ │ Spring Boot #2 │ │ Spring Boot #3 │
 └────────┬───────┘ └────────┬───────┘ └────────┬───────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
      ┌───────────────┐            ┌────────────────┐
      │     Redis     │            │    MongoDB     │
      │ Cache / Rate  │            │ Persistent DB  │
      │ Limiting / ZS │            │ Analytics Data │
      └───────────────┘            └────────────────┘
```

---

# Core System Design Concepts Implemented

## Redis Cache-Aside Pattern

```text
Request
   ↓
Redis Lookup
   ↓
Cache Hit → Return Fast
   ↓
Cache Miss
   ↓
MongoDB Lookup
   ↓
Store in Redis (24h TTL)
```

---

## Async Analytics Processing

Analytics processing is fully asynchronous using:

```java
@Async
```

This prevents redirect requests from being blocked by analytics writes.

---

## Rate Limiting

Per-IP fixed window rate limiter implemented using Redis.

```text
Limit: 10 requests / second / IP
```

Redis atomic increment operations ensure thread safety.

---

## Trending URLs

Trending URLs are implemented using Redis Sorted Sets.

```text
ZINCRBY trending_urls
```

This provides:

* O(log n) ranking updates
* Fast leaderboard retrieval
* Distributed shared state

---

# API Endpoints

## Create Short URL

```http
POST /api/url/shorten
```

### Request

```json
{
  "url": "https://github.com",
  "customAlias": "github",
  "expiryDays": 7
}
```

### Response

```json
{
  "shortUrl": "http://localhost:8080/github"
}
```

---

## Redirect URL

```http
GET /{shortCode}
```

Returns:

```http
302 FOUND
```

---

## Get Analytics

```http
GET /analytics/{code}
```

### Response

```json
{
  "totalClicks": 120,
  "topBrowsers": {
    "Chrome": 90,
    "Firefox": 20
  },
  "topDevices": {
    "Desktop": 100,
    "Mobile": 20
  }
}
```

---

## Trending URLs

```http
GET /api/url/trending
```

---

## Health Check

```http
GET /health
```

### Response

```json
{
  "mongo": "UP",
  "redis": "UP"
}
```

---

# Project Structure

```text
src/main/java/com/urlShortner/project
│
├── controller
│   └── UrlController.java
│
├── service
│   └── UrlService.java
│
├── repository
│   ├── UrlRepository.java
│   └── AnalyticsRepository.java
│
├── entity
│   ├── Url.java
│   └── Analytics.java
│
├── dto
│   ├── ShortenUrlRequest.java
│   ├── ShortenUrlResponse.java
│   ├── AnalyticsResponse.java
│   └── ErrorResponse.java
│
├── config
│   ├── RedisConfig.java
│   └── MongoConfig.java
│
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── UrlExpiredException.java
│   └── UrlNotFoundException.java
│
└── util
    └── Base62Generator.java
```

---

# Running Locally

## Clone Repository

```bash
git clone <your-github-url>
cd url-shortener
```

---

# Start MongoDB + Redis

Using Docker:

```bash
docker compose up -d
```

---

# Run Spring Boot App

```bash
mvn spring-boot:run
```

Application starts at:

```text
http://localhost:8080
```

---

# Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

---

# Docker Setup

## Build JAR

```bash
mvn clean package
```

---

## Build Docker Image

```bash
docker build -t url-shortener .
```

---

## Run Container

```bash
docker run -p 8080:8080 url-shortener
```

---

# Horizontal Scaling

Run multiple Spring Boot instances:

```bash
java -jar app.jar --server.port=8080
```

```bash
java -jar app.jar --server.port=8081
```

Both instances share:

* Redis
* MongoDB

This demonstrates:

* distributed shared state
* stateless backend architecture
* horizontal scalability

---

# Redis Key Design

| Key                    | Purpose             |
| ---------------------- | ------------------- |
| `url:{shortCode}`      | Cached original URL |
| `url:hits:{shortCode}` | Click counter       |
| `trending_urls`        | Redis sorted set    |
| `rate_limit:{ip}`      | Rate limiting       |

---

# Production Features

* Distributed caching
* Async event processing
* Health monitoring
* Rate limiting
* Global exception handling
* Analytics pipeline
* Docker deployment
* Stateless architecture
* Redis distributed state
* Scalable system design

---

# Future Improvements

* JWT Authentication
* Kafka event streaming
* Redis Cluster
* Kubernetes deployment
* Prometheus + Grafana monitoring
* Click geo-location analytics
* QR code generation
* User dashboard
* CI/CD pipelines

---

# Learning Outcomes

This project demonstrates understanding of:

* REST API design
* Distributed caching
* Redis data structures
* MongoDB document modeling
* Async processing
* Rate limiting algorithms
* Scalable backend architecture
* Horizontal scaling
* Dockerization
* Production-ready backend engineering

