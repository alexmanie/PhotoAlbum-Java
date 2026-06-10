# Photo Album Application - React + Spring Boot + Oracle

A modern photo gallery that uses a React SPA for the UI and a Spring Boot REST API for backend services. Photos are stored as Oracle BLOBs and served directly by the backend.

## Features

- Multi-photo upload with drag-and-drop support
- Responsive gallery and full detail view
- Previous/next navigation in detail page
- Photo deletion from detail page
- AI-generated photo descriptions via Azure OpenAI (optional)
- React Router-based client navigation
- Spring Boot REST API backend

## Technology Stack

- Backend: Spring Boot 2.7.18 (Java 8)
- Frontend: React 18 + TypeScript + Vite
- Database: Oracle Database 21c Express Edition
- Build: Maven + frontend-maven-plugin
- Containerization: Docker + Docker Compose

## Architecture

- Frontend SPA: React app in [frontend](frontend)
- API layer: Spring Boot controllers under [src/main/java/com/photoalbum/controller](src/main/java/com/photoalbum/controller)
- Binary photo serving: `GET /photo/{id}`
- Metadata and operations: `/api/photos/**`

## Prerequisites

- Java 8+
- Maven 3.6+
- Node.js 18+ (for local frontend development)
- Docker Desktop (recommended for Oracle + app runtime)
- Optional: Azure OpenAI resource for AI descriptions

## Quick Start (Docker)

```bash
docker-compose up --build -d
```

App URL: `http://localhost:8080`

## Local Development

### 1. Start backend

```bash
mvn spring-boot:run
```

### 2. Start frontend dev server

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

Vite proxies API requests to Spring Boot at `http://localhost:8080`.

## Production Build

```bash
mvn clean package
java -jar target/photo-album-1.0.0.jar
```

The Maven build:

1. Installs Node.js/npm via `frontend-maven-plugin`
2. Installs frontend dependencies
3. Builds the React SPA
4. Copies built assets into Spring Boot static output
5. Packages everything into a single runnable JAR

## API Endpoints

### Photos

- `GET /api/photos` - list photos and `aiEnabled` flag
- `POST /api/photos/upload` - upload one or more photos (`files` form field)
- `GET /api/photos/{id}` - single photo detail + previous/next IDs
- `DELETE /api/photos/{id}` - delete photo
- `GET /api/photos/{id}/description` - AI description polling endpoint

### Binary Content

- `GET /photo/{id}` - returns photo bytes with MIME type

## React Routes

- `/` - gallery view
- `/detail/:id` - detail view

Spring MVC forwards detail routes to `index.html` in production for SPA navigation.

## AI Description Configuration (Optional)

Set in [src/main/resources/application.properties](src/main/resources/application.properties):

```properties
azure.openai.enabled=true
azure.openai.endpoint=https://YOUR_RESOURCE_NAME.openai.azure.com
azure.openai.api-key=YOUR_API_KEY
azure.openai.deployment-name=gpt-4o
azure.openai.api-version=2024-02-15-preview
```

When disabled, uploads still work and AI description UI is hidden.

## Project Structure

```text
PhotoAlbum-Java/
├── frontend/                         # React + TypeScript SPA
├── src/main/java/com/photoalbum/     # Spring Boot backend
├── src/main/resources/application.properties
├── oracle-init/                      # Oracle bootstrap scripts
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Troubleshooting

- If Docker Oracle startup is slow, wait 2-3 minutes before backend health checks pass.
- If frontend dev server cannot reach APIs, confirm backend is running on port 8080.
- If Maven frontend step fails, verify network access for npm package download.

## License

This project is provided as-is for educational and demonstration purposes.
