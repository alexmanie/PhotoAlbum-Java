import { useEffect, useState } from 'react';
import { fetchDescription, fetchPhotos, uploadPhotos } from '../api';
import { Gallery } from '../components/Gallery';
import { UploadZone } from '../components/UploadZone';
import type { Photo } from '../types';

export function GalleryPage() {
  const [photos, setPhotos] = useState<Photo[]>([]);
  const [aiEnabled, setAiEnabled] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadPhotos = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchPhotos();
      setPhotos(data.photos);
      setAiEnabled(data.aiEnabled);
    } catch {
      setError('Failed to load photos.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadPhotos();
  }, []);

  useEffect(() => {
    if (!aiEnabled) {
      return;
    }

    const pendingPhotoIds = photos
      .filter((photo) => !photo.description || !photo.description.trim())
      .map((photo) => photo.id);

    if (!pendingPhotoIds.length) {
      return;
    }

    const intervalId = window.setInterval(() => {
      pendingPhotoIds.forEach((photoId) => {
        void fetchDescription(photoId)
          .then((payload) => {
            if (payload.ready && payload.description) {
              setPhotos((current) =>
                current.map((photo) =>
                  photo.id === photoId ? { ...photo, description: payload.description } : photo
                )
              );
            }
          })
          .catch(() => {
            // Ignore transient polling errors.
          });
      });
    }, 3000);

    return () => window.clearInterval(intervalId);
  }, [photos, aiEnabled]);

  const handleUpload = async (files: File[]) => {
    await uploadPhotos(files);
    await loadPhotos();
  };

  return (
    <>
      <div className="mb-4">
        <h1 className="display-5 mb-1">Photo Gallery</h1>
        <p className="lead text-muted mb-0">Upload and view your photos</p>
      </div>

      <UploadZone onUpload={handleUpload} />

      {loading && <div className="alert alert-secondary">Loading photos...</div>}
      {error && <div className="alert alert-danger">{error}</div>}
      {!loading && !error && <Gallery photos={photos} aiEnabled={aiEnabled} />}
    </>
  );
}
