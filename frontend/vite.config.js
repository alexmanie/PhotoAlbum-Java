import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
// During development the Spring Boot backend runs on :8080.
// Vite serves the SPA on :3000 and proxies API + photo-binary requests to the backend.
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        proxy: {
            '/api': 'http://localhost:8080',
            '/photo': 'http://localhost:8080',
        },
    },
    build: {
        // Output the production bundle into frontend/dist; Maven copies it into the JAR's static folder.
        outDir: 'dist',
        emptyOutDir: true,
    },
});
