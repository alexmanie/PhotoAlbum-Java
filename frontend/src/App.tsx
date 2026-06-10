import { Navigate, Route, Routes } from 'react-router-dom';
import { GalleryPage } from './pages/GalleryPage';
import { PhotoDetailPage } from './pages/PhotoDetailPage';

export function App() {
  return (
    <div className="app-shell">
      <header className="navbar navbar-expand-sm navbar-dark bg-dark border-bottom shadow-sm">
        <div className="container">
          <a className="navbar-brand" href="/">
            Photo Album
          </a>
        </div>
      </header>

      <main className="container py-4">
        <Routes>
          <Route path="/" element={<GalleryPage />} />
          <Route path="/detail/:id" element={<PhotoDetailPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
