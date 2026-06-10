import type { Photo } from '../types';
import PhotoCard from './PhotoCard';

interface GalleryProps {
  photos: Photo[];
  aiEnabled: boolean;
}

/** Responsive grid of photo cards, or an empty-state message. */
export default function Gallery({ photos, aiEnabled }: GalleryProps) {
  if (photos.length === 0) {
    return (
      <div className="alert alert-info text-center">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="48"
          height="48"
          fill="currentColor"
          className="bi bi-images mb-3"
          viewBox="0 0 16 16"
        >
          <path d="M4.502 9a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z" />
          <path d="M14.002 13a2 2 0 0 1-2 2h-10a2 2 0 0 1-2-2V5A2 2 0 0 1 2 3a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v8a2 2 0 0 1-1.998 2zM14 2H4a1 1 0 0 0-1 1h9.002a2 2 0 0 1 2 2v7A1 1 0 0 0 15 11V3a1 1 0 0 0-1-1zM2.002 4a1 1 0 0 0-1 1v8l2.646-2.354a.5.5 0 0 1 .63-.062l2.66 1.773 3.71-3.71a.5.5 0 0 1 .577-.094l1.777 1.947V5a1 1 0 0 0-1-1h-10z" />
        </svg>
        <p className="mb-0">
          No photos yet. Upload your first photo to get started!
        </p>
      </div>
    );
  }

  return (
    <div className="row" id="photo-gallery">
      {photos.map((photo) => (
        <PhotoCard key={photo.id} photo={photo} aiEnabled={aiEnabled} />
      ))}
    </div>
  );
}
