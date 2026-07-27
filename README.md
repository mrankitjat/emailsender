# Recruitment Email Service

A production-ready **Spring Boot 3.3.x** application built with **Java 21** designed to automate recruitment email distribution with resume attachments (PDF/DOCX). The service features an intelligent priority fallback engine that seamlessly integrates with **Google Drive API v3** whenever optional HTTP request parameters are omitted.

---

## 🌟 Key Features

- **Clean Architecture & SOLID Principles**: Clear separation between Controllers, Orchestrator, Domain Services, DTO Records, and Exception Handlers.
- **Dynamic Priority Fallback**:
  1. Priority 1: Values supplied in HTTP request payload (`to`, `subject`, `body`).
  2. Priority 2: Missing or blank fields automatically fetched from Google Drive files (`emails.txt`, `subject.txt`, `body.txt`).
- **Google Drive Integration**: Authenticates using Service Account OAuth2 JSON and caches text assets using **Caffeine Cache**.
- **On-Demand Resume Attachment**: Downloads `resume.pdf` or `resume.docx` dynamically when processing emails and auto-detects MIME types using **Apache Tika**.
- **Enterprise Spring Mail**: Supports TLS/SSL, App Passwords, configurable timeouts, rate limiting, and `@Retryable` transient fault handling.
- **Asynchronous Bulk Transmission**: Concurrently dispatches emails using custom bounded thread pool executors (`ThreadPoolTaskExecutor`) without failing the whole batch if an individual email fails.
- **Email Sanitation & Validation**: Deduplicates recipients, skips invalid email addresses with explicit logging, and outputs comprehensive delivery metrics (`totalRecipients`, `success`, `failed`, `failedEmails`).
- **Actuator & OpenAPI/Swagger UI**: Includes `/actuator/health` monitoring and dynamic interactive documentation at `/swagger-ui.html`.
- **Containerized Deployment**: Includes multi-stage `Dockerfile` and `docker-compose.yml`.

---

## 🌐 Storage Provider Options (`recruitment.storage.type`)

You can switch the asset storage backend by setting `recruitment.storage.type` in `application.yml` or via environment variables (`STORAGE_TYPE`):

### 1. HTTP / Web Remote Storage (`STORAGE_TYPE=HTTP`) - Option 4
Fetch recruitment files directly over HTTP/HTTPS from a remote Web server, GitHub Raw repository, S3 public URL, or Gist:

```yaml
recruitment:
  storage:
    type: HTTP
    http:
      base-url: https://raw.githubusercontent.com/your-username/recruitment-assets/main
      emails-file: emails.txt
      subject-file: subject.txt
      body-file: body.txt
      resume-file: resume.pdf
```

#### GitHub / Remote Web Server Directory Structure:
```text
https://raw.githubusercontent.com/your-username/recruitment-assets/main/
├── emails.txt
├── subject.txt
├── body.txt
└── resume.pdf
```

### 2. Local Disk Folder (`STORAGE_TYPE=LOCAL`)
```yaml
recruitment:
  storage:
    type: LOCAL
    local-dir: ./recruitment-assets
```

### 3. Google Drive API (`STORAGE_TYPE=GOOGLE_DRIVE`)
```yaml
recruitment:
  storage:
    type: GOOGLE_DRIVE
```

---

## ⚙️ Configuration (`application.yml`)

Configure environment variables or update `src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}

google:
  drive:
    credentials-path: ${GOOGLE_CREDENTIALS_PATH:classpath:credentials.json}
    folder-id: ${GOOGLE_FOLDER_ID:1a2b3c4d5e6f7g8h9i0j}

email:
  cache-minutes: 10
  async:
    core-pool-size: 5
    max-pool-size: 15
    queue-capacity: 100
  rate-limit:
    delay-ms: 100
```

---

## 🚀 API Endpoint Documentation

### `POST /api/email/send`

Request Body is **Optional**.

#### Scenario A: Full Payload (Overrides Google Drive)
```json
POST /api/email/send
Content-Type: application/json

{
  "to": [
    "abc@company.com",
    "xyz@company.com"
  ],
  "subject": "Application for Software Developer",
  "body": "Dear Recruiter..."
}
```

#### Scenario B: Partial Payload (Subject & Body from Request, Recipients from Drive)
```json
POST /api/email/send
Content-Type: application/json

{
  "subject": "Application for Software Engineer",
  "body": "Dear Hiring Manager..."
}
```

#### Scenario C: Empty Body or Missing Payload (Everything from Drive)
```json
POST /api/email/send
Content-Type: application/json

{}
```

#### Sample Response Payload (`HTTP 200 OK`)
```json
{
  "totalRecipients": 100,
  "success": 98,
  "failed": 2,
  "failedEmails": [
    "abc@test.com",
    "xyz@test.com"
  ]
}
```

---

## 🛠️ Building & Running

### Local Maven Execution
Ensure Java 21 is installed.
```bash
mvn clean package
mvn spring-boot:run
```

### Running Unit Tests
```bash
mvn clean test
```

### Docker Execution
```bash
docker build -t recruitment-email-service:latest .
docker run -p 8080:8080 \
  -e MAIL_USERNAME="your-email@gmail.com" \
  -e MAIL_PASSWORD="your-app-password" \
  -e GOOGLE_FOLDER_ID="your-drive-folder-id" \
  recruitment-email-service:latest
```

### Docker Compose
```bash
docker-compose up --build -d
```

---

## 🔍 Monitoring & API Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Json Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Actuator Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🛡️ Exception Handling & Security

- **No Password Logging**: Log outputs strictly obscure SMTP password configurations.
- **Global Error Handling**: Unhandled domain and technical exceptions return structured RFC 7807 responses.
- **Fault Tolerant Bulk Sending**: Network glitches or invalid individual email addresses do not interrupt execution for remaining recipients.
