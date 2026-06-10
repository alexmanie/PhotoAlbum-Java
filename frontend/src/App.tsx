import { useEffect, useState } from 'react';
import { Route, Routes } from 'react-router-dom';
import Navigation from './components/Navigation';
import Footer from './components/Footer';
import GalleryPage from './pages/GalleryPage';
import PhotoDetailPage from './pages/PhotoDetailPage';
import { fetchConfig } from './api/photos';

/** Root application shell: loads runtime config and wires client-side routes. */
export default function App() {
  const [aiEnabled, setAiEnabled] = useState(false);

  useEffect(() => {
    fetchConfig()
      .then((config) => setAiEnabled(config.aiEnabled))
      .catch(() => setAiEnabled(false));
  }, []);

  return (
    <>
      <Navigation />
      <div className="container">
        <main role="main" className="pb-3">
          <Routes>
            <Route path="/" element={<GalleryPage aiEnabled={aiEnabled} />} />
            <Route
              path="/detail/:id"
              element={<PhotoDetailPage aiEnabled={aiEnabled} />}
            />
          </Routes>
        </main>
      </div>
      <Footer />
    </>
  );
}
