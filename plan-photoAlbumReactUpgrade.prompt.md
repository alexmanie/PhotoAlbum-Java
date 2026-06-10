# Plan: Upgrade PhotoAlbum UI from Thymeleaf to React

**TL;DR:** Migrate from server-side rendered Thymeleaf templates to a React SPA (Single Page Application) while keeping the Spring Boot backend as a REST API service. The React app will run on a separate port during development and bundle separately for production, consuming JSON endpoints from your existing Spring Boot controllers.

## Execution Steps

### 1. Set up React development environment

Create new React app with Create React App or Vite, configure for TypeScript, set up API proxy for development to communicate with Spring Boot backend (http://localhost:8080).

**Outputs:**
- New React project structure
- Development proxy configuration
- Package management setup

### 2. Create React component structure

Build `Gallery` component for photo grid, `UploadZone` component for drag-drop upload, `PhotoDetail` component for full-size view with navigation, `Navigation` component for header/footer.

**Outputs:**
- Reusable React components
- Component hierarchy established
- Props and composition patterns defined

### 3. Convert REST endpoints

Keep Spring Boot controllers intact but remove Thymeleaf rendering; update `HomeController` and `DetailController` to return JSON instead of HTML views; verify `/upload`, `/photo/{id}`, `/detail/{id}` endpoints work as pure APIs.

**Outputs:**
- JSON API endpoints from Spring Boot
- CORS configuration if needed
- Removed Thymeleaf dependency from pom.xml

### 4. Implement React hooks & state

Use `useState` for photos, upload status; `useEffect` for fetching photos on mount; `useContext` or state management library (Redux/Zustand) if needed; implement photo deletion API endpoint in Spring Boot.

**Outputs:**
- React hooks integration
- State management setup
- Photo delete endpoint in backend

### 5. Add React Router for navigation

Set up routes for `/` (gallery), `/detail/:id` (photo detail with prev/next navigation), handle routing client-side instead of server-side.

**Outputs:**
- Client-side routing configuration
- Dynamic route parameters
- Navigation helpers

### 6. Update styling & assets

Convert existing Bootstrap 5 CSS to React-compatible structure; migrate `upload.js` logic to React hooks; ensure responsive design maintained; move CSS files to React app structure.

**Outputs:**
- React-compatible CSS organization
- Upload logic in React
- Responsive Bootstrap integration

### 7. Update Spring Boot configuration & build

Remove Thymeleaf dependency; add CORS support; configure Spring Boot to serve React app as static assets for production; update `pom.xml` build process to build React first then bundle with JAR.

**Outputs:**
- Updated pom.xml without Thymeleaf
- Frontend Maven plugin integration
- Static asset serving configuration

### 8. Update documentation

Modify README with new dev workflow (React dev server + Spring Boot), update technology stack, add React setup instructions.

**Outputs:**
- Updated README.md
- Development workflow documentation
- New technology stack listed

## Current State Analysis

**Current Technology Stack:**
- Framework: Spring Boot 2.7.18 (Java 8)
- Frontend: Thymeleaf templates with Bootstrap 5.3.0 and vanilla JavaScript
- Templating files: `layout.html`, `index.html`, `detail.html`
- JavaScript: Single file `upload.js` with vanilla DOM manipulation
- CSS: `site.css` with Bootstrap integration

**Current Architecture:**
- Server-side rendering with Thymeleaf
- Controllers return HTML views (index, detail)
- Upload endpoint returns JSON already (`/upload` → JSON response)
- Photo serving endpoint (`/photo/{id}`)
- Database: Oracle with BLOB storage for photos

**Existing API Endpoints:**
- `GET /` - Returns rendered gallery page
- `POST /upload` - Returns JSON with uploaded photos
- `GET /photo/{id}` - Returns photo binary data
- `GET /detail/{id}` - Returns rendered detail page

## Deployment Strategy Considerations

### Option A: Single JAR (Recommended for simplicity)
React built into Spring Boot JAR as static assets
- Build React app first
- Copy built files to Spring Boot static folder
- Maven frontend plugin handles build orchestration
- Single deployment artifact
- Best for containerized deployments (Docker)

### Option B: Separate Deployment
React on CDN/blob storage with CORS-enabled API backend
- Two separate deployments
- React served from edge/CDN
- Spring Boot API at different origin
- Requires CORS configuration
- Best for scaling frontend independently

### Option C: Containerized (Two Containers)
Separate Docker images for React (Node/nginx) and Spring Boot
- React image with Node or nginx
- Spring Boot image
- Docker Compose orchestration
- Better resource isolation
- More complex deployment

## State Management Considerations

### Option A: Context API (Simplest)
- Built-in React primitive
- Good for moderate apps
- Suitable for photo gallery
- No additional dependencies

### Option B: Zustand (Lightweight)
- Minimal boilerplate
- Easy to learn
- Good alternative to Redux
- Small bundle size

### Option C: Redux/Redux Toolkit
- Full-featured state management
- Complex learning curve
- Overkill for gallery unless adding complex features
- Good for scaling

### Option D: TanStack Query (Server State)
- Specialized for server state
- Caching and synchronization
- Can combine with Context for UI state
- Recommended pattern

## Development Experience Approach

### Option A: Separate Dev Servers (Standard - RECOMMENDED)
```
Terminal 1: npm start (React dev server on port 3000)
Terminal 2: mvn spring-boot:run (Spring Boot on port 8080)
```
- Proxy configuration in React for API calls
- Fast HMR (Hot Module Reload) for React
- Standard React development workflow
- Easy to debug both frontend and backend

### Option B: Single Spring Boot with Manual React Rebuilds
```
Terminal: mvn spring-boot:run
```
- React app built into resources
- Requires rebuild for frontend changes
- Simpler deployment but slower development
- Less ideal for active development

## Migration Path

1. **Phase 1 - Setup** (Step 1-2): Create React project, establish component structure
2. **Phase 2 - Backend Conversion** (Step 3, 4): Update Spring Boot to serve JSON, remove Thymeleaf
3. **Phase 3 - Frontend Implementation** (Step 5-6): Implement React components, routing, styling
4. **Phase 4 - Integration & Build** (Step 7): Configure build process, static asset serving
5. **Phase 5 - Documentation** (Step 8): Update docs, README, contributor guidelines

## Key Files to Modify/Create

**New Files:**
- React project directory structure
- `frontend/` or `src/main/webapp/` (React app source)
- `.env.development` (API proxy configuration)

**Modified Files:**
- `pom.xml` - Add frontend-maven-plugin, remove Thymeleaf
- `src/main/java/com/photoalbum/controller/HomeController.java` - Return JSON
- `src/main/java/com/photoalbum/controller/DetailController.java` - Return JSON
- `README.md` - Update instructions and tech stack

**Removed Files:**
- `src/main/resources/templates/layout.html`
- `src/main/resources/templates/index.html`
- `src/main/resources/templates/detail.html`
- Spring Boot Thymeleaf configuration

## Prerequisites Summary

Before starting:
- Node.js 18+ and npm/yarn installed
- Docker Desktop (for container testing)
- Maven 3.6+ (already have)
- Java 8 (already have)
- Familiarity with React hooks and modern JavaScript
- Understanding of REST APIs and CORS

## Expected Outcomes

- **Development:** Fast React HMR development experience with live updates
- **Production:** Single optimized JAR file with React SPA bundled as static assets
- **Maintainability:** Clean separation between frontend React code and backend API logic
- **Scalability:** Easy to extend with new features on either frontend or backend independently
- **Testing:** Simpler unit testing for components, API testing for backend endpoints

## Benefits of This Approach

1. **Modern Frontend:** React provides better component reusability and state management
2. **Type Safety:** Can add TypeScript for better developer experience
3. **Developer Tools:** React DevTools browser extension for debugging
4. **Community:** Larger ecosystem of React libraries and solutions
5. **Performance:** Better client-side rendering, reduced server load
6. **Maintainability:** Clear separation of concerns between frontend and backend
7. **Scalability:** Can run frontend and backend on separate servers/regions as needed

