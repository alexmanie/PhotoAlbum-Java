import { useEffect, useState } from 'react';
import Gallery from '../components/Gallery';
import UploadZone from '../components/UploadZone';
import { fetchPhotos } from '../api/photos';
import type { Photo } from '../types';

interface GalleryPageProps {
  aiEnabled: boolean;
}

/** Home page: upload zone plus the photo gallery grid. */
export default function GalleryPage({ aiEnabled }: GalleryPageProps) {
  const [photos, setPhotos] = useState<Photo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchPhotos()
      .then((data) => {
        if (!cancelled) setPhotos(data);
      })
      .catch(() => {
        if (!cancelled) setError('Failed to load photos.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const handleUploaded = (uploaded: Photo[]) => {
    // Newest first: prepend the freshly uploaded photos.
    setPhotos((prev) => [...uploaded, ...prev]);
  };

  return (
    <>
      <div className="mb-4">
        <h1 className="display-4">&#128248; Photo Gallery</h1>
        <p className="lead">Upload and view your photos</p>
      </div>

      <UploadZone onUploaded={handleUploaded} />

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div className="text-center text-muted py-5">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      ) : (
        <Gallery photos={photos} aiEnabled={aiEnabled} />
      )}
    </>
  );
}
